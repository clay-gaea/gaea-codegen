package cn.clay.codegen.entity;

import cn.clay.codegen.Helper;
import io.swagger.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * todo：1.ClassName2.Namespace 3.文件路径
 */
public class CodegenModel extends CodegenEntity {
    public Schema<?> schema;

    // 基本属性，以后有需要在拓展
    public String name;
    public String title;
    public String description;

    List<CodegenProperty> properties = null; // 按需加载

    public CodegenModel(Schema<?> schema) {
        this.schema = schema;

        this.name = schema.getName();
        this.title = schema.getTitle();
        this.description = schema.getDescription();
    }

    public CodegenModel(Schema<?> schema, String name) {
        this.schema = schema;

        this.name = schema.getName();
        this.title = schema.getTitle();
        this.description = schema.getDescription();

        this.name = name;
    }

    public Schema<?> getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<CodegenProperty> getProperties() {
        if (properties == null) {
            properties = new ArrayList<>();

            for (Map.Entry<String, Schema> propertyEntry : schema.getProperties().entrySet()) {
                properties.add(new CodegenProperty(propertyEntry.getKey(), propertyEntry.getValue()));
            }
        }
        return properties;
    }

    public String getClassname() {
        return Helper.camelize(name, false);
    }

    public String getParamName() {
        return Helper.camelize(name, true);
    }

    @Override
    public String toString() {
        return "CodegenModel{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", properties=" + properties +
                '}';
    }
}
