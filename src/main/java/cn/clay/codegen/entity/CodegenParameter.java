package cn.clay.codegen.entity;

import cn.clay.codegen.lib.Helper;
import io.swagger.oas.models.media.MediaType;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;

import java.util.Map;

public class CodegenParameter {

    public String in; // query path body
    public String name;
    public Boolean required;
    public String description;
    public String defaultVal;

    public Parameter parameter;
    public RequestBody requestBody;
    public Schema<?> schema;

    public CodegenParameter(Parameter parameter) {
        this.parameter = parameter;

        this.in = parameter.getIn();
        this.name = parameter.getName();
        this.description = parameter.getDescription();
        this.required = parameter.getRequired();

        this.schema = parameter.getSchema();

        // path、query
        if (this.schema != null) {
            this.defaultVal = this.schema.getDefault() != null ? this.schema.getDefault().toString() : null;
        } else {
            System.out.println("Warning: CodegenParameter schema is null." + parameter);
        }
    }

    public CodegenParameter(RequestBody requestBody) {
        this.requestBody = requestBody;

        this.in = "body";
        this.name = "";
        this.description = requestBody.getDescription();
        this.required = requestBody.getRequired();

        for (Map.Entry<String, MediaType> mediaTypeEntry : requestBody.getContent().entrySet()) {
            if (mediaTypeEntry.getKey().equals("application/json")) {
                this.schema = mediaTypeEntry.getValue().getSchema();
                break;
            }
        }

        if (this.schema != null && this.schema.get$ref() != null) {
            int pos = this.schema.get$ref().lastIndexOf("/");
            this.name = Helper.camelize(this.schema.get$ref().substring(pos + 1), true);
            // 说明：body定义即视为默认必填
        } else {
            System.out.println("Warning: CodegenParameter schema is null." + requestBody);
        }
    }

    // thymeleaf 用 parameter.in 错误；用 parameter.getIn() 则正确
    public String getIn() {
        return in;
    }
}
