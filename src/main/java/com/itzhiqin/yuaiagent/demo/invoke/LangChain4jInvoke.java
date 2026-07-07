package com.itzhiqin.yuaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.output.Response;

import java.awt.*;

public class LangChain4jInvoke {

    public static void main(String[] args) {
        WanxImageModel wanxImageModel = WanxImageModel.builder()
                .modelName("wanx2.1-t2i-plus")
                .apiKey("sk-5470d073c94f45d3aff9b9703f6a8c81")
                .build();
        Response<Image> response = wanxImageModel.generate("美女");
        System.out.println(response.content().url());
    }
}
