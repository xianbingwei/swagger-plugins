package com.wxb.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.wxb.plugin.core.gen.InterfaceApiGen2;

import java.util.ArrayList;
import java.util.List;

public class SwaggerAction extends AnAction {
    static String java_source = "src/main/java";


    public SwaggerAction() {
        super("生成api文档");
    }


    public String getStoreDir(AnActionEvent event) {
        Project project = event.getProject();
        String name = project.getName();
        if (project == null) {
            return null;
        }
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        descriptor.setDescription("请选择接口文档的保存目录");
        descriptor.setTitle("选择文档保存路径");
        VirtualFile[] virtualFiles = FileChooser.chooseFiles(descriptor, project, null);
        for (VirtualFile virtualFile : virtualFiles) {
            return virtualFile.getCanonicalPath();
        }
        return project.getBasePath();
    }

    public PsiElement getScanDir(AnActionEvent event) {

        PsiElement data = event.getData(CommonDataKeys.PSI_ELEMENT);
        List<PsiJavaFile> psiClassList = new ArrayList<>();
        if (data != null) {
            return data;

        }
        // 如果用户没有选择指定目录，则从根目录开始
        Project project = event.getProject();
        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        PsiDirectory subdirectory = directory.findSubdirectory("src");

        return subdirectory.findSubdirectory("main");
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        String name = project.getName();
        if (project == null) {
            return;
        }

        // 获取文件存储目录
        String basePath = getStoreDir(event);
        // 获取扫描目录
        PsiElement scanDir = getScanDir(event);
        String s = InterfaceApiGen2.genFile(scanDir, basePath + "/", project.getName());

        PsiFile po = PsiManager.getInstance(project).findFile(project.getProjectFile());
        Messages.showMessageDialog(project, "路径:\n" + s, "文档生成成功", Messages.getInformationIcon());


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
