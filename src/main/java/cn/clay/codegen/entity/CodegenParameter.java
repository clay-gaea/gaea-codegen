package cn.clay.codegen.entity;

import cn.clay.codegen.Helper;
import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.MediaType;
import io.swagger.oas.models.media.ObjectSchema;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;
import io.swagger.oas.models.responses.ApiResponse;
import io.swagger.oas.models.responses.ApiResponses;
import v2.io.swagger.models.Response;

import java.lang.reflect.Array;
import java.util.Map;

public class CodegenParameter {

    public String in; // query path body
    public String name;
    public Boolean required;
    public String description;
    public String defaultVal;

    public Parameter parameter;
    public RequestBody requestBody;
    public ApiResponses apiResponses;
    public Schema<?> schema;
    public Boolean isArray;
    public Boolean isObject;

    /**
     * 目前仅支持 query 参数，每个参数映射一个变量
     */
    public CodegenParameter(Parameter parameter) {
        this.parameter = parameter;

        this.init(parameter.getSchema(), parameter.getIn(), parameter.getName(), parameter.getRequired(), parameter.getDescription());

        if (this.schema != null) {
            this.defaultVal = this.schema.getDefault() != null ? this.schema.getDefault().toString() : null;
        }

//        this.in = parameter.getIn();
//        this.name = parameter.getName();
//        this.description = parameter.getDescription();
//        this.required = parameter.getRequired();
//
//        this.schema = parameter.getSchema();
//
//        if (this.schema instanceof ArraySchema) {
//            this.isArray = true;
//        } else if (this.schema instanceof ObjectSchema) {
//            this.isObject = true;
//        }
//
//        // path、query
//        if (this.schema != null) {
//            this.defaultVal = this.schema.getDefault() != null ? this.schema.getDefault().toString() : null;
//        } else {
//            System.out.println("Warning: CodegenParameter schema is null. " + parameter);
//        }
    }

    /**
     * RequestBody 转换为参数说明：
     * 1. 如果是 $refs 引用，直接转化一个参数即可，RequestBody.required 确定是否是必填
     * 2. 如果是 allOf，转化为模板对象（举例 Page，已 list 属性为 T 类）
     * 3. 如果是 object，则根据 properties 生成对应的参数
     */
    public CodegenParameter(RequestBody requestBody, Schema<?> schema, String in, String name, Boolean required, String description) {
        this.requestBody = requestBody;
        this.init(schema, in, name, required, description);
    }

    public CodegenParameter(ApiResponses apiResponses) {
        this.apiResponses = apiResponses;

        if (apiResponses != null) {
            // 说明：必须有200返回（正确返回）,JSON返回
            for (Map.Entry<String, ApiResponse> apiResponseEntry : apiResponses.entrySet()) {
                if (apiResponseEntry.getKey().equals("200")) {
                    for (Map.Entry<String, MediaType> mediaTypeEntry : apiResponseEntry.getValue().getContent().entrySet()) {
                        if (mediaTypeEntry.getKey().equals("application/json")) {
                            this.init(mediaTypeEntry.getValue().getSchema(), "response", "", true, apiResponseEntry.getValue().getDescription());
                        }
                    }
                }
            }
        }
    }

    protected void init(Schema<?> schema, String in, String name, Boolean required, String description) {
        this.schema = schema;
        this.in = in;
        this.name = name;
        this.required = required;
        this.description = description;

        this.isArray = (this.schema instanceof ArraySchema);
        this.isObject = (this.schema instanceof ObjectSchema);

        if (this.schema == null) {
            System.out.println("Warning: CodegenParameter[" + name + "] schema is null.");
        }
    }

    // thymeleaf 用 parameter.in 错误；用 parameter.getIn() 则正确
    public String getIn() {
        return in;
    }
}
