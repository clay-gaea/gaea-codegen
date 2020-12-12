package cn.clay.codegen.frame.phpLumen;

import cn.clay.codegen.lang.PhpCodegen;
import cn.clay.codegen.lib.CodegenConfig;
import cn.clay.codegen.lib.TemplateFile;

/**
 * composer.json: name baseNamespace authors
 * <p>
 * ServiceProvider.php
 * <p>
 * Controller.php
 */
public class PhpLumenCodegen extends PhpCodegen {
    public PhpLumenCodegen(CodegenConfig config) {
        super(config);
    }

    @Override
    public String getKey() {
        return "php-lumen";
    }

    @Override
    public String getDescription() {
        return "PHP LUMEN 生成器";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    protected void configTemplates() {
        templateFiles.add(new TemplateFile("composer.tlf", "composer.json"));
        templateFiles.add(new TemplateFile("serviceProvider.tlf", "ServiceProvider.php"));

        templateApis.add(new TemplateFile("controller.tlf", "src/Controller/", "", "Controller.php"));
        templateApis.add(new TemplateFile("interface.tlf", "src/Interface/", "", "Interface.php"));
        templateApis.add(new TemplateFile("facade.tlf", "src/Facade/", "", "Facade.php"));

        templateEntities.add(new TemplateFile("entity.tlf", "src/Entity/", "", ".php"));
    }
}
