package util;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Quatrz调度框架的管理类，用于添加，删除，重置任务
 */
public class QuartzManager {
    private final static Logger Log = Logger.getLogger(QuartzManager.class);

    // 创建调度者工厂
    private static SchedulerFactory sf = new StdSchedulerFactory();

    /**
     * 添加调度任务
     */
    public static void addJob(String jobName, String jobGroupName,
                              String triggerName, String triggerGroupName, Class jobClass,
                              int time, int count, Map<String, Object> jobParam) {
        try {
            // 通过工厂创建一个调度者
            Scheduler sched = sf.getScheduler();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
            if (!sched.checkExists(triggerKey)) {
                JobDetail job = newJob(jobClass).withIdentity(jobName, jobGroupName).build();// 任务名，任务组，任务执行类
                if (jobParam != null && jobParam.size() > 0) {
                    //给job添加参数
                    job.getJobDataMap().putAll(jobParam);
                }
                // 触发器
                Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, triggerGroupName)
                        .startAt(futureDate(time, IntervalUnit.SECOND))
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(time).withRepeatCount(count))
                        .build();
                Date ft = sched.scheduleJob(job, trigger);
                Log.info(jobName + "启动于" + ft);
                // 启动
                if (!sched.isShutdown()) {
                    sched.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除调度任务
     */
    public static void removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName) {
        try {
            Scheduler sched = sf.getScheduler();
            TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroupName);
            JobKey jobKey = new JobKey(jobName, jobGroupName);
            sched.pauseTrigger(triggerKey);//停止触发器
            sched.unscheduleJob(triggerKey);//移除触发器
            sched.deleteJob(jobKey);//删除任务
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重置调度任务
     */
    public static void resetJob(String jobName, String jobGroupName,
                                String triggerName, String triggerGroupName, Class jobClass,
                                int time, int count, Map<String, Object> jobParam) {
        removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
        addJob(jobName, jobGroupName, triggerName, triggerGroupName, jobClass, time, count, jobParam);
    }
}
