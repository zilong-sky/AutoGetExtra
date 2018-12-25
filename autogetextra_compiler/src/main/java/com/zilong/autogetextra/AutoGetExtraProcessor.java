package com.zilong.autogetextra;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoGetExtraProcessor extends AbstractProcessor {


    private TypeName activityTpeName = ClassName.get("android.app", "Activity").withoutAnnotations();
    private TypeName intentTypeName = ClassName.get("android.content", "Intent").withoutAnnotations();
    private Messager messager;
    private Filer filer;
    private Elements elementUtils;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(AutoGetExtra.class.getCanonicalName());
        return set;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        elementUtils = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addParameter(activityTpeName, "activity");


        Map<String, List<AutoSetFieldParam>> map = findAllNeedAutoSetElements(set, roundEnvironment);


        boolean isFirst = true;
        for (Map.Entry<String, List<AutoSetFieldParam>> entry : map.entrySet()) {
            String fullClassName = entry.getKey();
            ClassName className = ClassUtil.getClassName(fullClassName);

            List<AutoSetFieldParam> list = entry.getValue();

            //创建 自动获取extra 给注解字段 设置值的 class
            TypeName autoSetTypeName = createAutoSetClassFile(className, list);


            //创建 bind class，供客户调用
            if (isFirst) {
                methodBuilder.addCode("if (activity instanceof $T) {\n", className);
                isFirst = false;
            } else {
                methodBuilder.addCode("else if (activity instanceof $T) {\n", className);
            }
            methodBuilder.addCode("\t$T binder = new $T();\n", autoSetTypeName, autoSetTypeName);
            methodBuilder.addCode("\tbinder.bind(($T)activity);\n", className);
            methodBuilder.addCode("}\n");
        }

        createJavaFile("com.zilong.autogetextra", "InjectAutoGetExtra", methodBuilder.build());
        return false;
    }

    //创建获取intent extra 自动设置给 被注解字段 类 ...$Binder,里边只有一个bind静态方法
    private TypeName createAutoSetClassFile(ClassName className, List<AutoSetFieldParam> params) {
        //在相同目录下，新建一个  ...$Binder 类，用来给注解的字段，设置值
        TypeName autoSetTypeName = ClassName.get(className.packageName(), className.simpleName() + "$Binder");

        //给新建的 ...$Binder类，添加方法
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addParameter(className, "activity");
        //addStatement  可以自动导包  addCode 不能
        methodBuilder.addStatement("$T intent = activity.getIntent()", intentTypeName);
        for (AutoSetFieldParam param : params) {
            TypeName fieldTypeName = ClassUtil.getTypeName(param.fieldType);
            methodBuilder.addCode("if (intent.hasExtra($S)){\n", param.fieldKeyName);
            methodBuilder.addCode("activity.$N = ($T)intent.getSerializableExtra($S);\n", param.fieldName, fieldTypeName, param.fieldKeyName);
            methodBuilder.addCode("}\n");
        }

        createJavaFile(className.packageName(), ((ClassName) autoSetTypeName).simpleName(), methodBuilder.build());

        //返回新建类的  TypeName(继承自 ClassName，包含了类信息)
        return autoSetTypeName;
    }


    //找到所有被Auto注解的字段，并存下来相关信息
    private Map<String, List<AutoSetFieldParam>> findAllNeedAutoSetElements(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Map<String, List<AutoSetFieldParam>> map = new LinkedHashMap<>();

        //获取被 AutoGetExtra 注解的 元素
        //遍历所有的被注解元素，（这些元素可能不再一个类里哦，所以里边有区分）
        for (Element element : roundEnvironment.getElementsAnnotatedWith(AutoGetExtra.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, " AutoGetExtra 只能修饰 FIELD ！", element);
                continue;
            }

            //获取被修饰字段 的 上一层 元素 （在这里就是 class ）
            String fullClassName = ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();

            //判断map中是否已经存在该class 的 被注解字段 集合，没有创建新的
            List<AutoSetFieldParam> autoSetFieldParamList;
            if (map.containsKey(fullClassName)) {
                autoSetFieldParamList = map.get(fullClassName);
            } else {
                autoSetFieldParamList = new ArrayList<>();
                map.put(fullClassName, autoSetFieldParamList);
            }

            //获取被注解元素 名称，类型，intent 的extra  keyName
            String fieldName = element.getSimpleName().toString();
            String fieldType = element.asType().toString();
            String fieldKeyName = element.getAnnotation(AutoGetExtra.class).value();

            AutoSetFieldParam param = new AutoSetFieldParam();
            param.fieldName = fieldName;
            param.fieldType = fieldType;
            param.fieldKeyName = fieldKeyName;
            //存储
            autoSetFieldParamList.add(param);
        }

        return map;

    }


    private void createJavaFile(String pkg, String classShortName, MethodSpec... methods) {
        try {
            TypeSpec.Builder builder = TypeSpec.classBuilder(classShortName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            for (MethodSpec methodSpec : methods) {
                builder.addMethod(methodSpec);
            }

            TypeSpec classType = builder.build();

            JavaFile javaFile = JavaFile.builder(pkg, classType)
                    .addFileComment("This codes are generated automatically. Do not modify!")
                    .indent("   ")
                    .build();
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //被注解 字段 信息
    class AutoSetFieldParam {
        public String fieldName;
        public String fieldType;
        public String fieldKeyName;

        @Override
        public String toString() {
            return "AutoSetFieldParam{" +
                    "fieldName='" + fieldName + '\'' +
                    ", fieldType='" + fieldType + '\'' +
                    ", fieldKeyName='" + fieldKeyName + '\'' +
                    '}';
        }
    }


}
