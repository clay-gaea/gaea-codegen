package cn.clay.codegen.lib.ts;

import cn.clay.codegen.entity.TemplateFile;
import cn.clay.codegen.lib.CodegenConfig;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.media.*;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TSCodegenConfig extends CodegenConfig {
    public TSCodegenConfig(OpenAPI openAPI) {
        super(openAPI);

        templateFiles.add(new TemplateFile("package.tlf", "package.json"));

        apiTemplateFiles.add(new TemplateFile("client.tlf", "client/", "", "Client.ts"));

        modelTemplateFiles.add(new TemplateFile("entity.tlf", "entity/", "", ".ts"));
    }

    @Override
    public String getKey() {
        return "ts";
    }

    @Override
    public String getDescription() {
        return "template for typescript";
    }

    @Override
    public String getTemplateDir() {
        return "src/main/java/cn/clay/codegen/lib/ts/templates";
    }

    @Override
    public Map<String, Object> getScope() {
        Map<String, Object> map = new HashMap<>();

        map.put("groupId", groupId);
        map.put("artifactId", artifactId);
        map.put("version", getVersion());
        map.put("description", this.openAPI.getInfo().getDescription());
        map.put("openAPI", openAPI);
//        map.put("namespace", getNamespace());
//        map.put("groupNamespace", this.getGroupNamespace());

        map.put("apis", this.getApis());
        map.put("config", this);

        return map;
    }

    @Override
    public String getClassBySchema(Schema<?> schema) {
        String baseClassname = getBaseClassBySchema(schema);
        if (baseClassname != null) return baseClassname;

        // 模板类
        if (schema instanceof ArraySchema) {
            return getClassBySchema(getTemplateSchema(schema)) + "[]";
        } else if (schema instanceof ComposedSchema) {
            List<Schema> list3 = ((ComposedSchema) schema).getAllOf();
            if (list3 != null && list3.size() > 0 && list3.get(0).get$ref().endsWith("Page")) {
                return "Page<" + getClassBySchema(getTemplateSchema(schema)) + ">";
            } else {
                System.out.println("Warning: getClassBySchema not supported." + schema);
            }
        } else if (schema.get$ref() != null) {
            int pos = schema.get$ref().lastIndexOf("/");
            return schema.get$ref().substring(pos + 1);
        }

        System.out.println("Warning: getClassBySchema not supported." + schema);
        return null;
    }

    @Override
    public List<String> getImportsBySchema(Schema<?> schema) {
        List<String> rt = new ArrayList<>();
        String classname = getBaseClassBySchema(schema);
        if (classname != null) return rt;

        if (schema instanceof ArraySchema) {
            rt.addAll(getImportsBySchema(((ArraySchema) schema).getItems()));
        } else if (schema instanceof ComposedSchema) {
            List<Schema> listAllOf = ((ComposedSchema) schema).getAllOf();
            List<?> listOneOf = ((ComposedSchema) schema).getOneOf(); // 不允许有不确定类型
            List<?> listAnyOf = ((ComposedSchema) schema).getAnyOf(); // 不允许有不确定类型
            if (listAllOf == null) {
                return rt;
            } else if (listOneOf != null || listAnyOf != null) {
                System.out.println("Warning OneOf&AnyOf not supported.");
                return rt;
            }

            for (Schema<?> item : listAllOf) {
                if (item.get$ref() != null && item.get$ref().endsWith("Page")) {
                    rt.add(String.format("import Page from \"../../common/libs/page\""));
                }
            }
        } else if (schema instanceof ObjectSchema) {
            for (Map.Entry<String, Schema> entryProperty : schema.getProperties().entrySet()) {
                rt.addAll(getImportsBySchema(entryProperty.getValue()));
            }
        } else if (schema.get$ref() != null) {
            String tmp = getClassBySchema(schema);
            rt.add(String.format("import %s from \"../entity/%s\"", tmp, tmp));
        }

        return rt;
    }

    @Override
    public String getBaseClassBySchema(Schema<?> schema) {
        if (schema instanceof IntegerSchema) {
            return "number";
        } else if (schema.getType() == null) {
            return null;
        }

        switch (schema.getType()) {
            case "boolean":
                return "boolean";
            case "string":
                return "string";
            case "integer":
            case "number":
                return "number";
            default:
                return null;
        }
    }
}
