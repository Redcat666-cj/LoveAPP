package com.itzhiqin.yuaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileBaseChatMemory implements ChatMemory {

    private final int searchN = 3;

    private final String BASE_PATH;

    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
    }

    // ---- 可序列化 DTO：避开 Spring AI Message 内部的复杂泛型字段 ----
    // Spring AI Message 实现类只有单参 (String text) 公开构造器，
    // metadata 构造器为 private/protected，故只保存 text 和 type

    record MessageRecord(String type, String text) implements Serializable {
        static MessageRecord from(Message msg) {
            return new MessageRecord(msg.getMessageType().name(), msg.getText());
        }

        Message toMessage() {
            return switch (MessageType.valueOf(type)) {
                case USER -> new UserMessage(text);
                case ASSISTANT -> new AssistantMessage(text);
                case SYSTEM -> new SystemMessage(text);
                default ->
                        new UserMessage(text);
            };
        }
    }

    // ---- 构造 & CRUD ----

    public FileBaseChatMemory(String basePath) {
        this.BASE_PATH = basePath;
        File baseDir = new File(BASE_PATH);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, Message message) {
        saveConversation(conversationId, List.of(message));
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> messageList = getOrCreateConversation(conversationId);
        messageList.addAll(messages);
        saveConversation(conversationId, messageList);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> messageList = getOrCreateConversation(conversationId);
        if (messageList.size() <= searchN) {
            return new ArrayList<>(messageList);
        }
        return new ArrayList<>(messageList.subList(messageList.size() - searchN, messageList.size()));
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    // ---- 内部实现 ----

    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists() && file.length() > 0) {
            try (Input input = new Input(new FileInputStream(file))) {
                // 读取 DTO 列表，再转换为 Spring AI Message
                List<MessageRecord> records = kryo.readObject(input, ArrayList.class);
                for (MessageRecord record : records) {
                    messages.add(record.toMessage());
                }
            } catch (Exception e) {
                throw new RuntimeException("读取会话文件失败: " + file.getAbsolutePath(), e);
            }
        }
        return messages;
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        // 先转换为 DTO，再序列化
        List<MessageRecord> records = new ArrayList<>(messages.size());
        for (Message msg : messages) {
            records.add(MessageRecord.from(msg));
        }
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, records);
        } catch (IOException e) {
            throw new RuntimeException("保存会话文件失败: " + file.getAbsolutePath(), e);
        }
    }

    private File getConversationFile(String conversationId) {
        return new File(BASE_PATH, conversationId + ".kryo");
    }
}