package com.itzhiqin.yuaiagent.controller;

import com.itzhiqin.yuaiagent.agent.MoMoBall;
import com.itzhiqin.yuaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.Disposable;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashsocpeChatModel;

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveApp(String message,String chatId){

        return loveApp.doChat(message,chatId);


    }

    //开发 SSE 流式接口

    //1） 返回 Flux 响应式对象，并且添加 ‌对应的‌ MediaType：
    @GetMapping(value = "/love_app/chat/sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithSse(String message,String chatId){

        return loveApp.doChatStream(message,chatId);



    }
    //返回 Flux 对象，并且设置泛型为 ServerSentEvent。使用这种方式可以省略 MediaType：
    @GetMapping("/love_app/chat/see")
    public Flux<ServerSentEvent<String>> doChatWithSeeApp(String message,String chatId){

        return loveApp.doChatStream(message,chatId).map(chunk-> ServerSentEvent.<String>builder()
                .data(chunk)
                .build());

    }
    //3）使用SSEEmiter，通过 send 方法持续向 SseEmitter 发送消息（有点像 IO 操作）：
    @GetMapping("/love_app/chat/see/emiter")
    public SseEmitter doChatWithSeeEmiter(String message,String chatId){
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); //三分钟超时
        // 获取 Flux 数据流并直接订阅
        Disposable subscription = loveApp.doChatStream(message, chatId).subscribe(
                chunk -> {
                    try {
                        emitter.send(chunk);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );

        // 客户端断开、超时时取消上游 Flux，避免后端继续执行
        emitter.onError(throwable -> subscription.dispose());
        emitter.onTimeout(subscription::dispose);
        emitter.onCompletion(subscription::dispose);

        return emitter;
    }

    @GetMapping("/momoball/chat")
    public SseEmitter doChatWithMomoball(String message){

        MoMoBall moMoBall = new MoMoBall(allTools,dashsocpeChatModel);
        return moMoBall.runStream(message);



    }

}
