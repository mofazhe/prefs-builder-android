package com.mfz.prefsbuilder.annotationprocessor.data;

import com.mfz.prefsbuilder.PrefsDefVal;
import com.mfz.prefsbuilder.PrefsGenerateCtrl;
import com.mfz.prefsbuilder.PrefsParams;
import com.mfz.prefsbuilder.annotationprocessor.ElementHandler;
import com.mfz.prefsbuilder.annotationprocessor.MethodUtils;
import com.mfz.prefsbuilder.annotationprocessor.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

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
    private TypeName mEmptyTypeName;

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
        setEmptyTypeName(builder.mEmptyTypeName);
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

    public TypeName getEmptyTypeName() {
        return mEmptyTypeName;
    }

    public void setEmptyTypeName(TypeName emptyTypeName) {
        mEmptyTypeName = emptyTypeName;
    }

    public static AnnotationParams create(ElementHandler handler,
                                          VariableElement element,
                                          Class<? extends Annotation> annotationCls) {
        Builder builder = AnnotationParams.newBuilder();
        Elements elementUtils = handler.getElementUtils();

        PrefsParams prefParams = element.getAnnotation(PrefsParams.class);
        if (prefParams != null) {
            builder.codeId(prefParams.codecId());
        }
        PrefsDefVal prefsDefVal = element.getAnnotation(PrefsDefVal.class);
        if (prefsDefVal != null) {
            Map<Class<? extends Annotation>, Class<?>> defEmptyMap = handler.getDefEmptyMap();
            TypeName emptyType = null;
            if (defEmptyMap.containsKey(annotationCls)) {
                emptyType = MethodUtils.getEmptyType(element, elementUtils);
                if (emptyType == null || TypeName.VOID.equals(emptyType)) {
                    emptyType = TypeName.get(defEmptyMap.get(annotationCls));
                }
            }
            builder.defValFromId(prefsDefVal.defValFromId())
                    .defNull(prefsDefVal.defNull())
                    .defEmpty(prefsDefVal.defEmpty())
                    .defString(prefsDefVal.defString())
                    .emptyTypeName(emptyType);
        }
        PrefsGenerateCtrl generateCtrl = element.getAnnotation(PrefsGenerateCtrl.class);
        if (generateCtrl != null) {
            builder.generateRemove(generateCtrl.generateRemove())
                    .generateContains(generateCtrl.generateContains());
        }

        TypeName prefixType = MethodUtils.getPrefixType(element, elementUtils);
        TypeName suffixType = MethodUtils.getSuffixType(element, elementUtils);

        boolean hasPrefix = prefixType != null && prefixType != TypeName.VOID;
        boolean hasSuffix = suffixType != null && suffixType != TypeName.VOID;

        String keyStatement = "$T.%s";
        if (hasPrefix) {
            keyStatement = builder.mPrefixParamName + "+" + keyStatement;
        }
        if (hasSuffix) {
            keyStatement = keyStatement + "+" + builder.mSuffixParamName;
        }
        keyStatement = StringUtils.format(keyStatement, element.getSimpleName().toString());

        return builder.hasPrefix(hasPrefix)
                .hasSuffix(hasSuffix)
                .prefixTypeName(prefixType)
                .suffixTypeName(suffixType)
                .keyStatement(keyStatement)
                .build();
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
        builder.mEmptyTypeName = getEmptyTypeName();
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
        private TypeName mEmptyTypeName;

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

        public Builder emptyTypeName(TypeName val) {
            mEmptyTypeName = val;
            return this;
        }

        public AnnotationParams build() {
            return new AnnotationParams(this);
        }
    }
}
