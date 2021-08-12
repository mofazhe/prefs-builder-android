package com.mfz.prefsbuilder.annotationprocessor.data;

import com.mfz.prefsbuilder.annotationprocessor.StringUtils;
import com.squareup.javapoet.TypeName;

/**
 * @author cjj
 * @version 1.0
 * @date 2021/07/14/周三
 * @time 17:35
 */
public class AnnotationParams {
    private boolean mDefNull;
    private int mDefValFromId;
    private boolean mDefEmpty;
    private String mDefString;
    private int mCodeId;
    private TypeName mPrefixTypeName;
    private TypeName mSuffixTypeName;
    private boolean mGenerateRemove;
    private boolean mGenerateContains;
    private boolean mHasPrefix;
    private boolean mHasSuffix;
    private String mPrefixParamName;
    private String mSuffixParamName;
    private String mKeyStatement;

    private AnnotationParams(Builder builder) {
        setDefNull(builder.mDefNull);
        setDefValFromId(builder.mDefValFromId);
        setDefEmpty(builder.mDefEmpty);
        setDefString(builder.mDefString);
        setCodeId(builder.mCodeId);
        setPrefixTypeName(builder.mPrefixTypeName);
        setSuffixTypeName(builder.mSuffixTypeName);
        setGenerateRemove(builder.mGenerateRemove);
        setGenerateContains(builder.mGenerateContains);
        setHasPrefix(builder.mHasPrefix);
        setHasSuffix(builder.mHasSuffix);
        setPrefixParamName(builder.mPrefixParamName);
        setSuffixParamName(builder.mSuffixParamName);
        setKeyStatement(builder.mKeyStatement);
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

    public boolean isHasPrefix() {
        return mHasPrefix;
    }

    public void setHasPrefix(boolean hasPrefix) {
        mHasPrefix = hasPrefix;
    }

    public boolean isHasSuffix() {
        return mHasSuffix;
    }

    public void setHasSuffix(boolean hasSuffix) {
        mHasSuffix = hasSuffix;
    }

    public String getPrefixParamName() {
        return mPrefixParamName;
    }

    public void setPrefixParamName(String prefixParamName) {
        mPrefixParamName = prefixParamName;
    }

    public String getSuffixParamName() {
        return mSuffixParamName;
    }

    public void setSuffixParamName(String suffixParamName) {
        mSuffixParamName = suffixParamName;
    }

    public String getKeyStatement() {
        return mKeyStatement;
    }

    public void setKeyStatement(String keyStatement) {
        mKeyStatement = keyStatement;
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
        builder.mHasPrefix = isHasPrefix();
        builder.mHasSuffix = isHasSuffix();
        builder.mPrefixParamName = getPrefixParamName();
        builder.mSuffixParamName = getSuffixParamName();
        builder.mKeyStatement = getKeyStatement();
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
        private boolean mHasPrefix;
        private boolean mHasSuffix;
        private String mPrefixParamName;
        private String mSuffixParamName;
        private String mKeyStatement;

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
            mHasPrefix = false;
            mHasSuffix = false;
            mPrefixParamName = "prefix";
            mSuffixParamName = "suffix";
            mKeyStatement = "$T.%s";
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

        public Builder hasPrefix(boolean val) {
            mHasPrefix = val;
            return this;
        }

        public Builder hasSuffix(boolean val) {
            mHasSuffix = val;
            return this;
        }

        public Builder prefixParamName(String val) {
            mPrefixParamName = val;
            return this;
        }

        public Builder suffixParamName(String val) {
            mSuffixParamName = val;
            return this;
        }

        public Builder keyStatement(String val) {
            mKeyStatement = val;
            return this;
        }

        public AnnotationParams build(KeyParams params) {
            boolean hasPrefix = mPrefixTypeName != null
                    && mPrefixTypeName != TypeName.VOID;
            boolean hasSuffix = mSuffixTypeName != null
                    && mSuffixTypeName != TypeName.VOID;

            String keyStatement;
            if (hasPrefix && hasSuffix) {
                keyStatement = mPrefixParamName + "+$T.%s+" + mSuffixParamName;
            } else if (hasPrefix) {
                keyStatement = mPrefixParamName + "+$T.%s";
            } else if (hasSuffix) {
                keyStatement = "$T.%s+" + mSuffixParamName;
            } else {
                keyStatement = "$T.%s";
            }
            keyStatement = StringUtils.format(keyStatement, params.getFiledName());
            return new AnnotationParams(hasPrefix(hasPrefix)
                    .hasSuffix(hasSuffix)
                    .keyStatement(keyStatement));
        }
    }
}
