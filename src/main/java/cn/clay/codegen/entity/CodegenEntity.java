package cn.clay.codegen.entity;

import cn.clay.codegen.lib.Helper;
import io.swagger.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO：1.ClassName2.Namespace 3.文件路径
 * TODO：
 *  1. components.schemas 对应 Entity
 *  2. components.responses 是否需要表示
 *  3. components.parameters 是否需要表示
 *  4. components.examples 是否需要表示
 *  5. components.requestBodies 是否需要表示
 *  6. components.headers 是否需要表示
 *  7. components.securitySchemes
 *  8. components.links
 *  9. components.callbacks
 */
public class CodegenEntity {
    public Schema<?> schema;

    // 基本属性，以后有需要在拓展
    public String name;
    public String title;
    public String description;

    List<CodegenProperty> properties = null; // 按需加载

    public CodegenEntity(Schema<?> schema) {
        this.schema = schema;

        this.name = schema.getName();
        this.title = schema.getTitle();
        this.description = schema.getDescription();
    }

    public CodegenEntity(Schema<?> schema, String name) {
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
