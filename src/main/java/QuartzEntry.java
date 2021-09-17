import cn.hutool.core.map.MapUtil;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author: hellodk
 * @description quartz entry
 * @date: 9/16/2021 10:00 AM
 */

public class QuartzEntry {

    private static final Logger logger = LoggerFactory.getLogger(QuartzEntry.class);

    // 命令行传参 configPath
    private static String configPath;

    /**
     * @param * @param args:
     * @return void
     * @author hellodk
     * @description 程序主入口
     * @date 9/16/2021 2:26 PM
     */
    public static void main(String[] args) throws SchedulerException {
        if (args.length < 1) {
            logger.warn("Invalid parameter. Usage: /path/to/java -jar /path/to/app.jar --configPath=xxx");
            return;
        }
        configPath = args[0];
        if (!configPath.startsWith("--configPath=")) {
            logger.warn("Usage: /path/to/java -jar /path/to/app.jar --configPath=xxx");
            return;
        }

        // 每隔 1 分钟请求一次 API
        // String cron = "0 */1 * * * ?";
        String cron = MapUtil.getStr(readConfigFile(), "cron");
        logger.info("cron is " + cron);
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        JobDetail jobDetail = JobBuilder.newJob(MainService.class).withIdentity("job1", "group").build();

        try {
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            scheduler.start();
        }
        catch (Exception e) {
            logger.warn("exception when running job", e);
            System.exit(0);
        }
    }

    /**
     * @param * @param :
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author hellodk
     * @description 读取配置文件
     * @date 9/16/2021 2:26 PM
     */
    public static Map<String, Object> readConfigFile() {
        String fileLocation = configPath.replace("--configPath=", "");
        // 读取文件内容到 Stream 流当中，按行读取
        Map<String, Object> map = new HashMap<>();
        try {
            Stream<String> lines = Files.lines(Paths.get(fileLocation));
            lines.forEachOrdered(ele ->
                    {
                        /**
                         * 允许 # 开头的注释行和空行
                         * 允许 key=value
                         * 只允许这两种形式，只用到 4 个 key-value pair
                         * 其他情况均提示用户：read config file error, please check your config file
                         */
                        if (!ele.startsWith("#") && ele.contains("=")) {
                            String[] arr = ele.split("=");
                            map.put(arr[0].trim(), arr[1].trim());
                        }
                        else if (!ele.startsWith("#") && !"".equals(ele)) {
                            throw new RuntimeException("read config file error, please check your config file");
                        }
                    }
            );
        }
        catch (Exception e) {
            logger.warn("read config file error", e);
            System.exit(0);
        }
        logger.info("configMap is " + map.toString());
        return map;
    }
}
