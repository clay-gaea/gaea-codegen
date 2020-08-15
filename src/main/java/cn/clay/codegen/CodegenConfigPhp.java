package cn.clay.codegen;

import cn.clay.codegen.entity.*;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.google.common.collect.Collections2;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.ComposedSchema;
import io.swagger.oas.models.media.MediaType;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;

import java.security.cert.Certificate;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    public String getSchemaClassName(Schema<?> schema) {

        String $ref = schema.get$ref();
        if ($ref != null) {
            int lastIndex = $ref.lastIndexOf('/');
            return $ref.substring(lastIndex + 1);
        } else if (schema instanceof ComposedSchema) {
            if (((ComposedSchema) schema).getOneOf() != null) {
                return ((ComposedSchema) schema).getOneOf().stream().map(this::getSchemaClassName).filter(one -> !one.isBlank()).collect(Collectors.joining("|"));
            } else if (((ComposedSchema) schema).getAllOf() != null && !((ComposedSchema) schema).getAllOf().isEmpty()) {
                return getSchemaClassName(((ComposedSchema) schema).getAllOf().get(0));
            } else {
                return "";
            }
        }

        switch (schema.getType()) {
            case "integer":
                return "int";
            case "string":
                return "string";
            case "object":
                return "array";
            case "boolean":
                return "boolean";
        }

        return "";
    }

    public List<String> getSchemaImports(Schema<?> schema) {
        List<String> rtList = new ArrayList<>();
        if (schema.get$ref() != null) {
            rtList.add(this.getNamespace() + "\\Entity\\" + getSchemaClassName(schema));
        } else if (schema instanceof ComposedSchema) {
            for (Schema<?> one : ((ComposedSchema) schema).getOneOf()) {
                rtList.addAll(this.getSchemaImports(one));
            }

            return rtList;
        }

        return rtList;
    }

    /**
     * 获取的实体模型的外部包
     *
     * @param model CodegenModel
     * @return List<String>
     */
    public List<String> getImportsByModel(CodegenModel model) {
        List<String> rt = new ArrayList<>();

        for (CodegenProperty property : model.getProperties()) {
            String ipt = getImportBySchema(property.schema);
            if (!ipt.isEmpty()) rt.add(ipt);
        }

        return rt;
    }

    public List<String> getImportsByApi(CodegenApi api) {
        Set<String> rt = new HashSet<>();
        for (CodegenOperation operation : api.operations) {
            for (CodegenParameter parameter : operation.getParameters()) {
                String ipt = getImportBySchema(parameter.schema);
                if (!ipt.isEmpty()) rt.add(ipt);
            }

            String ipt = getImportBySchema(operation.returnSchema);
            if (!ipt.isEmpty()) rt.add(ipt);
        }

        return new ArrayList<>(rt);
    }

    public String getImportBySchema(Schema<?> schema) {
        Schema<?> schemaTmp = schema;
        if (schema instanceof ComposedSchema) {
            List<Schema> list1 = ((ComposedSchema) schema).getOneOf();
            List<Schema> list2 = ((ComposedSchema) schema).getAnyOf();
            List<Schema> list3 = ((ComposedSchema) schema).getAllOf();

            if (list1 != null) {
                schemaTmp = list1.get(0);
            } else if (list2 != null) {
                schemaTmp = list2.get(0);
            } else if (list3 != null) {
                schemaTmp = list3.get(0);
            } else {
                return "";
            }
        } else if (schema instanceof ArraySchema) {
            schemaTmp = ((ArraySchema) schema).getItems();
        }

        if (schemaTmp.get$ref() == null) {
            return "";
        }

        String baseType = getBaseType(schemaTmp);
        if (baseType.equals("Page")) {
            return getGroupNamespace() + "\\Common\\Libs\\Page";
        }
        return getNamespace() + "\\Entity\\" + getBaseType(schemaTmp);
    }

    /**
     * 获取属性的类型字符串
     *
     * @param schema Schema<?>
     * @return String
     */
    public String getTypeBySchema(Schema<?> schema) {
        Schema<?> tmpSchema = schema;
        if (schema instanceof ComposedSchema) {
            /*
             * oneof/anyof 只取第一个返回
             * allof 只取第一个返回
             */
            List<Schema> list1 = ((ComposedSchema) schema).getOneOf();
            List<Schema> list2 = ((ComposedSchema) schema).getAnyOf();
            List<Schema> list3 = ((ComposedSchema) schema).getAllOf();

            if (list1 != null) {
                tmpSchema = list1.get(0);
            } else if (list2 != null) {
                tmpSchema = list2.get(0);
            } else if (list3 != null) {
                tmpSchema = list3.get(0);
            } else {
                System.out.println("Warning " + schema);
                return "";
            }
        } else if (schema instanceof ArraySchema) {
            tmpSchema = ((ArraySchema) schema).getItems();
        }

        String type = getBaseType(tmpSchema);
        if (schema instanceof ArraySchema) {
            return type + "[]";
        } else {
            return type;
        }
    }

    public String getBaseTypeBySchema(Schema<?> schema) {
        Schema<?> baseSchema = getBaseSchema(schema);
        return baseSchema != null ? getBaseType(baseSchema) : "";
    }

    public Schema<?> getBaseSchema(Schema<?> schema) {
        if (schema instanceof ComposedSchema) {
            /*
             * oneof/anyof 只取第一个返回
             * allof 只取第一个返回
             */
            List<Schema> list1 = ((ComposedSchema) schema).getOneOf();
            List<Schema> list2 = ((ComposedSchema) schema).getAnyOf();
            List<Schema> list3 = ((ComposedSchema) schema).getAllOf();

            if (list1 != null) {
                return list1.get(0);
            } else if (list2 != null) {
                return list2.get(0);
            } else if (
                    list3 != null
                            && list3.get(list3.size() - 1).getProperties() != null
                            && list3.get(list3.size() - 1).getProperties().containsKey("list")
            ) {
                return getBaseSchema((Schema<?>) list3.get(list3.size() - 1).getProperties().get("list"));
            } else {
                System.out.println("Warning: getBaseSchema " + schema);
                return null;
            }
        } else if (schema instanceof ArraySchema) {
            return ((ArraySchema) schema).getItems();
        } else {
            return schema;
        }
    }

    /**
     * 基础类型转化
     *
     * @param schema Schema<?>
     * @return String
     */
    public String getBaseType(Schema<?> schema) {
        if (schema.getType() != null) {
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
                    System.out.println("Warning: getBaseType " + schema);
                    return "";
            }
        } else if (schema.get$ref() != null) {
            String $ref = schema.get$ref();
            int pos = $ref.lastIndexOf("/");
            return $ref.substring(pos + 1);
        } else {
            System.out.println("Warning: getBaseType " + schema);
            return "";
        }
    }
}
