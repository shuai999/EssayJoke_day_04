package com.hc.baselibrary.ioc.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hc.baselibrary.ioc.ioc.ViewUtils;

/**
 * Email: 2185134304@qq.com
 * Created by JackChen 2018/3/31 21:43
 * Version 1.0
 * Params:
 * Description:  整个应用的 BaseActivity
*/
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置布局 layout
        setContentView();

        // 一些特定的算法，子类基本都会使用的
        ViewUtils.inject(this);

        // 设置标题
        initTitle() ;

        // 初始化界面
        initView() ;

        // 初始化数据
        initData() ;

    }

    // 设置布局 layout
    protected abstract void setContentView();

    // 设置标题
    protected abstract void initTitle();

    // 初始化界面
    protected abstract void initView();

    // 初始化数据
    protected abstract void initData();


    /**
     * 启动Activity
     */
    protected void startActivity(Class<?> clazz){
        Intent intent = new Intent(this , clazz) ;
        startActivity(intent);
    }


    /**
     *  如果不去使用这个注解，就想去用findViewById，那么在Activity或者Fragment中直接用下边的这个方法就行
     *  下边意思就是：泛型在方法上边的使用
     */
    protected <T extends View> T viewById(int viewId){
        return (T) findViewById(viewId);
    }
}
