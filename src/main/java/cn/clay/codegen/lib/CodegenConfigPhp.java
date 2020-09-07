package cn.clay.codegen.lib;

import cn.clay.codegen.Helper;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.media.*;

import java.util.*;

public abstract class CodegenConfigPhp extends CodegenConfig {
    public CodegenConfigPhp(OpenAPI openAPI) {
        super(openAPI);

        this.artifactId = this.openAPI.getInfo().getTitle().toLowerCase();
    }

    public String getNamespace() {
        return this.getGroupNamespace() + "\\" + Helper.camelize(this.artifactId, false);
    }

    @Override
    public Map<String, Object> getScope() {
        Map<String, Object> map = new HashMap<>();

        map.put("groupId", groupId);
        map.put("artifactId", artifactId);
        map.put("version", getVersion());
        map.put("description", this.openAPI.getInfo().getDescription());
        map.put("namespace", getNamespace());
        map.put("openAPI", openAPI);
        map.put("groupNamespace", this.getGroupNamespace());

        map.put("apis", this.getApis());
        map.put("config", this);

        return map;
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
                    rt.add(getGroupNamespace() + "\\Common\\Libs\\Page");
                }
            }
        } else if (schema instanceof ObjectSchema) {
            for (Map.Entry<String, Schema> entryProperty : schema.getProperties().entrySet()) {
                rt.addAll(getImportsBySchema(entryProperty.getValue()));
            }
        } else if (schema.get$ref() != null) {
            rt.add(getNamespace() + "\\Entity\\" + getClassBySchema(schema));
        }

        return rt;
    }

    @Override
    public String getBaseClassBySchema(Schema<?> schema) {
        if (schema instanceof IntegerSchema) {
            return "int";
        } else if (schema.getType() == null) {
            return null;
        }

        switch (schema.getType()) {
            case "boolean":
                return "bool";
            case "string":
                return "string";
            case "integer":
                return "int";
            case "number":
                return schema.getFormat().equals("float") || schema.getFormat().equals("double") ? "float" : "int";
            case "array":
                return null;
            default:
                System.out.println("Warning: getBaseClassBySchema " + schema.getType());
                return null;
        }
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
                return "Page";
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
}
