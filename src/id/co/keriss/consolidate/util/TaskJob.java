package id.co.keriss.consolidate.util;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TaskJob implements Job {
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		System.out.println("Checking postpaid " + new Date());
	
	}

}
