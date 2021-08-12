package com.mfz.prefsbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mz
 * @date 2021/08/12/周四
 * @time 22:32:27
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PrefGenerateCtrl {

    /**
     * 是否要生成对应的remove方法
     *
     * @return the boolean
     */
    boolean generateRemove() default true;

    /**
     * 是否要生成对应的contains方法
     *
     * @return the boolean
     */
    boolean generateContains() default true;
}
