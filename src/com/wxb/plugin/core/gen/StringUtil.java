package com.wxb.plugin.core.gen;

import java.util.HashMap;
import java.util.Map;

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

    public  static Map<String, String> pageInfo = new HashMap<>();
    static {
        pageInfo.put("records","数据");
        pageInfo.put("total","总条数");
        pageInfo.put("size","分页大小");
        pageInfo.put("current","当前页");
        pageInfo.put("orders","排序");
        pageInfo.put("optimizeCountSql","是否优化统计sql");
        pageInfo.put("isSearchCount","是否搜索统计");
    }
}
