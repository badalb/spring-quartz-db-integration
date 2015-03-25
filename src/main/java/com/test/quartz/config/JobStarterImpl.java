package com.test.quartz.config;

import org.springframework.stereotype.Service;

@Service
public class JobStarterImpl implements JobStarter {

	@Override
	public void startJob() {
		System.out.println("Job started successfully........");
	}

}
