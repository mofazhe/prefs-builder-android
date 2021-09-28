package com.mfz.prefsbuilder.annotationprocessor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mfz.prefsbuilder.Deserializer;
import com.mfz.prefsbuilder.PrefsKey;
import com.mfz.prefsbuilder.Serializer;

/**
 * @author mz
 * @date 2020/05/14/Thu
 * @time 15:20
 */
public class AnnotationList {
    public static List<Class<? extends Annotation>> getPrefsKeyList() {
        List<Class<? extends Annotation>> set = new ArrayList<>(30);
        set.add(PrefsKey.Int.class);
        set.add(PrefsKey.Float.class);
        set.add(PrefsKey.Bool.class);
        set.add(PrefsKey.Byte.class);
        set.add(PrefsKey.Double.class);
        set.add(PrefsKey.Char.class);
        set.add(PrefsKey.Long.class);
        set.add(PrefsKey.Short.class);
        set.add(PrefsKey.String.class);
        set.add(PrefsKey.Object.class);
        set.add(PrefsKey.List.class);
        set.add(PrefsKey.Set.class);
        set.add(PrefsKey.Queue.class);
        set.add(PrefsKey.Deque.class);
        // set.add(PrefsKey.SparseArray.class);
        set.add(PrefsKey.Map.class);
        return set;
    }

    public static Map<Class<? extends Annotation>, Class<?>> getDefEmptyMap() {
        Map<Class<? extends Annotation>, Class<?>> set = new HashMap<>(10);
        set.put(PrefsKey.List.class, ArrayList.class);
        set.put(PrefsKey.Set.class, HashSet.class);
        set.put(PrefsKey.Queue.class, LinkedList.class);
        set.put(PrefsKey.Deque.class, LinkedList.class);
        set.put(PrefsKey.Map.class, HashMap.class);
        return set;
    }

    public static List<Class<? extends Annotation>> getRuleMethodList() {
        List<Class<? extends Annotation>> map = new ArrayList<>(30);
        map.add(Serializer.Object.class);
        map.add(Serializer.List.class);
        map.add(Serializer.Set.class);
        map.add(Serializer.Queue.class);
        map.add(Serializer.Deque.class);
        // map.add(Serializer.SparseArray.class);
        map.add(Serializer.Map.class);

        map.add(Deserializer.Object.class);
        map.add(Deserializer.List.class);
        map.add(Deserializer.Set.class);
        map.add(Deserializer.Queue.class);
        map.add(Deserializer.Deque.class);
        // map.add(Deserializer.SparseArray.class);
        map.add(Deserializer.Map.class);
        return map;
    }

    public static List<Class<? extends Annotation>> getDeserializerList(Class<? extends Annotation> cls) {
        List<Class<? extends Annotation>> list = new ArrayList<>(5);
        if (cls == PrefsKey.List.class) {
            list.add(Deserializer.List.class);
        } else if (cls == PrefsKey.Set.class) {
            list.add(Deserializer.Set.class);
        } else if (cls == PrefsKey.Queue.class) {
            list.add(Deserializer.Queue.class);
        } else if (cls == PrefsKey.Deque.class) {
            list.add(Deserializer.Deque.class);
            // } else if (cls == PrefsKey.SparseArray.class) {
            //     list.add(Deserializer.SparseArray.class);
        } else if (cls == PrefsKey.Map.class) {
            list.add(Deserializer.Map.class);
        }
        list.add(Deserializer.Object.class);
        return list;
    }

    public static List<Class<? extends Annotation>> getSerializerList(Class<? extends Annotation> cls) {
        List<Class<? extends Annotation>> list = new ArrayList<>(5);
        if (cls == PrefsKey.List.class) {
            list.add(Serializer.List.class);
        } else if (cls == PrefsKey.Set.class) {
            list.add(Serializer.Set.class);
        } else if (cls == PrefsKey.Queue.class) {
            list.add(Serializer.Queue.class);
        } else if (cls == PrefsKey.Deque.class) {
            list.add(Serializer.Deque.class);
            // } else if (cls == PrefsKey.SparseArray.class) {
            //     list.add(Serializer.SparseArray.class);
        } else if (cls == PrefsKey.Map.class) {
            list.add(Serializer.Map.class);
        }
        list.add(Serializer.Object.class);
        return list;
    }
}
