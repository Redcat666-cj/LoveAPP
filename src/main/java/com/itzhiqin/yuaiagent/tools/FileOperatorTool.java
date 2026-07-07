package com.itzhiqin.yuaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.itzhiqin.yuaiagent.constant.FileConstent;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperatorTool {

    private final String  FILE_DIR = FileConstent.FILE_SAVE_PATH + "/file";


    @Tool(description = "Read content from  a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String fileName) {
    String filePath = FILE_DIR +"/" + fileName;
        try {
           return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
           return "Error reading file " + e.getMessage();
        }

    }
    @Tool(description  = "Write cotent to a file")
    public String writeFile(@ToolParam(description = "Name of file to write") String  fileName
            ,@ToolParam(description = "Content to write to the file")String content) {
        String filePath = FILE_DIR +"/" + fileName;
        //创建目录
        try {
            /*
            主动创建目录，防止目录被意外删除导致写入失败
            这不是逻辑错误，而是为了提高写操作的容错性和成功率*/
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to " + filePath;
        } catch (Exception e) {
            return "Error writing file " + e.getMessage();
        }


    }
}
