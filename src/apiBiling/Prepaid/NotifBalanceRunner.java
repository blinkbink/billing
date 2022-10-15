package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.util.JobListeners;
import id.co.keriss.consolidate.util.NotifBalanceJob;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpublish.JPublishContext;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;

import com.anthonyeden.lib.config.Configuration;


public class NotifBalanceRunner extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) 
	{
		HttpServletRequest  request  = context.getRequest();
		List<FileItem> fileItems=null;
		String jsonString=null;

        JobDetail job = JobBuilder.newJob(NotifBalanceJob.class)
                .withIdentity("JobSendBalanceMinimum", "Billing").build();


        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("TriggerCheck", "Billing")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
//                		  CronScheduleBuilder.cronSchedule("0 0 12 * * ?"))
                .build();

        JSONObject jsonRecv = null;
        Scheduler scheduler = null;
       
		try {
			ServletFileUpload upload = new ServletFileUpload(
					new DiskFileItemFactory());
	
			// parse requests
			 try {
				fileItems = upload.parseRequest(request);
			} catch (FileUploadException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			for (FileItem fileItem : fileItems) {
				// a regular form field
				if (fileItem.isFormField()) {
					if(fileItem.getFieldName().equals("jsonfield")){
						jsonString=fileItem.getString();
					}
				}
			}
			
			jsonRecv = new JSONObject(jsonString).getJSONObject("JSONFile");
			
			if(jsonRecv.getBoolean("action"))
			{
				scheduler = new StdSchedulerFactory().getScheduler();
				scheduler.start();
				scheduler.scheduleJob(job, trigger);
			}
			else
			{
				scheduler.shutdown();
			}
			
		} catch (SchedulerException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

		
//		final JobKey jobKey = new JobKey("Billing", "Digisign Balance Notification");
//		final JobDetail job = JobBuilder.newJob(NotifBalanceJob.class).withIdentity(jobKey).build();
//
//		final Trigger trigger = TriggerBuilder.newTrigger().withIdentity("Billing", "Digisign")
////				.withSchedule(CronScheduleBuilder.cronSchedule("* 0 * * * ?")).build();
//				.withSchedule(CronScheduleBuilder.cronSchedule("*/1 * * * * ?")).build();
//
//		Scheduler scheduler = null;
//
//		// Create a new file upload handler
//		ServletFileUpload upload = new ServletFileUpload(
//				new DiskFileItemFactory());
//
//		// parse requests
//		 try {
//			fileItems = upload.parseRequest(request);
//		} catch (FileUploadException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//
//		// Process the uploaded items
//		for (FileItem fileItem : fileItems) {
//			// a regular form field
//			if (fileItem.isFormField()) {
//				if(fileItem.getFieldName().equals("jsonfield")){
//					jsonString=fileItem.getString();
//				}
//			}
//		}
//		
//		JSONObject jsonRecv = null;
//		try {
//			jsonRecv = new JSONObject(jsonString).getJSONObject("JSONFile");
//		} catch (JSONException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//	
//		try {
//			scheduler = new StdSchedulerFactory().getScheduler();
//		} catch (SchedulerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// Listener attached to jobKey
//		try {
//			scheduler.getListenerManager().addJobListener(new JobListeners(), KeyMatcher.keyEquals(jobKey));
//		} catch (SchedulerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		try {
//			if(jsonRecv.getBoolean("action"))
//			{
//				try {
//					scheduler.start();
//					scheduler.scheduleJob(job, trigger);
//				} catch (SchedulerException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			else
//			{
//				 try {
//						scheduler.shutdown();
//					} catch (SchedulerException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			}
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//}