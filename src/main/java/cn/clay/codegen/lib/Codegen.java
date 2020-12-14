package cn.clay.codegen.lib;

import cn.clay.codegen.entity.CodegenApi;
import cn.clay.codegen.entity.CodegenEntity;
import cn.clay.codegen.entity.CodegenOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.tags.Tag;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public abstract class Codegen {
    protected OpenAPI openAPI;
    protected CodegenConfig config;

    public Codegen(CodegenConfig config) {
        this.config = config;
        this.configTemplates();
        this.initOpenAPI();
    }

    protected List<CodegenApi> apis = null;
    protected List<CodegenEntity> entities = null;

    protected List<TemplateFile> templateFiles = new ArrayList<>();
    protected List<TemplateFile> templateApis = new ArrayList<>();
    protected List<TemplateFile> templateEntities = new ArrayList<>();

    // 生成器表示
    public abstract String getKey();

    // 生成器描述
    public abstract String getDescription();

    // 生成器版本号
    public abstract String getVersion();

    abstract protected void configTemplates();

    abstract protected String getSchemaClassName(Schema<?> schema);

    abstract protected List<String> getSchemaImports(Schema<?> schema);

    protected void initOpenAPI() {
        // 通过配置获取openAPI
    }

    final protected String getTemplateFilePath(TemplateFile templateFile) {
        return getTemplateDir() + templateFile.getTemplate();
    }

    /**
     * @param templatePath String 模板文件路径
     * @param outputPath   String 目标文件路径
     * @param scope        Map
     * @throws IOException 文件异常
     */
    protected void parseWithThymeleaf(String templatePath, String outputPath, Map<String, Object> scope) throws IOException {
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setCacheable(true);
        templateResolver.setTemplateMode(TemplateMode.TEXT);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);

        File outputFile = new File(outputPath);
        if (!outputFile.getParentFile().exists()) {
            boolean bOK = outputFile.getParentFile().mkdirs();
            if (!bOK) return;
        }
        if (!outputFile.exists()) {
            boolean bOK = outputFile.createNewFile();
            if (!bOK) return;
        }

        Context ctx = new Context(Locale.getDefault(), scope);
        FileWriter writer = new FileWriter(outputFile);
        engine.process(templatePath, ctx, writer);
        writer.flush();
    }

    protected List<CodegenApi> getApis() {
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

    protected List<CodegenEntity> getEntities() {
        if (entities == null) {
            entities = new ArrayList<>();

            for (Map.Entry<String, Schema> schemaEntry : openAPI.getComponents().getSchemas().entrySet()) {
                if (schemaEntry.getKey().equals("Page")) continue;
                entities.add(new CodegenEntity(schemaEntry.getValue(), schemaEntry.getKey()));
            }
        }

        return entities;
    }

    protected Map<String, Object> getScope() {
        Map<String, Object> map = new HashMap<>();

        map.put("codegenKey", getKey());
        map.put("codegenDescription", getDescription());
        map.put("codegenVersion", getVersion());
        map.put("config", config);

        map.put("openAPI", openAPI);
        map.put("apis", this.getApis());
        map.put("entities", this.getEntities());

        return map;
    }

    protected Map<String, Object> getApiScope(CodegenApi api) {
        Map<String, Object> map = this.getScope();
        map.put("api", api);
        return map;
    }

    protected Map<String, Object> getEntityScope(CodegenEntity model) {
        Map<String, Object> map = this.getScope();
        map.put("model", model);
        return map;
    }

    protected String getTemplateDir() {
        return "./templates/";
    }

    // 生成目标压缩包
    public void exec() {
        Map<String, Object> singleScope = getScope();
        for (TemplateFile templateFile : templateFiles) {
            try {
                parseWithThymeleaf(getTemplateFilePath(templateFile), templateFile.getLastOutput(), singleScope);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        for (CodegenApi api : getApis()) {
            Map<String, Object> apiScope = getApiScope(api);
            for (TemplateFile templateFile : templateApis) {
                try {
                    parseWithThymeleaf(getTemplateFilePath(templateFile), templateFile.getLastOutput(), apiScope);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        for (CodegenEntity model : getEntities()) {
            Map<String, Object> modelScope = getEntityScope(model);
            for (TemplateFile templateFile : templateEntities) {
                try {
                    parseWithThymeleaf(getTemplateFilePath(templateFile), templateFile.getLastOutput(), modelScope);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
