package com.jackchen.essayjoke_day04;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hc.baselibrary.ioc.fixBug.FixDexManager;
import com.hc.baselibrary.ioc.ioc.ViewById;
import com.hc.framelibrary.ioc.BaseSkinActivity;

import java.io.File;

public class MainActivity extends BaseSkinActivity {


    @ViewById(R.id.btn_test)
    Button btn_test ;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initView() {
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(TestActvity.class);
            }
        });
    }

    @Override
    protected void initData() {
        // 用户只要一打开app，就去调用我们自己的修复方式
        fixDexBug() ;
    }

    private void fixDexBug() {
        File fixFile = new File(Environment.getExternalStorageDirectory() , "fix.dex") ;

        if (fixFile.exists()) {
            FixDexManager fixDexManager = new FixDexManager(this);
            try {
                fixDexManager.fixDex(fixFile.getAbsolutePath()) ;
                Toast.makeText(MainActivity.this , "修复成功" , Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this , "修复失败" , Toast.LENGTH_SHORT).show();
            }
        }

    }
}
