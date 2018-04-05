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
 * Description:  检测网络注解的Annotation   用于在类中 @CheckNet 这样用
*/

@Target(ElementType.METHOD)  // Target表示作用在哪里 FIELD：属性  TYPE：类  CONSTRUCTOR：构造方法
@Retention(RetentionPolicy.RUNTIME) // Retention表示什么时间生效  RUNTIME：运行时  CLASS：编译时  SOURCE：源码资源
public @interface CheckNet {

}
