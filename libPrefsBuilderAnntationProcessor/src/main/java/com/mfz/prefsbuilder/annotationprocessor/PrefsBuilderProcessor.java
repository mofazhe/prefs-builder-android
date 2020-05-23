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
import com.mfz.prefsbuilder.PrefsClass;
import com.mfz.prefsbuilder.StringDecode;
import com.mfz.prefsbuilder.StringEncode;

@AutoService(Processor.class)
@SupportedOptions(value = {
        Const.OptionArg.PKG,
        Const.OptionArg.CLASS_PREFIX,
        Const.OptionArg.CLASS_SUFFIX,
})
public class PrefsBuilderProcessor extends AbstractProcessor {

    private ElementHandler mHandler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        PrintLog.init(processingEnvironment.getMessager());
        mHandler = new ElementHandler(processingEnvironment.getFiler(),
                processingEnvironment.getElementUtils());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(DefaultValue.class.getCanonicalName());
        annotations.add(PrefsClass.class.getCanonicalName());
        annotations.add(BasePrefsClass.class.getCanonicalName());
        annotations.add(StringDecode.class.getCanonicalName());
        annotations.add(StringEncode.class.getCanonicalName());
        for (Class<? extends Annotation> c : AnnotationList.getPrefsVal()) {
            annotations.add(c.getCanonicalName());
        }
        for (Class<? extends Annotation> c : AnnotationList.getRuleMethod().keySet()) {
            annotations.add(c.getCanonicalName());
        }
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
        mHandler.handlePrefsClass(processingEnv, roundEnvironment);
        mHandler.handleCodecMethod(roundEnvironment);
        mHandler.handleDefObjectVal(roundEnvironment);
        mHandler.handleRuleMethod(roundEnvironment);
        for (Class<? extends Annotation> c : AnnotationList.getPrefsVal()) {
            mHandler.handlePrefsVal(roundEnvironment, c);
        }
        mHandler.createJavaFiles();
        return true;
    }

    // private void createFile(TypeElement enclosingElement, String bindViewFiledClassType,
    //                         String filedName, String filedValue, Class type) {
    //     // String pkName = mElementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();
    //     try {
    //         String classNameString = "SettingsPrefs";
    //         String warningTxt = "Auto generated by apt,do not modify!!\n";
    //         String pkgName = Const.NEW_PREFS_PKG;
    //         ClassName className = ClassName.get(pkgName, classNameString);
    //         String prefsName = "settings_prefs";
    //
    //         TypeSpec.Builder builder = TypeSpec.classBuilder(classNameString)
    //                 .addModifiers(Modifier.PUBLIC)
    //                 .superclass(ClassName.get(Const.BASE_PREFS_PKG, Const.BASE_PREF_NAME));
    //
    //         // 警告
    //         builder.addJavadoc(warningTxt);
    //
    //         // 内部单例类
    //         builder.addType(TypeSpec.classBuilder("SingletonHolder")
    //                 .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
    //                 .addField(FieldSpec.builder(className, "INSTANCE")
    //                         .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
    //                         .initializer("new $T()", className)
    //                         .build())
    //                 .build());
    //
    //         // 构造方法
    //         builder.addMethod(MethodSpec.constructorBuilder()
    //                 .addModifiers(Modifier.PRIVATE)
    //                 .addCode(String.format(Locale.getDefault(), "super(\"%s\");\n", prefsName))
    //                 .build());
    //
    //         // 获取实例方法
    //         builder.addMethod(MethodSpec.methodBuilder("getInstance")
    //                 .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    //                 .addCode("return SingletonHolder.INSTANCE;\n")
    //                 .returns(className)
    //                 .build());
    //
    //         // get方法
    //         builder.addMethod(MethodSpec.methodBuilder(getGetMethodName(filedName))
    //                 .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    //                 .returns(TypeName.INT)
    //                 .addCode(String.format(Locale.getDefault(),
    //                         "return getInstance().getInt(\"%s\", 0);\n", filedValue))
    //                 .build());
    //
    //         // set方法
    //         builder.addMethod(MethodSpec.methodBuilder(getSetMethodName(filedName))
    //                 .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    //                 .addParameter(TypeName.INT, "value", Modifier.FINAL)
    //                 .returns(TypeName.VOID)
    //                 .addCode(String.format(Locale.getDefault(),
    //                         "getInstance().setInt(\"%s\", value);\n", filedValue))
    //                 .build());
    //
    //         // String info = String.format(Locale.getDefault(),
    //         //         "%s %s = %d", bindViewFiledClassType, bindViewFiledName, id);
    //         // CodeBlock.Builder cb = CodeBlock.builder();
    //         // cb.add("$T<String> set = null;\n", ClassName.get(Set.class));
    //         // ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.INT, "value")
    //         //         .addModifiers(Modifier.FINAL)
    //         //         .build();
    //         // MethodSpec listener = MethodSpec.methodBuilder("setInt")
    //         //         .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    //         //         .returns(void.class)
    //         //         .addParameter(parameterSpec)
    //         //         // .addCode("System.out.println(\"hhh\");\n")
    //         //         // .addCode("getInstance().setInt(value);")
    //         //         .addCode(cb.build())
    //         //         .build();
    //         // builder.addMethod(listener);
    //         //
    //         // listener = MethodSpec.methodBuilder("getInt")
    //         //         .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    //         //         .returns(TypeName.INT)
    //         //         .addParameter(parameterSpec)
    //         //         .addCode("return 1;\n")
    //         //         .build();
    //         // builder.addMethod(listener);
    //
    //         JavaFile javaFile = JavaFile.builder(pkgName, builder.build()).build();
    //         javaFile.writeTo(mFiler);
    //     } catch (Throwable e) {
    //         PrintLog.info(e.getMessage());
    //     }
    // }
    //
    // private static String getGetMethodName(String filedName) {
    //     return "get" + filedNameToMethodName(filedName);
    // }
    //
    // private static String getSetMethodName(String filedName) {
    //     return "set" + filedNameToMethodName(filedName);
    // }
    //
    // private static String filedNameToMethodName(String filedName) {
    //     String lowerCase = filedName.toLowerCase();
    //     int topUpperCaseStep = 'A' - 'a';
    //     char[] charArray = lowerCase.toCharArray();
    //     if (charArray.length <= 0) {
    //         return filedName;
    //     }
    //     charArray[0] += topUpperCaseStep;
    //     for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
    //         char c = charArray[i];
    //         if (c == '_') {
    //             int nextI = i + 1;
    //             if (nextI < charArrayLength && charArray[nextI] != '_') {
    //                 charArray[nextI] += topUpperCaseStep;
    //             }
    //         }
    //     }
    //     return new String(charArray).replace("_", "");
    // }
}
