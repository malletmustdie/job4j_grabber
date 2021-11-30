package com.elias.grabber.grab;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.elias.grabber.Parse;
import com.elias.grabber.SqlRuParse;
import com.elias.grabber.store.PsqlStore;
import com.elias.grabber.store.Store;
import com.elias.grabber.utils.DateTimeParser;
import com.elias.grabber.utils.SqlRuDateTimeParser;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class Grabber implements Grab {

    private static final String PATH = "C:\\projects\\job4j_grabber\\src\\main\\resources\\app.properties";
    private static final String SQL_RU_URL = "https://www.sql.ru/forum/job-offers/";

    private final Properties cfg = new Properties();

    public Store store() {
        return new PsqlStore(cfg);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void cfg() throws IOException {
        try (InputStream in = new FileInputStream(PATH)) {
            cfg.load(in);
        }
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = JobBuilder.newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                .repeatForever();
        Trigger trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            parse.list(SQL_RU_URL)
                 .forEach(store::save);
        }

    }

    public static void main(String[] args) throws Exception {
        DateTimeParser dateParser = new SqlRuDateTimeParser();
        Grabber grab = new Grabber();
        grab.cfg();
        Scheduler scheduler = grab.scheduler();
        Store store = grab.store();
        grab.init(new SqlRuParse(dateParser), store, scheduler);
    }

}
