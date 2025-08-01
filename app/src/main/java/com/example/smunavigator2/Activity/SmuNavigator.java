package com.example.smunavigator2.Activity;

import android.app.Application;
import com.kakao.vectormap.KakaoMapSdk;

public class SmuNavigator extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoMapSdk.init(this, "2c1881d13901afe42059ecaf4283e2eb");
    }
}