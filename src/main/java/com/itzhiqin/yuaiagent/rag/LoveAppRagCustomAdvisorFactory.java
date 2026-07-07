package com.itzhiqin.yuaiagent.rag;


/*
* 创建自定义的rag检索顾问的工厂
**/

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;



public class LoveAppRagCustomAdvisorFactory {

    //自定义检索增强

    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore,String status) {
        //3再把过lu器创建 过lu特定状态的文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status",status)
                .build();

        //2先实例化文档检索
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)
                .similarityThreshold(0.5) //相似度阈值
                .topK(3) //召回数量
                .build();


        //1创建检索增强
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                //自定义上下文查询器
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
