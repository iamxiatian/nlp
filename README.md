# nlp #

网页正文抽取，关键词抽取，命名实体识别, 情感分析等Web数据挖掘分析所需要的基础模块的HTTP服务接口，可以通过HTTP方式，提供相关服务。

## 编译运行 ##

通过sbt进行测试：

```sh
$ cd nlp
$ sbt
> runMain HTTP
```

打包成包含有所有依赖的单一jar文件并运行测试：

```sh
$ cd nlp
$ sbt assembly
$ cd target/scala-2.11
$ nohup scala -J-Xmx3G nlp.jar -h 0.0.0.0 &
```

测试抽取效果：

http://localhost:8080/api/extract?url=http://news.ifeng.com/a/20161003/50058514_0.shtml

测试抽取并转换为PDF效果：

http://localhost:8080/api/aspdf?url=http://news.ifeng.com/a/20161003/50058514_0.shtml

代理抓取，即传入url和refer，把内容抓取下来，当需要通过该服务器做代理抓取网页内容时，可以使用该功能：

http://localhost:8080/api/fetch?url=http://www.baidu.com/img/bd_logo1.png&ref=http://www.baidu.com/
