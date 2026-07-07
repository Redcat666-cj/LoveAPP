package com.itzhiqin.yuaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

//@Configuration
//@ConditionalOnProperty(name = "loveapp.vectorstore.enabled", havingValue = "true", matchIfMissing = true)
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private  MyKeyWordEnricher myKeyWordEnricher;

   // @Bean
    @Lazy
    VectorStore loveAppVectorStore(EmbeddingModel embeddingModel) {
        List<Document> documents = loveAppDocumentLoader.loadDocuments();
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        //自动补充关键字元信息
        List<Document> enrichDocuments = myKeyWordEnricher.enrichDocuments(documents);
        simpleVectorStore.add(enrichDocuments);
        return simpleVectorStore;




    }

}
