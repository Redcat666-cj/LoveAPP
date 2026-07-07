package com.itzhiqin.yuaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

public class WebScrapingTool {

    @Tool(description = "Scrape the content of the web")
    public String ScrapeWeb(@ToolParam(description = "The url of the web page to scrape") String url) {

        try {
            Document document = Jsoup.connect(url).get();
            return document.html();
        } catch (Exception e) {
           return "Error scraping the web page"  + e.getMessage();
        }


    }



}
