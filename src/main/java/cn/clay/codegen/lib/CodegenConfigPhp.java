package cn.clay.codegen.lib;

import cn.clay.codegen.Helper;
import cn.clay.codegen.entity.*;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.ComposedSchema;
import io.swagger.oas.models.media.Schema;

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
    public Map<String, Object> getApiScope(CodegenApi api) {
        Map<String, Object> map = this.getScope();
        map.put("api", api);
        return map;
    }

    @Override
    public Map<String, Object> getModelScope(CodegenModel model) {
        Map<String, Object> map = this.getScope();
        map.put("model", model);
        return map;
    }

    public List<String> getImportsBySchema(Schema<?> schema) {
        List<String> rt = new ArrayList<>();
        if (schema.getType() != null) { // 基础类型
            return rt;
        }

        String baseClass = getBaseClassBySchema(schema);
        if (baseClass.equals("Page")) {
            rt.add(getGroupNamespace() + "\\Common\\Libs\\Page");
        } else {
            rt.add(getNamespace() + "\\Entity\\" + baseClass);
        }
        return rt;
    }

    /**
     * 获取模板类基本 class 类型
     */
    public String getTemplateClassBySchema(Schema<?> schema) {
        String baseClass = getBaseClassBySchema(schema);
        if (baseClass.equals("Page") && schema instanceof ComposedSchema) {
            List<Schema> list3 = ((ComposedSchema) schema).getAllOf();
            if (list3 != null && list3.get(list3.size() - 1).getProperties() != null && list3.get(list3.size() - 1).getProperties().containsKey("list")) {
                return getBaseClassBySchema((Schema<?>) list3.get(list3.size() - 1).getProperties().get("list"));
            }
        }

        return null;
    }

    /**
     * 通过 Schema 获取基础类型
     *
     * @param schema Schema
     * @return String
     */
    public String getBaseClassBySchema(Schema<?> schema) {
        if (schema instanceof ArraySchema) {
            return getBaseClassBySchema(((ArraySchema) schema).getItems());
        } else if (schema instanceof ComposedSchema) {
            /*
             * oneof/anyof 只取第一个返回
             * allof 只取第一个返回
             */
            List<Schema> list1 = ((ComposedSchema) schema).getOneOf();
            List<Schema> list2 = ((ComposedSchema) schema).getAnyOf();
            List<Schema> list3 = ((ComposedSchema) schema).getAllOf();

            if (list1 != null) {
                return getBaseClassBySchema(list1.get(0));
            } else if (list2 != null) {
                return getBaseClassBySchema(list2.get(0));
            } else if (list3 != null) {
                return getBaseClassBySchema(list3.get(0));
            } else {
                System.out.println("Warning: getBaseClassBySchema " + schema);
                return null;
            }
        } else if (schema.get$ref() != null) {
            String $ref = schema.get$ref();
            int pos = $ref.lastIndexOf("/");
            return $ref.substring(pos + 1);
        } else {
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
                case "object":
                default:
                    System.out.println("Warning: getBaseClassBySchema " + schema);
                    return "";
            }
        }
    }

    /**
     * 通过 Schema 获取类型
     *
     * @param schema Schema
     * @return String
     */
    public String getClassBySchema(Schema<?> schema) {
        String baseClass = getBaseClassBySchema(schema);
        return schema instanceof ArraySchema ? baseClass + "[]" : baseClass;
    }
}