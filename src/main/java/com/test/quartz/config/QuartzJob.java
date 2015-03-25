package com.test.quartz.config;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

@Service
public class QuartzJob implements Job {

	private JobStarter jobStarter;

	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		try {
			SchedulerContext scontext = context.getScheduler().getContext();
			jobStarter = (JobStarter) scontext
					.get("jobStarter");

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		jobStarter.startJob();
	}
}