package com.hc.baselibrary.ioc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Email: 2185134304@qq.com
 * Created by JackChen 2018/4/1 9:43
 * Version 1.0
 * Params:
 * Description:  异常捕捉类：单例设计模式
*/
public class ExceptionCrashHandler implements Thread.UncaughtExceptionHandler {


    // 获取应用的一些信息
    private Context mContext ;
    // 获取系统默认的异常，让默认异常在 uncaughtException()方法中 去处理
    private Thread.UncaughtExceptionHandler mDefaultExceptionHandler ;
    private static volatile ExceptionCrashHandler mInstance ;
    private ExceptionCrashHandler(){}


    public void init(Context context){
        this.mContext = context ;

        // 设置全局的异常类 为 本类
        Thread.currentThread().setUncaughtExceptionHandler(this);
        mDefaultExceptionHandler = Thread.currentThread().getDefaultUncaughtExceptionHandler() ;
    }


    /**
     * 双重锁校验
     */
    public static ExceptionCrashHandler getInstance(){
        if (mInstance == null){
            // 用来解决多个线程并发的问题(多个线程抢资源)
            synchronized (ExceptionCrashHandler.class){
                if (mInstance == null){
                    mInstance = new ExceptionCrashHandler() ;
                }
            }
        }
        return mInstance ;
    }


    /**
     * 全局异常
     * 只要程序发生异常，就会执行这个方法，然后打印报异常了
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.e("TAG" , "报异常了") ;
        //1.  把异常写入本地文件
            // 1.1 崩溃的详细信息、
            // 1.2 app应用的信息：包名、版本号
            // 1.3 手机信息：比如红米Note、5.0或者6.0版本

        //2. 上传崩溃信息服务器
            // 这里直接保存当前的本地文件，等应用再次启动时候再去上传到服务器（但是上传不要在这里做处理）



        String crashFileName = saveInfoToSD(e);

        Log.e("TAG", "fileName --> " + crashFileName);

        // 3. 缓存崩溃日志文件
        cacheCrashFile(crashFileName);


        // 自己获取到异常后，让系统默认的处理异常
        mDefaultExceptionHandler.uncaughtException(t , e);

    }


    /**
     * 缓存崩溃日志文件
     *
     * @param fileName
     */
    private void cacheCrashFile(String fileName) {
        SharedPreferences sp = mContext.getSharedPreferences("crash", Context.MODE_PRIVATE);
        sp.edit().putString("CRASH_FILE_NAME", fileName).commit();
    }

    /**
     * 获取崩溃文件名称
     *
     * @return
     */
    public File getCrashFile() {
        String crashFileName = mContext.getSharedPreferences("crash",
                Context.MODE_PRIVATE).getString("CRASH_FILE_NAME", "");
        return new File(crashFileName);
    }



    /**
     * 保存获取的 软件信息，设备信息和出错信息保存在SDcard中
     *
     * @param ex
     * @return
     */
    private String saveInfoToSD(Throwable ex) {
        String fileName = null;
        StringBuffer sb = new StringBuffer();

        // 1. 手机信息 + 应用信息   --> obtainSimpleInfo()
        for (Map.Entry<String, String> entry : obtainSimpleInfo(mContext)
                .entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append(" = ").append(value).append("\n");
        }

        // 2.崩溃的详细信息
        sb.append(obtainExceptionInfo(ex));

        // 保存文件  手机应用的目录，并没有拿手机sdCard目录， 6.0 以上需要动态申请权限

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            File dir = new File(mContext.getFilesDir() + File.separator + "crash"
                    + File.separator);

            // 先删除之前的异常信息
            if (dir.exists()) {
                // 删除该目录下的所有子文件
                deleteDir(dir);
            }

            // 再从新创建文件夹
            if (!dir.exists()) {
                dir.mkdir();
            }

            try {
                fileName = dir.toString()
                        + File.separator
                        + getAssignTime("yyyy_MM_dd_HH_mm") + ".txt";
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(sb.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    private String getAssignTime(String dateFormatStr) {
        DateFormat dataFormat = new SimpleDateFormat(dateFormatStr);
        long currentTime = System.currentTimeMillis();
        return dataFormat.format(currentTime);
    }

    /**
     * 获取一些简单的信息,软件版本，手机版本，型号等信息存放在HashMap中
     *
     * @return
     */
    private HashMap<String, String> obtainSimpleInfo(Context context) {
        HashMap<String, String> map = new HashMap<>();
        PackageManager mPackageManager = context.getPackageManager();
        PackageInfo mPackageInfo = null;
        try {
            mPackageInfo = mPackageManager.getPackageInfo(
                    context.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        map.put("versionName", mPackageInfo.versionName);
        map.put("versionCode", "" + mPackageInfo.versionCode);
        map.put("MODEL", "" + Build.MODEL);
        map.put("SDK_INT", "" + Build.VERSION.SDK_INT);
        map.put("PRODUCT", "" + Build.PRODUCT);
        map.put("MOBLE_INFO", getMobileInfo());
        return map;
    }

    /**
     * 获取手机信息  HomiNote 6.0
     *
     * @return
     */
    public static String getMobileInfo() {
        StringBuffer sb = new StringBuffer();
        try {
            // 利用反射获取 Build 的所有属性
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                String value = field.get(null).toString();
                sb.append(name + "=" + value);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    /**
     * 获取系统未捕捉的错误信息
     *
     * @param throwable
     * @return
     */
    private String obtainExceptionInfo(Throwable throwable) {
        // Java基础 异常
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }


    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful. If a
     * deletion fails, the method stops attempting to delete and returns
     * "false".
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            // 递归删除目录中的子目录下
            for (File child : children) {
                child.delete();
            }
        }
        // 目录此时为空，可以删除
        return true;
    }

}
