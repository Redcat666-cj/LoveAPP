package com.itzhiqin.yuaiagent.demo.invoke;

//spring ai 框架调用ai


import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

//@Component
//@ConditionalOnProperty(name = "demo.runner.enabled", havingValue = "true", matchIfMissing = true)
public class SpringAiAilInvoke implements CommandLineRunner {
    //Resource优先搜索名称 后 类型
    @Resource //自动将大模型注入
    private ChatModel dashscopeChatModel;

    @Override //单次执行验证大模型是否注入
    public void run(String... args) throws Exception {
        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("你好我是崔健"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }
}
