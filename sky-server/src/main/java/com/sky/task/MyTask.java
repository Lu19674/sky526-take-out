package com.sky.task;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyTask {

    /**
     * 从0秒开始，每隔5秒触发一次
     */
//    @Scheduled(cron = "0/5 * * * * ? ") //cron表达式可网站找 在线生成器
    public void logfo(){
        log.info("MyTask自动执行任务执行了。。。");
    }
}
