package cn.clay.codegen;

import io.swagger.oas.models.tags.Tag;
import org.jetbrains.annotations.NotNull;

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
}
