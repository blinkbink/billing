package apiBiling.Postpaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Tenant;
import id.co.keriss.consolidate.ee.Transactions;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.LogSystem;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
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
import org.jpos.ee.Accounts;
import org.jpos.ee.AccountsManager;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;


public class CreateAccountPostpaid extends ActionSupport {

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
				jo.put("notif", "Error create postpaid billing account");
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject CreateBilling(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException{
		long startTime = System.nanoTime();
		NumberFormat formatter = new DecimalFormat("#0.00000");
		DB db = new DB();
		
		InetAddress inetAddress = null;
		
		try {
			inetAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		try {
			
			db = getDB(context);
		}catch(Exception e)
		{
			e.printStackTrace();
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
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
		}
        
  	  	if(!jsonRecv.has("externalkey")) 
  	  	{
  	  		notif="JSON externalkey does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
  	  	}
  	
  	  	Transaction tx = db.session().getTransaction();

  	  	try {
  	  		tx.setTimeout(15);
  	  		tx.begin();
  	  		
  			Accounts account = new Accounts();
  			TenantDao tdo = new TenantDao(db);
  			if(tdo.findByType("postpaid") != null)
  			{
  				Tenant tenantData = tdo.findByType("postpaid");
  			  	 
  				AccountsDao acdo = new AccountsDao(db);
  				
  				if(acdo.findByExternalKey(jsonRecv.getString("externalkey"), tenantData.getRecord_id()) != null)
  				{
  					notif="Account postpaid already exist";
  	  		  	  	jo.put("result", "05");
  	  				jo.put("notif", notif);
  	  				jo.put("log", uuid);
  	  				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
  	  				LogSystem.info(getClass(), jo.toString());
  	  				return jo;
  				}
  				else
  				{
  					account.setId(UUID.randomUUID().toString());
  	  	  	  		account.setExternal_key(jsonRecv.getString("externalkey"));
  	  	  	  		account.setName(jsonRecv.getString("name"));
  	  	  	  		account.setEmail(jsonRecv.getString("email"));
  	  	  	  		account.setAddress1(jsonRecv.getString("address"));
  	  	  	  		account.setCreated_date(new Date());
  	  	  	  		account.setUpdated_date(new Date());
  	  	  	  		account.setPhone(jsonRecv.getString("phone"));
  	  	  	  		account.setCompany_name(jsonRecv.getString("company_name"));
  	  	  	  		account.setTenant_record_id(tenantData);
  	  	  	  		account.setNotif_balance(new BigDecimal(200));
  	  	  	  		account.setNotif_email(false);
  	  	  	  		account.setSend_notif_balance(false);
  	  	  	  		db.session().save(account);
  	  	  	  		
  	  	  	  		tx.commit ();
  	  	  	  		
  	  		  	  	notif="Success create postpaid billing account";
  	  		  	  	jo.put("result", "00");
  	  				jo.put("notif", notif);
  	  				jo.put("log", uuid);
	  				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
  	  				LogSystem.info(getClass(), jo.toString());
  	  				return jo;
  				}
  			}
  			else
  			{
  				notif="Tenant not exist";
  		  	  	jo.put("result", "05");
  				jo.put("notif", notif);
  				jo.put("log", uuid);
	  			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
  				LogSystem.info(getClass(), jo.toString());
  				return jo;
  			}
  	 		
  	  	}catch(RuntimeException e)
  	  	{
  	  		tx.rollback();
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Error create postpaid billing account";
	  	  	jo.put("result", "06");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			try
			{
				TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
				bot.execute(new SendMessage(213382980, "Error create postpaid transaction : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Externalkey : " + "\n externalkey : " + jsonRecv.getString("externalkey") + "\n Message : " + e));
			}catch(Exception t)
			{
				LogSystem.error(request, "Failed send message telegram", uuid);
				LogSystem.error(getClass(), e, uuid);
			}
			return jo;
  	  	}  	
	}
}