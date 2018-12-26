AutoGetExtra
=================

# 示例

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_test)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
//                        intent.putExtra("key_test","ekjkjkdsknenen呢嫩");
//                        startActivity(intent);
                        OneFragment oneFragment = OneFragment.getInstance("fragmnet");
                        getSupportFragmentManager().beginTransaction().add(R.id.fl, oneFragment).commitAllowingStateLoss();
                    }
                });

    }
}
```

```java

public class SecondActivity extends AppCompatActivity {
    //通过注解，设置好对应的 key
    @AutoGetExtra("key_test")
    public String value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //注入
        InjectAutoGetExtra.bind(this);


        Log.e("SecondActivity", "---------> " + value);
    }
}


```

如何导入
===========

```groovy
//项目 build.gradle 引入 jitpack仓库
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }

}

//使用到注解的module ，build.gradle 引入依赖

dependencies {
   //引入依赖
   implementation 'com.github.zilong-sky.AutoGetExtra:annotation:2.0'
   //引入 编译处理器
   annotationProcessor 'com.github.zilong-sky.AutoGetExtra:compiler:2.0'
}
```