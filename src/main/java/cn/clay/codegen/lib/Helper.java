package cn.clay.codegen.lib;

import io.swagger.v3.oas.models.tags.Tag;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static org.yaml.snakeyaml.tokens.Token.ID.Tag;

public class Helper {
    protected static Tag defaultTag;

    /**
     * 字符串驼峰
     * uac_simple -> UacSimple uacSimple
     * uac-simple -> UacSimple uacSimple
     * UacSimple -> UacSimple UacSimple
     */
    public static String camelize(String str, Boolean lowercaseFirstLetter) {
        StringBuilder stringBuffer = new StringBuilder(str);
        StringBuilder rtBuffer = new StringBuilder();

        for (int i = 0; i < stringBuffer.length(); i++) {
            char cht = stringBuffer.charAt(i);
            if (i == 0) {
                rtBuffer.append(lowercaseFirstLetter ? Character.toLowerCase(cht) : Character.toUpperCase(cht));
            } else if (cht == '-' || cht == '_') {
                int nextPos = i + 1;
                if (nextPos < stringBuffer.length()) {
                    rtBuffer.append(Character.toUpperCase(stringBuffer.charAt(nextPos)));
                }

                i++;
            } else {
                rtBuffer.append(cht);
            }

        }

        return rtBuffer.toString();
    }

    @NotNull
    public static Tag defaultTag() {
        if (defaultTag == null) {
            defaultTag = new Tag();
            defaultTag.setName("Default");
            defaultTag.setDescription("默认");
        }
        return defaultTag;
    }

    // 此处需要谨慎操作
    public static boolean deleteDir(String path) {
        File file = new File(path);
        if (!file.exists()) {// 判断是否待删除目录是否存在
            System.err.println("The dir are not exists!");
            return false;
        }

        String[] content = file.list();//取得当前目录下所有文件和文件夹
        for (String name : content) {
            File temp = new File(path, name);
            if (temp.isDirectory()) {// 判断是否是目录
                deleteDir(temp.getAbsolutePath());// 递归调用，删除目录里的内容
                temp.delete();// 删除空目录
            } else {
                if (!temp.delete()) {// 直接删除文件
                    System.err.println("Failed to delete " + name);
                }
            }
        }
        return true;
    }
}
