package com.mfz.prefsbuilder.annotationprocessor;

import com.squareup.javapoet.ClassName;

/**
 * @author mz
 * @date 2020/05/19/Tue
 * @time 11:32
 */
public class PrefsClassInfo {
    private ClassName mClassName;
    private String mFileName;
    private ClassName mOriginalClassName;

    private PrefsClassInfo(Builder builder) {
        setClassName(builder.mClassName);
        setFileName(builder.mFileName);
        setOriginalClassName(builder.mOriginalClassName);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public ClassName getClassName() {
        return mClassName;
    }

    public void setClassName(ClassName className) {
        mClassName = className;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public ClassName getOriginalClassName() {
        return mOriginalClassName;
    }

    public void setOriginalClassName(ClassName originalClassName) {
        mOriginalClassName = originalClassName;
    }

    public Builder builder() {
        Builder builder = new Builder();
        builder.mClassName = getClassName();
        builder.mFileName = getFileName();
        builder.mOriginalClassName = getOriginalClassName();
        return builder;
    }

    public static final class Builder {
        private String mFileName;
        private ClassName mOriginalClassName;
        private ClassName mClassName;

        private Builder() {
        }

        public PrefsClassInfo build() {
            return new PrefsClassInfo(this);
        }

        public Builder fileName(String fileName) {
            mFileName = fileName;
            return this;
        }

        public Builder originalClassName(ClassName val) {
            mOriginalClassName = val;
            return this;
        }

        public Builder className(ClassName className) {
            mClassName = className;
            return this;
        }
    }
}
