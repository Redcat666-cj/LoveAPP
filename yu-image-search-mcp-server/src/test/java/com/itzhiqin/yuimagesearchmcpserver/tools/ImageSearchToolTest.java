package com.itzhiqin.yuimagesearchmcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ImageSearchToolTest {
    @Resource
    private ImageSearchTool imageSearchTool;
    @Test
    public void testImageSearch() {
        String computer = imageSearchTool.searchImage("computer");
        Assertions.assertNotNull(computer);
    }



}


