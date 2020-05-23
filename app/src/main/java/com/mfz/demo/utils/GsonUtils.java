package com.mfz.demo.utils;

import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import androidx.annotation.Nullable;
import ikidou.reflect.TypeBuilder;
import com.mfz.prefsbuilder.Deserializer;
import com.mfz.prefsbuilder.Serializer;

/**
 * @author mz
 */
public class GsonUtils {
    public static Gson createHaveExposeGson() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    @Deserializer.Object
    @Nullable
    public static <T> T toObject(String json, Class<T> classOfT) {
        try {
            return createHaveExposeGson().fromJson(json, classOfT);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T toObject(String json, Type typeOfT) {
        try {
            return createHaveExposeGson().fromJson(json, typeOfT);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Deserializer.List
    public static <T> List<T> toList(String json, Class<T> classOfT) {
        Type type = TypeBuilder.newInstance(List.class)
                .addTypeParam(classOfT)
                .build();
        return toObject(json, type);
    }

    @Deserializer.Set
    public static <T> Set<T> toSet(String json, Class<T> classOfT) {
        Type type = TypeBuilder.newInstance(Set.class)
                .addTypeParam(classOfT)
                .build();
        return toObject(json, type);
    }

    @Deserializer.Queue
    public static <T> Queue<T> toQueue(String json, Class<T> classOfT) {
        Type type = TypeBuilder.newInstance(Queue.class)
                .addTypeParam(classOfT)
                .build();
        return toObject(json, type);
    }

    @Deserializer.Deque
    public static <T> Deque<T> toDeque(String json, Class<T> classOfT) {
        Type type = TypeBuilder.newInstance(Deque.class)
                .addTypeParam(classOfT)
                .build();
        return toObject(json, type);
    }

    @Deserializer.SparseArray
    public static <T> SparseArray<T> toSparseArray(String json, Class<T> classOfT) {
        Type type = TypeBuilder.newInstance(SparseArray.class)
                .addTypeParam(classOfT)
                .build();
        return toObject(json, type);
    }

    @Deserializer.Map
    public static <K, V> Map<K, V> toMap(String json, Class<K> keyCls, Class<V> valCls) {
        Type type = TypeBuilder.newInstance(Map.class)
                .addTypeParam(keyCls)
                .addTypeParam(valCls)
                .build();
        return toObject(json, type);
    }

    @Serializer.Object
    @Nullable
    public static String toJson(Object src) {
        try {
            return createHaveExposeGson().toJson(src);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
