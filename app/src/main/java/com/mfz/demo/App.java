package com.mfz.demo;

import android.app.Application;

import com.mfz.demo.utils.Utils;

/**
 * @author mz
 * @date 2020/05/13/Wed
 * @time 18:56
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
