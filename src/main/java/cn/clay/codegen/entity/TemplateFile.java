package cn.clay.codegen.entity;

public class TemplateFile {
    protected String template = "";
    protected String output = "";
    protected String prefix = ""; // 前缀
    protected String suffix = ""; // 后缀

    public TemplateFile(String template, String output) {
        this.template = template;
        this.output = output;
    }

    public TemplateFile(String template, String output, String prefix, String suffix) {
        this.template = template;
        this.output = output;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
