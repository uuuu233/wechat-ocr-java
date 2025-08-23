# wechat-ocr-java
使用 WeChat 的 OCR 插件, 开箱即用

轻量化, 简单, 速度快

## 实际测试
使用 6 台服务器进行负载均衡, 累计 ocr 次数 百万次以上, 稳定运行 3 个月以上

但是实际过程中, `WeChatOCR.exe` 会 1 天崩掉 1 次, 怀疑是上游稳定性问题, 不过我加了异常自动重启服务, 妥协了

## 说明
由于 WeChat 是基于本地路径的, 图片会本地保存, 再识别

不能接受性能或硬盘寿命, 可以使用虚拟硬盘

目前只能在 `windows x64` 环境下运行

可能某些 CPU 缺少一些集指令导致无法正常工作

## 优点
1. 精准度高
2. 识别快, 无论是小图还是大图, 都能控制在 300ms 左右, 其他产品测试结果是 150~800ms
3. 连续识别 cpu 稳定在 20% 左右, 其他产品测试结果是 100%

## 使用
可能需要手动上传到本地仓库, 再引入坐标

``` java
WeChatOCR.load(); // 仅一次
String path = "文件路径";
Result result = WeChatOCR.apply(path);
```

``` bat
mvn install:install-file ^
-Dfile=wechat-ocr-java-1.2.0.jar ^
-DgroupId=uu ^ 
-DartifactId=wechat-ocr-java ^
-Dversion=1.2.0 ^
-Dpackaging=jar
```

## 感谢
https://github.com/swigger/wechat-ocr

https://github.com/EEEEhex/qqimpl