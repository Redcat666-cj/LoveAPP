package com.itzhiqin.yuaiagent.demo.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

/*
查询扩展器(基于ai)
*
* */

@Component
public class MultiQueryExpenderDemo {

   private  ChatClient.Builder chatClientBuilder;
   public  MultiQueryExpenderDemo (ChatModel dashscopeChatModel){
       this.chatClientBuilder = ChatClient.builder(dashscopeChatModel);
   }



        public List<Query> expand(String query){
            MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                    .chatClientBuilder( chatClientBuilder )
                    .numberOfQueries(3)
                    .build();
            List<Query> queries = queryExpander.expand(new Query("谁是星绘啊？"));


            return queries;
        }




}
