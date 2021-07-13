# Prefs Builder
![LatestVersion](https://maven-badges.herokuapp.com/maven-central/io.github.mofazhe/prefs-builder-core/badge.png)


## 内容列表

- [简介](#简介)
- [使用说明](#使用说明)
- [示例](#示例)
- [文档](#文档)
- [致谢](#致谢)
- [使用许可](#使用许可)


## 简介
使用注解生成偏好代码。已开放接口，可以使用其他的偏好替代品实现持久化存储
<br/>
可以自定义序列化方法，目前只测试了gson的兼容性，其他库暂未测试


## 使用说明

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


## 示例

[PrefsBuilder](https://github.com/mofazhe/prefs-builder-android)


## 文档

暂无


## 致谢

本项目参考以下项目

[Glide](https://github.com/bumptech/glide)
<br/>
[Butter Knife](https://github.com/JakeWharton/butterknife)
<br/>
[EventBus](https://github.com/greenrobot/EventBus)
<br/>
[Standard Readme](https://github.com/RichardLitt/standard-readme)


## 使用许可

该项目签署了Apache License 2.0 授权许可，详情请参阅[LICENSE](https://github.com/mofazhe/prefs-builder-android/blob/master/LICENSE)


