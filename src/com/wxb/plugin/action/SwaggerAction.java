package com.wxb.plugin.action;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SwaggerAction extends AnAction {
    static String java_source = "src/main/java";


    public SwaggerAction() {
        super("生成api文档");
    }


    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
//        String scanPath;
//        String projectFilePath = project.getProjectFilePath();
//        if(projectFilePath== null || projectFilePath==""){
//            Messages.showMessageDialog(project,"please select one project","warning",Messages.getInformationIcon());
//            return;
//        }
//        scanPath = projectFilePath;
//        PsiClass psiClass;
//        GlobalSearchScope s = new JavaFilesSearchScope(project);
//        PsiClass aClass = JavaPsiFacade.getInstance(project).findClass("com.xxl.job.core.enums.ExecutorBlockStrategyEnum", s);
//        String projectFilePath = project.getProjectFilePath();
//        Document currentDoc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
//        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
//        VirtualFile workspaceFile = project.getWorkspaceFile();
//        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        PsiElement data = event.getData(CommonDataKeys.PSI_ELEMENT);
        List<PsiJavaFile> psiClassList = new ArrayList<>();
        getPsiClass(data, psiClassList);
        for (PsiJavaFile javaFile : psiClassList) {
            PsiClass[] classes = javaFile.getClasses();
            String name = javaFile.getName();
            String name12 = classes[0].getName();

            PsiClass aClass = classes[0];
            if (name.contains("AlarmController")) {
                for (PsiMethod method : aClass.getMethods()) {
                    PsiAnnotation[] annotations = method.getAnnotations();
                    PsiAnnotation requestMapping = method.getAnnotation("PostMapping");
                    String qualifiedName = annotations[0].getQualifiedName();
                    PsiAnnotationMemberValue value = annotations[0].findAttributeValue("value");
                    System.out.println();
                    if(method.getName().contains("listPage")){
                        // 获取方法返回类型
                        PsiType returnType = method.getReturnType();
                        // 获取方法返回类型，与上一步获取的是同一个对象
                        PsiType deepComponentType = returnType.getDeepComponentType();
                        System.out.println();
                        if(returnType instanceof PsiClassReferenceType){
                            PsiClassReferenceType field = (PsiClassReferenceType) returnType;
                            // 获取字段名称等，参考字段
                            String name1 = field.getName();
                            String className1 = field.getClassName();
                            String canonicalText1 = field.getCanonicalText();
                            String internalCanonicalText = field.getInternalCanonicalText();
                            String presentableText = field.getPresentableText();
                            PsiClass resolve = field.resolve();
                            PsiClassType.ClassResolveResult classResolveResult = field.resolveGenerics();
                            PsiClass element = classResolveResult.getElement();
                            element.hasTypeParameters();
                            PsiSubstitutor substitutor = classResolveResult.getSubstitutor();
                            // 字段泛型里面的内容，key是泛型标识的符号比如T（PsiTypeParameter）,value(PsiType/PsiClassReferenceType)是泛型的具体内容
                            Map<PsiTypeParameter, PsiType> substitutionMap = substitutor.getSubstitutionMap();
//                            if(classResolveResult instanceof PsiClassReferenceType){

//                                PsiClassReferenceType classResolveResult2 = (PsiClassReferenceType) classResolveResult;
//                                PsiClass resolve2 = field.resolve();
//                                String name2 = field.getName();
//                                String className2 = field.getClassName();
//                            }
                            String className = field.getClassName();
                            String canonicalText = field.getCanonicalText();
                            System.out.println();
                        }
                    }
                }
            }
            if (name.contains("SaveTacticRequest")) {
                System.out.println();
                // 获取字段
                PsiField[] fields = aClass.getFields();
                // 获取字段名称
                String text = fields[1].getText();
                // 获取字段类型
                PsiType type1 = fields[1].getType();
                if(type1 instanceof PsiClassReferenceType){
                    PsiClassReferenceType field = (PsiClassReferenceType) type1;
                    // 解析字段类型属于的类
                    PsiClass resolve = field.resolve();
                    PsiClassType.ClassResolveResult classResolveResult = field.resolveGenerics();
                    String className = field.getClassName();
                    String canonicalText = field.getCanonicalText();
                    System.out.println();
                }

                PsiType type = fields[0].getType();
                PsiType deepComponentType = type.getDeepComponentType();
            }
//            PsiAnnotation[] s = aClass.getAnnotations();
//            if (s != null && s.length > 0) {
//                String qualifiedName = s[0].getQualifiedName();
//                ASTNode node = s[0].getNode();
//                System.out.println();
//            }
//
////            type.
//
//            String qualifiedName = aClass.getQualifiedName();
//            if (classes.length > 1) {
//                String name1 = classes[0].getName();
//                System.out.println();
//            }
//            name = null;
//            if()
        }

//        PsiFile psiFile = PsiManager.getInstance(project).findFile(project.getBaseDir());
        PsiFile po = PsiManager.getInstance(project).findFile(project.getProjectFile());
//        Messages.showMessageDialog(project, "Hello, " +  project.getProjectFilePath(), "Information", Messages.getInformationIcon());


//        psiFile.getChildren();
//
//        InterfaceApiGen.run(project.getBasePath(), project.getBasePath());
//        Messages.showMessageDialog(project,"generator api success","success",Messages.getInformationIcon());
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

//    public static void main(String[] args) {
//        ExternalJarLoadClassLoader classLoader = new ExternalJarLoadClassLoader(SwaggerAction.class.getClassLoader(),
//                "fileGen.jar");
//        try {
//            Class<?> aClass = classLoader.loadClass("com.wxb.core.file.api.gen.InterfaceApiGen");
//            Method run = aClass.getMethod("run",String.class,String.class);
//            run.invoke(null, "src/main/java/test","src");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void test(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        String txt = Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
        Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information", Messages.getInformationIcon());
    }

    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
