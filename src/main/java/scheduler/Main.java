package scheduler;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scheduler.jobs.UpdateRankingJob;

public class Main {
    
    public static void main(String[] args) throws Exception
    {
        logger.info("Started main");
        
        if (!Options.Init(args)) {
            return;
        }
        
        // Update Ranking Job
        JobDetail job = newJob(UpdateRankingJob.class).withIdentity("updateranking", "personal").build();
        CronTrigger trigger = newTrigger()
                .withIdentity("updateranking", "personal")
                .withSchedule(cronSchedule(Options.GetCurrent().UpdateRankingCron))
                .build();
        
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.scheduleJob(job, trigger);
        scheduler.start();
        
        logger.info("Started scheduler");
    }
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
}
