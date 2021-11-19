package com.mfz.prefsbuilder.annotationprocessor.data;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

/**
 * @author cjj
 * @version 1.0
 * @date 2021/11/19/周五
 * @time 11:42
 */
public class PrefixSuffixParams {
    private TypeName mTypeName;
    private boolean mNeed;
    private String mParamName;
    private ParameterSpec mParameterSpec;

    private PrefixSuffixParams(Builder builder) {
        setTypeName(builder.mTypeName);
        setNeed(builder.mNeed);
        setParamName(builder.mParamName);
        setParameterSpec(builder.mParameterSpec);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public TypeName getTypeName() {
        return mTypeName;
    }

    public void setTypeName(TypeName typeName) {
        mTypeName = typeName;
    }

    public boolean isNeed() {
        return mNeed;
    }

    public void setNeed(boolean need) {
        mNeed = need;
    }

    public String getParamName() {
        return mParamName;
    }

    public void setParamName(String paramName) {
        mParamName = paramName;
    }

    public ParameterSpec getParameterSpec() {
        return mParameterSpec;
    }

    public void setParameterSpec(ParameterSpec parameterSpec) {
        mParameterSpec = parameterSpec;
    }

    public Builder builder() {
        Builder builder = new Builder();
        builder.mTypeName = getTypeName();
        builder.mNeed = isNeed();
        builder.mParamName = getParamName();
        builder.mParameterSpec = getParameterSpec();
        return builder;
    }

    public static final class Builder {
        private TypeName mTypeName;
        private boolean mNeed;
        private String mParamName;
        private ParameterSpec mParameterSpec;

        private Builder() {
        }

        public Builder typeName(TypeName val) {
            mTypeName = val;
            return this;
        }

        public Builder need(boolean val) {
            mNeed = val;
            return this;
        }

        public Builder paramName(String val) {
            mParamName = val;
            return this;
        }

        public Builder parameterSpec(ParameterSpec val) {
            mParameterSpec = val;
            return this;
        }

        public PrefixSuffixParams build() {
            return new PrefixSuffixParams(this);
        }
    }
}
