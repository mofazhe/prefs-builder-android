package com.mfz.demo.utils;

import android.util.SparseArray;

import com.mfz.prefsbuilder.DefaultValue;
import com.mfz.prefsbuilder.PrefsDefVal;
import com.mfz.prefsbuilder.PrefsGenerateCtrl;
import com.mfz.prefsbuilder.PrefsKeyHeadTail;
import com.mfz.prefsbuilder.PrefsParams;
import com.mfz.prefsbuilder.PrefsClass;
import com.mfz.prefsbuilder.PrefsKey;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author mz
 * @date 2020/05/14/Thu
 * @time 10:17
 */
@PrefsClass(className = "Settings", currentClassName = false)
public class PrefsConst {
    @PrefsClass()
    public static class User {
        @PrefsKey.Int()
        public static final String TEST_INT = "null";

        @PrefsKey.String()
        @PrefsDefVal(defNull = true)
        public static final String TEST_STRING = "b";

        @PrefsKey.String(defVal = "111")
        @PrefsDefVal(defNull = false)
        @PrefsGenerateCtrl()
        public static final String TEST_STRING_NOT = "b";

        @PrefsKey.Byte()
        @PrefsGenerateCtrl(generateContains = false)
        public static final String TEST_BYTE = "c";
    }

    @PrefsClass(pkgName = "com.mfz.test", className = "AccountPrefs")
    public static class Account {
        @PrefsKey.Int()
        public static final String TEST_INT = "null";

        @PrefsKey.String()
        @PrefsDefVal(defNull = true)
        public static final String TEST_STRING = "b";

        @PrefsKey.String(defVal = "111")
        @PrefsDefVal(defNull = false)
        @PrefsGenerateCtrl()
        public static final String TEST_STRING_NOT = "b";

        @PrefsKey.Byte()
        @PrefsGenerateCtrl(generateContains = false)
        public static final String TEST_BYTE = "c";
    }

    @PrefsKey.Int(defVal = 122)
    @PrefsDefVal(defValFromId = 123)
    @PrefsGenerateCtrl(generateRemove = false)
    public static final String TEST_INT = "a";

    @PrefsKey.Float(defVal = 1.233300f)
    @PrefsDefVal(defValFromId = 133)
    public static final String TEST_FLOAT = "float";

    @PrefsKey.Bool(defVal = true)
    @PrefsDefVal(defValFromId = 144)
    @PrefsGenerateCtrl(generateRemove = false, generateContains = false)
    public static final String TEST_BOOL = "a";

    @PrefsKey.Bool(defVal = true)
    @PrefsParams()
    public static final String IS_TEST_IS_BOOL = "a";

    @PrefsKey.Byte(defVal = 12)
    @PrefsDefVal(defValFromId = 155)
    public static final String TEST_BYTE = "a";

    @PrefsKey.Double(defVal = 102.2435235)
    @PrefsDefVal(defValFromId = 166)
    public static final String TEST_DOUBLE = "a";

    @PrefsKey.Char(defVal = 's')
    @PrefsDefVal(defValFromId = 177)
    @PrefsKeyHeadTail(prefixType = byte.class)
    public static final String TEST_CHAR = "a";

    @PrefsKey.Short(defVal = 234)
    @PrefsDefVal(defValFromId = 188)
    @PrefsParams(codecId = 1)
    public static final String TEST_SHORT = "a";

    @PrefsKey.String()
    @PrefsDefVal(defValFromId = 111)
    @PrefsKeyHeadTail(prefixType = float.class, suffixType = double.class)
    public static final String TEST_STRING = "b";

    @PrefsKey.String(defVal = "[\"foo\", {\"bar\": [\"baz\", null, 1.0, 2]}]")
    @PrefsDefVal()
    public static final String TEST_STRING_JSON = "b";

    @PrefsKey.String()
    @PrefsDefVal(defNull = true)
    @PrefsParams(codecId = 1)
    @PrefsKeyHeadTail(prefixType = String.class, suffixType = int.class)
    public static final String TEST_STRING_BASE64 = "b";

    @PrefsKey.Object(type = Test.class)
    @PrefsDefVal(defValFromId = 1, defNull = true)
    public static final String TEST_OBJECT = "c";

    @PrefsKey.List(type = Test.class)
    @PrefsDefVal(defEmpty = false, defValFromId = 2, defString = "[]")
    public static final String TEST_LIST = "d";

    @PrefsKey.List(type = Test.class)
    @PrefsDefVal()
    public static final String TEST_LIST_EMPTY = "d";

    @PrefsKey.Set(type = Test.class)
    @PrefsDefVal(defEmpty = false, defValFromId = 3)
    public static final String TEST_SET = "e";

    @PrefsKey.Queue(type = String.class)
    @PrefsDefVal(defEmpty = false, defValFromId = 4)
    @PrefsKeyHeadTail(suffixType = String.class)
    public static final String TEST_QUEUE = "f";

    @PrefsKey.Deque(type = Test.class)
    @PrefsDefVal(defEmpty = false, defValFromId = 5)
    @PrefsKeyHeadTail(prefixType = int.class)
    public static final String TEST_DEQUE = "g";

    // @PrefsKey.SparseArray(type = String.class)
    // @PrefsDefVal(defEmpty = false, defValFromId = 7)
    // @PrefsKeyHeadTail(prefixType = int.class)
    // public static final String TEST_SPARSE_ARRAY = "g";

    @PrefsKey.Map(keyType = String.class, valType = Test.class)
    @PrefsDefVal(defEmpty = false, defValFromId = 6)
    public static final String TEST_MAP = "h";

    @DefaultValue(id = 123)
    public static int iii = 22;

    @DefaultValue(id = 7)
    public static final SparseArray<String> sSparseArray = new SparseArray<>();

    @DefaultValue(id = 111)
    public static String sss = "new Test()";
    public static Test sTest = new Test();
    @DefaultValue(id = 3)
    public static Set<Test> sList = Collections.emptySet();
    @DefaultValue(id = 4)
    public static final Queue<String> MY_QUEUE = new ArrayBlockingQueue<>(1);
    @DefaultValue(id = 5)
    public static final Deque<Test> MyQueue = new LinkedList<>();
    @DefaultValue(id = 6)
    public static final Map<String, Test> MyMap = new HashMap<>();

    @DefaultValue(id = 1)
    public static Test getTest() {
        return new Test();
    }

    @DefaultValue(id = 2)
    public static List<Test> getList() {
        return Collections.emptyList();
    }

    public static String getNewString() {
        return "";
    }

    public static class Test {

    }
}
