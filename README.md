# gaea-codegen

## 介绍
### 项目初衷是什么
Codegen是一个很棒的注意，可以帮我们建议自己团队的代码规范，把我们从繁杂重复的工作中截图。
但是您使用过程，很多地方需要定制，我在使用MUSTACHE过程中，刚到无助甚至绝望。
基于Codegen基本理念，使用Themeleaf模板技术重新实现了一下，这令我对未来更加期待。

### 项目做了什么
该项目是一个简单例子，可以生成 PHP 项目的相关依赖包，并且其中根据个人需要，做了大量的定制。

### 我还想做什么
我有个更多的主意，但是并没有经历一一实现，我列举下来，希望以后有机会能实现。
- 实现类似swagger-editor，能中多人同时在线编辑
- 对于OSA文档能够有线上仓库，可以编辑、保存、分享、模拟请求、测试、监控
    - 可能你想将OSA直接共享给他人
    - 可能你仅仅想将OSA渲染的文档共享贵他人
- 基于定制代码
    - JsonSchema，是的自动化测试成为可能
    - 接口的线上监控、预警
    - 日志标准化
    - 异常标准化
    - 服务间通信方式更加多样化
    - 打包测试发布监控更加平滑

这里有个Demo，帮你了解 gaea-codegen 干了什么。
https://github.com/clay-gaea/demo-php-lumen

## 参考资料
- http://www.thymeleaf.org
- http://www.snakeyaml.org
- https://github.com/alibaba/fastjson/wiki/Quick-Start-CN
