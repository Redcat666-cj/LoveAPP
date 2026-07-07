//package com.itzhiqin.yuaiagent.demo.rag;
//
//import jakarta.annotation.Resource;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.ai.rag.Query;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//@SpringBootTest
//class MultiQueryExpenderDemoTest {
//    @Resource
//    private MultiQueryExpenderDemo multiQueryExpenderDemo;
//    @Test
//    void expand() {
//        List<Query> queries = multiQueryExpenderDemo.expand("卡拉比丘这个游戏太好玩了,卡拉比丘里星绘最漂亮最贤惠,星绘是我老婆!");
//        Assertions.assertNotNull(queries);
//    }
//}