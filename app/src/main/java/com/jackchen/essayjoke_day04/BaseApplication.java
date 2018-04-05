package com.jackchen.essayjoke_day04;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alipay.euler.andfix.patch.PatchManager;
import com.hc.baselibrary.ioc.ExceptionCrashHandler;
import com.hc.baselibrary.ioc.fixBug.FixDexManager;

/**
 * Email: 2185134304@qq.com
 * Created by JackChen 2018/4/1 12:48
 * Version 1.0
 * Params:
 * Description:
*/
public class BaseApplication extends Application {


    public static PatchManager mPatchManager ;
    @Override
    public void onCreate() {
        super.onCreate();

        /*// 设置全局异常捕捉类
        ExceptionCrashHandler.getInstance().init(this);

        // 初始化阿里热修复
        mPatchManager = new PatchManager(this) ;
        // 初始化版本，获取当前应用的版本
        mPatchManager.init(getAppVersionName(this));
        // 加载之前的 apatch包
        mPatchManager.loadPatch();*/


        // 加载所有修复的 dex包
        try {
            FixDexManager fixDexManager = new FixDexManager(this) ;
            fixDexManager.loadFixDex() ;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



//    /**
//     * 返回当前程序版本名
//     */
//    public String getAppVersionName(Context context) {
//        String versionName = "";
//        try {
//            PackageManager pm = context.getPackageManager();
//            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
//            versionName = pi.versionName;
//            if (versionName == null || versionName.length() <= 0) {
//                return "";
//            }
//        } catch (Exception e) {
//            Log.e("VersionInfo", "Exception", e);
//        }
//        return versionName;
//    }
}
