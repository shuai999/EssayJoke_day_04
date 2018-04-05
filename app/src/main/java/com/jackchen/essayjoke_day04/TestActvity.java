package com.jackchen.essayjoke_day04;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hc.baselibrary.ioc.ioc.ViewById;
import com.hc.framelibrary.ioc.BaseSkinActivity;

/**
 * Created by Administrator on 2018/4/1.
 */

public class TestActvity extends BaseSkinActivity {


    @ViewById(R.id.btn_test)
    Button btn_test ;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_test);
    }

    @Override
    protected void initTitle() {

    }

    @Override
    protected void initView() {
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActvity.this , 2/0+"测试" , Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void initData() {

    }
}
