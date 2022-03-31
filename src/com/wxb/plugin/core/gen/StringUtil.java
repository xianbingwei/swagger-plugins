package com.wxb.plugin.core.gen;

/**
 * <p>
 *
 * </p>
 *
 * @author weixianbing
 * @create 2022/3/2 17:20
 */
public class StringUtil {

    public static boolean isBlank(String str) {
        return str == null || "".equals(str);
    }
    public static boolean isNotBlank(String str){
        return !isBlank(str);
    }
}
