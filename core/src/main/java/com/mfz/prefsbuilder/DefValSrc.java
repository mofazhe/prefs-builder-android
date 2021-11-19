package com.mfz.prefsbuilder;

/**
 * @author cjj
 * @version 1.0
 * @date 2021/09/28/周二
 * @time 14:20
 */
public enum DefValSrc {
    /**
     * 默认情况
     * 根据value类型分为以下情况：
     * 1.基础类型时，为defVal的值，比如int类型为{@link PrefsKey.Int#defVal()}
     * 2.{@link String}类型时，为{@link PrefsKey.String#defVal()};
     * 3.集合类型时，为空集合，空集合类型由{@link PrefsDefVal#emptyType()}定义
     * 4.其他类型时，为空对象
     */
    DEFAULT,
    /**
     * 空对象
     * 当value为基础类型时，效果同{@link DefValSrc#DEFAULT}
     */
    NULL,
    /**
     * 从{@link PrefsDefVal#fromId()}读取id，
     * 然后从与{@link DefaultValue#id()}相等的位置读取
     */
    FROM_ID,
}
