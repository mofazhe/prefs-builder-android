package com.mfz.prefsbuilder.annotationprocessor;

/**
 * @author mz
 * @date 2020/05/13/Wed
 * @time 18:27
 */
public interface Const {

    interface OptionArg {
        String PKG = "prefsBuilderPkg";
        String CLASS_PREFIX = "prefsBuilderClassPrefix";
        String CLASS_SUFFIX = "prefsBuilderClassSuffix";
    }

    interface Default {
        String PKG = "com.mfz.prefs";
        String CLASS_PREFIX = "";
        String CLASS_SUFFIX = "";
    }

}
