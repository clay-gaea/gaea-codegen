package cn.clay.codegen.lib.javaSpring;

import cn.clay.codegen.CodegenConfigJava;
import cn.clay.codegen.TemplateFile;
import io.swagger.oas.models.OpenAPI;

public class JavaSpringCodegenConfig extends CodegenConfigJava {
    public JavaSpringCodegenConfig(OpenAPI openAPI) {
        super(openAPI);

        templateFiles.add(new TemplateFile("pom.tlf", "pom.xml"));

        apiTemplateFiles.add(new TemplateFile("controller.tlf", "controller/", "", "Controller.java"));
        apiTemplateFiles.add(new TemplateFile("service.tlf", "service/", "", "Service.java"));
        apiTemplateFiles.add(new TemplateFile("client.tlf", "client/", "", "Client.java"));

        modelTemplateFiles.add(new TemplateFile("entity.tlf", "entity/", "", ".java"));
    }

    @Override
    public String getKey() {
        return "java-spring";
    }

    @Override
    public String getDescription() {
        return "Java Spring 代码生成器";
    }

    @Override
    public String getTemplateDir() {
        return "src/main/java/cn/clay/codegen/lib/javaSpring/templates";
    }
}