package com.wxb.plugin.core;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 *
 * </p>
 *
 * @author weixianbing
 * @create 2022/3/3 11:09
 */
public class FileUtils {

    public static void InputStreamToFile(InputStream in, File file) throws IOException {

        FileOutputStream fo = new FileOutputStream(file);
        byte[] b = new byte[1024];
        int len;
        while ((len=in.read(b))>0){
            fo.write(b,0,len);
        }
        in.close();
        fo.close();
    }

    public static byte[] inputStreamToByte(InputStream in) throws IOException {
        ByteArrayOutputStream op = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];

        int len;
        while ((len=in.read(bytes))>0){
            op.write(bytes,0,len);
        }
        in.close();
        op.close();
        return  op.toByteArray();
    }

    public static String readPackage(File path) {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(path);
            BufferedReader bf = new BufferedReader(fileReader);
            String s = bf.readLine();
            while ((s = bf.readLine()) != null){
                if(s.contains("package")){
                    String[] s1 = s.split(" ");
                    String pack = s1[1];
                    return pack.replaceAll(";", "").trim()+"."+path.getName().replace(".java","");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("D:\\data\\test.txt");

        FileWriter fo = new FileWriter(file);

        fo.write("nishuo");
        fo.close();
    }
}
