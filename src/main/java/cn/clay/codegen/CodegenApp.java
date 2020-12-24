package cn.clay.codegen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.io.IOException;

@SpringBootApplication
@Component(value = "cn.clay.codegen.lib.CodegenCommand")
public class CodegenApp {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(CodegenApp.class, args);
    }
}
