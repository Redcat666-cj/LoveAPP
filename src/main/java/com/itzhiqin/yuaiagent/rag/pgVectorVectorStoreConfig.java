package com.itzhiqin.yuaiagent.rag;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Configuration
public class pgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Value("${pgvector.data.auto-load:false}")
    private boolean autoLoadData;

    //@Bean
   // @Lazy
    public VectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        PgVectorStore pgVectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1024)
                .distanceType(PgVectorStore.PgDistanceType.EUCLIDEAN_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10000)
                .batchingStrategy(new BatchingStrategy() {
                    @Override
                    public List<List<Document>> batch(List<Document> documents) {
                        List<List<Document>> batches = new java.util.ArrayList<>();
                        for (int i = 0; i < documents.size(); i += 10) {
                            batches.add(documents.subList(i, Math.min(i + 10, documents.size())));
                        }
                        return batches;
                    }
                })
                .build();

        if (autoLoadData) {
            List<Document> documents = loveAppDocumentLoader.loadDocuments();
            pgVectorStore.add(documents);
        }
        return pgVectorStore;
    }
}
