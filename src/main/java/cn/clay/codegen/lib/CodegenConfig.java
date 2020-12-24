package cn.clay.codegen.lib;

/**
 * 功能介绍：定位 - 命令函/服务传参
 * 1. 模板映射说明，设计属性有templateFiles，templateApis，templateEntities
 * 2. 配置加载，需要支持配置如下：groupId，outputDir、callback（上传私仓等操作）
 * 3. 回调 callback 仅支持服务调用，细节待定，可以考虑服务调用、shell脚本定义
 * 4. 定义 key
 * 5. 可以考虑，配置和定义对象拆分
 */
public class CodegenConfig {

    // groupId 属于全局配置；artifactId 则是目标属性

    protected String key = ""; // 必填项
    protected String input = "./oas.yaml"; // 设置默认值
    protected String output = "./oas-{version}"; // 仅对命令行起作用
    protected String groupId = "cn.clay";
    protected String artifactId = "oas";
    protected String jar = ""; // 拓展用 jar 包

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

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

    public String getJar() {
        return jar;
    }

    public void setJar(String jar) {
        this.jar = jar;
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
}
