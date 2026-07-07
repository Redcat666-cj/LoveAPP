package com.itzhiqin.yuaiagent.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.itzhiqin.yuaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class MoMoBall extends ToolCallAgent {
    public MoMoBall(ToolCallback[] availableTools,ChatModel dashScopeChatModel) {
        super(availableTools);
        this.setName("MoMoBall");
        String SYSTEM_PROPMT = """  
                You are MoMoBall, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                """;


        this.setSystemPropmt(SYSTEM_PROPMT);
        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                If you want to stop the interaction at any point, use the `terminate` tool/function call.  
                """;

        this.setNextPropmt(NEXT_STEP_PROMPT);
        this.setMAXSTEP(5);
        //初始化客户端
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}