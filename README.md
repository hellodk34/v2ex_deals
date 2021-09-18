# v2ex_deals
v2ex 优惠信息 节点新帖推送。**频道名称：`v2ex 优惠信息 新帖推送`，link: https://t.me/v2ex_deals**

迫于总是错过 v2ex 上 `优惠信息` 节点的信息，创建了一个 `v2ex 优惠信息 新帖推送` 的 telegram channel. 欢迎关注/订阅频道。

preview:

![20210915113558](https://cdn.jsdelivr.net/gh/hellodk34/image@main/img/20210915113558.png)

---

# 更新

上述优惠信息节点新帖推送的频道不会受到影响。个人会维护下去，也占用不了多少系统资源（运行在个人 VPS 上）。

本程序已经更新成可以部署为个人关注的节点新帖推送的后端程序。前端只需要

1. 创建一个 telegram bot
2. 获取 bot 的 `token`
3. 获取 bot 的 `chatId`

如何创建 telegram bot 请自行搜索（是很简单的）。

# 重要更新 at 2021-09-18 16:04:56

由于 telegram bot API `sendMessage` 方法请求限制：每次最大 4096 个字符，所以如果用户发的帖子正文内容特别多的话，就很容易超过这个限制。超过限制发送请求会导致 response code 400, bad request, 所以会再次推送一个不带 `content` 字段的版本给 bot.

而我又希望推送 `content` 字段，因为这个字段帮助你直接预览帖子正文，在 telegram 上直接判断是否有必要打开帖子。如果你频繁收到 `Pushing failed` 的提示，可以考虑更改 cron 表达式，改成更短的时间请求一次，比如原来每 30 分钟请求一次，现在改成每 10 分钟请求一次。

目前第一次推送失败后推送的文案如下

> Pushing failed. It maybe caused by too many topics matched or too much content in some topic/post, which exceeds the telegram API request limit.
>
> See API document: https://core.telegram.org/bots/api 
>
> Now push the version without content field.

preview:

![20210918160937](https://cdn.jsdelivr.net/gh/hellodk34/image@main/img/20210918160937.png)

**请使用最新 jar 包重新部署**，文件名：`app-2021-09-18.jar`
## 配置文件说明

配置文件的名字是可以随便命名的，比如 `config.txt`

```text
# 创建的 telegram bot 的 token，长得像这样，`数字:字母`
token=123456789:abcdEFGhijkLMN

# bot 的 chatId
chatId=1234567

# cron 表达式，Java(String) 类型或者 Java(Quartz) 类型 参考网站 https://tool.lu/crontab/
# 下面这是每一分钟请求一次 API，有新帖则推送，无则不推送
# 建议每 10 分钟或 20 分钟、或 30 分钟 请求一次 ，假如是 20 分钟，这么写：cron=0 */20 * * * ?
cron=0 */1 * * * ?

# 关注的节点列表，节点 code 可以通过 `https://www.v2ex.com/api/nodes/s2.json` 获取。
# 用英文逗号/半角逗号分隔，已作 trim 处理，逗号后可添加空格（更容易看清楚）
nodeList=qna, deals, bike, apple
```

## 运行方式

```shell script
java -jar /path/to/app.jar --configPath=./config.txt >> ./application.log 2>&1 &
```

tips：

1. `--configPath=` 右边的值既可以是相对路径也可以是绝对路径，支持在 Windows 下的路径
2. 只有 4 个 key-value pair
3. 可以根据关注的节点热度和数量调整请求频率，请自行调整 cron 表达式，减轻 v2ex 服务器负担 :)。比如 `问与答` `Apple` `iPhone` 都是热门节点
4. 配置文件支持以 # 开头的注释
5. 由于你懂🉐️️的原因，V 站被墙，建议运行在非大陆的服务器上
6. 由于获取的是 V 站首页 `全部` tab 下的帖子信息，故而不可能出现在此 tab 下的帖子就不可能被推送，比如 `二手交易`、`水深火热`

> 默认情况下，每个 IP 每小时可以发起的 API 请求数被限制在 120 次。

[V2EX API 接口](https://www.v2ex.com/p/7v9TEc53)

## Preview

![20210916170056](https://cdn.jsdelivr.net/gh/hellodk34/image@main/img/20210916170056.png)
![20210916170223](https://cdn.jsdelivr.net/gh/hellodk34/image@main/img/20210916170223.png)

项目还有不完善的地方，如果你有兴趣，欢迎 fork 提交 mr/pr 提交你的贡献，一起来完善这个有趣的项目吧~

最后，求个 star ⭐️️
