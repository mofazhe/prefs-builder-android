package com.mfz.prefsbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mz
 * @date 2021/08/12/周四
 * @time 22:23:17
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PrefKeyHeadTail {

    /**
     * key的前缀class类型
     * 注意：使用的是该类的{@link java.lang.Object#toString()}方法
     *
     * @return the class
     */
    Class<?> prefixType() default void.class;

    /**
     * key的后缀class类型.
     * 注意：使用的是该类的{@link java.lang.Object#toString()}方法
     *
     * @return the class
     */
    Class<?> suffixType() default void.class;
}
