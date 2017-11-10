package protocol.process.event.job;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import protocol.process.ProtocolProcess;

/**
 * Publish消息重发事件需要做的工作，即重发消息到对应的clientId
 */
public class RePubRelJob implements Job {
    private final static Logger Log = Logger.getLogger(RePubRelJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 取出参数，参数为ProtocolProcess，调用此类的函数
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        ProtocolProcess process = (ProtocolProcess) dataMap.get("ProtocolProcess");
        String pubRelKey = (String) dataMap.get("pubRelKey");
        // 在未收到对应包的情况下，重传PubRel消息
        process.reUnKnowPubRelMessage(pubRelKey);
    }
}