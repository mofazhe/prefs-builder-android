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
public @interface PrefParams {

    /**
     * 默认值是否为null，
     * 若为true，{@link PrefsVal.String#defVal()}无效，
     * {@link PrefsVal.String#defValFromId()}有效时，该值无效
     *
     * @return 是否为null boolean
     */
    boolean defNull() default true;

    /**
     * 默认值的方法
     * 优先级高
     * 大于0时才有效
     *
     * @return the int
     */
    int defValFromId() default 0;

    /**
     * 默认值是否为空列表，
     * 优先级低于{@link #defNull()}
     * {@link #defValFromId()}
     * 集合类型专用
     *
     * @return 是否为null boolean
     */
    boolean defEmpty() default true;

    /**
     * 默认的string
     * 除基础变量和String类型外，其他类型都是序列化为String来保存的
     * 所以只有其他类型会有该参数，该参数用于反序列化
     *
     * @return 字符串 java . lang . string
     */
    java.lang.String defString() default "";

    /**
     * 编解码方法id
     * 可以用于base64编解码或加密解密等需求
     * 注意：编解码的方法应该共用一个id，若不成对出现将会出错
     *
     * @return the int
     */
    int codecId() default 0;

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
