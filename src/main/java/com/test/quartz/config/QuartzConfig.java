package com.test.quartz.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class QuartzConfig {

	@Autowired
	@Qualifier("resamDataSource")
	private DataSource dataSource;

	@Autowired
	@Qualifier("resamTransactionManager")
	private PlatformTransactionManager transactionManager;

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void init() {
		System.out.println("initalizing quartz config ......");
	}

	@Autowired
	private JobStarter jobStarter;

	@Value("${resam.batch.cron.exprn}")
	private String quartzConfig;

	@Bean
	@DependsOn("flyway")
	public SchedulerFactoryBean schedularFactoryBean() {
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		schedulerFactoryBean.setDataSource(dataSource);
		schedulerFactoryBean.setTransactionManager(transactionManager);
		schedulerFactoryBean.setOverwriteExistingJobs(true);
		schedulerFactoryBean.setSchedulerName("quartz-scheduler");

		schedulerFactoryBean.setConfigLocation(new ClassPathResource(
				"quartz.properties"));
		schedulerFactoryBean.setJobFactory(jobFactory());
		schedulerFactoryBean
				.setApplicationContextSchedulerContextKey("applicationContext");

		schedulerFactoryBean.setApplicationContext(applicationContext);

		schedulerFactoryBean.setSchedulerContextAsMap(schedulerContextAsMap());

		Trigger[] triggers = { procesoTrigger().getObject() };
		schedulerFactoryBean.setTriggers(triggers);

		schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
		return schedulerFactoryBean;
	}

	private Map<String, Object> schedulerContextAsMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("jobStarter", jobStarter);
		return map;
	}

	@Bean
	public SpringBeanJobFactory jobFactory() {
		SpringBeanJobFactory factory = new SpringBeanJobFactory();
		return factory;
	}

	@Bean
	public JobDetailFactoryBean procesoJob() {
		JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(QuartzJob.class);
		jobDetailFactory.setDurability(true);
		jobDetailFactory.setRequestsRecovery(false);
		jobDetailFactory.setGroup("spring-quartz");
		return jobDetailFactory;
	}

	@Bean
	public CronTriggerFactoryBean procesoTrigger() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(procesoJob().getObject());
		cronTriggerFactoryBean.setCronExpression(quartzConfig);
		cronTriggerFactoryBean.setGroup("spring-quartz");
		return cronTriggerFactoryBean;
	}

}
