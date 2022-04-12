package com.wxb.plugin.core.gen;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    private static Pattern p = Pattern.compile("(?<=\").*(?=\")");

    //排除字段
    static Set<String> excludeFields = new HashSet<>();
    //排除类
    static Set<String> excludeClass = new HashSet<>();


    static {
        excludeFields.addAll(Arrays.asList("signature", "version"));
//        excludeClass.addAll(Arrays.asList());
    }

    public static void getPsiClass(PsiElement children, List<PsiJavaFile> psiClassList) {
        if (children instanceof PsiJavaFile) {
            psiClassList.add((PsiJavaFile) children);
        } else if (children instanceof PsiDirectory) {
            PsiElement[] elements = children.getChildren();
            if (elements != null && elements.length > 1) {
                for (PsiElement child : elements) {
                    getPsiClass(child, psiClassList);
                }
            }
        }

    }

    public static String genFile(PsiElement data, String outputPath) {
        List<PsiJavaFile> files = new ArrayList<>();
        getPsiClass(data, files);
        List<MethodApiInfo> methods = new ArrayList<>();
        for (PsiJavaFile file : files) {
            PsiClass aClass;
            try {
                PsiClass[] classes = file.getClasses();
                String name = file.getName();
                aClass = classes[0];
            } catch (Exception e) {
                System.out.println("NOT CLASS: " + file.getName());
                continue;
            }
            if (!isController(aClass)) {
                System.out.println("not controller:" + file.getName());
                continue;
            }
            String note = getNote(aClass.getAnnotation(Api.class.getName()));
            String prefixPath = getPath(aClass.getAnnotation(RequestMapping.class.getName()));
            System.out.println(prefixPath);
            for (PsiMethod method : aClass.getMethods()) {
                String mapping = getMapping(method);
                if (mapping == null) {
                    continue;
                }
                methods.add(getInfo(method, prefixPath, note));
            }
        }
        out = outputPath;
       return toFile(methods);
    }

    public static final String controller = "org.springframework.stereotype.Controller";
    public static final String restController = "org.springframework.web.bind.annotation.RestController";

    public static boolean isController(PsiClass aClass) {
        PsiAnnotation[] annotations = aClass.getAnnotations();
        if (annotations != null) {
            for (PsiAnnotation annotation : annotations) {
                if (restController.equals(annotation.getQualifiedName())
                        || controller.equals(annotation.getQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 解析方法参数类型，返回参数类型
     */
    public static MethodApiInfo getInfo(PsiMethod method, String prefixPath, String notePref) {

        MethodApiInfo methodApiInfo = new MethodApiInfo();
        String path = getMapping(method);
        methodApiInfo.url = prefixPath + path;
        System.out.println("url: " + methodApiInfo.url);
        methodApiInfo.businessDesc = getNote(method.getAnnotation(ApiOperation.class.getName()));
        methodApiInfo.title = notePref + "-" + methodApiInfo.businessDesc;
        methodApiInfo.paramEntities = new ArrayList<>();
        JSONArray req = new JSONArray();
        List<Class<?>> sub = new ArrayList<>();
        // 解析方法参数
        for (PsiParameter parameterType : method.getParameterList().getParameters()) {
            PsiClassReferenceType type = (PsiClassReferenceType) parameterType.getType();

            if (type.resolve().getAnnotation(ApiModel.class.getName()) == null) {
                continue;
            }

            JSONObject object = new JSONObject();
            req.add(object);

            getEntityInfo(type, methodApiInfo.paramEntities, object);
        }
        // 解析返回类型
        JSONObject res = new JSONObject();
        methodApiInfo.returnEntities = getReturnInfo(method, res);

        //设置样例
        methodApiInfo.requestExample = req.toJSONString();
        methodApiInfo.responseExample = res.toJSONString();
        return methodApiInfo;
    }

    public static List<MethodApiInfo.EntityInfo> getReturnInfo(PsiMethod method, JSONObject res) {
        List<MethodApiInfo.EntityInfo> returnEntities = new ArrayList<>();
        List<Class<?>> sub = new ArrayList<>();
        PsiClassReferenceType returnType = (PsiClassReferenceType) method.getReturnType();
        PsiClass resolve = returnType.resolve();
        if (resolve == null || resolve.getAnnotation(ApiModel.class.getName()) == null) {
            return null;
        }

        getEntityInfo(returnType, returnEntities, res);

        return returnEntities;
    }

//    public static String formatContent(Class<?> c) {
//        try {
//            return formatContent(c.newInstance());
//        } catch (InstantiationException | IllegalAccessException e) {
//            System.out.println(e);
//        }
//        return "{}";
//    }
//
//    public static String formatContent(Object o) {
//        return JSON.toJSONString(o,
//                SerializerFeature.PrettyFormat,
//                SerializerFeature.WriteNullBooleanAsFalse,
//                SerializerFeature.WriteBigDecimalAsPlain,
//                SerializerFeature.WriteMapNullValue,
//                SerializerFeature.WriteNullListAsEmpty,
//                SerializerFeature.WriteNullStringAsEmpty,
//                SerializerFeature.WriteNullNumberAsZero,
//                SerializerFeature.WriteDateUseDateFormat
//        );
//    }

    public static void getEntityInfo(PsiClassReferenceType type, List<MethodApiInfo.EntityInfo> sub, JSONObject json) {
        PsiClass resolve = type.resolve();
        if (resolve.getAnnotation(ApiModel.class.getName()) == null) {
            return;
        }
        MethodApiInfo.EntityInfo entityInfo = new MethodApiInfo.EntityInfo();
        entityInfo.entityClass = resolve.getQualifiedName();
        entityInfo.nameEn = resolve.getName();
        entityInfo.nameCn = getEntityDesc(resolve.getAnnotation(ApiModel.class.getName()));
        entityInfo.fieldInfos = new ArrayList<>();
        List<PsiField> fields = new ArrayList<>();
        // 获取所有字段
        getAllFields(fields, resolve);
        // 获取泛型
        Map<PsiTypeParameter, PsiType> genericType = getGenericType(type);
        // 遍历
        for (PsiField field : fields) {
            if (field.getAnnotation(ApiModelProperty.class.getName()) == null
                    || "true".equals(field.getAnnotation(ApiModelProperty.class.getName()).findAttributeValue("hidden"))
                    || excludeFields.contains(field.getName())) {
                continue;
            }
            entityInfo.fieldInfos.add(getFieldInfo(entityInfo, field, genericType, sub, json));
        }
        sub.add(entityInfo);
    }

    public static void getAllFields(List<PsiField> fields, PsiClass resolve) {
        if (resolve == null) {
            return;
        }
        fields.addAll(Arrays.asList(resolve.getAllFields()));
    }

    public static MethodApiInfo.FieldInfo getFieldInfo(MethodApiInfo.EntityInfo entityInfo, PsiField field,
                                                       Map<PsiTypeParameter, PsiType> genericType,
                                                       List<MethodApiInfo.EntityInfo> sub, JSONObject json) {
        MethodApiInfo.FieldInfo fieldInfo = new MethodApiInfo.FieldInfo();
        PsiAnnotation annotation = field.getAnnotation(ApiModelProperty.class.getName());
        fieldInfo.nameEn = field.getName();
        fieldInfo.serial = entityInfo.serial++;
        fieldInfo.information = getText(annotation, "value");
        fieldInfo.isNecessary = Boolean.valueOf(getText(annotation, "required"));
        fieldInfo.type = getType(field, genericType, sub, json);
        fieldInfo.nameCn = getSubString(getText(annotation, "value"));
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

    public static Map<PsiTypeParameter, PsiType> getGenericType(PsiClassReferenceType type) {
        PsiClassType.ClassResolveResult classResolveResult = type.resolveGenerics();
        PsiClass element = classResolveResult.getElement();
        element.hasTypeParameters();
        return classResolveResult.getSubstitutor().getSubstitutionMap();
    }

    static String[] primitive = {"short", "byte", "int", "long", "boolean", "char", "float", "double"};
    static String[] primitives = {"数字整形", "数字整形", "数字整形", "数字整形", "布尔型", "字符型", "浮点型", "浮点型"};

    public static String getPrimitive(PsiPrimitiveType args) {
        String name = args.getName();
        for (int i = 0; i < primitive.length; i++) {
            if (primitive[i].equals(name)) {
                return primitives[i];
            }
        }
        return null;
    }

    public static String getType(PsiField field, Map<PsiTypeParameter, PsiType> genericType,
                                 List<MethodApiInfo.EntityInfo> sub, JSONObject jsonObject) {
        System.out.println(field.getName());
        // 处理原始数据类型
        if (field.getType() instanceof PsiPrimitiveType) {
            return getPrimitive((PsiPrimitiveType) field.getType());
        }
        PsiClassReferenceType type = (PsiClassReferenceType) field.getType();

        // 处理泛型
        if (type.resolve() instanceof PsiTypeParameter) {
            PsiType psiType = genericType.get((PsiTypeParameter) type.resolve());
            if(psiType == null){
                jsonObject.put(field.getName(), "object");
                return "Object";
            }
            type = (PsiClassReferenceType) psiType;
        }

        String qualifiedName = type.resolve().getQualifiedName();
        // TODO 最好替换成isAssignableFrom,
        if (qualifiedName.equals(String.class.getName())) {
            return "字符型";
        }
        if (qualifiedName.equals(Boolean.class.getName())) {
            return "布尔型";
        }
        if (qualifiedName.equals(Integer.class.getName()) || qualifiedName.equals(Long.class.getName())) {
            return "数字整型";
        }
        if (qualifiedName.equals(Double.class.getName()) || qualifiedName.equals(Float.class.getName())) {
            return "浮点型";
        }
        if (qualifiedName.equals(Date.class.getName())) {
            return "日期型";
        }

        // 对象,json考虑循环引用
        JSONObject object = new JSONObject();
        jsonObject.put(field.getName(), object);

        dealWithGeneric((PsiClassReferenceType) type, object, sub);

        return type.getPresentableText();
    }

    public static String getTypeSimpleName(Type actualType) {
        if (actualType instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) actualType;
            String typeName = p.getRawType().getTypeName();
            return typeName.substring(typeName.lastIndexOf(".") + 1);
        }
        return actualType.getTypeName();
    }

    public static boolean entityExists(List<MethodApiInfo.EntityInfo> sub, String exist) {
        for (MethodApiInfo.EntityInfo entityInfo : sub) {
            if (Objects.equals(exist, entityInfo.entityClass)) {
                return true;
            }
        }
        return false;
    }

    public static void dealWithGeneric(PsiClassReferenceType type, JSONObject json, List<MethodApiInfo.EntityInfo> sub) {
        if (type == null) {
            return;
        }
        // 处理对象
        getClassInfo(type, json, sub);

        Map<PsiTypeParameter, PsiType> genericType = getGenericType(type);
        if (genericType == null) {
            return;
        }
        // 处理对象泛型
        for (PsiType actualType : genericType.values()) {
            if (actualType instanceof PsiClassReferenceType) {
                PsiClassReferenceType referenceType = (PsiClassReferenceType) actualType;

                dealWithGeneric(referenceType, new JSONObject(), sub);
            }
        }
    }

    public static void getClassInfo(PsiClassReferenceType actualType, JSONObject json,
                                    List<MethodApiInfo.EntityInfo> sub) {
        PsiClass resolve = actualType.resolve();
        if (resolve != null) {

            if (entityExists(sub, resolve.getQualifiedName())) {
                return;
            }

            if (resolve.getAnnotation(ApiModel.class.getName()) != null
                    && !excludeClass.contains(resolve.getName())) {
                getEntityInfo(actualType, sub, json);
            }
        }
    }


    public static String getEntityDesc(PsiAnnotation api) {
        if (api == null) {
            return "";
        }

        String s;
        if (StringUtil.isNotBlank(s = getText(api, "value"))) {
            return s;
        }
        if (StringUtil.isNotBlank(s = getText(api, "description"))) {
            return s;
        }
        return "";
    }

    public static String getNote(PsiAnnotation api) {
        if (api == null) {
            return "";
        }
        PsiAnnotationMemberValue value = api.findAttributeValue("value");
        return value == null ? "" : match(value.getText());
    }

    public static String getText(PsiAnnotation api, String attribute) {
        if (api == null) {
            return null;
        }
        PsiAnnotationMemberValue value = api.findAttributeValue(attribute);
        return value == null ? "" : match(value.getText());
    }

    public static String getTag(PsiAnnotation api) {
        if (api == null) {
            return "";
        }
        PsiAnnotationMemberValue value = api.findAttributeValue("tags");
        return value == null ? "" : value.getText();
    }

    public static String getMapping(PsiMethod method) {
        PsiAnnotation annotation;
        if (method.getAnnotation(PostMapping.class.getName()) != null) {
            annotation = method.getAnnotation(PostMapping.class.getName());
        } else if (method.getAnnotation(PutMapping.class.getName()) != null) {
            annotation = method.getAnnotation(PutMapping.class.getName());
        } else if (method.getAnnotation(GetMapping.class.getName()) != null) {
            annotation = method.getAnnotation(GetMapping.class.getName());
        } else if (method.getAnnotation(DeleteMapping.class.getName()) != null) {
            annotation = method.getAnnotation(DeleteMapping.class.getName());
        } else if (method.getAnnotation(RequestMapping.class.getName()) != null) {
            annotation = method.getAnnotation(RequestMapping.class.getName());
        } else {
            annotation = null;
        }
        return annotation == null ? null : formatPath(getPath(annotation));
    }

    public static String getPath(PsiAnnotation annotation) {
        if (annotation == null) {
            return "";
        }
        String s;
        if (StringUtil.isNotBlank(s = getText(annotation, "path"))) {
            return s;
        }
        if (StringUtil.isNotBlank(s = getText(annotation, "value"))) {
            return s;
        }
        if (StringUtil.isNotBlank(s = getText(annotation, "name"))) {
            return s;
        }
        return "";
    }

    public static String match(String s) {
        Matcher matcher = p.matcher(s);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static String formatPath(String s) {
        if (s != null) {
            if (!s.startsWith("/")) {
                s = "/" + s;
            }
            if (s.endsWith("/")) {
                s = s.substring(0, s.length() - 1);
            }
            return s;
        }
        return s;
    }

    public static String toFile(List<MethodApiInfo> methods) {
        try {
            //获取docx解析对象
            XWPFDocument document = new XWPFDocument(InterfaceApiGen2.class.getClassLoader().getResourceAsStream(template));
//            XWPFDocument document = new XWPFDocument(POIXMLDocument.openPackage(template));
            int size = document.getParagraphs().size();
            System.out.println("docx file size:" + size);
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
            System.out.println("file name :" + file.getName());
            System.out.println("generator file success！");
            return file.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
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
