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
public @interface PrefsDefVal {

    /**
     * 默认值来源
     *
     * @return the def val src
     */
    DefValSrc defValSrc() default DefValSrc.DEFAULT;

    /**
     * 当{@link #defValSrc()}等于{@link DefValSrc#FROM_ID}时有效
     *
     * @return the int
     */
    int fromId() default 0;

    /**
     * 当集合类型要求返回空列表时
     * 用该值指定实际的返回的类型
     * 默认空类型定义在{@link com.mfz.prefsbuilder.annotationprocessor.AnnotationList#getDefEmptyMap()}
     *
     * @return the class
     */
    Class<?> emptyType() default void.class;

    /**
     * 默认的string
     * 除基础变量和String类型外，其他类型都是序列化为String来保存的
     * 所以只有其他类型会有该参数，该参数用于反序列化
     *
     * @return 字符串 java . lang . string
     */
    java.lang.String defString() default "";
}
