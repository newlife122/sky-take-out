package com.sky.aspect;

import com.sky.annotation.AutoFIll;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @author raoxin
 */
@Slf4j
@Aspect
@Component
public class AutoFillAspect {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFIll)")
    public void fillTimePointCut(){

    }
    @Before("fillTimePointCut()")
    public void fillTime(JoinPoint joinPoint){
        log.info("代理填充公共字段");

        //获取参数还有这个方法的信息
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Annotation annotation = signature.getMethod().getAnnotation(AutoFIll.class);

        if(args == null || args.length == 0 || annotation == null){
            return;
        }

        //获得形参的第一个参数，然后获得器class对象，利用反射来对这两个字段进行赋值
        Object arg = args[0];
        Class<?> entity = arg.getClass();
        //获取相应的方法，通过方法点invoke来运行对应的方法


        //准备好的参数
        Long currentId = BaseContext.getCurrentId();
        LocalDateTime localDateTime = LocalDateTime.now();

        //这里是获取这个方法的参数
        OperationType operationType = ((AutoFIll) annotation).value();

        if(operationType == OperationType.INSERT){
            try {
                log.info("代理填充插入信息:setUpdateTime:{},setCreateTime:{},setUpdateUser:{},setCreateUser:{}",
                        localDateTime,localDateTime,currentId,currentId
                        );
                Method setUpdateTime = entity.getMethod("setUpdateTime", LocalDateTime.class);
                Method setCreateTime = entity.getMethod("setCreateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getMethod("setUpdateUser", Long.class);
                Method setCreateUser = entity.getMethod("setCreateUser", Long.class);
                setUpdateUser.invoke(arg, currentId);
                setCreateUser.invoke(arg, currentId);
                setUpdateTime.invoke(arg, localDateTime);
                setCreateTime.invoke(arg, localDateTime);
            }catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
                log.error("代理填充信息错误:{}",e.toString());
                e.printStackTrace();
            }
        }else if(operationType == OperationType.UPDATE){
            try {
                log.info("代理填充插入信息:setUpdateTime:{},setUpdateUser:{}",
                        localDateTime,currentId
                );
                Method setUpdateTime = entity.getMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getMethod("setUpdateUser", Long.class);
                setUpdateUser.invoke(arg, currentId);
                setUpdateTime.invoke(arg, localDateTime);
            }catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
                e.printStackTrace();
            }
        }





    }
}
