package cn.clay.codegen.lib;

import cn.clay.codegen.Helper;
import cn.clay.codegen.entity.TemplateFile;
import cn.clay.codegen.entity.*;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.media.ArraySchema;
import io.swagger.oas.models.media.ComposedSchema;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.tags.Tag;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public abstract class CodegenConfig {
    protected OpenAPI openAPI;

    // groupId artifactId
    protected String groupId = "com.clay";
    protected String artifactId = "";
    private String outputDir = "";

    List<CodegenApi> apis = null;
    List<CodegenModel> models = null;

    /**
     * 模板使用方式
     * 1.对单个文件解析
     * 2.对 Components 遍历解析
     * 3.对 Tags 遍历解析
     */
    protected List<TemplateFile> templateFiles = new ArrayList<>();
    protected List<TemplateFile> apiTemplateFiles = new ArrayList<>();
    protected List<TemplateFile> modelTemplateFiles = new ArrayList<>();

    public CodegenConfig(OpenAPI openAPI) {
        this.openAPI = openAPI;

        setOptions();
    }

    /**
     * option 配置标识
     */
    public abstract String getKey();

    /**
     * option 配置描述
     */
    public abstract String getDescription();

    /**
     * option 模板文件位置
     */
    public abstract String getTemplateDir();

    /**
     * option 生成目录包
     */
    public String getOutputDir() {
        return this.outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    abstract public Map<String, Object> getScope();

    public Map<String, Object> getApiScope(CodegenApi api) {
        Map<String, Object> scope = this.getScope();
        scope.put("api", api);
        return scope;
    }

    public Map<String, Object> getModelScope(CodegenModel model) {
        Map<String, Object> scope = this.getScope();
        scope.put("model", model);
        return scope;
    }

    protected void validate() {
        // Spec 格式类型校验
    }

    protected String getVersion() {
        return "1.0.0";
    }

    // 初始化配置
    protected void setOptions() {
        /**
         * 初始化 groupId，artifactId
         */
//        this.groupId = ""
        this.artifactId = this.openAPI.getInfo().getTitle().toLowerCase();
    }

    public List<TemplateFile> getTemplateFiles() {
        return templateFiles;
    }

    public List<TemplateFile> getApiTemplateFiles() {
        return apiTemplateFiles;
    }

    public List<TemplateFile> getModelTemplateFiles() {
        return modelTemplateFiles;
    }

    public boolean haveDefaultTags() {
        if (this.openAPI == null) return false;
        List<Tag> listTag = this.openAPI.getTags();
        for (Map.Entry<String, PathItem> next : this.openAPI.getPaths().entrySet()) {
            String key = next.getKey();
            PathItem pathItem = next.getValue();
            for (Operation operation : pathItem.readOperations()) {
                if (operation.getTags().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getGroupNamespace() {
        return "Com\\Clay";
    }

    public String getFilePath(TemplateFile templateFile) {
        return getOutputDir() + File.separator + File.separator + templateFile.getOutput();
    }

    public String getApiFilePath(TemplateFile templateFile, CodegenApi api) {
        return getOutputDir() + File.separator + templateFile.getPrefix() + templateFile.getOutput() + api.getClassname() + templateFile.getSuffix();
    }

    public String getModelFilePath(TemplateFile templateFile, CodegenModel model) {
        return getOutputDir() + File.separator + templateFile.getPrefix() + templateFile.getOutput() + model.getClassname() + templateFile.getSuffix();
    }

    final public String getTemplatePath(TemplateFile templateFile) {
        return getTemplateDir() + File.separator + templateFile.getTemplate();
    }

    public List<CodegenApi> getApis() {
        if (apis == null) {
            apis = new ArrayList<>();

            Map<String, List<CodegenOperation>> operationMap = new HashMap<>(); // tag2Operations
            for (Map.Entry<String, PathItem> pathItemEntry : openAPI.getPaths().entrySet()) {
                String path = pathItemEntry.getKey();
                for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : pathItemEntry.getValue().readOperationsMap().entrySet()) {
                    PathItem.HttpMethod method = operationEntry.getKey();
                    Operation operation = operationEntry.getValue();
                    String tagName = operation.getTags() == null || operation.getTags().isEmpty() ? Helper.defaultTag().getName() : operation.getTags().get(0);
                    List<CodegenOperation> operates = operationMap.computeIfAbsent(tagName, k -> new ArrayList<>());
                    operates.add(new CodegenOperation(path, method, operation));
                }
            }

            for (Tag tag : openAPI.getTags()) {
                String tagName = tag.getName();
                if (!operationMap.containsKey(tagName)) continue;
                apis.add(new CodegenApi(tag, operationMap.get(tagName)));
            }

            Tag defaultTag = Helper.defaultTag();
            if (operationMap.containsKey(defaultTag.getName())) {
                apis.add(new CodegenApi(defaultTag, operationMap.get(defaultTag.getName())));
            }
        }
        return apis;
    }

    public List<CodegenModel> getModels() {
        if (models == null) {
            models = new ArrayList<>();

            for (Map.Entry<String, Schema> schemaEntry : openAPI.getComponents().getSchemas().entrySet()) {
                if (schemaEntry.getKey().equals("Page")) continue;
                models.add(new CodegenModel(schemaEntry.getValue(), schemaEntry.getKey()));
            }
        }

        return models;
    }

    /**
     * 通过 Schema 获取类型
     *
     * @param schema Schema
     * @return String
     */
    abstract public String getClassBySchema(Schema<?> schema);

    /**
     * 获取模板类型的模板 Schema，如果非模板类则返回 null
     *
     * @param schema Schema
     * @return Schema|null
     */
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

    public String getTemplateClass(Schema<?> schema) {
        Schema<?> templateSchema = getTemplateSchema(schema);
        if (templateSchema != null) {
            return getClassBySchema(templateSchema);
        }

        return "";
    }

    /**
     * 获取 imports 函数
     */
    public List<String> getImportsByModel(CodegenModel model) {
        Set<String> rt = new HashSet<>();

        for (CodegenProperty property : model.getProperties()) {
            rt.addAll(getImportsBySchema(property.schema));
        }

        return rt.parallelStream()
                .filter(item -> !item.isEmpty() && !item.contains(StringUtils.replace(artifactId, "-", "_")))
                .collect(Collectors.toList());
    }

    public List<String> getImportsByApi(CodegenApi api) {
        Set<String> rt = new HashSet<>();
        for (CodegenOperation operation : api.operations) {
            for (CodegenParameter parameter : operation.getParameters()) {
                rt.addAll(getImportsBySchema(parameter.schema));
            }

            rt.addAll(getImportsBySchema(operation.returnParameter.schema));
        }

        return rt.parallelStream().filter(item -> !item.isEmpty()).collect(Collectors.toList());
    }

    /**
     * 通过 Schema 获取需要依赖的对象
     *
     * @param schema Schema
     * @return List<String>
     */
    abstract public List<String> getImportsBySchema(Schema<?> schema);

    /**
     * 通过 Schema 获取基础类型（数组、对象、$ref非基础类型）
     *
     * @param schema Schema
     * @return String|NULL
     */
    abstract public String getBaseClassBySchema(Schema<?> schema);

    public String getParameterDefaultVal(CodegenParameter parameter) {
        if (parameter.schema.getDefault() != null) {
            return parameter.schema.getDefault().toString();
        } else if (parameter.requestBody != null && parameter.required != null && !parameter.required) {
            return "null";
        } else {
            return "";
        }
    }
}
