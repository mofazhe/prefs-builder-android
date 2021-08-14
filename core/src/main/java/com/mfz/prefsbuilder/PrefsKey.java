package com.mfz.prefsbuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 根据注解的类型创建prefs类中的get/is/set方法
 * 注意1：类需要注解{@link PrefsClass}
 * 注意2：需要有类实现{@link BasePrefsInterface}并注解{@link BasePrefsClass}
 * 注意3：仅支持以下值，部分类型需要定义序列化与反序列化方法
 *
 * @author mz
 * @date 2020 /05/14/Thu
 * @time 14 :56
 */
public class PrefsKey {

    /**
     * int类型
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Int {

        /**
         * 默认值
         *
         * @return the int
         */
        int defVal() default 0;
    }

    /**
     * float类型
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Float {

        /**
         * Def val float.
         *
         * @return the float
         */
        float defVal() default 0f;
    }

    /**
     * boolean类型
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Bool {

        /**
         * Def val boolean.
         *
         * @return the boolean
         */
        boolean defVal() default false;
    }

    /**
     * byte类型
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Byte {

        /**
         * Def val byte.
         *
         * @return the byte
         */
        byte defVal() default 0;
    }

    /**
     * double类型
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Double {

        /**
         * Def val double.
         *
         * @return the double
         */
        double defVal() default 0.0;
    }

    /**
     * char类型
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Char {

        /**
         * Def val char.
         *
         * @return the char
         */
        char defVal() default '\0';
    }

    /**
     * long类型
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Long {

        /**
         * Def val long.
         *
         * @return the long
         */
        long defVal() default 0L;
    }

    /**
     * short类型
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Short {

        /**
         * Def val short.
         *
         * @return the short
         */
        short defVal() default 0;
    }

    /**
     * String类型
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface String {

        /**
         * 默认值
         * 优先级最低
         *
         * @return the java . lang . string
         */
        java.lang.String defVal() default "";
    }

    /**
     * 除基本类型以外的类型保存，包括基本类型的装箱类
     * 注意：需要设置序列号和反序列化方法，否则将会出错
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Object {

        /**
         * 返回值类型
         *
         * @return the class
         */
        Class<?> type();
    }

    /**
     * {@link java.util.List}类型
     * 注意：需要设置序列化和反序列化方法(序列化可使用object的)
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface List {

        /**
         * 返回值类型
         *
         * @return the class
         */
        Class<?> type();
    }

    /**
     * {@link java.util.Set}类型
     * 需要设置序列化和反序列化方法(序列化可使用object的)
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Set {

        /**
         * 返回值类型
         *
         * @return the class
         */
        Class<?> type();
    }

    /**
     * {@link java.util.Queue}类型
     * 需要设置序列化和反序列化方法(序列化可使用object的)
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Queue {

        /**
         * 返回值类型
         *
         * @return the class
         */
        Class<?> type();
    }

    /**
     * {@link java.util.Deque}类型
     * 需要设置序列化和反序列化方法(序列化可使用object的)
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Deque {

        /**
         * 返回值类型
         *
         * @return the class
         */
        Class<?> type();
    }

    // /**
    //  * SparseArray类型
    //  * 需要设置序列化和反序列化方法(序列化可使用object的)
    //  * 这是android库的类，若使用Gson库则需要手动添加序列化与反序列化适配器
    //  *
    //  * 暂时不支持，后续会提供支持
    //  */
    // @Retention(RetentionPolicy.SOURCE)
    // @Target(ElementType.FIELD)
    // public @interface SparseArray {
    //
    //     /**
    //      * 返回值类型
    //      *
    //      * @return the class
    //      */
    //     Class<?> type();
    // }

    /**
     * {@link java.util.Map}类型
     * 需要设置序列化和反序列化方法(序列化可使用object的)
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface Map {

        /**
         * 返回值类型
         *
         * @return the class
         */
        Class<?> keyType();

        /**
         * 返回值的泛型
         *
         * @return the class
         */
        Class<?> valType();
    }
}
