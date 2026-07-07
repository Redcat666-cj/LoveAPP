package com.itzhiqin.yuaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FileOperatorToolTest {

    @Test
    void readFile() {
        FileOperatorTool fileOperatorTool = new FileOperatorTool();
        String fileName = "编程导航.txt";
        String result = fileOperatorTool.readFile(fileName);
        Assertions.assertNotNull(result);
    }

    @Test
    void writeFile() {
        FileOperatorTool fileOperatorTool = new FileOperatorTool();
        String fileName = "编程导航.txt";
        String content = "鱼皮666666";
        String result = fileOperatorTool.writeFile(fileName, content);
        Assertions.assertNotNull(result);

    }
}