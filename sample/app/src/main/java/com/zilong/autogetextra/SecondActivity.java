package com.zilong.autogetextra;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class SecondActivity extends AppCompatActivity {

    @AutoGetExtra("key_test")
    public String value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InjectAutoGetExtra.bind(this);


        Log.e("SecondActivity", "---------> " + value);
    }
}
