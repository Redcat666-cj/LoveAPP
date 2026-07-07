package com.itzhiqin.yuaiagent.app;

import com.itzhiqin.yuaiagent.advisor.MyLoggerAdvisor;
import com.itzhiqin.yuaiagent.advisor.MyReReadingAdvisor;
import com.itzhiqin.yuaiagent.advisor.MySafeGuardAdvisor;
import com.itzhiqin.yuaiagent.chatmemory.FileBaseChatMemory;
import com.itzhiqin.yuaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.itzhiqin.yuaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class LoveApp {


    @Autowired(required = false)
    private Advisor loveAppRagCloudAdvisor;

   // @Autowired(required = false)
    //private VectorStore loveAppVectorStore;

   // @Resource
    //private VectorStore pgVectorStore;

    private final ChatClient chatClient;


    @Resource
    private QueryRewriter queryRewriter;





    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";


    //初始化ai客户端
    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于内存的对话记忆
        // 另外提醒：原代码中限制检索 10 条消息的意图，在新 API 中应改为在构建 MessageWindowChatMemory 时设置：
        ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();

        //基于文件的会话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory1 = new FileBaseChatMemory(fileDir);

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory1).build(),
                        //自定义日志Advisor
                        new MyLoggerAdvisor(),
                        //自定义增强Advisor,可按需开启
                     new MyReReadingAdvisor()
        //new MySafeGuardAdvisor(List.of("账号","密码","血腥","暴力","色情"))
                )
                .build();
    }
    //Ai 基础对话(文件多轮对话)
    public String doChat(String message,String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                // CHAT_MEMORY_CONVERSATION_ID_KEY │ 常量不存在 │ 改为 ChatMemory.CONVERSATION_ID（值为 "chat_memory_conversation_id"） │
                //  ├─────────────────────────────────┼────────────┼───────────────────────────────────────────────────────────────────────┤
                //  │ CHAT_MEMORY_RETRIEVE_SIZE_KEY   │ 常量不存在 │ 删除，此版本不再支持在 advisor param 中动态设检索条数
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();

        log.info("context:{}",content);

        return content;
    }

    record LoveReport(String title, List<String> suggestions){




    }

    public LoveReport doChatReport(String message,String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT+"每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                // CHAT_MEMORY_CONVERSATION_ID_KEY │ 常量不存在 │ 改为 ChatMemory.CONVERSATION_ID（值为 "chat_memory_conversation_id"） │
                //  ├─────────────────────────────────┼────────────┼───────────────────────────────────────────────────────────────────────┤
                //  │ CHAT_MEMORY_RETRIEVE_SIZE_KEY   │ 常量不存在 │ 删除，此版本不再支持在 advisor param 中动态设检索条数
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);


     log.info("loveReport:{}",loveReport);

        return loveReport;
    }

    public String doChatWithRag(String message,String chatId) {
        //queryRewrite查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())

                //RAG知识库问答
                //.advisors(RetrievalAugmentationAdvisor.builder()
                //    .documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(loveAppVectorStore).build())
                //    .documentJoiner(new ConcatenationDocumentJoiner())
                //    .queryAugmenter(ContextualQueryAugmenter.builder().build())
                //    .build())

                //应用RAG检索增强服务(基于云知识库)(默认)
                .advisors(loveAppRagCloudAdvisor)

                //应用RAG检索增强服务基于PGvector向量存储
//                .advisors(RetrievalAugmentationAdvisor.builder()
//                    .documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(pgVectorStore).build())
//                    .documentJoiner(new ConcatenationDocumentJoiner())
//                    .queryAugmenter(ContextualQueryAugmenter.builder().build())
//                    .build())

//                  自定义的RAG检索增强服务 (文档查询器和上下文增强)
//                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore,"单身"))
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("context:{}",content);
        return content;
    }

    //基于工具调用
    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))

                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }



    //基于mcp 通过自动注入的 ToolCallbackProvider 获取到配置中定义的 MCP 服务提供的 所有工具
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))

                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    //流式chatclient返回数据
    public Flux<String> doChatStream(String message,String chatId) {
        return chatClient
                .prompt()
                .user(message)
                // CHAT_MEMORY_CONVERSATION_ID_KEY │ 常量不存在 │ 改为 ChatMemory.CONVERSATION_ID（值为 "chat_memory_conversation_id"） │
                //  ├─────────────────────────────────┼────────────┼───────────────────────────────────────────────────────────────────────┤
                //  │ CHAT_MEMORY_RETRIEVE_SIZE_KEY   │ 常量不存在 │ 删除，此版本不再支持在 advisor param 中动态设检索条数
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();






    }





}
