package com.zilong.autogetextra;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OneFragment extends Fragment {

    @AutoGetExtra("key_test")
    public DemoBean value;

    public static OneFragment getInstance(String value) {
        OneFragment oneFragment = new OneFragment();
        Bundle bundle = new Bundle();
        DemoBean demoBean = new DemoBean();
        demoBean.setDemo("demobean");
        bundle.putParcelable("key_test", demoBean);
        oneFragment.setArguments(bundle);

        return oneFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InjectAutoGetExtra.bind(this);

        Log.e("OneFragment", "------------------> " + value.getDemo());
    }
}
