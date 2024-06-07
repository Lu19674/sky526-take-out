
package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;


/**
 * 自定义aop切面类，实现新增和修改操作的公共字段自动填充功能逻辑
 */

@Slf4j
@Aspect
@Component
public class AutoFillAspect {
    /*
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }
    /*
     * 前置通知，在新增和修改前为公共字段填充赋值
     *
     * @param joinPoint
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始对公共字段进行自动填充...");
        //1、获取方法操作类型--加在方法上的自定义的value值
        //获取方法签名对象（向下转型，MethodSignature 是 Signature 的子接口）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType = autoFill.value();//获取数据库操作类型

        //2、获取方法操作对象 -- 方法参数
        Object[] args = joinPoint.getArgs();
        if(args==null || args.length==0){
            return;
        }
        Object entity = args[0];//约定第一个参数为操作的实体对象


        //3、准备填充数据 -- 操作时间和操作人id
        LocalDateTime now = LocalDateTime.now();//时间
        Long currentId = BaseContext.getCurrentId();//操作人id

        //4、根据1获取的不同操作类型为操作对象进行不同的赋值 --为对象对应的属性通过反射赋值
        if(operationType == OperationType.INSERT){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性来赋值
                setUpdateTime.invoke(entity,now);
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(operationType == OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);

                //通过反射为对象属性来赋值
                setUpdateTime.invoke(entity,now);
                setCreateTime.invoke(entity,now);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}

