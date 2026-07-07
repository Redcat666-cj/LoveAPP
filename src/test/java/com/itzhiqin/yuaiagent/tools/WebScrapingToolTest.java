package com.itzhiqin.yuaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWeb() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String result = webScrapingTool.ScrapeWeb("https://www.codefather.cn");
        Assertions.assertNotNull(result);

    }
}