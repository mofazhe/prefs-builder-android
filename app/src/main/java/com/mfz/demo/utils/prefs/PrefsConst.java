package com.mfz.demo.utils.prefs;

import com.mfz.prefsbuilder.PrefParams;
import com.mfz.prefsbuilder.PrefsClass;
import com.mfz.prefsbuilder.PrefsKey;

/**
 * @author mz
 * @date 2020/05/30/星期六
 * @time 18:33:01
 */
public class PrefsConst {
    @PrefsClass(currentPkg = true)
    public static class Test {
        @PrefsKey.String()
        public static String SHOW_EDIT_TEXT_BUTTON = "b";

        @PrefsKey.String()
        @PrefParams(codecId = 2)
        public static String SETTING_A = "a";
    }
}
