package com.itzhiqin.yuaiagent.rag;


import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;


//自定义基于阿里云的增强顾问

@Configuration
@ConditionalOnProperty(name = "loveapp.rag.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j


class LoveAppRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    //                        问题                        │                            修复                            │
    //  ├────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
    //  │ new DashScopeApi(key) 报错 — 构造函数需要 9 个参数 │ 改用 DashScopeApi.builder().apiKey(key).build()            │
    //  ├────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
    //  │ withIndexName() 已弃用                             │ 改为 .indexName()                                          │
    //  ├────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────┤
    //  │ 两个未使用的 import                                │ 删除了 DashScopeDocumentRetrievalAdvisor 和 LoveApp 的导入 │
    //  └────────────────────────────────────────────────────┴────────────────────────────────────────────────────────────┘
    //
    //  DashScopeApi 这个类在 1.1.2.0 版本中已经全面改成 Builder 模式了，不再接受直接传一个 String 的构造方式。其他用到 DashScopeApi 的地方也要注意这一点。

    @Bean
    @Lazy
    public Advisor loveAppRagCloudAdvisor() {

       // DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey); 废弃
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(dashScopeApiKey)
                .build();
        final String KNOWLEDGE_INDEX = "恋爱大师";
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .indexName(KNOWLEDGE_INDEX)
                        .build());
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
    }


}


