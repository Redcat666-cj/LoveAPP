package com.itzhiqin.yuaiagent.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component


public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver; //spring内置的资源解析类

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadDocuments() {
        List<Document> documents = new ArrayList<>();
        //加载多篇markdowm
        try {

            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                String status = fileName.substring(fileName.length() - 6, fileName.length() - 4);
                //构造器 指定怎么加载读取markdown
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)  //是否包含代码快
                        .withIncludeBlockquote(false) //是否用引用格式 <>
                        .withAdditionalMetadata("filename", fileName) //添加额外信息
                        .withAdditionalMetadata("status",status)
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                //添加到所有文档的列表
                documents.addAll(markdownDocumentReader.get());

            }
        } catch (IOException e) {
          log.error("markdown加载失败");
        }


        return documents;
    }
}
