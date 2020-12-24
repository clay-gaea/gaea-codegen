package cn.clay.codegen.lib;

import cn.clay.codegen.frame.phpLumen.PhpLumenCodegen;
import org.springframework.stereotype.Service;

@Service
public class CodegenImpl {
    public void generate(CodegenConfig config) {
        // 区别 Shell 和 服务
        // 1.通过 key 加载不同的 Codegen 生成器
        // 2.加载 input 接口规范文件, 支持1.本地路径(shell) 2.URL地址
        // 3.生成文件压缩包，文件名和压缩包名称一致

        Codegen codegen = new PhpLumenCodegen(config);
        codegen.exec();
    }

    public void generate(CodegenConfig config, String ioStream) {

    }

}
