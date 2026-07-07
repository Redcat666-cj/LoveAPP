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
             throw new RuntimeException(e);
         }

          this.agentState = AgentState.RUNNING;
         memoryList.add(new UserMessage(userPrompt));

         try {
            for(int i = 1 ; i <= MAXSTEP && this.agentState!=AgentState.FINISHED; i++){

               currentStep = i;
               log.info("execuating step: " + i + "/" + MAXSTEP);

               //单步执行
               String stepResult = step();
               String result = "step" + i + ": " + stepResult;

               emitter.send(result);


            }
            //校验步骤
            if(currentStep>=MAXSTEP){
               this.agentState = AgentState.FINISHED;
               emitter.send("\"MaxStep reached\"");
            }
            //正常完成
            emitter.complete();

         } catch (Exception e) {
            this.agentState = AgentState.ERROR;
            log.error("agent run error",e);
      //            return "执行错误"+e.getMessage();
            try {
               emitter.send("执行错误: " + e.getMessage());
               emitter.complete();
            } catch (Exception ex) {
               emitter.completeWithError(ex);
            }
         }finally {
            this.cleanup();
         }
      });


      //超时处理
      // 设置超时和完成回调
      emitter.onTimeout(() -> {
         this.agentState = AgentState.ERROR;
         this.cleanup();
         log.warn("SSE connection timed out");
      });

      emitter.onCompletion(() -> {
         if (this.agentState == AgentState.RUNNING) {
            this.agentState = AgentState.FINISHED;
         }
         this.cleanup();
         log.info("SSE connection completed");
      });



         return emitter;
   }


}
