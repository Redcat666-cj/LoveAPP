package com.itzhiqin.yuaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class ResouceDownloadToolTest {

    @Test
    void downloadResource() {

               ResouceDownloadTool resouceDownloadTool = new ResouceDownloadTool();
                String url = "https://www.codefather.cn/logo.png";
                String fileName = "logo.png";
                String result = resouceDownloadTool.downloadResource(url, fileName);
                assertNotNull(result);



    }
}