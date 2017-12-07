package protocol.process.event.job;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import protocol.process.ProtocolProcess;
import protocol.process.event.PubRelEvent;

/**
 * Publish消息重发事件需要做的工作，即重发消息到对应的clientId
 */
public class RePublishJob implements Job {
    private final static Logger logger = Logger.getLogger(RePublishJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 取出参数，参数为ProtocolProcess，调用此类的函数
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        ProtocolProcess process = (ProtocolProcess) dataMap.get("ProtocolProcess");
        String publishKey = (String) dataMap.get("publishKey");
        // 在未收到对应包的情况下，重传Publish消息
        process.reUnKnowPublishMessage(publishKey);
    }
}