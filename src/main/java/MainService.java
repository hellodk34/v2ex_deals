import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: hellodk
 * @description main service
 * @date: 9/14/2021 10:52 AM
 */

public class MainService {

    private static final String apiUrl = "https://www.v2ex.com/api/topics/latest.json";

    public static void main(String[] args) {

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
             * 自己部署的话可以修改源码重新编译，这里的判断条件可以变成你关注的各个节点。假如希望收到 `优惠信息` 和 `二手交易` （nodeCode is all4all）的帖子信息可以更改这里的条件
             */
            if ("deals".equals(nodeCode)) {
                JSONObject jsonObject = getInfo(topic);
                resultArray.add(jsonObject);
            }
        }
        if (resultArray.size() < 1) {
            return;
        }
        sendToTelegram(resultArray);

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

        /**
         * Add Telegram token (given Token is fake) channel token
         */
        String apiToken = "191992291212:AAEMZfwetwerwersdfaaaasdfA_xNr6RumT9drJLwXfMBQ";

        /**
         * Add chatId (given chatId is fake)
         * channelId or chatId, if you are using channel, paste your channelId to following `chatId` field
         */
        String chatId = "-100113211166775";

        urlString = String.format(urlString, apiToken);

        Map<String, Object> map = new HashMap<>();
        map.put("chat_id", chatId);
        Date nowTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuilder out = new StringBuilder("# ").append(sdf.format(nowTime)).append(" topics pushing").append("\r\n").append("\r\n");
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
                .timeout(20000)
                .execute()
                .body();
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
