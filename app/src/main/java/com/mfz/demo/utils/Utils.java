package com.mfz.demo.utils;

import android.content.Context;

/**
 * @author mz
 * @date 2020/05/23/星期六
 * @time 20:51:50
 */
public class Utils {
    private static Context sContext;

    public static void init(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        return sContext;
    }
}
