import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: hellodk
 * @description main service
 * @date: 9/14/2021 10:52 AM
 */

public class MainService implements Job {

    private static final String apiUrl = "https://www.v2ex.com/api/topics/latest.json";

    // 保证推送 url 唯一性的 map，保证一天之内不会重复推送
    private static Map<String, String> map = new ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger(MainService.class);

    private static String token;

    private static String chatId;

    private static String nodeList;

    // 时间间隔，每隔 24 小时清空 map，每天凌晨 1 点执行
    private static final long PERIOD_OF_DAY = 24 * 60 * 60 * 1000;

    /**
     * 从配置文件中读取相关参数
     */
    static {
        Map<String, Object> configMap = QuartzEntry.readConfigFile();
        token = MapUtil.getStr(configMap, "token");
        chatId = MapUtil.getStr(configMap, "chatId");
        nodeList = MapUtil.getStr(configMap, "nodeList");
    }

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date date = calendar.getTime();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            // 每隔 24 小时清空 map，每天凌晨 1 点执行
            @Override
            public void run() {
                // 清空 map 的目的是为了清除内存占用
                map.clear();
            }
        };
        timer.schedule(timerTask, date, PERIOD_OF_DAY);
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        this.entry();
    }


    public static void entry() {

        String response = HttpUtil.get(apiUrl, CharsetUtil.CHARSET_UTF_8);
        JSONArray arr = JSONArray.parseArray(response);
        JSONArray resultArray = new JSONArray();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject topic = (JSONObject) arr.get(i);
            JSONObject node = topic.getJSONObject("node");
            String nodeCode = node.getString("name");
            /**
             * 优惠信息节点的 nodeCode 是 `deals`
             * `qna` 是 `问与答` 节点的 nodeCode
             */
            String[] nodeArr = nodeList.split(",");
            Set<String> set = new HashSet<>(Arrays.asList(nodeArr));
            if (set.contains(nodeCode)) {
                JSONObject jsonObject = getInfo(topic);
                resultArray.add(jsonObject);
            }
        }

        /**
         * 不要用 jsonArray.remove(object); 这种方式删除，可能会报出 ConcurrentModificationException
         */
        Iterator<Object> it = resultArray.iterator();
        while (it.hasNext()) {
            JSONObject jo = (JSONObject) it.next();
            String url = jo.getString("url");
            if (!map.containsKey(url)) {
                // 将 url 缓存到 map，作为 key 和 value，解决重复推送问题。value 没存储 jsonObject 对象而只存储了 url 是为了空间考虑
                map.put(url, url);
            }
            else {
                it.remove();
            }
        }
        if (resultArray.size() > 0) {
            logger.info(getNowTime() + " " + resultArray.toJSONString());
            sendToTelegram(resultArray);
        }
        else {
            logger.info(getNowTime() + " no new topics!");
        }

    }

    /**
     * @param * @param topic:
     * @return com.alibaba.fastjson.JSONObject
     * @author hellodk
     * @description 封装返回的基本信息
     * @date 9/15/2021 11:22 AM
     */
    private static JSONObject getInfo(JSONObject topic) {
        JSONObject node = topic.getJSONObject("node");
        JSONObject member = topic.getJSONObject("member");
        String v2er = member.getString("username");
        String nodeName = node.getString("title");
        String title = topic.getString("title");
        String url = topic.getString("url");
        String created = topic.getString("created");
        String createdTime = getRealTime(created);
        String content = topic.getString("content");
        JSONObject result = new JSONObject();
        result.put("v2er", v2er);
        result.put("nodeName", nodeName);
        result.put("title", title);
        result.put("url", url);
        result.put("createdTime", createdTime);
        result.put("content", content);
        return result;
    }

    /**
     * @param * @param created:
     * @return java.lang.String
     * @author hellodk
     * @description 将 时间戳转换成 Java.util.Date
     * @date 9/14/2021 12:29 PM
     */
    private static String getRealTime(String created) {
        long time = new Long(created);
        Date date = new Date(time * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * @param * @param array:
     * @return void
     * @author hellodk
     * @description 使用 telegram api 方法 sendMessage 使用 post 请求
     * telegram channel 发送的消息回显内容是  body 中的 text
     * @date 9/14/2021 3:59 PM
     */
    public static void sendToTelegram(JSONArray array) {

        String urlString = "https://api.telegram.org/bot%s/sendMessage";

        urlString = String.format(urlString, token);

        Map<String, Object> map = new HashMap<>();
        map.put("chat_id", chatId);
        StringBuilder out = new StringBuilder("# ").append(getNowTime()).append(" topics pushing").append("\r\n").append("\r\n");
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = (JSONObject) array.get(i);
            String emojiSeq = getEmojiSeq(i + 1);
            out.append("## topic ").append(emojiSeq).append("\r\n").append("\r\n");
            out.append("- title: ").append(item.getString("title")).append("\r\n");
            out.append("- nodeName: ").append(item.getString("nodeName")).append("\r\n");
            out.append("- v2er: ").append(item.getString("v2er")).append("\r\n");
            out.append("- url: ").append(item.getString("url")).append("\r\n");
            out.append("- createdTime: ").append(item.getString("createdTime")).append("\r\n");
            out.append("- content: `").append(item.getString("content")).append("`\r\n").append("\r\n");
        }

        map.put("text", out.toString());
        /**
         * parse_mode 取值有
         * 1. Markdown
         * 2. HTML
         * 3. MarkdownV2
         *
         * 经过测试 MarkdownV2 没有生效。Markdown 可用，但是支持的标签很有限
         */
        map.put("parse_mode", "Markdown");
        map.put("disable_web_page_preview", true);
        // 发送 POST 请求
        HttpRequest.post(urlString)
                .form(map)
                .timeout(20000) // 设置请求 20s 超时
                .execute()
                .body();
    }

    /**
     * @param * @param :
     * @return java.lang.String
     * @author hellodk
     * @description 获取当前时间字符串 格式类似于 2021-09-16 15:07:25
     * @date 9/16/2021 3:07 PM
     */
    private static String getNowTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * @param * @param i:
     * @return java.lang.String
     * @author hellodk
     * @description 获取 emoji 序号
     * @date 9/14/2021 5:54 PM
     */
    private static String getEmojiSeq(int i) {
        String str = String.valueOf(i);
        char[] chars = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < chars.length; j++) {
            Integer item = Integer.parseInt(String.valueOf(chars[j]));
            sb.append(NumbersDict.getSpecificEmoji(item));
        }
        return sb.toString();
    }

}
