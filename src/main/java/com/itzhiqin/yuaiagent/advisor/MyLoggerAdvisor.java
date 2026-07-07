//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.itzhiqin.yuaiagent.advisor;

import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */


@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {


    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        this.logRequest(chatClientRequest);
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        this.logResponse(chatClientResponse);
        return chatClientResponse;
    }

    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        this.logRequest(chatClientRequest);
        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);
        return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponses, this::logResponse);
    }

    protected void logRequest(ChatClientRequest request) {
        log.info("AI Request : {}", request.toString());


    }

    protected void logResponse(ChatClientResponse chatClientResponse) {
        log.info("AI Response : {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int getOrder() {
        return 0;
    }

    public String toString() {
        return MyLoggerAdvisor.class.getSimpleName();
    }

    //public static Builder builder() {
    // return new Builder();
    //}
}
//    public static final class Builder {
//        private Function<ChatClientRequest, String> requestToString;
//        private Function<ChatResponse, String> responseToString;
//        private int order = 0;
//
//        private Builder() {
//        }
//
//        public Builder requestToString(Function<ChatClientRequest, String> requestToString) {
//            this.requestToString = requestToString;
//            return this;
//        }
//
//        public Builder responseToString(Function<ChatResponse, String> responseToString) {
//            this.responseToString = responseToString;
//            return this;
//        }
//
//        public Builder order(int order) {
//            this.order = order;
//            return this;
//        }
//
//        public MyLoggerAdvisor build() {
//            return new MyLoggerAdvisor(this.requestToString, this.responseToString, this.order);
//        }
//    }
//}
