package cn.clay.codegen.lib.phpLumen;

import cn.clay.codegen.CodegenConfigPhp;
import cn.clay.codegen.TemplateFile;
import io.swagger.oas.models.OpenAPI;

/**
 * composer.json: name baseNamespace authors
 * <p>
 * ServiceProvider.php
 * <p>
 * Controller.php
 */
public class CodegenConfig extends CodegenConfigPhp {
    public CodegenConfig(OpenAPI openAPI) {
        super(openAPI);

        templateFiles.add(new TemplateFile("composer.tlf", "composer.json"));
        templateFiles.add(new TemplateFile("serviceProvider.tlf", "ServiceProvider.php"));

        apiTemplateFiles.add(new TemplateFile("controller.tlf", "src/Controller/", "", "Controller.php"));
        apiTemplateFiles.add(new TemplateFile("interface.tlf", "src/Interface/", "", "Interface.php"));
        apiTemplateFiles.add(new TemplateFile("facade.tlf", "src/Facade/", "", "Facade.php"));

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
        return "/Users/chenlei/Dev/Gaea/gaea-codegen/src/main/java/cn/clay/codegen/lib/phpLumen/templates";
    }

}
