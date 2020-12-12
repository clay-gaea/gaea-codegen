package cn.clay.codegen;

/**
 * 1. 通过本地文件转换（支持相对路径和绝对路径）
 * 2. 支持url地址转换
 * 3. 支持 IOStream 转换
 */
public interface CodegenService {
    public void generate(String filePath);

//    public void generate(String url);
}
