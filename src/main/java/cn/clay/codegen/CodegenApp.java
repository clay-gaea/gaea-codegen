package cn.clay.codegen;

import cn.clay.codegen.entity.CodegenApi;
import cn.clay.codegen.entity.CodegenModel;
import cn.clay.codegen.lib.phpLumen.PhpLumenCodegenConfig;
import cn.clay.codegen.lib.javaSpring.JavaSpringCodegenConfig;
import io.swagger.oas.models.OpenAPI;
import io.swagger.parser.v3.OpenAPIV3Parser;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class CodegenApp {

    // 读取json文件
    protected static String readFile(String fileName) {
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 1.通过 jar 加载配置包，命令行工具
     * 2.加载 JSON/YAML 配置文件
     * 3.解析成 OPENAPI SPEC 对象
     * 4.通过模板生成对应的文件
     * 5.钩子方法
     */
    public static void main(String[] args) {
        String pathJson = "src/main/resources/uac-simple.json";
//        String pathYaml = "/Users/chenlei/Dev/Gaea/gaea-codegen/src/main/resources/uac-simple.yaml";

        /*
          加载文件
          方式一：通过IO
          方式二：Spring
         */
//        String str = readFile(pathJson);
//        // 解析 JSON 字符串
//        OpenAPI openAPI = JSON.parseObject(str, OpenAPI.class);
//        OpenAPI openAPI = JSONObject.parseObject(str, OpenAPI.class);
//        // 解析 YAML 字符串
//        Yaml yaml = new Yaml();
//        OpenAPI openAPI = yaml.loadAs(str, OpenAPI.class);

        OpenAPI openAPI = (new OpenAPIV3Parser()).read(pathJson);
        assert openAPI != null;
        CodegenConfig codegenConfig = new JavaSpringCodegenConfig(openAPI);
        codegenConfig.setOutputDir("./dist");

        // 清理目录
        deleteDir(codegenConfig.getOutputDir());

        Map<String, Object> singleScope = codegenConfig.getScope();
        for (TemplateFile templateFile : codegenConfig.getTemplateFiles()) {
            try {
                parseWithThymeleaf(codegenConfig.getTemplatePath(templateFile), codegenConfig.getFilePath(templateFile), singleScope);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        for (CodegenApi api : codegenConfig.getApis()) {
            Map<String, Object> apiScope = codegenConfig.getApiScope(api);
            for (TemplateFile templateFile : codegenConfig.getApiTemplateFiles()) {
                try {
                    parseWithThymeleaf(codegenConfig.getTemplatePath(templateFile), codegenConfig.getApiFilePath(templateFile, api), apiScope);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        for (CodegenModel model : codegenConfig.getModels()) {
            Map<String, Object> modelScope = codegenConfig.getModelScope(model);
            for (TemplateFile templateFile : codegenConfig.getModelTemplateFiles()) {
                try {
                    parseWithThymeleaf(codegenConfig.getTemplatePath(templateFile), codegenConfig.getModelFilePath(templateFile, model), modelScope);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    /**
     * @param templatePath String 模板文件路径
     * @param outputPath   String 目标文件路径
     * @param scope        Map
     * @throws IOException 文件异常
     */
    public static void parseWithThymeleaf(String templatePath, String outputPath, Map<String, Object> scope) throws IOException {
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

    public static boolean deleteDir(String path) {
        File file = new File(path);
        if (!file.exists()) {//判断是否待删除目录是否存在
            System.err.println("The dir are not exists!");
            return false;
        }

        String[] content = file.list();//取得当前目录下所有文件和文件夹
        for (String name : content) {
            File temp = new File(path, name);
            if (temp.isDirectory()) {//判断是否是目录
                deleteDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
                temp.delete();//删除空目录
            } else {
                if (!temp.delete()) {//直接删除文件
                    System.err.println("Failed to delete " + name);
                }
            }
        }
        return true;
    }
}

