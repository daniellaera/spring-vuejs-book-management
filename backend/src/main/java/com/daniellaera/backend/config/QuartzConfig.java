package com.daniellaera.backend.config;

import com.daniellaera.backend.properties.QuartzSchedulerProperties;
import com.daniellaera.backend.scheduler.BookStatusUpdateJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    private final QuartzSchedulerProperties quartzSchedulerProperties;

    public QuartzConfig(QuartzSchedulerProperties quartzSchedulerProperties) {
        this.quartzSchedulerProperties = quartzSchedulerProperties;
    }

    @Bean
    public JobDetail borrowExpirationJobDetail() {
        return JobBuilder.newJob(BookStatusUpdateJob.class)
                .withIdentity("bookStatusUpdateJobDetail")
                .storeDurably()  // Keep the job even if no triggers are associated
                .build();
    }

    @Bean
    public Trigger borrowExpirationTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(borrowExpirationJobDetail())
                .withIdentity("bookStatusUpdateTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(quartzSchedulerProperties.getCron()))
                .build();
    }
}
