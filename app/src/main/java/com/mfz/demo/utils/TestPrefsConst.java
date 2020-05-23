package com.mfz.demo.utils;

import com.mfz.prefsbuilder.DefaultValue;

/**
 * @author mz
 * @date 2020/05/14/Thu
 * @time 10:17
 */
// @PrefsClass(className = "User")
public class TestPrefsConst {
    // @PrefsVal.Int()
    // public static final String TEST_INT = null;
    // @PrefsVal.String(defNull = true)

    @DefaultValue(id = 144)
    public static final boolean testBool = true;
    @DefaultValue(id = 166)
    public static final double testDouble = 1.2;
    @DefaultValue(id = 177)
    public static final byte testByte = ' ';

    @DefaultValue(id = 133)
    public static float getFloat() {
        return 1f;
    }

    @DefaultValue(id = 155)
    public static byte getByte() {
        return 1;
    }

    @DefaultValue(id = 188)
    public static short getShort() {
        return 1;
    }
}
