package com.hc.baselibrary.ioc.ioc;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Email: 2185134304@qq.com
 * Created by JackChen 2018/3/31 9:20
 * Version 1.0
 * Params:
 * Description:
*/
public class ViewUtils {


    /**
     * 用于Activity中
     */
    public static void inject(Activity activity){
        inject(new ViewFinder(activity) , activity) ;
    }


    /**
     * 用于自定义View中
     */
    public static void inject(View view){
        inject(new ViewFinder(view) , view) ;
    }


    /**
     * 在项目时有时候会用到Fragment中
     */
    public static void inject(View view  ,Object object){
        inject(new ViewFinder(view) , object) ;
    }


    /**
     * 兼容上边3个方法      object -> 反射需要执行的类
     */
    private static void inject(ViewFinder finder , Object object){
        // 动态注入属性
        injectField(finder , object) ;
        // 动态注入事件
        injectEvent(finder , object) ;
    }


    /**
     * 动态注入属性    object -> 反射需要执行的类
     */
    private static void injectField(ViewFinder finder, Object object) {
        // 1. 获取类中所有属性           ->  private TextView mTextTV , private int mPage ; 等等这些变量属性
        Class<?> clazz = object.getClass();
        // 获取所有的属性 包括private、public、protected等所有属性
        Field[] fields = clazz.getDeclaredFields();


        // 2. 获取ViewById里边的value值  ->  R.id.test_tv这些东西
        for (Field field : fields) {
            ViewById viewById = field.getAnnotation(ViewById.class);
            if (viewById != null){
                // 获取注解里边的 id 值  -  R.id.test_tv
                int viewId = viewById.value();

                // 3. findviewbyid  找到View
                // 这里就相当于在 MainActivity中调用 TextView test_tv = (TextView) findViewById(R.id.test_tv);  是一码事
                View view = finder.findViewById(viewId);

                if(view != null) {
                    // 能够注入所有的修饰符，不管是 private、public、protected都是可以的，相当于添加权限
                    field.setAccessible(true);

                    // 4. 动态的注入属性 找到的View
                    try {
                        field.set(object, view);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            }
        }


    }



    /**
     * 动态注入事件    object -> 反射需要执行的类
     */
    private static void injectEvent(ViewFinder finder, Object object) {
        // 1. 获取类中所有的方法
        Class<?> clazz = object.getClass();
        Method[] methods = clazz.getDeclaredMethods();

        // 2. 获取onClick里面的 value值
        for (Method method : methods) {
            OnClick onClick = method.getAnnotation(OnClick.class);
            if (onClick != null){
                int[] viewIds = onClick.value();
                for (int viewId : viewIds) {

                    // 3. findviewbyid 找到 view
                    View view= finder.findViewById(viewId);


                    // 拓展功能  检测网络  这里 !=null  代表需要检测网络
                    boolean isCheckNet = method.getAnnotation(CheckNet.class) != null;

                    if (view != null){
                        // 4. view.setOnClickListener
                        // 参数1：方法  参数2：谁去执行
                        view.setOnClickListener(new DeclaredOnClickListener(method , object , isCheckNet));
                    }

                }

            }
        }
    }


    /**
     * 这里是根据View的 onClick的源码
     */
    private static class DeclaredOnClickListener implements View.OnClickListener{

        private Method mMethod ;
        private Object mObject ;
        private boolean mIsCheckNet ;

        public DeclaredOnClickListener(Method method, Object object, boolean isCheckNet) {
            this.mMethod = method ;
            this.mObject = object ;
            this.mIsCheckNet = isCheckNet ;
        }


        /**
         * 最终我们在代码中写的点击事件会调用这个方法
         */
        @Override
        public void onClick(View v) {

            // 点击事件之前首先判断 需不需要检测网络
            if (mIsCheckNet){
                // 这里代表需要
                // 如果没网
                if (!networkAvailable(v.getContext())){
                    // 打印toast  "请检查网络" 这里写死会有问题 ，最好是可以去配置
                    Toast.makeText(v.getContext() , "请检查网络" , Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // 下边是执行有网的操作

            // 参数1：在哪一个类中执行  参数2：传递View
            try {
                // 确保所有类型方法都可以执行，包括private、public、protected等方法都可以执行
                mMethod.setAccessible(true);
                // 5. 反射注入方法
                mMethod.invoke(mObject , v) ;
            } catch (Exception e) {
                e.printStackTrace();


                // 这里是确保在类中的 onClick()方法中，不去传递View view的情况
                try {
                    mMethod.invoke(mObject , null) ;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }



    /**
     * 判断当前网络是否可用
     */
    private static boolean networkAvailable(Context context) {
        // 得到连接管理器对象
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
