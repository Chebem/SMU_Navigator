package com.example.smunavigator2.Activity;

import android.app.Application;

import com.example.smunavigator2.BuildConfig;
import com.kakao.vectormap.KakaoMapSdk;

public class SmuNavigator extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoMapSdk.init(this, BuildConfig.KAKAO_MAP_API_KEY);
    }
}