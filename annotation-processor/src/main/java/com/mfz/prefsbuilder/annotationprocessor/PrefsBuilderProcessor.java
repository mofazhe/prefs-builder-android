package com.mfz.prefsbuilder.annotationprocessor;

import com.google.auto.service.AutoService;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.mfz.prefsbuilder.BasePrefsClass;
import com.mfz.prefsbuilder.DefaultValue;
import com.mfz.prefsbuilder.PrefParams;
import com.mfz.prefsbuilder.PrefsClass;
import com.mfz.prefsbuilder.StringCodec;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

/**
 * @author mz
 */
@AutoService(Processor.class)
@SupportedOptions(value = {
        Const.OptionArg.PKG,
        Const.OptionArg.CLASS_PREFIX,
        Const.OptionArg.CLASS_SUFFIX,
})
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
public class PrefsBuilderProcessor extends AbstractProcessor {

    private ElementHandler mHandler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        PrintLog.init(processingEnv.getMessager());
        mHandler = new ElementHandler(processingEnv.getFiler(),
                processingEnv.getElementUtils(),
                processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(DefaultValue.class.getCanonicalName());
        annotations.add(PrefsClass.class.getCanonicalName());
        annotations.add(BasePrefsClass.class.getCanonicalName());
        annotations.add(StringCodec.Decode.class.getCanonicalName());
        annotations.add(StringCodec.Encode.class.getCanonicalName());
        for (Class<? extends Annotation> c : AnnotationList.getPrefsKeyList()) {
            annotations.add(c.getCanonicalName());
        }
        for (Class<? extends Annotation> c : AnnotationList.getRuleMethodList()) {
            annotations.add(c.getCanonicalName());
        }
        annotations.add(PrefParams.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mHandler.clean();
        mHandler.handleBasePrefs(roundEnvironment);
        mHandler.handlePrefsClass(roundEnvironment);
        mHandler.handleCodecMethod(roundEnvironment);
        mHandler.handleDefaultVal(roundEnvironment);
        mHandler.handleRuleMethod(roundEnvironment);
        for (Class<? extends Annotation> c : AnnotationList.getPrefsKeyList()) {
            mHandler.handlePrefsVal(roundEnvironment, c);
        }
        mHandler.createJavaFiles();
        return true;
    }
}
