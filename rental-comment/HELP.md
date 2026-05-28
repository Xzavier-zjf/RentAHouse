# 快速开始

### 参考文档
如需进一步了解，可参考以下资料：

- [Apache Maven 官方文档](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven 插件参考](https://docs.spring.io/spring-boot/3.5.0/maven-plugin)
- [构建 OCI 镜像](https://docs.spring.io/spring-boot/3.5.0/maven-plugin/build-image.html)
- [Spring Boot Web 文档](https://docs.spring.io/spring-boot/3.5.0/reference/web/servlet.html)

### 使用说明
这些 `HELP.md` 为 Spring Initializr 生成的基础说明，可按各模块实际依赖继续补充。

### Maven Parent 说明
由于 Maven 的继承机制，父 POM 中的部分元素会自动继承到子项目。
如果你不希望继承某些字段（如 `<license>`、`<developers>`），可以在当前项目 POM 中显式覆盖为空。
