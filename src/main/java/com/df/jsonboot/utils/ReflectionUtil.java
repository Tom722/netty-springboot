package com.df.jsonboot.utils;

import com.df.jsonboot.annotation.Component;
import com.df.jsonboot.annotation.RestController;
import com.df.jsonboot.core.ioc.BeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射工具类
 *
 * @author qinghuo
 * @since 2021/03/22 14:39
 */
@Slf4j
public class ReflectionUtil {

    /**
     * @param method 目标方法
     * @param args   调用的参数
     * @return 执行结果
     */
    public static Object executeMethod(Method method, Object... args) {
        Object result = null;
        try {
            String beanName = null;
            Object targetObject;
            //先判断是否已经生成该对象了 直接在ioc的容器中取出来
            Class<?> targetClass = method.getDeclaringClass();
            if (targetClass.isAnnotationPresent(RestController.class)){
                beanName = targetClass.getName();
            }
            if (targetClass.isAnnotationPresent(Component.class)){
                Component component = targetClass.getAnnotation(Component.class);
                beanName = StringUtils.isBlank(component.value()) ? targetClass.getName() : component.value();
            }
            if (StringUtils.isNotEmpty(beanName)){
                targetObject = BeanFactory.BEANS.get(beanName);
            }else{
                targetObject = method.getDeclaringClass().newInstance();
            }
            // 调用对象的方法
            result = method.invoke(targetObject, args);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 通过反射生成对象
     *
     * @param aClass 类的类型
     * @return 类生成的对象
     */
    public static Object newInstance(Class<?> aClass){
        Object instance = null;
        try {
            instance = aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("实例化对象失败 class: {}", aClass);
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * 为对象的属性设值
     *
     * @param obj 需要设置属性的对象
     * @param field 属性
     * @param value 设置的值
     */
    public static void setReflectionField(Object obj, Field field, Object value){
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            log.error("设置对象field失败");
            e.printStackTrace();
        }
    }

    /**
     * 用于将String对象转换为需要的类型对象(已废弃，使用PropertyEditorManager代替)
     *
     * @param type 原类型
     * @param str 需要转换的字符串
     * @return 转换后的对象
     */
    @Deprecated
    public static Object getNumber(Class<?> type,String str) {
        Class<?>[] paramsClasses = { str.getClass() };
        Object[] params = { str };
        Class<?> typeClass = ObjectUtil.convertBaseClass(type);
        Object o = null;
        try {
            Constructor<?> c = typeClass.getConstructor(paramsClasses);
            o = c.newInstance(params);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return o;
    }
}
