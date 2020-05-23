package mz.libcompiler.noused;

import static mz.libcompiler.noused.ProxyConstant.CLS_DOWNLOADER_TASK;
import static mz.libcompiler.noused.ProxyConstant.PKG_FILE_DOWNLOADER;
import static mz.libcompiler.noused.ProxyConstant.PROXY_SUFFIX_DOWNLOAD;

/**
 * @author mz
 * @date 2020/05/13/Wed
 * @time 17:02
 */
public enum TaskEnum {
    DOWNLOAD(PKG_FILE_DOWNLOADER, CLS_DOWNLOADER_TASK, PROXY_SUFFIX_DOWNLOAD);

    private String pkg;
    private String className;
    private String proxySuffix;

    /**
     * @param pkg         包名
     * @param className   任务完整类名
     * @param proxySuffix 事件代理后缀
     */
    TaskEnum(String pkg, String className, String proxySuffix) {
        this.pkg = pkg;
        this.className = className;
        this.proxySuffix = proxySuffix;
    }

    public String getPkg() {
        return pkg;
    }

    public String getClassName() {
        return className;
    }

    public String getProxySuffix() {
        return proxySuffix;
    }
}
