package mz.libcompiler.noused;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author mz
 * @date 2020/05/13/Wed
 * @time 17:02
 */
public class ProxyMethodParam {
    String packageName;
    String className;
    Set<TaskEnum> taskEnums;
    Map<String, Set<String>> keyMappings = new HashMap<>();
    Map<TaskEnum, Map<Class<? extends Annotation>, String>> methods = new HashMap<>();
}
