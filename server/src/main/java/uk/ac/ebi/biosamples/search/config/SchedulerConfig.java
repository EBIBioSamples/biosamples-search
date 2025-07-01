package uk.ac.ebi.biosamples.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadFactory;

@Configuration
public class SchedulerConfig {

  @Bean
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    ThreadFactory virtualThreadFactory = Thread.ofVirtual().name("scheduled-task-", 0).factory();
    scheduler.setThreadFactory(virtualThreadFactory);
    scheduler.setPoolSize(10);
    return scheduler;
  }
}
