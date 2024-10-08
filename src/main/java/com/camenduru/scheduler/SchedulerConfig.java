package com.camenduru.scheduler;

import java.util.Date;
import java.util.concurrent.Executor;

import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.camenduru.scheduler.domain.enumeration.JobStatus;
import com.camenduru.scheduler.repository.SettingRepository;
import com.camenduru.scheduler.repository.JobRepository;

@Configuration
@EnableAsync
@EnableScheduling
public class SchedulerConfig implements AsyncConfigurer, SchedulingConfigurer {

    @Value("${camenduru.scheduler.default.free.total}")
    private String defaultFreeTotal;

    @Value("${camenduru.scheduler.default.paid.total}")
    private String defaultPaidTotal;
    
    @Value("${camenduru.scheduler.cron1}")
    private String cron1;

    @Value("${camenduru.scheduler.cron2}")
    private String cron2;

    @Value("${camenduru.scheduler.cron3}")
    private String cron3;

    @Value("${camenduru.scheduler.cron4}")
    private String cron4;

    @Value("${camenduru.scheduler.cron5}")
    private String cron5;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SettingRepository settingRepository;
    
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(100);
        executor.setThreadNamePrefix("schedule-pool-");
        executor.initialize();
        return executor;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(getAsyncExecutor());
        taskRegistrar.addCronTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Date twelveHoursAgo = new Date(System.currentTimeMillis() - (12 * 60 * 60 * 1000));
                    jobRepository.findAllNonExpiredJobsOlderThanTheDate(twelveHoursAgo)
                        .forEach(job -> {
                            if (!job.getType().startsWith("train")) {
                                job.setStatus(JobStatus.EXPIRED);
                                jobRepository.save(job);
                            }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, cron1);
        taskRegistrar.addCronTask(new Runnable() {
            @Override
            public void run() {
                try {
                    settingRepository.findAllByMembershipIsFree()
                        .forEach(setting -> {
                            if (Integer.parseInt(setting.getTotal()) < Integer.parseInt(defaultFreeTotal)) {
                                setting.setTotal(defaultFreeTotal);
                                settingRepository.save(setting);
                            }
                        });
                    settingRepository.findAllByMembershipIsPaid()
                        .forEach(setting -> {
                            if (Integer.parseInt(setting.getTotal()) < Integer.parseInt(defaultPaidTotal)) {
                                setting.setTotal(defaultPaidTotal);
                                settingRepository.save(setting);
                            }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, cron2);
        taskRegistrar.addCronTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Date tenMinutesAgo = new Date(System.currentTimeMillis() - (10 * 60 * 1000));
                    jobRepository.findAllWorkingJobsOlderThanTheDate(tenMinutesAgo)
                        .forEach(job -> {
                            if (!job.getType().startsWith("train")) {
                                job.setStatus(JobStatus.FAILED);
                                jobRepository.save(job);
                            }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, cron3);
        taskRegistrar.addCronTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Date fortyMinutesAgo = new Date(System.currentTimeMillis() - (40 * 60 * 1000));
                    jobRepository.findAllWorkingJobsOlderThanTheDate(fortyMinutesAgo)
                        .forEach(job -> {
                            if (job.getType().startsWith("train")) {
                                job.setStatus(JobStatus.FAILED);
                                jobRepository.save(job);
                            }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, cron4);
        taskRegistrar.addCronTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Date fourDaysAgo = new Date(System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000));
                    jobRepository.findAllNonExpiredJobsOlderThanTheDate(fourDaysAgo)
                        .forEach(job -> {
                            if (job.getType().startsWith("train")) {
                                job.setStatus(JobStatus.EXPIRED);
                                jobRepository.save(job);
                            }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, cron5);
    }

}