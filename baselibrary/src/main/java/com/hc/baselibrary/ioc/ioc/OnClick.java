package com.hc.baselibrary.ioc.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Email: 2185134304@qq.com
 * Created by JackChen 2018/3/28 9:17
 * Version 1.0
 * Params:
 * Description:  View事件  注解的 Annotation  用于在类中 @OnClick({R.id.test_tv , R.id.test_iv})  这样用
*/
@Target(ElementType.METHOD)  // 代表注解的位置  FIELD：属性  TYPE：类  CONSTRUCTOR：构造方法  METHOD：方法
@Retention(RetentionPolicy.RUNTIME)  // 代表什么时间生效  RUNTIME：运行时  CLASS：编译时  SOURCE：源码资源
public @interface OnClick {

    // {R.id.test_tv , R.id.test_iv}
    int[] value() ;
}
