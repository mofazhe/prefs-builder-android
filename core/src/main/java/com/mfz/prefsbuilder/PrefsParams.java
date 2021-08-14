package com.mfz.prefsbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cjj
 * @version 1.0
 * @date 2021/07/14/周三
 * @time 15:55
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PrefsParams {

    /**
     * 编解码方法id
     * 可以用于base64编解码或加密解密等需求
     * 注意：编解码的方法应该共用一个id，若不成对出现将会出错
     * 暂时只支持{@link PrefsKey.String}
     *
     * @return the int
     */
    int codecId() default 0;
}
