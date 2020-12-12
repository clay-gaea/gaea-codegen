package cn.clay.codegen.entity;

import io.swagger.oas.models.media.Schema;

public class CodegenProperty {
    public Schema<?> schema;

    public String name;
    public String title;
    public String description;

    public CodegenProperty(Schema<?> schema) {
        this.schema = schema;

        this.name = schema.getName();
        this.title = schema.getTitle();
        this.description = schema.getDescription();
    }

    public CodegenProperty(String name, Schema<?> schema) {
        this.name = name;
        this.schema = schema;

        this.title = schema.getTitle();
        this.description = schema.getDescription();
    }

    @Override
    public String toString() {
        return "CodegenProperty{" +
                "schema=" + schema +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
