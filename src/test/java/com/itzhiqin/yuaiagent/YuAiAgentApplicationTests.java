package com.itzhiqin.yuaiagent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.ai.vectorstore.pgvector.enabled=false",
    "loveapp.vectorstore.enabled=false",
    "loveapp.rag.enabled=false"
})
class YuAiAgentApplicationTests {

//    @Test
//    void contextLoads() {
//    }

}
