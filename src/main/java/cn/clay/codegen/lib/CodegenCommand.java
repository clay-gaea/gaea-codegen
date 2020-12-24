package cn.clay.codegen.lib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class CodegenCommand {

    protected CodegenImpl codegenService;

    @Autowired
    public CodegenCommand(CodegenImpl codegenService) {
        this.codegenService = codegenService;
    }

    @ShellMethod("generate")
    public void generate(
            @ShellOption({"-K", "--key"}) String key,
            @ShellOption(value = {"-I", "--input"}, defaultValue = "") String input,
            @ShellOption(value = {"-O", "--output"}, defaultValue = "") String output,
            @ShellOption(value = {"-G", "--groupId"}, defaultValue = "") String groupId,
            @ShellOption(value = {"-A", "--artifactId"}, defaultValue = "") String artifactId,
            @ShellOption(value = {"-J", "--jar"}, defaultValue = "") String jar
    ) {
        CodegenConfig config = new CodegenConfig();
        config.setKey(key);
        if (!input.isEmpty()) config.setInput(input);
        if (!output.isEmpty()) config.setOutput(output);
        if (!groupId.isEmpty()) config.setGroupId(groupId);
        if (!artifactId.isEmpty()) config.setGroupId(artifactId);
        if (!jar.isEmpty()) config.setJar(jar);

        codegenService.generate(config);
    }

}
