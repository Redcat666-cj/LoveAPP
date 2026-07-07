package com.itzhiqin.yuaiagent.agent;

import cn.hutool.ai.Models;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.itzhiqin.yuaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.StaticToolCallbackResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

//callSuper = false，意思是：只比较当前类自己声明的字段，不管父类字段
@EqualsAndHashCode(callSuper = true)
/*继承链中，子类必须显式写 @EqualsAndHashCode(callSuper = true)，覆盖 @Data 的默认行为，让 equals/hashCode
  ▎ 比较父类字段。否则一个没有自己字段的子类，两个不同实例会被错误地判为相等。
*/

@Data
@Slf4j
public abstract class ToolCallAgent extends ReActAgent {
    //可用的工具
    private final ToolCallback[] availableTools;

    //保存了工具调用信息的响应
    private ChatResponse toolCallChatResponse;

    //工具调用的管理者
    private final ToolCallingManager toolCallingManager;


    // 禁用内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions;

    //构造函数 用new注入
    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder()
                .toolCallbackResolver(new StaticToolCallbackResolver(List.of(availableTools)))
                .build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withParallelToolCalls(true)
                .build();
    }


    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */

    @Override
    public boolean think() {
        if (getNextPropmt() != null && !getNextPropmt().isEmpty()) {
            // 如果有下一步提示词，说明需要执行行动
            UserMessage userMessage = new UserMessage(getNextPropmt());
            getMemoryList().add(userMessage);
        }
        List<Message> messageList = new ArrayList<>(getMemoryList());
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            // 获取带工具的响应，设置 120 秒超时防止永久阻塞
            ChatResponse chatResponse = CompletableFuture
                    .supplyAsync(() -> getChatClient().prompt(prompt)
                            .system(getSystemPropmt())
                            .toolCallbacks(availableTools)
                            .call()
                            .chatResponse())
                    .get(120, TimeUnit.SECONDS);

            //记录响应 用于act
           this.toolCallChatResponse = chatResponse;
           AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            //输出提示信息
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall>  toolCalls = assistantMessage.getToolCalls();
            log.info(getName()+"的思考"+result);
            log.info(getName()+"的选择"+toolCalls.size()+"调用");
            String toolCallInfo = toolCalls.stream()
                    .map(toolCall -> String.format("工具名称: %s,参数: %s",toolCall.name(),toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if(toolCallInfo.isEmpty()){
                getMemoryList().add(assistantMessage);
                // 没有工具调用，说明 LLM 已给出最终回复，标记为完成
                setAgentState(AgentState.FINISHED);
                return false;
            } else {
                return true;
            }
        }
        catch (TimeoutException e) {
            log.error(getName() + "的思考超时了(120秒)");
            getMemoryList().add(new AssistantMessage(getName() + "的API调用超时，已跳过当前步骤"));
            return false;
        }
        catch (Exception e) {


            log.error(getName()+"的思考遇到了问题:"+e.getMessage());
            getMemoryList().add(new AssistantMessage(getName()+"的处理遇到了问题"+e.getMessage()));
            return false;

        }

    }

    /**
     * 获取最后一次思考的文本内容（无工具调用时使用）
     */
    public String getLastThinkText() {
        if (toolCallChatResponse != null && toolCallChatResponse.getResult() != null) {
            AssistantMessage output = toolCallChatResponse.getResult().getOutput();
            if (output != null && output.getText() != null) {
                return output.getText();
            }
        }
        return "思考完成";
    }


    @Override
    public String step() {
        try {
            boolean isAction = think();
            if (isAction) {
                return act();
            } else {
                return getLastThinkText();
            }
        } catch (Exception e) {
            log.error("步骤执行失败", e);
            return "步骤执行失败: " + e.getMessage();
        }
    }

    @Override
    public String act() {
        if(!toolCallChatResponse.hasToolCalls()){
            return "没有工具调用";
        }
        //调用工具
        Prompt prompt = new Prompt(getMemoryList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMemoryList(toolExecutionResult.conversationHistory() );

        // 当前工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(result -> "工具"+result.name()+"完成了调用，返回结果为："+result.responseData())
                .collect(Collectors.joining("\n"));
        // 判断是否调用了终止工具
        boolean terminalToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(result -> "doTerminate".equals(result.name()));

        if(terminalToolCalled){
            setAgentState(AgentState.FINISHED);
        }

        log.info(results);
        return results;
    }
}