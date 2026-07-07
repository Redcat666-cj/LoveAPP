package com.itzhiqin.yuaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
    "loveapp.vectorstore.enabled=false",
    "loveapp.rag.enabled=false"
})
class MoMoBallTest {
    @MockBean
    private VectorStore pgVectorStore;

    @Resource
    private MoMoBall moMoBall;
    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void run ()
    {
        String userPropmt = """
                我的另一半居住在乌鲁木齐，请帮我找到 5 公里内合适的约会地点
                               并结合一些网络图片，制定一份详细的约会计""";

        String answer = moMoBall.run(userPropmt);
        Assertions.assertNotNull(answer);
    }

}