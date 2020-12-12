package cn.clay.codegen.lib;

public class TemplateFile {
    protected String template = "";
    protected String output = "";
    protected String prefix = ""; // 前缀
    protected String suffix = ""; // 后缀

    public TemplateFile(String template, String output) {
        this.template = template;
        this.output = output;
    }

    public TemplateFile(String template, String output, String prefix) {
        this.template = template;
        this.output = output;
        this.prefix = prefix;
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

    public String getOutput() {
        return output;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getLastOutput() {
        return this.prefix + this.output + this.suffix;
    }
}
