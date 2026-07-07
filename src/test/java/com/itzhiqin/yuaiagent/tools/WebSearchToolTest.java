package com.itzhiqin.yuaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebSearchToolTest {

    private String searchApiKey = "CdoM1N2eaYoTQYh92YnnjL7z";


    @Test
    void searchWeb() {
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        String result = webSearchTool.searchWeb("卡拉比丘星绘");
        Assertions.assertNotNull(result);


    }
}