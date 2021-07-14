package com.mfz.prefsbuilder.annotationprocessor;

import com.squareup.javapoet.TypeName;

/**
 * @author cjj
 * @version 1.0
 * @date 2021/07/14/周三
 * @time 17:35
 */
public class PrefParamsData {
    private boolean mDefNull;
    private int mDefValFromId;
    private boolean mDefEmpty;
    private String mDefString;
    private int mCodeId;
    private TypeName mPrefixTypeName;
    private TypeName mSuffixTypeName;
    private boolean mGenerateRemove;
    private boolean mGenerateContains;

    private PrefParamsData(Builder builder) {
        setDefNull(builder.mDefNull);
        setDefValFromId(builder.mDefValFromId);
        setDefEmpty(builder.mDefEmpty);
        setDefString(builder.mDefString);
        setCodeId(builder.mCodeId);
        setPrefixTypeName(builder.mPrefixTypeName);
        setSuffixTypeName(builder.mSuffixTypeName);
        setGenerateRemove(builder.mGenerateRemove);
        setGenerateContains(builder.mGenerateContains);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public boolean isDefNull() {
        return mDefNull;
    }

    public void setDefNull(boolean defNull) {
        mDefNull = defNull;
    }

    public int getDefValFromId() {
        return mDefValFromId;
    }

    public void setDefValFromId(int defValFromId) {
        mDefValFromId = defValFromId;
    }

    public boolean isDefEmpty() {
        return mDefEmpty;
    }

    public void setDefEmpty(boolean defEmpty) {
        mDefEmpty = defEmpty;
    }

    public String getDefString() {
        return mDefString;
    }

    public void setDefString(String defString) {
        mDefString = defString;
    }

    public int getCodeId() {
        return mCodeId;
    }

    public void setCodeId(int codeId) {
        mCodeId = codeId;
    }

    public TypeName getPrefixTypeName() {
        return mPrefixTypeName;
    }

    public void setPrefixTypeName(TypeName prefixTypeName) {
        mPrefixTypeName = prefixTypeName;
    }

    public TypeName getSuffixTypeName() {
        return mSuffixTypeName;
    }

    public void setSuffixTypeName(TypeName suffixTypeName) {
        mSuffixTypeName = suffixTypeName;
    }

    public boolean isGenerateRemove() {
        return mGenerateRemove;
    }

    public void setGenerateRemove(boolean generateRemove) {
        mGenerateRemove = generateRemove;
    }

    public boolean isGenerateContains() {
        return mGenerateContains;
    }

    public void setGenerateContains(boolean generateContains) {
        mGenerateContains = generateContains;
    }

    public Builder builder() {
        Builder builder = new Builder();
        builder.mDefNull = isDefNull();
        builder.mDefValFromId = getDefValFromId();
        builder.mDefEmpty = isDefEmpty();
        builder.mDefString = getDefString();
        builder.mCodeId = getCodeId();
        builder.mPrefixTypeName = getPrefixTypeName();
        builder.mSuffixTypeName = getSuffixTypeName();
        builder.mGenerateRemove = isGenerateRemove();
        builder.mGenerateContains = isGenerateContains();
        return builder;
    }

    public static final class Builder {
        private boolean mDefNull;
        private int mDefValFromId;
        private boolean mDefEmpty;
        private String mDefString;
        private int mCodeId;
        private TypeName mPrefixTypeName;
        private TypeName mSuffixTypeName;
        private boolean mGenerateRemove;
        private boolean mGenerateContains;

        private Builder() {
            mDefNull = true;
            mDefValFromId = 0;
            mDefEmpty = true;
            mDefString = "";
            mCodeId = 0;
            mPrefixTypeName = TypeName.VOID;
            mSuffixTypeName = TypeName.VOID;
            mGenerateRemove = true;
            mGenerateContains = true;
        }

        public Builder defNull(boolean val) {
            mDefNull = val;
            return this;
        }

        public Builder defValFromId(int val) {
            mDefValFromId = val;
            return this;
        }

        public Builder defEmpty(boolean val) {
            mDefEmpty = val;
            return this;
        }

        public Builder defString(String val) {
            mDefString = val;
            return this;
        }

        public Builder codeId(int val) {
            mCodeId = val;
            return this;
        }

        public Builder prefixTypeName(TypeName val) {
            mPrefixTypeName = val;
            return this;
        }

        public Builder suffixTypeName(TypeName val) {
            mSuffixTypeName = val;
            return this;
        }

        public Builder generateRemove(boolean val) {
            mGenerateRemove = val;
            return this;
        }

        public Builder generateContains(boolean val) {
            mGenerateContains = val;
            return this;
        }

        public PrefParamsData build() {
            return new PrefParamsData(this);
        }
    }
}
