package com.mfz.prefsbuilder.annotationprocessor.data;

import com.mfz.prefsbuilder.DefValSrc;
import com.mfz.prefsbuilder.PrefsDefVal;
import com.mfz.prefsbuilder.PrefsGenerateCtrl;
import com.mfz.prefsbuilder.PrefsParams;
import com.mfz.prefsbuilder.annotationprocessor.ElementHandler;
import com.mfz.prefsbuilder.annotationprocessor.MethodUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * @author cjj
 * @version 1.0
 * @date 2021/08/10/周二
 * @time 9:33
 */
public class KeyParams {
    private String mFiledName;
    private String mValueName;
    private Object mDefValue;
    private ClassName mFullClassName;
    private String mFullClassStr;
    private TypeName mTypeName;
    private Annotation mAnnotation;
    private TypeName mKeyTypeName;
    private TypeName mValTypeName;
    private boolean mGenericVal;
    private VariableElement mElement;
    private DefValSrc mDefValSrc;
    private int mDefValFromId;
    private String mDefString;
    private int mCodecId;
    private boolean mGenerateRemove;
    private boolean mGenerateContains;
    private String mKeyStatement;
    private TypeName mEmptyTypeName;
    private PrefixSuffixParams mSuffixParams;
    private PrefixSuffixParams mPrefixParams;

    private KeyParams(Builder builder) {
        setFiledName(builder.mFiledName);
        setValueName(builder.mValueName);
        setDefValue(builder.mDefValue);
        setFullClassName(builder.mFullClassName);
        setFullClassStr(builder.mFullClassStr);
        setTypeName(builder.mTypeName);
        setAnnotation(builder.mAnnotation);
        setKeyTypeName(builder.mKeyTypeName);
        setValTypeName(builder.mValTypeName);
        setGenericVal(builder.mGenericVal);
        setElement(builder.mElement);
        setDefValSrc(builder.mDefValSrc);
        setDefValFromId(builder.mDefValFromId);
        setDefString(builder.mDefString);
        setCodecId(builder.mCodecId);
        setGenerateRemove(builder.mGenerateRemove);
        setGenerateContains(builder.mGenerateContains);
        setKeyStatement(builder.mKeyStatement);
        setEmptyTypeName(builder.mEmptyTypeName);
        setSuffixParams(builder.mSuffixParams);
        setPrefixParams(builder.mPrefixParams);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getFiledName() {
        return mFiledName;
    }

    public void setFiledName(String filedName) {
        mFiledName = filedName;
    }

    public String getValueName() {
        return mValueName;
    }

    public void setValueName(String valueName) {
        mValueName = valueName;
    }

    public Object getDefValue() {
        return mDefValue;
    }

    public void setDefValue(Object defValue) {
        mDefValue = defValue;
    }

    public ClassName getFullClassName() {
        return mFullClassName;
    }

    public void setFullClassName(ClassName fullClassName) {
        mFullClassName = fullClassName;
    }

    public String getFullClassStr() {
        return mFullClassStr;
    }

    public void setFullClassStr(String fullClassStr) {
        mFullClassStr = fullClassStr;
    }

    public TypeName getTypeName() {
        return mTypeName;
    }

    public void setTypeName(TypeName typeName) {
        mTypeName = typeName;
    }

    public Annotation getAnnotation() {
        return mAnnotation;
    }

    public void setAnnotation(Annotation annotation) {
        mAnnotation = annotation;
    }

    public TypeName getKeyTypeName() {
        return mKeyTypeName;
    }

    public void setKeyTypeName(TypeName keyTypeName) {
        mKeyTypeName = keyTypeName;
    }

    public TypeName getValTypeName() {
        return mValTypeName;
    }

    public void setValTypeName(TypeName valTypeName) {
        mValTypeName = valTypeName;
    }

    public boolean isGenericVal() {
        return mGenericVal;
    }

    public void setGenericVal(boolean genericVal) {
        mGenericVal = genericVal;
    }

    public VariableElement getElement() {
        return mElement;
    }

    public void setElement(VariableElement element) {
        mElement = element;
    }

    public DefValSrc getDefValSrc() {
        return mDefValSrc;
    }

    public void setDefValSrc(DefValSrc defValSrc) {
        mDefValSrc = defValSrc;
    }

    public int getDefValFromId() {
        return mDefValFromId;
    }

    public void setDefValFromId(int defValFromId) {
        mDefValFromId = defValFromId;
    }

    public String getDefString() {
        return mDefString;
    }

    public void setDefString(String defString) {
        mDefString = defString;
    }

    public int getCodecId() {
        return mCodecId;
    }

    public void setCodecId(int codecId) {
        mCodecId = codecId;
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

    public PrefixSuffixParams getSuffixParams() {
        return mSuffixParams;
    }

    public void setSuffixParams(PrefixSuffixParams suffixParams) {
        mSuffixParams = suffixParams;
    }

    public PrefixSuffixParams getPrefixParams() {
        return mPrefixParams;
    }

    public void setPrefixParams(PrefixSuffixParams prefixParams) {
        mPrefixParams = prefixParams;
    }

    public static KeyParams.Builder create(ElementHandler handler,
                                           VariableElement element,
                                           Annotation annotation) {
        Class<? extends Annotation> annotationCls = annotation.annotationType();
        KeyParams.Builder builder = KeyParams.newBuilder();
        Elements elementUtils = handler.getElementUtils();

        PrefsParams prefParams = element.getAnnotation(PrefsParams.class);
        if (prefParams != null) {
            builder.codecId(prefParams.codecId());
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
            builder.defValFromId(prefsDefVal.fromId())
                    .defValSrc(prefsDefVal.defValSrc())
                    .defString(prefsDefVal.defString())
                    .emptyTypeName(emptyType);
        }
        PrefsGenerateCtrl generateCtrl = element.getAnnotation(PrefsGenerateCtrl.class);
        if (generateCtrl != null) {
            builder.generateRemove(generateCtrl.generateRemove())
                    .generateContains(generateCtrl.generateContains());
        }

        String keyStatement = "$T." + element.getSimpleName().toString();

        PrefixSuffixParams.Builder prefixBuilder = PrefixSuffixParams.newBuilder();
        TypeName prefixType = MethodUtils.getPrefixType(element, elementUtils);
        if (prefixType != null && prefixType != TypeName.VOID) {
            String prefixParamName = "prefix";
            keyStatement = prefixParamName + "+" + keyStatement;
            ParameterSpec parameterSpec = ParameterSpec.builder(
                    prefixType, prefixParamName, Modifier.FINAL)
                    .build();
            prefixBuilder.parameterSpec(parameterSpec)
                    .need(true)
                    .paramName(prefixParamName)
                    .typeName(prefixType);
        }
        TypeName suffixType = MethodUtils.getSuffixType(element, elementUtils);
        PrefixSuffixParams.Builder suffixBuilder = PrefixSuffixParams.newBuilder();
        if (suffixType != null && suffixType != TypeName.VOID) {
            String suffixParamName = "suffix";
            keyStatement = keyStatement + "+" + suffixParamName;
            ParameterSpec parameterSpec = ParameterSpec.builder(
                    suffixType, suffixParamName, Modifier.FINAL)
                    .build();
            suffixBuilder.parameterSpec(parameterSpec)
                    .need(true)
                    .paramName(suffixParamName)
                    .typeName(suffixType);
        }

        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String fullClassStr = enclosingElement.getQualifiedName().toString();
        return builder
                .keyStatement(keyStatement)
                .prefixParams(prefixBuilder.build())
                .suffixParams(suffixBuilder.build())
                .element(element)
                .annotation(annotation)
                .fullClassName(ClassName.bestGuess(fullClassStr))
                .fullClassStr(fullClassStr)
                .filedName(element.getSimpleName().toString())
                .valueName("String");
    }

    public Builder builder() {
        Builder builder = new Builder();
        builder.mFiledName = getFiledName();
        builder.mValueName = getValueName();
        builder.mDefValue = getDefValue();
        builder.mFullClassName = getFullClassName();
        builder.mFullClassStr = getFullClassStr();
        builder.mTypeName = getTypeName();
        builder.mAnnotation = getAnnotation();
        builder.mKeyTypeName = getKeyTypeName();
        builder.mValTypeName = getValTypeName();
        builder.mGenericVal = isGenericVal();
        builder.mElement = getElement();
        builder.mDefValSrc = getDefValSrc();
        builder.mDefValFromId = getDefValFromId();
        builder.mDefString = getDefString();
        builder.mCodecId = getCodecId();
        builder.mGenerateRemove = isGenerateRemove();
        builder.mGenerateContains = isGenerateContains();
        builder.mKeyStatement = getKeyStatement();
        builder.mEmptyTypeName = getEmptyTypeName();
        builder.mSuffixParams = getSuffixParams();
        builder.mPrefixParams = getPrefixParams();
        return builder;
    }

    public static final class Builder {
        private String mFiledName;
        private String mValueName;
        private Object mDefValue;
        private ClassName mFullClassName;
        private String mFullClassStr;
        private TypeName mTypeName;
        private Annotation mAnnotation;
        private TypeName mKeyTypeName;
        private TypeName mValTypeName;
        private boolean mGenericVal;
        private VariableElement mElement;
        private DefValSrc mDefValSrc;
        private int mDefValFromId;
        private String mDefString;
        private int mCodecId;
        private boolean mGenerateRemove;
        private boolean mGenerateContains;
        private String mKeyStatement;
        private TypeName mEmptyTypeName;
        private PrefixSuffixParams mSuffixParams;
        private PrefixSuffixParams mPrefixParams;

        private Builder() {
        }

        public Builder filedName(String val) {
            mFiledName = val;
            return this;
        }

        public Builder valueName(String val) {
            mValueName = val;
            return this;
        }

        public Builder defValue(Object val) {
            mDefValue = val;
            return this;
        }

        public Builder fullClassName(ClassName val) {
            mFullClassName = val;
            return this;
        }

        public Builder fullClassStr(String val) {
            mFullClassStr = val;
            return this;
        }

        public Builder typeName(TypeName val) {
            mTypeName = val;
            return this;
        }

        public Builder annotation(Annotation val) {
            mAnnotation = val;
            return this;
        }

        public Builder keyTypeName(TypeName val) {
            mKeyTypeName = val;
            return this;
        }

        public Builder valTypeName(TypeName val) {
            mValTypeName = val;
            return this;
        }

        public Builder genericVal(boolean val) {
            mGenericVal = val;
            return this;
        }

        public Builder element(VariableElement val) {
            mElement = val;
            return this;
        }

        public Builder defValSrc(DefValSrc val) {
            mDefValSrc = val;
            return this;
        }

        public Builder defValFromId(int val) {
            mDefValFromId = val;
            return this;
        }

        public Builder defString(String val) {
            mDefString = val;
            return this;
        }

        public Builder codecId(int val) {
            mCodecId = val;
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

        public Builder keyStatement(String val) {
            mKeyStatement = val;
            return this;
        }

        public Builder emptyTypeName(TypeName val) {
            mEmptyTypeName = val;
            return this;
        }

        public Builder suffixParams(PrefixSuffixParams val) {
            mSuffixParams = val;
            return this;
        }

        public Builder prefixParams(PrefixSuffixParams val) {
            mPrefixParams = val;
            return this;
        }

        public KeyParams build() {
            return new KeyParams(this);
        }
    }
}
