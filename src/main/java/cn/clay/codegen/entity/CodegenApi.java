package cn.clay.codegen.entity;

import cn.clay.codegen.lib.Helper;
import io.swagger.oas.models.tags.Tag;

import java.util.List;

public class CodegenApi {
    public Tag tag;

    public String name;
    public String description;

    public List<CodegenOperation> operations;

    public CodegenApi(Tag tag, List<CodegenOperation> operations) {
        this.tag = tag;
        this.operations = operations;

        this.name = tag.getName();
        this.description = tag.getDescription();
    }

    public String getClassname() {
        return Helper.camelize(name, false);
    }

    public String getParamName() {
        return Helper.camelize(name, true);
    }

    @Override
    public String toString() {
        return "CodegenApi{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", operations=" + operations +
                '}';
    }
}
