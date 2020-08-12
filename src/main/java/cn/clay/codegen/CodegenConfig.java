package cn.clay.codegen;

import cn.clay.codegen.entity.*;
import io.swagger.oas.models.OpenAPI;
import io.swagger.oas.models.Operation;
import io.swagger.oas.models.PathItem;
import io.swagger.oas.models.media.Schema;
import io.swagger.oas.models.tags.Tag;

import java.io.File;
import java.util.*;

abstract class CodegenConfig {
    OpenAPI openAPI;

    // groupId artifactId
    String groupId = "com.clay";
    String artifactId = "";
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

    public abstract Map<String, Object> getScope();

    public abstract Map<String, Object> getApiScope(CodegenApi api);

    public abstract Map<String, Object> getModelScope(CodegenModel model);

    // groupId artifactId version

    protected void validate() {
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

//    public List<Tag> getTags() {
//        if (this.openAPI == null) return new ArrayList<>();
//        List<Tag> listTag = this.openAPI.getTags();
//        if (this.haveDefaultTags()) {
//            listTag.add(Helper.defaultTag());
//        }
//        return listTag;
//    }

    public String getGroupNamespace() {
        return "Com\\Clay";
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

    abstract public String getSchemaClassName(Schema<?> schema);

    abstract public List<String> getSchemaImports(Schema<?> schema);

    public String getFilePath(TemplateFile templateFile) {
        return getOutputDir() + File.separator + File.separator + templateFile.getOutput();
    }

    public String getApiFilePath(TemplateFile templateFile, CodegenApi api) {
        return getOutputDir() + File.separator + templateFile.getPrefix() + templateFile.output + api.getClassname() + templateFile.getSuffix();
    }

    public String getModelFilePath(TemplateFile templateFile, CodegenModel model) {
        return getOutputDir() + File.separator + templateFile.getPrefix() + templateFile.getOutput() + model.getClassname() + templateFile.getSuffix();
    }

    final public String getTemplatePath(TemplateFile templateFile) {
        return getTemplateDir() + File.separator + templateFile.getTemplate();
    }
}
