package cn.clay.codegen.entity;

import cn.clay.codegen.Helper;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.MediaType;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;
import io.swagger.oas.models.responses.ApiResponse;

import java.util.*;

public class CodegenOperation {

    public Operation operation;

    public String path;
    public PathItem.HttpMethod httpMethod;
    public String operationId;
    public String summary;
    public String description;

    public Schema<?> returnSchema;
    public String returnDescription;
    public boolean returnIsArray;

//    public List<CodegenParameter> bodyParams = new ArrayList<>();
//    public List<CodegenParameter> pathParams = new ArrayList<>();
//    public List<CodegenParameter> queryParams = new ArrayList<>();

    List<CodegenParameter> parameters;

    public CodegenOperation(String path, PathItem.HttpMethod httpMethod, Operation operation) {
        this.operation = operation;

        this.path = path;
        this.httpMethod = httpMethod;
        this.operationId = operation.getOperationId();
        this.summary = operation.getSummary();
        this.description = operation.getDescription();

        // 说明：必须有200返回（正确返回）,JSON返回
        for (Map.Entry<String, ApiResponse> apiResponseEntry : operation.getResponses().entrySet()) {
            if (apiResponseEntry.getKey().equals("200")) {
                this.returnDescription = apiResponseEntry.getValue().getDescription();
                for (Map.Entry<String, MediaType> mediaTypeEntry : apiResponseEntry.getValue().getContent().entrySet()) {
                    if (mediaTypeEntry.getKey().equals("application/json")) {
                        this.returnSchema = mediaTypeEntry.getValue().getSchema();
                        this.returnIsArray = (this.returnSchema instanceof ArraySchema);
                    }
                }
            }
        }

        if (this.returnSchema == null) {
            System.out.println("Warning: " + this.operationId + " return not defined");
        }
    }

    public List<CodegenParameter> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<>();

            if (operation.getParameters() != null) {
                for (Parameter parameter : operation.getParameters()) {
                    parameters.add(new CodegenParameter(parameter));
                }
            }

            RequestBody requestBody = operation.getRequestBody();
            if (requestBody != null) {
                parameters.add(new CodegenParameter(requestBody));
            }

            parameters.sort(new Comparator<CodegenParameter>() {
                @Override
                public int compare(CodegenParameter p1, CodegenParameter p2) {
                    // 参数名称：名称为 id 前置
                    if (p1.name.equals("id")) return -1;
                    if (p2.name.equals("id")) return 1;

                    // 默认值：有默认值放在后面
                    if (p1.defaultVal == null ^ p2.defaultVal == null) {
                        return p1.defaultVal == null ? -1 : 1;
                    }

                    // 参数类型：path < query < body
                    if (p1.in.equals(p2.in)) {
                        return 0;
                    } else if (p1.in.equals("path")) {
                        return 1;
                    } else if (p1.in.equals("query")) {
                        return p2.in.equals("path") ? 1 : 0;
                    } else {
                        return -1;
                    }
                }
            });
        }

        return parameters;
    }

    @Override
    public String toString() {
        return "CodegenOperation{" +
                "path='" + path + '\'' +
                ", httpMethod=" + httpMethod +
                ", operationId='" + operationId + '\'' +
                ", summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
