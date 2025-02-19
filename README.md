# wechat-ocr-java
使用 wechat 的 ocr 组件, 开箱即用的 ocr

轻量化, 简单, 一般情况下无需环境

## 感谢
https://github.com/swigger/wechat-ocr

## 说明
由于 wechat 是基于本地路径的, 图片会本地保存, 再识别

不能接受性能或硬盘寿命, 可以使用虚拟硬盘

目前只能在 `windows x64` 环境下运行

## 使用
可能需要手动上传到本地仓库, 再引入坐标

``` java
WeChatOCR.load();
Result result = WeChatOCR.apply(path);
```

``` bat
mvn install:install-file ^
-Dfile=wechat-ocr-java-1.0.1.jar ^
-DgroupId=uu ^ 
-DartifactId=wechat-ocr-java ^
-Dversion=1.0.1 ^
-Dpackaging=jar
```