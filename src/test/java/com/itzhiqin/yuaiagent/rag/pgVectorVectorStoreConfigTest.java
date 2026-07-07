//package com.itzhiqin.yuaiagent.rag;
//
//import jakarta.annotation.Resource;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.vectorstore.SearchRequest;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//@SpringBootTest(properties = "demo.runner.enabled=false")
//class pgVectorVectorStoreConfigTest {
//    @Resource(name = "pgVectorStore")
//    private VectorStore pgvectorStore;
//
//
//    @Test
//    void pgVectorStore() {
//        List<Document> documents = List.of(
//                new Document("卡拉彼丘中星汇是我老婆", Map.of("meta1", "meta1")),
//                new Document("星汇老婆天天粘着我我很烦"),
//                new Document("星汇非常漂亮非常贤惠每天都哄我", Map.of("meta2", "meta2")));
//
//// Add the documents to PGVector
//        pgvectorStore.add(documents);
//
//// Retrieve documents similar to a query
//        List<Document> results = this.pgvectorStore.similaritySearch(SearchRequest.builder().query("怎么正确回应星汇的爱意").topK(5).build());
//        Assertions.assertNotNull(results);
//
//    }
//}