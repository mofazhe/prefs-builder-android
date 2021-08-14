package com.mfz.prefsbuilder.annotationprocessor.data;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;

import javax.lang.model.element.VariableElement;

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
    private ClassName mCurrentClass;
    private TypeName mTypeName;
    private AnnotationParams mAnnotationParams;
    private Annotation mAnnotation;
    private TypeName mKeyTypeName;
    private TypeName mValTypeName;
    private boolean mGenericVal;
    private VariableElement mElement;

    private KeyParams(Builder builder) {
        setFiledName(builder.mFiledName);
        setValueName(builder.mValueName);
        setDefValue(builder.mDefValue);
        setCurrentClass(builder.mCurrentClass);
        setTypeName(builder.mTypeName);
        setAnnotationParams(builder.mAnnotationParams);
        setAnnotation(builder.mAnnotation);
        setKeyTypeName(builder.mKeyTypeName);
        setValTypeName(builder.mValTypeName);
        setGenericVal(builder.mGenericVal);
        setElement(builder.mElement);
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

    public ClassName getCurrentClass() {
        return mCurrentClass;
    }

    public void setCurrentClass(ClassName currentClass) {
        mCurrentClass = currentClass;
    }

    public TypeName getTypeName() {
        return mTypeName;
    }

    public void setTypeName(TypeName typeName) {
        mTypeName = typeName;
    }

    public AnnotationParams getAnnotationParams() {
        return mAnnotationParams;
    }

    public void setAnnotationParams(AnnotationParams annotationParams) {
        mAnnotationParams = annotationParams;
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

    public Builder builder() {
        Builder builder = new Builder();
        builder.mFiledName = getFiledName();
        builder.mValueName = getValueName();
        builder.mDefValue = getDefValue();
        builder.mCurrentClass = getCurrentClass();
        builder.mTypeName = getTypeName();
        builder.mAnnotationParams = getAnnotationParams();
        builder.mAnnotation = getAnnotation();
        builder.mKeyTypeName = getKeyTypeName();
        builder.mValTypeName = getValTypeName();
        builder.mGenericVal = isGenericVal();
        builder.mElement = getElement();
        return builder;
    }

    public static final class Builder {
        private String mFiledName;
        private String mValueName;
        private Object mDefValue;
        private ClassName mCurrentClass;
        private TypeName mTypeName;
        private AnnotationParams mAnnotationParams;
        private Annotation mAnnotation;
        private TypeName mKeyTypeName;
        private TypeName mValTypeName;
        private boolean mGenericVal;
        private VariableElement mElement;

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

        public Builder currentClass(ClassName val) {
            mCurrentClass = val;
            return this;
        }

        public Builder typeName(TypeName val) {
            mTypeName = val;
            return this;
        }

        public Builder annotationParams(AnnotationParams val) {
            mAnnotationParams = val;
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

        public KeyParams build() {
            return new KeyParams(this);
        }
    }
}
