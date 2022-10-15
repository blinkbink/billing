package id.co.keriss.consolidate.util;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;

import apiBiling.Postpaid.PostpaidRunner;

public class NotifBalanceScheduler {

	public static void main(String[] args) throws Exception {

		final JobKey jobKey = new JobKey("Billing", "Digisign");
		final JobDetail job = JobBuilder.newJob(TaskJob.class).withIdentity(jobKey).build();

		final Trigger trigger = TriggerBuilder.newTrigger().withIdentity("Billing", "Digisign")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 59 23 * * ?")).build();

		final Scheduler scheduler = new StdSchedulerFactory().getScheduler();

		// Listener attached to jobKey
		scheduler.getListenerManager().addJobListener(new JobListeners(), KeyMatcher.keyEquals(jobKey));

		scheduler.start();
		scheduler.scheduleJob(job, trigger);
		scheduler.shutdown();
	}
}
