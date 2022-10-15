package apiBiling.Postpaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.Tenant;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.LogSystem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;


public class PostpaidUsage extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		String uuid = UUID.randomUUID().toString().replace("-", "");
		List<FileItem> fileItems=null;
		LogSystem.info(request, "DATA DEBUG :"+(i++), uuid);

		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				
				// no multipart form
				if (!isMultipart) {
					LogSystem.info(request, "Bukan multipart", uuid);
					JSONObject jo=new JSONObject();
					jo.put("result", "30");
					jo.put("notif", "Format request API salah.");
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					
					return;
				}
				
				// multipart form
				else {
			
					ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

					fileItems = upload.parseRequest(request);

					for (FileItem fileItem : fileItems) {
						if (fileItem.isFormField()) {
							if(fileItem.getFieldName().equals("jsonfield")){
								jsonString=fileItem.getString();
							}
						}
					}
				}
			
	        LogSystem.info(request, "PATH :"+request.getRequestURI(), uuid);
	        LogSystem.request(request, fileItems, uuid);
			if(jsonString==null) return;	         
	        JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");         
	         
	        JSONObject jo = null;
	        jo=CreateBilling(jsonRecv, context, request, uuid);

			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
			context.put("trxjson", res);
			LogSystem.response(request, jo, uuid);

		}catch (Exception e) {
            LogSystem.error(getClass(), e, uuid);

            JSONObject jo=new JSONObject();
            try {
				jo.put("result", "06");
				jo.put("notif", "Failed check postpaid usage");
				LogSystem.info(request, jo.toString(), uuid);
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	JSONObject CreateBilling(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException{

		DB db = new DB();
		try {
			
			db = getDB(context);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		InetAddress inetAddress = null;
		
		try {
			inetAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		JSONObject jo=new JSONObject();

        String notif="Account already exist";

        ApiSystemDao maintanance = new ApiSystemDao(db);
        
		LogSystem.info(request, "Maintanance : " + maintanance.checkSystem(), uuid);
		
		if(maintanance.checkSystem())
		{
			notif="Billing API under maintanance";
	  	  	jo.put("result", "1122");
			jo.put("notif", notif);
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
		}
        
  	  	if(!jsonRecv.has("externalkey")) 
  	  	{
  	  		notif="JSON externalkey does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
  	  	}

  	  	Transaction tx = db.session().getTransaction();

  	  	try {
  	  		tx.setTimeout(15);
  	  		tx.begin();
  	  		
	  	  	AccountsDao ado = new AccountsDao(db);
	  	  	TenantDao tdo = new TenantDao(db);
	  	  	
	  	  	Tenant tenantData = null;
	  	  	
	  	  	if(tdo.findByType("postpaid") != null)
	  	  	{
	  	  		tenantData = tdo.findByType("postpaid");
	  	  	}
	  	  	else
	  	  	{
		  	  	notif="Tenant postpaid not exist";
		  	  	jo.put("result", "05");
				jo.put("notif", notif);
				LogSystem.info(request, jo.toString(), uuid);
				return jo;
	  	  	}
	  		
		  	if(ado.findByExternalKey(jsonRecv.getString("externalkey"), tenantData.getRecord_id()) == null)
			{
				notif="Account postpaid not exist";
		  	  	jo.put("result", "05");
				jo.put("notif", notif);
				LogSystem.info(request, jo.toString(), uuid);
				return jo;
			}
		  	else
		  	{
		  		InvoiceItemsDao ido = new InvoiceItemsDao(db);
		  		
		  		if(ido.findTotalUsage(jsonRecv.getString("externalkey")).length() > 0)
				{
		  			Date end_date = ido.findPostpaidTransactionActive(jsonRecv.getString("externalkey")).getEnd_date();
		  			Date start_date = ido.findPostpaidTransactionActive(jsonRecv.getString("externalkey")).getStart_date();

		  			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");  
		  			formatter = new SimpleDateFormat("yyyy-MM-dd");  
		  			
			  	  	jo.put("result", "00");
					jo.put("usage", ido.findTotalUsage(jsonRecv.getString("externalkey")));
					jo.put("start_date", formatter.format(start_date));
					jo.put("end_date", formatter.format(end_date));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
				}
		  		else
		  		{
		  			notif="No usage active";
			  	  	jo.put("result", "05");
					jo.put("notif", notif);
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
		  		}

		  	}
  	  		
  	  	}catch(RuntimeException e)
  	  	{
  	  		tx.rollback();
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Failed check postpaid usage";
	  	  	jo.put("result", "06");
			jo.put("notif", notif);
			try
			{
				TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
				bot.execute(new SendMessage(213382980, "Error check postpaid usage : " + uuid + "\nAddress : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\nExternalkey : " + "\nexternalkey : " + jsonRecv.getString("externalkey") + "\nMessage : " + e));
			}catch(Exception t)
			{
				LogSystem.error(request, "Failed send message telegram", uuid);
				LogSystem.error(getClass(), e, uuid);
			}
			return jo;
  	  	}  	
	}
}