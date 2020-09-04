package cn.clay.codegen.lib;

import cn.clay.codegen.entity.TemplateFile;
import cn.clay.codegen.entity.*;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.media.*;
import org.thymeleaf.util.StringUtils;

import javax.validation.constraints.Null;
import java.io.File;
import java.util.*;

public abstract class CodegenConfigJava extends CodegenConfig {

    public CodegenConfigJava(OpenAPI openAPI) {
        super(openAPI);
    }

    @Override
    public Map<String, Object> getScope() {
        Map<String, Object> map = new HashMap<>();

        map.put("groupId", groupId);
        map.put("artifactId", artifactId);
        map.put("version", getVersion());
        map.put("description", this.openAPI.getInfo().getDescription());
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

    @Override
    public String getClassBySchema(Schema<?> schema) {
        String baseClassname = getBaseClassBySchema(schema);
        if (baseClassname != null) return baseClassname;

        // 模板类
        if (schema instanceof ArraySchema) {
            return "List<" + getClassBySchema(getTemplateSchema(schema)) + ">";
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
    public String getBaseClassBySchema(Schema<?> schema) {
        if (schema instanceof IntegerSchema) {
            return "Long";
        } else if (schema.getType() == null) {
            return null;
        }

        switch (schema.getType()) {
            case "boolean":
                return "Boolean";
            case "string":
                return "String";
            case "integer":
                return "Long";
            case "number":
                if (schema.getFormat().equals("float")) {
                    return "Float";
                } else if (schema.getFormat().equals("double")) {
                    return "Double";
                }
            default:
                return null;
        }
    }

    @Override
    public Schema<?> getTemplateSchema(Schema<?> schema) {
        if (schema instanceof ComposedSchema) {
            List<Schema> list3 = ((ComposedSchema) schema).getAllOf();
            if (list3 != null && list3.get(0).get$ref() != null && list3.get(0).get$ref().endsWith("Page")) {
                Schema<?> listSchema = (Schema<?>) list3.get(list3.size() - 1).getProperties().get("list");
                if (listSchema instanceof ArraySchema)
                    return ((ArraySchema) listSchema).getItems();
            } else {
                System.out.println("Waring: getTemplateSchema" + schema);
            }
        } else if (schema instanceof ArraySchema) {
            return ((ArraySchema) schema).getItems();
        }

        return null;
    }

    @Override
    public List<String> getImportsBySchema(Schema<?> schema) {
        List<String> rt = new ArrayList<>();
        String classname = getBaseClassBySchema(schema);
        if (classname != null) return rt;

        if (schema instanceof ArraySchema) {
            rt.add("java.util.List");
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
                    rt.add(groupId + ".common.lib.Page");
                }
            }
        } else if (schema instanceof ObjectSchema) {
            rt.add("java.util.Map");
            for (Map.Entry<String, Schema> entryProperty : schema.getProperties().entrySet()) {
                rt.addAll(getImportsBySchema(entryProperty.getValue()));
            }
        } else if (schema.get$ref() != null) {
            rt.add(groupId + "." + StringUtils.replace(artifactId, "-", "_") + ".entity." + getClassBySchema(schema));
        }

        return rt;
    }

    @Override
    public String getApiFilePath(TemplateFile templateFile, CodegenApi api) {
        return getOutputDir() + "/src/main/java/" + StringUtils.replace(groupId, ".", File.separator) + File.separator + StringUtils.replace(artifactId, "-", "_") + File.separator + templateFile.getPrefix() + templateFile.getOutput() + api.getClassname() + templateFile.getSuffix();
    }

    @Override
    public String getModelFilePath(TemplateFile templateFile, CodegenModel model) {
        return getOutputDir() + "/src/main/java/" + StringUtils.replace(groupId, ".", File.separator) + File.separator + StringUtils.replace(artifactId, "-", "_") + File.separator + templateFile.getPrefix() + templateFile.getOutput() + model.getClassname() + templateFile.getSuffix();
    }
}
