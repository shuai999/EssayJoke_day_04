package com.hc.baselibrary.ioc.fixBug;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.BaseDexClassLoader;

import static android.content.ContentValues.TAG;

/**
 * Email: 2185134304@qq.com
 * Created by JackChen 2018/4/5 9:49
 * Version 1.0
 * Params:
 * Description:    修复
*/
public class FixDexManager  {


    private Context mContext ;
    private File mDexDir ;
    public FixDexManager(Context context) {
        this.mContext = context ;
        // 获取应用可以访问的 dex目录
        this.mDexDir = context.getDir("odex" , Context.MODE_PRIVATE) ;
    }



    /**
     * 修复dex包
     * fixDexPath：下载补丁的路径
     */
    public void fixDex(String fixDexPath) throws Exception {


        // 2. 然后获取下载好的补丁 dexElement
        // 2.1 把fixDexPath移动到 系统能够访问的 dex目录下，因为我们最终要把它变为 ClassLoader
        File srcFile = new File(fixDexPath) ;
        if (!srcFile.exists()){
            throw new FileNotFoundException(fixDexPath) ;
        }
        File destFile = new File(mDexDir , srcFile.getName()) ;
        if (destFile.exists()){
            Log.d(TAG, "patch [" + fixDexPath + "] has be loaded.");
            return;
        }

        copyFile(srcFile , destFile);

        // 2.2 让该ClassLoader读取 fixDex路径
        //需要修复的文件
        List<File> fixDexFiles = new ArrayList<>() ;
        fixDexFiles.add(destFile) ;
        fixDexFiles(fixDexFiles) ;
    }


    /**
     * 把合并的数组applicationDexElements  注入到 原来的 applicationClassLoader中即可
     */
    private void injectDexElements(ClassLoader classLoader, Object dexElements) throws Exception {
        // 1. 先获取 pathList（通过反射）
        Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList") ;
        // private、public、protected都可以反射
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader) ;

        // 2. 获取pathList里边的 dexElements
        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);

        // 合并
        dexElementsField.set(pathList , dexElements);
    }


    /**
     * 合并两个数组
     *
     * @param arrayLhs ：左边的数组
     * @param arrayRhs ：右边的数组
     * @return
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        // 第一个数组的长度
        int i = Array.getLength(arrayLhs);
        // 数组总共的长度 = 第一个数组长度 + 第二个数组长度
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }



    /**
     * copy file
     *
     * @param src  source file
     * @param dest target file
     * @throws IOException
     */
    public static void copyFile(File src, File dest) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dest).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }



    /**
     * 从 ClassLoader中 获取 dexElements
     */
    private Object getDexElementsByClassLoader(ClassLoader classLoader) throws Exception {
        // 1. 先获取 pathList（通过反射）
        Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList") ;  // pathList是源码中的
        // private、public、protected都可以反射
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader) ;

        // 2. 获取pathList里边的 dexElements
        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements"); // dexElements是源码中的
        dexElementsField.setAccessible(true);


        return dexElementsField.get(pathList);

    }


    /**
     * 加载全部的修复包
     */
    public void loadFixDex() throws Exception {
        File[] dexFiles = mDexDir.listFiles() ;
        List<File> fixDexFiles = new ArrayList<>() ;
        for (File dexFile : dexFiles) {
            if (dexFile.getName().endsWith(".dex")){
                fixDexFiles.add(dexFile) ;
            }
        }
        fixDexFiles(fixDexFiles) ;
    }


    /**
     * 修复 dex
     */
    private void fixDexFiles(List<File> fixDexFiles) throws Exception {
        // 1. 先获取应用的 已经运行的 dexElements
        ClassLoader applicationClassLoader = mContext.getClassLoader();

        Object applicationDexElements = getDexElementsByClassLoader(applicationClassLoader) ;

        File optimizedDirectory = new File(mDexDir , "odex") ;
        if (!optimizedDirectory.exists()){
            optimizedDirectory.mkdirs() ;
        }
        // 开始修复
        for (File fixDexFile : fixDexFiles) {
            // dexPath:修复dex的路径  -->  fixDexFiles
            // optimizedDirectory：解压路径
            // libraryPath：so文件的路径
            // parent：父的ClassLoader
            ClassLoader fixDexClassLoader = new BaseDexClassLoader(
                    fixDexFile.getAbsolutePath() , // 修复的dex的路径   必须要在应用目录下的odex文件中
                    optimizedDirectory, // 解压路径
                    null , // .so文件的路径
                    applicationClassLoader // 父的 ClassLoader
            ) ;


            // 获取app中的 dexElements数组，然后解析来就需要把 下载的没有bug的 补丁的 dexElement插入到这个数组的最前边
            Object fixDexElements = getDexElementsByClassLoader(fixDexClassLoader) ;

            // 3. 把补丁的 dexElement插到 已经运行的 dexElement的最前面，其实就是合并，修复就ok
            // applicationClassLoader 是一个数组，fixDexElements也是一个数组，就是把两个数组合并

            // 3.1  合并完成
            // 前者是修复的，后者是没有修复的
            applicationDexElements = combineArray(fixDexElements , applicationDexElements) ;
        }


        // 3.2 把合并的数组注入到 原来的 applicationClassLoader中即可
        injectDexElements(applicationClassLoader , applicationDexElements) ;
    }
}
