package cn.clay.codegen.lib;

/**
 * 功能介绍：定位 - 命令函/服务传参
 * 1. 模板映射说明，设计属性有templateFiles，templateApis，templateEntities
 * 2. 配置加载，需要支持配置如下：groupId，outputDir、callback（上传私仓等操作）
 * 3. 回调 callback 仅支持服务调用，细节待定，可以考虑服务调用、shell脚本定义
 * 4. 定义 key
 * 5. 可以考虑，配置和定义对象拆分
 */
public abstract class CodegenParams {

    // groupId 属于全局配置；artifactId 则是目标属性
    protected String groupId = "cn.clay";
    protected String artifactId = "oas";
    protected String oasFile = "./oas.yaml"; // 设置默认值
    protected String outputDir = "./";
    protected String codegenKey = ""; // 必填项

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getOasFile() {
        return oasFile;
    }

    public void setOasFile(String oasFile) {
        this.oasFile = oasFile;
    }

    public String getCodegenKey() {
        return codegenKey;
    }

    public void setCodegenKey(String codegenKey) {
        this.codegenKey = codegenKey;
    }

    public CodegenParams() {
    }

//    public boolean haveDefaultTags() {
//        if (this.openAPI == null) return false;
//        List<Tag> listTag = this.openAPI.getTags();
//        for (Map.Entry<String, PathItem> next : this.openAPI.getPaths().entrySet()) {
//            String key = next.getKey();
//            PathItem pathItem = next.getValue();
//            for (Operation operation : pathItem.readOperations()) {
//                if (operation.getTags().isEmpty()) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }


//    public String getFilePath(TemplateFile templateFile) {
//        return getOutputDir() + File.separator + File.separator + templateFile.getOutput();
//    }
//
//    public String getApiFilePath(TemplateFile templateFile, CodegenApi api) {
//        return getOutputDir() + File.separator + templateFile.getPrefix() + templateFile.getOutput() + api.getClassname() + templateFile.getSuffix();
//    }
//
//    public String getModelFilePath(TemplateFile templateFile, CodegenEntity model) {
//        return getOutputDir() + File.separator + templateFile.getPrefix() + templateFile.getOutput() + model.getClassname() + templateFile.getSuffix();
//    }
//
//    final public String getTemplatePath(TemplateFile templateFile) {
//        return getTemplateDir() + File.separator + templateFile.getTemplate();
//    }
}
