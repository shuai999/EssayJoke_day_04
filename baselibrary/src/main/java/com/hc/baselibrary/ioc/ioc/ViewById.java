package com.hc.baselibrary.ioc.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Email: 2185134304@qq.com
 * Created by JackChen 2018/3/28 9:11
 * Version 1.0
 * Params:
 * Description:  View 注解的 Annotation   用于在类中 @ViewById(R.id.test_tv) 这样用
*/

@Target(ElementType.FIELD)  // Target表示作用在哪里 FIELD：属性  TYPE：类  CONSTRUCTOR：构造方法
@Retention(RetentionPolicy.RUNTIME) // Retention表示什么时间生效  RUNTIME：运行时  CLASS：编译时  SOURCE：源码资源
public @interface ViewById {

    // 指的是  --> ViewById(R.id.test_tv)
    int value() ;
}
