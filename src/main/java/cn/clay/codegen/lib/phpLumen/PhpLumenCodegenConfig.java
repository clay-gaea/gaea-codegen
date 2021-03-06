package cn.clay.codegen.lib.phpLumen;

import cn.clay.codegen.lib.CodegenConfigPhp;
import cn.clay.codegen.entity.TemplateFile;
import io.swagger.oas.models.OpenAPI;

/**
 * composer.json: name baseNamespace authors
 * <p>
 * ServiceProvider.php
 * <p>
 * Controller.php
 */
public class PhpLumenCodegenConfig extends CodegenConfigPhp {
    public PhpLumenCodegenConfig(OpenAPI openAPI) {
        super(openAPI);

        templateFiles.add(new TemplateFile("composer.tlf", "composer.json"));
        templateFiles.add(new TemplateFile("serviceProvider.tlf", "src/ServiceProvider.php"));

        apiTemplateFiles.add(new TemplateFile("controller.tlf", "src/Controller/", "", "Controller.php"));
        apiTemplateFiles.add(new TemplateFile("interface.tlf", "src/Service/", "", "Interface.php"));
        apiTemplateFiles.add(new TemplateFile("facade.tlf", "src/Facade/", "", "Facade.php"));
        apiTemplateFiles.add(new TemplateFile("client.tlf", "src/Client/", "", "Client.php"));

        modelTemplateFiles.add(new TemplateFile("entity.tlf", "src/Entity/", "", ".php"));
    }

    @Override
    public String getKey() {
        return "php-lumen";
    }

    @Override
    public String getDescription() {
        return "PHP LUMEN 服务包";
    }

    @Override
    public String getTemplateDir() {
        return "src/main/java/cn/clay/codegen/lib/phpLumen/templates";
    }

}
