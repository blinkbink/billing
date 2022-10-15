package id.co.keriss.consolidate.util;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;


public class JobListeners implements JobListener {

	public String getName() {
		return "HelloJcgJobListener";
	}

	public void jobToBeExecuted(JobExecutionContext context) {
		final String jobName = context.getJobDetail().getKey().toString();
		System.out.println("Notification Balance scheduler : " + jobName + " is starting...");
	}

	public void jobExecutionVetoed(JobExecutionContext context) {
		System.out.println("jobExecutionVetoed");
	}

	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		System.out.println("jobWasExecuted");

		final String jobName = context.getJobDetail().getKey().toString();
		System.out.println("Job Checking Balance : " + jobName + " is finished!!");

	}

}