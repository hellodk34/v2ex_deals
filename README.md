# v2ex_deals
v2ex 优惠信息 节点新帖推送。**频道名称：`v2ex 优惠信息 新帖推送`，link: https://t.me/v2ex_deals**

迫于总是错过 v2ex 上 `优惠信息` 节点的信息，创建了一个 `v2ex 优惠信息 新帖推送` 的 telegram channel. 欢迎关注/订阅频道。

preview:

![20210915113558](https://cdn.jsdelivr.net/gh/hellodk34/image@main/img/20210915113558.png)

---

# 说明

源码中 `token` 和 `chatId` 都是 fake 的，如果想要自己部署，需要自己申请一个 telegram bot，然后获取 bot 的 `token`，bot 的 `chatId` 可以获取保存，可以测试发给 bot 用。

然后再创建一个 public 的 channel，添加用户，添加刚刚创建的 bot，将此 bot 设置成管理员，授予相关权限。然后获取 channel 的 channelId (实际上就是 channel 的 chatId)。

这两个参数会是通过 telegram api 成功发送消息到频道的关键。详情可 google 查找相关博客、文章。

可以自己部署，找到自己关注的节点，然后获取对应节点的 nodeCode。有关于 v2ex 所有节点，可以请求这个 API, 返回 JSON 格式数据，control + f 找到你关注的节点的 nodeCode `https://www.v2ex.com/api/nodes/s2.json`

> 默认情况下，每个 IP 每小时可以发起的 API 请求数被限制在 120 次。

[V2EX API 接口](https://www.v2ex.com/p/7v9TEc53)

普通 Java 项目，maven based，为了不占用系统资源，程序单次执行。鉴于 v2ex `优惠信息` 节点比较冷门，所以 1 小时请求一次 API，将这段时间的帖子推送给用户，如果没有帖子，将不会发推送打扰用户 :)

本推送 channel 程序运行在我自己的 VPS 上。

---

# 部署

使用 idea 导入此 maven 项目

1. 修改 `NodeListDict.java`，添加自己关注的节点
2. 设置自己的 `token` 和 `chatId`

然后构建项目，将 target 文件夹下生成的 `v2-deals-1.0-SNAPSHOT-jar-with-dependencies.jar` 拷贝到任意路径，改名为 `app.jar`。由于 V 站现在被墙，建议直接在国外主机上运行程序。

使用 crontab 一小时运行一次程序

```shell
0 */1 * * * /usr/bin/java -jar /path/to/app.jar
```
