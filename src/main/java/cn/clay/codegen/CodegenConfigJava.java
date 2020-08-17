package cn.clay.codegen;

import cn.clay.codegen.entity.CodegenApi;
import cn.clay.codegen.entity.CodegenModel;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.ComposedSchema;
import io.swagger.oas.models.media.Schema;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String baseClass = getBaseClassBySchema(schema);
        return schema instanceof ArraySchema ? "List<" + baseClass + ">" : baseClass;
    }

    @Override
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
            boolean bool = true;
            switch (schema.getType()) {
                case "boolean":
                    return "Boolean";
                case "string":
                    return "String";
                case "integer":
                    return "Integer";
                case "number":
                    if (schema.getFormat().equals("float")) {
                        return "Float";
                    } else if (schema.getFormat().equals("double")) {
                        return "Double";
                    }
                case "array":
                case "object":
                default:
                    System.out.println("Warning: getBaseClassBySchema " + schema);
                    return "";
            }
        }
    }

    @Override
    public String getTemplateClassBySchema(Schema<?> schema) {
        return null;
    }

    @Override
    public String getImportBySchema(Schema<?> schema) {
        return null;
    }

    @Override
    public String getApiFilePath(TemplateFile templateFile, CodegenApi api) {
        return getOutputDir() + "/src/main/java/" + StringUtils.replace(groupId, ".", File.separator) + File.separator + StringUtils.replace(artifactId, "-", "_") + File.separator + templateFile.getPrefix() + templateFile.output + api.getClassname() + templateFile.getSuffix();
    }

    @Override
    public String getModelFilePath(TemplateFile templateFile, CodegenModel model) {
        return getOutputDir() + "/src/main/java/" + StringUtils.replace(groupId, ".", File.separator) + File.separator + StringUtils.replace(artifactId, "-", "_") + File.separator + templateFile.getPrefix() + templateFile.getOutput() + model.getClassname() + templateFile.getSuffix();
    }
}
