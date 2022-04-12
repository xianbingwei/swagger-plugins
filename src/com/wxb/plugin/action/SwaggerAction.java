package com.wxb.plugin.action;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jvm.JvmParameter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.wxb.plugin.core.gen.InterfaceApiGen2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        PsiElement data = event.getData(CommonDataKeys.PSI_ELEMENT);
        List<PsiJavaFile> psiClassList = new ArrayList<>();
        getPsiClass(data, psiClassList);
        String basePath = project.getBasePath();
        InterfaceApiGen2.genFile(data, project.getBasePath()+"/");

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
