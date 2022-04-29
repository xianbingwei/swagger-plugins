package com.wxb.plugin.core.gen;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author weixianbing
 * @create 2022/3/1 18:01
 */
public class MethodApiInfo {
    String url;
    String title;
    String businessDesc;
    String tags;
    String interfaceProvide = "小硕数科";
    String requestExample;
    String responseExample;
    List<EntityInfo> paramEntities;
    List<EntityInfo> returnEntities;

    static class EntityInfo {
        String entityClass;
        int serial = 1;
        String nameCn;
        String nameEn;
        List<FieldInfo> fieldInfos;
    }

    static class FieldInfo{
        int serial;
        String nameCn;
        String nameEn;
        String type;
        Boolean isNecessary;
        String information;
    }

}
