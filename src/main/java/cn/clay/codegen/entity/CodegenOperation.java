package cn.clay.codegen.entity;

import cn.clay.codegen.Helper;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.media.MediaType;
import io.swagger.oas.models.media.ObjectSchema;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.parameters.Parameter;
import io.swagger.oas.models.parameters.RequestBody;

import java.util.*;
import java.util.stream.Collectors;

public class CodegenOperation {

    public Operation operation;

    public String path;
    public PathItem.HttpMethod httpMethod;
    public String operationId;
    public String summary;
    public String description;

    public CodegenParameter returnParameter;

    List<CodegenParameter> parameters;

    public CodegenOperation(String path, PathItem.HttpMethod httpMethod, Operation operation) {
        this.operation = operation;

        this.path = path;
        this.httpMethod = httpMethod;
        this.operationId = operation.getOperationId();
        this.summary = operation.getSummary();
        this.description = operation.getDescription();

        this.returnParameter = new CodegenParameter(operation.getResponses());
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
                Schema<?> tmpSchema = null;
                for (Map.Entry<String, MediaType> mediaTypeEntry : requestBody.getContent().entrySet()) {
                    if (mediaTypeEntry.getKey().equals("application/json")) {
                        tmpSchema = mediaTypeEntry.getValue().getSchema();
                        break;
                    }
                }

                if (tmpSchema != null && tmpSchema.get$ref() != null) {
                    int pos = tmpSchema.get$ref().lastIndexOf("/");
                    String name = Helper.camelize(tmpSchema.get$ref().substring(pos + 1), true);
                    parameters.add(new CodegenParameter(
                            requestBody, tmpSchema, "body", name,
                            requestBody.getRequired() == null || requestBody.getRequired(),// 如果没有明确指定为 false，则理解是比填项
                            requestBody.getDescription()
                    ));
                } else if (tmpSchema instanceof ObjectSchema) {
                    for (Map.Entry<String, Schema> propertyEntry : tmpSchema.getProperties().entrySet()) {
                        String name = propertyEntry.getKey();
                        Schema<?> propertySchema = propertyEntry.getValue();
                        Boolean required = (
                                requestBody.getRequired() == null || requestBody.getRequired() // 如果没有明确指定为 false，则理解是比填项
                        ) && tmpSchema.getRequired() != null && tmpSchema.getRequired().contains(name);
                        parameters.add(new CodegenParameter(
                                requestBody, propertySchema,
                                "bodyProperty", name, required, propertySchema.getDescription()
                        ));
                    }
                }
            }

            if (parameters.size() > 5) {
                System.out.println("Warning: parameters of operation[" + operationId + "] must be less than or equal to five.");
                parameters.clear();
                return parameters;
            }

            parameters.sort(new Comparator<CodegenParameter>() {
                @Override
                public int compare(CodegenParameter p1, CodegenParameter p2) {
                    // 参数名称：名称为 id 前置
                    if (p1.name.equals("id")) return -1;
                    if (p2.name.equals("id")) return 1;

                    // 默认值：有默认值放在后面
                    if (p1.haveDefault() ^ p2.haveDefault()) {
                        return p1.haveDefault() ? -1 : 1;
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

    public Boolean haveBodyParameter() {
        for (CodegenParameter parameter : getParameters()) {
            if (parameter.getIn().equals("body")) {
                return true;
            }
        }

        return false;
    }

    public Boolean haveBodyPropertyParameter() {
        for (CodegenParameter parameter : getParameters()) {
            if (parameter.getIn().equals("bodyProperty")) {
                return true;
            }
        }

        return false;
    }

    public Boolean haveQueryParameter() {
        return !getQueryParameters().isEmpty();
    }

    public List<CodegenParameter> getQueryParameters() {
        return getParameters().stream().filter(parameter -> parameter.getIn().equals("query")).collect(Collectors.toList());
    }

    public List<CodegenParameter> getBodyParameters() {
        return getParameters().stream().filter(parameter -> parameter.getIn().startsWith("body")).collect(Collectors.toList());
    }
}
