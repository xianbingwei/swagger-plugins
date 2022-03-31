package com.wxb.plugin.core.gen;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.wxb.plugin.core.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 *
 * </p>
 *
 * @author weixianbing
 * @create 2022/3/1 17:15
 */
public class InterfaceApiGen2 {
    // 模板docx地址
    static String template = "interface.docx";
    // 扫描文件夹
//    static String scan = "src/main/java/com/xiaoshuotech/glp/risk/api/controller";
    // 新文档地址
    static String out = "D:\\data\\";
    // 新文档名称前缀
    static String docxPrefix = "swagger-api-";
    // docx模板
    static Template templateStyle = null;
    // 子目录标题
    static String[] subHeader = {"业务描述", "接口提供方", "接口地址",
            "接口请求字段", "接口响应字段", "请求示例报文", "响应示例报文"};
    //表头
    static String[] tabHeader = {"序号", "中文名称", "英文名称", "类型", "必填", "说明"};

    //排除字段
    static Set<String> excludeFields = new HashSet<>();
    //排除类
    static Set<String> excludeClass = new HashSet<>();


    static {
        excludeFields.addAll(Arrays.asList("signature", "version"));
//        excludeClass.addAll(Arrays.asList());
    }

    public static void scanFile(File file, List<File> files) {
        if (file.isDirectory()) {
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                if(file.getName().equals("test")){
                    continue;
                }
                scanFile(listFile, files);
            }
        } else if (file.getName().endsWith(".java")){
            System.out.println(file.getName());
            files.add(file);
        }
    }

    public static void main(String[] args) {

        System.out.println("this is main");
        System.out.println("scan: "+args[0]);
        if (args.length > 1 && StringUtil.isNotBlank(args[1])) {
            System.out.println("out: "+args[1]);
            out = args[1];
        }
        genFile(args[0]);
    }

    public static void run(String scan, String outPath) {
        System.out.println("this is run");
        System.out.println("scan: "+scan);
        System.out.println("out: "+outPath);
        if (StringUtil.isNotBlank(outPath)) {
            out = outPath;
        }
        genFile(scan);

    }

    public static void genFile(String scan) {
        List<File> files = new ArrayList<>();
        scanFile(new File(scan), files);
        List<MethodApiInfo> methods = new ArrayList<>();
        for (File file : files) {
            String replace = FileUtils.readPackage(file);
            System.out.println(replace);
            Class<?> aClass;
            try {
                aClass = Thread.currentThread().getContextClassLoader().loadClass(replace);
            } catch (ClassNotFoundException e) {
                System.out.println("NOT FOUND: " + replace);
                continue;
            }
            RequestMapping annotation = aClass.getAnnotation(RequestMapping.class);
            if (aClass.getAnnotation(RestController.class) == null) {
                System.out.println(file.getPath());
                continue;
            }
            String note = getNote(aClass.getAnnotation(Api.class));
            String prefixPath = getPath(annotation);
            System.out.println(prefixPath);
            for (Method method : aClass.getDeclaredMethods()) {
                String mapping = getMapping(method);
                if (mapping == null) {
                    continue;
                }
                methods.add(getInfo(method, prefixPath, note));
            }
        }
        toFile(methods);
    }

    /**
     * 解析方法参数类型，返回参数类型
     */
    public static MethodApiInfo getInfo(Method method, String prefixPath, String notePref) {

        MethodApiInfo methodApiInfo = new MethodApiInfo();
        String path = getMapping(method);
        methodApiInfo.url = prefixPath + path;
        System.out.println("url: " + methodApiInfo.url);
        methodApiInfo.businessDesc = getNote(method.getAnnotation(ApiOperation.class));
        methodApiInfo.title = notePref + "-" + methodApiInfo.businessDesc;
        methodApiInfo.paramEntities = new ArrayList<>();
        JSONObject req = new JSONObject();
        List<Class<?>> sub = new ArrayList<>();
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (parameterType.getAnnotation(ApiModel.class) == null) {
                continue;
            }
            req = JSON.parseObject(formatContent(parameterType));
            getEntityInfo(parameterType, methodApiInfo.paramEntities, req);
//            methodApiInfo.paramEntities.add(getEntityInfo(parameterType, sub, req));
            methodApiInfo.requestExample = formatContent(parameterType);
        }
//        for (Class<?> parameterType : sub) {
//            if (parameterType.getAnnotation(ApiModel.class) == null) {
//                continue;
//            }
//            methodApiInfo.paramEntities.add(getEntityInfo(parameterType, sub, req));
//        }
        JSONObject res = JSON.parseObject(formatContent(method.getReturnType()));
        methodApiInfo.returnEntities = getReturnInfo(method, res);

        methodApiInfo.requestExample = formatContent(req);
        methodApiInfo.responseExample = formatContent(res);
        return methodApiInfo;
    }

    public static List<MethodApiInfo.EntityInfo> getReturnInfo(Method method, JSONObject res) {
        List<MethodApiInfo.EntityInfo> returnEntities = new ArrayList<>();
        List<Class<?>> sub = new ArrayList<>();
        if (method.getReturnType().getAnnotation(ApiModel.class) == null) {
            return null;
        }
        Type genericReturnType = method.getGenericReturnType();

        Class<?> superclass = method.getReturnType().getSuperclass();
        Type genericSuperclass = superclass.getGenericSuperclass();
        getEntityInfo(method.getReturnType(), returnEntities, res,
                !BaseDataResponse.class.isAssignableFrom(superclass) ? genericReturnType : genericSuperclass);
//        returnEntities.add(getEntityInfo(method.getReturnType(), sub, res, genericSuperclass == null ? genericReturnType : genericSuperclass));

//        for (int i = 0; i < sub.size(); i++) {
//            Class<?> parameterType = sub.get(i);
//            if (parameterType.getAnnotation(ApiModel.class) == null) {
//                continue;
//            }
//            returnEntities.add(getEntityInfo(parameterType, sub, res));
//        }
        return returnEntities;
    }

    public static String formatContent(Class<?> c) {
        try {
            return formatContent(c.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            System.out.println(e);
        }
        return "{}";
    }

    public static String formatContent(Object o) {
        return JSON.toJSONString(o,
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteBigDecimalAsPlain,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteDateUseDateFormat
        );
    }

    public static void getEntityInfo(Class<?> clazz, List<MethodApiInfo.EntityInfo> sub, JSONObject json) {
        getEntityInfo(clazz, sub, json, null);
    }

    public static void getEntityInfo(Class<?> clazz, List<MethodApiInfo.EntityInfo> sub, JSONObject json, Type genericReturnType) {

        if (clazz.getAnnotation(ApiModel.class) == null) {
            return;
        }
        MethodApiInfo.EntityInfo entityInfo = new MethodApiInfo.EntityInfo();
        ApiModel annotation = clazz.getAnnotation(ApiModel.class);
        entityInfo.nameEn = clazz.getSimpleName();
        entityInfo.nameCn = getEntityDesc(annotation);
        entityInfo.fieldInfos = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        getAllFields(fields, clazz);
        for (Field field : fields) {
            if (field.getAnnotation(ApiModelProperty.class) == null
                    || field.getAnnotation(ApiModelProperty.class).hidden()
                    || excludeFields.contains(field.getName())) {
                continue;
            }
            entityInfo.fieldInfos.add(getFieldInfo(entityInfo, field, sub, json, genericReturnType));
        }
        sub.add(entityInfo);
    }

    public static String getEntityDesc(ApiModel annotation) {
        String nameCn = StringUtil.isBlank(annotation.value()) ? annotation.description() : annotation.value();
        return nameCn.replaceAll("DTO", "")
                .replaceAll("VO", "");
    }

    public static void getAllFields(List<Field> fields, Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        getAllFields(fields, clazz.getSuperclass());
    }

    public static MethodApiInfo.FieldInfo getFieldInfo(MethodApiInfo.EntityInfo entityInfo, Field field,
                                                       List<MethodApiInfo.EntityInfo> sub, JSONObject json, Type genericReturnType) {
        MethodApiInfo.FieldInfo fieldInfo = new MethodApiInfo.FieldInfo();
        ApiModelProperty annotation = field.getAnnotation(ApiModelProperty.class);
        fieldInfo.nameEn = field.getName();
        fieldInfo.serial = entityInfo.serial++;
        fieldInfo.information = annotation.value();
        fieldInfo.isNecessary = annotation.required();
        fieldInfo.type = getType(field, sub, json, genericReturnType);
        fieldInfo.nameCn = getSubString(annotation.value());
        return fieldInfo;
    }

    public static String getSubString(String args) {
        if (StringUtil.isNotBlank(args)) {
            if (args.indexOf("(") > 0) {
                return args.substring(0, args.indexOf("("));
            }
            if (args.indexOf("（") > 0) {
                return args.substring(0, args.indexOf("（"));
            }
        }
        return args;
    }

    public static String getType(Field field, List<MethodApiInfo.EntityInfo> sub, JSONObject jsonObject, Type genericReturnType) {
        Class<?> type = field.getType();
        Class<?> declaringClass = field.getDeclaringClass();

        if ("data".equals(field.getName()) && BaseResponse.class.isAssignableFrom(declaringClass)) {

            try {
                jsonObject.put(field.getName(), type.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            String s = dealWithGeneric(genericReturnType, declaringClass.getName(), jsonObject, sub);
            return s.startsWith("<") ? s.substring(1, s.length() - 1) : s;
        }
        if (type.isAssignableFrom(String.class)) {
            return "字符型";
        }
        if (type.isAssignableFrom(Boolean.class)) {
            return "布尔型";
        }
        if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(Long.class)) {
            return "数字整型";
        }
        if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(Float.class)) {
            return "浮点型";
        }
        if (type.isAssignableFrom(Date.class)) {
            return "日期型";
        }
        if (type.getAnnotation(ApiModel.class) != null
                && !excludeClass.contains(type.getSimpleName())) {
            try {
                jsonObject.put(field.getName(), type.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            getEntityInfo(type, sub, jsonObject);
        }
        if ("List".equals(type.getSimpleName())
                || "IPage".equals(type.getSimpleName())
                || "Page".equals(type.getSimpleName()) || "Map".equals(type.getSimpleName())) {
            Type genericType = field.getGenericType();
            return getTypeSimpleName(genericType) + dealWithGeneric(genericType, declaringClass.getName(), jsonObject, sub);
        }
        return type.getSimpleName();
    }

    public static String getTypeSimpleName(Type actualType) {
        if (actualType instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) actualType;
            String typeName = p.getRawType().getTypeName();
            return typeName.substring(typeName.lastIndexOf(".") + 1);
        }
        return actualType.getTypeName();
    }

    public static String dealWithGeneric(Type genericType, JSONObject json, List<MethodApiInfo.EntityInfo> sub) {
        return dealWithGeneric(genericType, null, json, sub);
    }

    public static String dealWithGeneric(Type genericType, String fieldClass, JSONObject json, List<MethodApiInfo.EntityInfo> sub) {
        Type[] actualTypeArguments = null;
        if (genericType instanceof ParameterizedType) {
            actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();

        }
        if (actualTypeArguments == null || actualTypeArguments.length == 0) {

            if (genericType instanceof WildcardType || genericType instanceof TypeVariable) {
                return Object.class.getSimpleName();
            }
            if (genericType instanceof Class) {
                return ((Class<?>) genericType).getSimpleName();
            }
            return genericType.getTypeName();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<");
        for (Type actualType : actualTypeArguments) {
            if (actualType instanceof ParameterizedType) {
                String typeName = actualType.getTypeName();

                Type[] arguments = ((ParameterizedType) actualType).getActualTypeArguments();
                LinkedList<Type> list = new LinkedList<>(Arrays.asList(arguments));
                map.put(typeName.split("<")[0], list);
                getClassInfo(((ParameterizedType) actualType).getRawType(), json, sub);
                sb.append(getTypeSimpleName(actualType))
                        .append(dealWithGeneric(actualType, json, sub)).append(",");
                map.clear();
                continue;
            }
            if (actualType instanceof WildcardType || actualType instanceof TypeVariable) {
                if (StringUtil.isNotBlank(fieldClass)) {
                    String find;
                    if (IPage.class.getName().equals(fieldClass)) {
                        find = "com.baomidou.mybatisplus.core.metadata.IPage";
                    } else {
                        find = fieldClass;
                    }
                    LinkedList<Type> types = map.get(find);
                    if (types != null && types.size() > 0) {
                        sb.append(subTypeName(types.pop().getTypeName()));
                        continue;

                    }

                }
                sb.append(Object.class.getSimpleName());
                continue;
            }

            getClassInfo(actualType, json, sub);

            sb.append(dealWithGeneric(actualType, json, sub)).append(",");
        }

        return sb.substring(0, sb.length() - 1) + ">";
    }


    private static String subTypeName(String scan) {
        String s = scan.split("<")[0];
        String[] split = s.split("\\.");
        String res = split.length > 1 ? split[split.length - 1] : split[0];
        Matcher matcher = pattern.matcher(scan);
        String sub = "";
        while (matcher.find()) {
            String group = matcher.group();
            sub = sub + subTypeName(group) + ",";
        }
        if (sub.length() > 1) {
            res = res + "<" + sub.substring(0, sub.length() - 1) + ">";
        }
        return res;
    }

    static Pattern pattern = Pattern.compile("(?<=<).*(?=>)");
    static Map<String, LinkedList<Type>> map = new HashMap<>();

    public static void getClassInfo(Type actualType, JSONObject json,
                                    List<MethodApiInfo.EntityInfo> sub) {
        if (actualType instanceof Class) {
            Class<?> actualTypeArgument = (Class<?>) actualType;
            if (com.baomidou.mybatisplus.core.metadata.IPage.class.isAssignableFrom(actualTypeArgument)) {
                actualTypeArgument = IPage.class;
            }
            if (actualTypeArgument.getAnnotation(ApiModel.class) != null
                    && !excludeClass.contains(actualTypeArgument.getSimpleName())) {
                getEntityInfo(actualTypeArgument, sub, json);
            }
        }
    }

    public static String getNote(Api api) {
        if (api == null) {
            return "";
        }
        return StringUtil.isBlank(api.value()) ? api.tags()[0] : api.value();
    }

    public static String getNote(ApiOperation api) {
        if (api == null) {
            return "";
        }
        return api.value() == null ? api.tags()[0] : api.value();
    }

    public static String getMapping(Method method) {
        if (method.getAnnotation(PostMapping.class) != null) {
            PostMapping annotation = method.getAnnotation(PostMapping.class);
            return annotation.value().length > 0 ? getPath(annotation.value()) : getPath(annotation.path());
        }
        if (method.getAnnotation(PutMapping.class) != null) {
            PutMapping annotation = method.getAnnotation(PutMapping.class);
            return annotation.value().length > 0 ? getPath(annotation.value()) : getPath(annotation.path());
        }
        if (method.getAnnotation(GetMapping.class) != null) {
            GetMapping annotation = method.getAnnotation(GetMapping.class);
            return annotation.value().length > 0 ? getPath(annotation.value()) : getPath(annotation.path());
        }
        if (method.getAnnotation(DeleteMapping.class) != null) {
            DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
            return annotation.value().length > 0 ? getPath(annotation.value()) : getPath(annotation.path());
        }
        if (method.getAnnotation(RequestMapping.class) == null) {
            return null;
        }
        return getPath(method.getAnnotation(RequestMapping.class));
    }

    public static String getPath(RequestMapping annotation) {
        if (annotation == null) {
            return "";
        }
        return annotation.value().length > 0 ? getPath(annotation.value()) : getPath(annotation.path());
    }

    public static String getPath(String[] value) {
        if (value.length > 0) {
            String s = value[0];
            if (!s.startsWith("/")) {
                s = "/" + s;
            }
            if (s.endsWith("/")) {
                s = s.substring(0, s.length() - 1);
            }
            return s;
        }
        return "";
    }

    public static void toFile(List<MethodApiInfo> methods) {
        try {
            //获取docx解析对象
            XWPFDocument document = new XWPFDocument(InterfaceApiGen2.class.getClassLoader().getResourceAsStream(template));
//            XWPFDocument document = new XWPFDocument(POIXMLDocument.openPackage(template));
            int size = document.getParagraphs().size();
            System.out.println("docx file size:"+ size);
            //解析样式
            templateStyle = parseTemplate(document);
            //填充内容
            for (MethodApiInfo method : methods) {
                fillMethodApi(document, method);
            }
            // 删除模板
            for (int i = 0; i <= size; i++) {
                document.removeBodyElement(0);
            }
            File file = new File(out + docxPrefix + System.currentTimeMillis() + ".docx");
            FileOutputStream stream = new FileOutputStream(file);
            document.write(stream);
            stream.close();
            System.out.println("file name :"+file.getName());
            System.out.println("generator file success！");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void fillMethodApi(XWPFDocument document, MethodApiInfo api) {
        if (api == null) {
            return;
        }
        // 设置一级标题
        addParagraph(document, api.title, 1);

        // 业务描述
        addParagraph(document, subHeader[0] + ": " + api.businessDesc, 2);

        // 接口提供方
        addParagraph(document, subHeader[1] + ": " + api.interfaceProvide, 2);

        // 接口地址
        addParagraph(document, subHeader[2] + ": " + api.url, 2);

        // 接口请求字段
        addEntityFields(document, api.paramEntities, subHeader[3], 2);
        // 接口响应字段
        addEntityFields(document, api.returnEntities, subHeader[4], 2);

        // 请求示例报文
        addParagraph(document, subHeader[5] + ": ", 2);
        addContent(document, api.requestExample, 3);

        // 响应示例报文
        addParagraph(document, subHeader[6] + ": ", 2);
        addContent(document, api.responseExample, 3);
    }

    public static void addEntityFields(XWPFDocument document, List<MethodApiInfo.EntityInfo> api, String text, int level) {
        // 接口请求字段
        addParagraph(document, text + ": ", level);
        if (api != null && api.size() > 0) {
            for (MethodApiInfo.EntityInfo entityInfo : api) {
                addParagraph(document, entityInfo.nameCn + "(" + entityInfo.nameEn + ")" + ": ", 3);
                addTable(document, entityInfo);
            }
        }
    }

    public static void addContent(XWPFDocument document, String text, int level) {
        if (StringUtil.isNotBlank(text)) {
            for (String s : text.split("\n")) {
                addParagraph(document, s, level);
            }
        }
    }

    public static XWPFParagraph addParagraph(XWPFDocument document, String text, int level) {
        if (templateStyle == null) {
            System.out.println("模板为空");
        }
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontFamily("宋体");
        switch (level) {
            case 1:
                paragraph.setStyle("2");
                paragraph.getCTP().setPPr(templateStyle.header1.gstyle);
                run.getCTR().setRPr(templateStyle.header1.rstyle);
                break;
            case 2:
                paragraph.setStyle("11");
                paragraph.getCTP().setPPr(templateStyle.header2.gstyle);
                run.getCTR().setRPr(templateStyle.header2.rstyle);
                break;
            case 3:
            default:
                paragraph.setStyle("11");
                paragraph.getCTP().setPPr(templateStyle.header3.gstyle);
                run.getCTR().setRPr(templateStyle.header3.rstyle);
        }
        return paragraph;
    }


    public static void addTable(XWPFDocument document, MethodApiInfo.EntityInfo entityInfo) {
        if (entityInfo != null
                && entityInfo.fieldInfos != null && entityInfo.fieldInfos.size() > 0) {
            // 初始化表及其样式
            XWPFTable xwpfTable = document.insertNewTbl(document.createParagraph().getCTP().newCursor());
            xwpfTable.getCTTbl().setTblGrid(templateStyle.getTable().getTableStyle().getTblGrid());
            xwpfTable.getCTTbl().setTblPr(templateStyle.getTable().getTableStyle().getTblPr());
            // 初始化行列样式和行数列数
            styleTableAndSize(xwpfTable, entityInfo.fieldInfos.size() + 1, tabHeader.length);
            // 设置表头
            setTableHeader(xwpfTable);
            // 填充内容
            for (int i = 0; i < entityInfo.fieldInfos.size(); i++) {
                MethodApiInfo.FieldInfo fieldInfo = entityInfo.fieldInfos.get(i);
                XWPFTableRow row = xwpfTable.getRow(i + 1);
                XWPFTableCell cell0 = row.getCell(0);
                cell0.setText(String.valueOf(fieldInfo.serial));
                XWPFTableCell cell1 = row.getCell(1);
                cell1.setText(fieldInfo.nameCn);
                XWPFTableCell cell2 = row.getCell(2);
                cell2.setText(fieldInfo.nameEn);
                XWPFTableCell cell3 = row.getCell(3);
                cell3.setText(fieldInfo.type);
                XWPFTableCell cell4 = row.getCell(4);
                cell4.setText(String.valueOf(fieldInfo.isNecessary));
                XWPFTableCell cell5 = row.getCell(5);
                cell5.setText(fieldInfo.information);
            }
        }
    }

    public static void setTableHeader(XWPFTable xwpfTable) {
        XWPFTableRow row = xwpfTable.getRow(0);
        for (int i = 0; i < tabHeader.length; i++) {
            row.getCell(i).setText(tabHeader[i]);
        }
    }

    public static void styleTableAndSize(XWPFTable xwpfTable, int row, int cell) {
        for (int i = 0; i < row; i++) {
            XWPFTableRow row1 = i == 0 ? xwpfTable.getRow(0) : xwpfTable.createRow();
            if (i == 0) {
                row1.getCtRow()
                        .setTrPr(templateStyle.getTable().row);
                for (int i1 = 0; i1 < cell; i1++) {
                    XWPFTableCell cell1 = i1 == 0 ? row1.getCell(0) : row1.createCell();
                    cell1.getCTTc().setTcPr(templateStyle.getTable().cell);
                }
            }

        }
    }


    public static Template parseTemplate(XWPFDocument document) throws IOException {
//        XWPFStyles styles = newDoc.createStyles();
//        styles.addStyle(document.getStyles().getStyle("2"));
//        styles.addStyle(document.getStyles().getStyle("11"));
        XWPFTable xwpfTable = document.getTables().get(0);
        Template.Table table = new Template.Table();
        table.tableStyle = xwpfTable.getCTTbl();
        table.row = xwpfTable.getRow(0).getCtRow().getTrPr();
        table.cell = xwpfTable.getRow(0).getCell(0).getCTTc().getTcPr();
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        Template.Header header1 = new Template.Header();
        header1.setGstyle(paragraphs.get(0).getCTP().getPPr());
        header1.setRstyle(paragraphs.get(0).getRuns().get(0).getCTR().getRPr());
        Template.Header header2 = new Template.Header();
        header2.setGstyle(paragraphs.get(1).getCTP().getPPr());
        header2.setRstyle(paragraphs.get(1).getRuns().get(0).getCTR().getRPr());
        Template.Header header3 = new Template.Header();
        header3.setGstyle(paragraphs.get(2).getCTP().getPPr());
        header3.setRstyle(paragraphs.get(2).getRuns().get(0).getCTR().getRPr());
        return new Template(header1, header2, header3, table);
    }
}
