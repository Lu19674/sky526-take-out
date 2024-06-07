package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用于创建 AliOssUtil 工具类对象 并初始化对应属性的配置类
 */
@Slf4j
@Configuration
public class OssConfiguration {


    @Bean //使项目启动后自动创建AliOssUtil对象成为bean，并交给IOC容器管理
    @ConditionalOnMissingBean //单例bean条件
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
        log.info("开始创建AliOssUtil对象 {}",aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}
