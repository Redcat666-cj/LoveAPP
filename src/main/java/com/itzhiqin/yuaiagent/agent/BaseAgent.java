package com.itzhiqin.yuaiagent.agent;

import com.itzhiqin.yuaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.util.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Slf4j
public abstract class BaseAgent {


   private String name;


   //提示词
   private String systemPropmt;
   private String nextPropmt;


   //状态
   private AgentState agentState =  AgentState.IDLE;


   //上下文存储
   private List<Message> memoryList = new ArrayList<>();

   //流程控制
   private  int MAXSTEP = 5;
   private int currentStep = 0;


   //LLM
   private ChatClient chatClient;

   /**
    * 运行代理
    *
    * @param userPrompt 用户提示词
    * @return 执行结果
    */

   /**
    * 执行单个步骤
    *
    * @return 步骤执行结果
    */
   public abstract String step();

   /*clean up*/
   protected void cleanup(){

   }

   public String run(String userPrompt){

      if(this.agentState != AgentState.IDLE)
      {
         throw new RuntimeException("cannt runing from " +this.agentState);
      }
      if(StringUtil.isEmpty(userPrompt)){
         throw new RuntimeException("user prompt is empty");
      }

      this.agentState = AgentState.RUNNING;
      memoryList.add(new UserMessage(userPrompt));
      List<String> results = new ArrayList<>();
       try {
           for(int i = 1 ; i <= MAXSTEP && this.agentState!=AgentState.FINISHED; i++){

              currentStep = i;
              log.info("execuating step: " + i + "/" + MAXSTEP);

              //单步执行
              String stepResult = step();
              String result = "step" + i + ": " + stepResult;
              results.add(result) ;

              //校验步骤
              if(currentStep>=MAXSTEP){
                 this.agentState = AgentState.FINISHED;
                  log.info("MaxStep reached") ;
              }
           }
       } catch (Exception e) {
          this.agentState = AgentState.ERROR;
          log.error("agent run error",e);
          return "执行错误"+e.getMessage();
       }finally {
            this.cleanup();
       }
       return String.join(" ", results);
   }


   /**
    * 运行代理（流式输出）
    *
    * @param userPrompt 用户提示词
    * @return SseEmitter实例
    */


   public SseEmitter runStream(String userPrompt){

      SseEmitter emitter = new SseEmitter(120000L);
      AtomicBoolean clientDisconnected = new AtomicBoolean(false);

      // 注册回调，在客户端断开、超时、完成时设置标志
      emitter.onError(throwable -> {
         clientDisconnected.set(true);
         this.agentState = AgentState.ERROR;
         this.cleanup();
         log.warn("SSE connection error (client may have disconnected): {}", throwable.getMessage());
      });

      emitter.onTimeout(() -> {
         clientDisconnected.set(true);
         this.agentState = AgentState.ERROR;
         this.cleanup();
         log.warn("SSE connection timed out");
      });

      emitter.onCompletion(() -> {
         clientDisconnected.set(true);
         if (this.agentState == AgentState.RUNNING) {
            this.agentState = AgentState.FINISHED;
         }
         this.cleanup();
         log.info("SSE connection completed");
      });

      // 使用线程异步处理，避免阻塞主线程
      CompletableFuture.runAsync(() -> {
         try {
            if(this.agentState != AgentState.IDLE)
            {
               emitter.send("错误:"+"无法从状态运行代理:"+this.agentState);
               emitter.complete();
               return;
            }
            if(StringUtil.isEmpty(userPrompt)){
               emitter.send("user prompt is empty");
               emitter.complete();
               return;
            }
         } catch (IOException e) {
            clientDisconnected.set(true);
            return;
         }

         this.agentState = AgentState.RUNNING;
         memoryList.add(new UserMessage(userPrompt));

         try {
            for(int i = 1 ; i <= MAXSTEP && this.agentState!=AgentState.FINISHED; i++){

               // 每次循环前检查客户端是否已断开
               if (clientDisconnected.get()) {
                  log.info("Client disconnected, stopping agent loop at step {}", i);
                  break;
               }

               currentStep = i;
               log.info("execuating step: " + i + "/" + MAXSTEP);

               //单步执行
               String stepResult = step();
               String result = "step" + i + ": " + stepResult;

               // 发送前再次检查，发送时捕获异常
               if (clientDisconnected.get()) {
                  log.info("Client disconnected before send at step {}", i);
                  break;
               }

               try {
                  emitter.send(result);
               } catch (IOException e) {
                  clientDisconnected.set(true);
                  log.warn("Failed to send SSE event, client disconnected at step {}", i);
                  break;
               }


            }
            //校验步骤
            if(!clientDisconnected.get()){
               if(currentStep>=MAXSTEP){
                  this.agentState = AgentState.FINISHED;
                  emitter.send("\"MaxStep reached\"");
               }
               //正常完成
               emitter.complete();
            }

         } catch (Exception e) {
            this.agentState = AgentState.ERROR;
            log.error("agent run error",e);
            if (!clientDisconnected.get()) {
               try {
                  emitter.send("执行错误: " + e.getMessage());
                  emitter.complete();
               } catch (IOException ex) {
                  emitter.completeWithError(ex);
               }
            }
         }finally {
            this.cleanup();
         }
      });


         return emitter;
   }


}
