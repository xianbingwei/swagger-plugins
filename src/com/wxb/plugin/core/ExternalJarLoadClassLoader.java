package com.wxb.plugin.core;

import javax.naming.NamingException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * <p>
 *
 * </p>
 *
 * @author weixianbing
 * @create 2022/2/14 11:36
 */
public class ExternalJarLoadClassLoader extends ClassLoader {
    public static String ClASS_PATH = "BOOT-INF/classes";
    public static String LIB_PATH = "BOOT-INF/lib";
    public static String[] SOURCE_TYPE = {".jar", ".html"};

    /**
     * 存放已经加载的类
     */
    private final static Map<String, Class<?>> CLASS_MAP = new ConcurrentHashMap<>();
    /**
     * 存放类的解析
     */
    private final static Map<String, Map<String, EntryMeta>> JAR_ENTRY = new ConcurrentHashMap<>();
    /**
     * 存放jar包
     */
    private final static Map<String, JarFile> JAR_MAP = new ConcurrentHashMap<>();

    /**
     * 自定义class的核心方法
     *
     * @param name jar包所在路径
     * @return 类
     * @throws ClassNotFoundException 异常
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (CLASS_MAP.containsKey(name)) {
            return CLASS_MAP.get(name);
        } else {
            try {
                Class<?> aClass = doGetClass(name);
                CLASS_MAP.put(name, aClass);
                return aClass;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.findClass(name);
    }

    public ExternalJarLoadClassLoader(ClassLoader parent, String jarPath) {
        super(parent);
        loadJarFile(new File(jarPath));
    }
    public ExternalJarLoadClassLoader(ClassLoader parent, URL url) {
        super(parent);
        try {
            loadJarFile(new File(url.toURI()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws ClassNotFoundException, NamingException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        ExternalJarLoadClassLoader loader = new ExternalJarLoadClassLoader(null, "target/fileGen.jar");
        InputStream resourceAsStream = loader.getResourceAsStream("interface.docx");
        Class<?> aClass = loader.loadClass("com.wxb.core.file.api.gen.InterfaceApiGen");
        Method run = aClass.getMethod("run", String.class, String.class);
        run.invoke(null, "a", "");

    }

    @Override
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            if (url != null) {
                return url.openStream();
            }
            for (JarFile value : JAR_MAP.values()) {
                ZipEntry entry;
                if ((entry = value.getEntry(name)) == null) {
                    entry = value.getEntry("BOOT-INF/classes/" + name);
                }
                if (entry != null) {
                    return value.getInputStream(entry);
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public void loadJarFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                loadJarFile(listFile);
            }
        }
        if (file.getName().endsWith(SOURCE_TYPE[0])) {
            // 重复jar包不解析
            if (JAR_ENTRY.containsKey(file.getName())) {
                return;
            }
            Map<String, EntryMeta> jarMap = new ConcurrentHashMap<>(32);
            JAR_ENTRY.put(file.getName(), jarMap);
            try {
                JarFile jarFile = new JarFile(file);
                JAR_MAP.put(jarFile.getName(), jarFile);

                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {

                    JarEntry entry = entries.nextElement();

                    //解析class文件
                    if (entry.getName().endsWith(".class")) {
                        String name = getClassName(entry.getName());
                        jarMap.put(name, new EntryMeta(entry, jarFile, name));
                        // 解析jar包
                    } else if (entry.getName().endsWith(".jar")) {
                        File file1 = new File(getJarName(entry.getName()));
                        FileUtils.InputStreamToFile(jarFile.getInputStream(entry), file1);
                        loadJarFile(file1);
                        file1.delete();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Class<?> doGetClass(String name) throws IOException, ClassNotFoundException {
        EntryMeta entryMeta = null;
        // 循环获取jar包中的类
        for (Map<String, EntryMeta> value : JAR_ENTRY.values()) {
            if (value.containsKey(name)) {
                entryMeta = value.get(name);
                break;
            }
        }
        // 获取不到则抛出异常
        if (entryMeta == null) {
            throw new ClassNotFoundException(name);
        }
        return super.defineClass(name, entryMeta.fileData, 0, entryMeta.fileData.length);
    }

    public static String getClassName(String path) {
        if (path.startsWith(ClASS_PATH)) {
            path = path.substring(path.indexOf(ClASS_PATH) + ClASS_PATH.length() + 1);
        }
        return path.replaceAll("/", ".")
                .replaceAll(".class", "");
    }

    public static String getJarName(String path) {
        if (path.startsWith(LIB_PATH)) {
            path = path.substring(path.indexOf(LIB_PATH) + LIB_PATH.length() + 1);
        }
        return path.replaceAll("/", ".");
    }

    static class EntryMeta {
        String name;
        byte[] fileData;

        public EntryMeta(JarEntry entry, JarFile jarFile, String name) {
            this.name = name;
            fileData = loadClassData(jarFile, entry);
        }

        public byte[] loadClassData(JarFile jarFile, JarEntry entry) {
            ByteArrayOutputStream outputStream = null;
            InputStream is = null;
            try {
                is = jarFile.getInputStream(entry);
                outputStream = new ByteArrayOutputStream();
                int i;
                while ((i = is.read()) != -1) {
                    outputStream.write(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return outputStream != null ? outputStream.toByteArray() : null;
        }
    }
}
