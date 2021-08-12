package com.mfz.prefsbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mz
 * @date 2021/08/12/周四
 * @time 22:30:44
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PrefDefVal {

    /**
     * 默认值是否为null，
     * 若为true，{@link PrefsKey.String#defVal()}无效，
     * {@link PrefsKey.String#defValFromId()}有效时，该值无效
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
}
