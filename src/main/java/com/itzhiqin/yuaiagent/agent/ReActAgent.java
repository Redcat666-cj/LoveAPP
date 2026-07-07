package com.itzhiqin.yuaiagent.agent;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */

public abstract class ReActAgent extends BaseAgent {

    public abstract boolean think();


    public abstract String act();


    /*先思考再执行*/
    @Override
    public String step(){
        try {
            boolean isAction = think();
            if(isAction){
                return act();
            }else
            {
                return "思考完成 - 无需行动";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "步骤执行失败" +  e.getMessage();
        }

    }




}
