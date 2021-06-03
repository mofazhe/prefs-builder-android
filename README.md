# Prefs Builder
![LatestVersion](https://img.shields.io/badge/PrefsBuilder-0.1.2-green)

## 文档
暂无

## 简介
使用注解生成偏好代码。已开放接口，可以使用其他的偏好替代品实现持久化存储
可以自定义序列化方法，目前只测试了gson的兼容性，其他库暂未测试

## 使用

> 1.导入依赖
```gradle
    // 发布在 maven central
    def prefsBuilderVersion = '0.1.2'
    implementation "io.github.mofazhe:prefs-builder-core:${prefsBuilderVersion}"
    annotationProcessor "io.github.mofazhe:prefs-builder-annotation-processor:${prefsBuilderVersion}"

    // 一些设置(optional)
    // 具体参考demo
    // 指定统一后缀
    // arguments += ['prefsBuilderClassSuffix': 'Prefs']
```

> 2.使用@BasePrefsClass注解基础类
```java
    // 不强制要求实现BasePrefsInterface接口，但是该接口的方法必须实现
    // BasePrefsInterface就是为了方便快速实现必要方法
    // 虽然不是强制的要求，但是推荐使用
    // 仅限有一个类注解
    @BasePrefsClass
    public class BasePrefs implements BasePrefsInterface {
        // 内容省略，具体看demo
    }
```

> 3.使用注解序列化和反序列化方法(Optional)
```java
    // 示例只展示了序列化/反序列化Object，还可以自定义序列化/反序列化list等数据类型，具体参考demo

    @Serializer.Object
    public static String toJson(Object src) {
        return new Gson().toJson(src);
    }

    @Deserializer.Object
    public static <T> T toObject(String json, Class<T> classOfT) {
        return new Gson()..fromJson(json, classOfT);
    }
```

> 4.注解需要持久化的key
```java
    @PrefsClass()
    public static class User {
        @PrefsVal.Int()
        public static final String TEST_INT = "null";
    }
```

> 5.make/run项目

> 6.使用
```java
    // 读取
    UserPrefs.getTestInt();
    // 写入
    UserPrefs.setTestInt(234);
```

