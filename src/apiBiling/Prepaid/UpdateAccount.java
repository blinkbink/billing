package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
//import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.dao.TenantDao;
//import id.co.keriss.consolidate.ee.Account;
//import id.co.keriss.consolidate.ee.MasterBalance;
//import id.co.keriss.consolidate.ee.Tenant;
//import org.hibernate.Transaction;
import id.co.keriss.consolidate.ee.Tenant;
import id.co.keriss.consolidate.util.LogSystem;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.Accounts;
//import org.jpos.ee.AccountsManager;
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;


public class UpdateAccount extends ActionSupport {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
//		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		String uuid = UUID.randomUUID().toString().replace("-", "");

		List<FileItem> fileItems=null;
//		LogSystem.info(request, "DATA DEBUG :"+(i++), uuid);
		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				
				// no multipart form
				if (!isMultipart) {
					LogSystem.info(request, "Bukan multipart", uuid);
					JSONObject jo=new JSONObject();
					jo.put("result", "30");
					jo.put("notif", "Format request bukan multipart");
					jo.put("log", uuid);
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
	        jo=UpdateAccount(jsonRecv, context, request, uuid);
	         
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
				jo.put("notif", "Error update billing account");
				jo.put("log", uuid);
				LogSystem.info(request, jo.toString(), uuid);
				
				try
				{
					TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
					bot.execute(new SendMessage(213382980, "Failed create billing account : " + uuid));
				}catch(Exception t)
				{
					LogSystem.error(request, "Failed send message telegram", uuid);
					LogSystem.error(getClass(), e, uuid);
				}
				
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject UpdateAccount(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException{
		long startTime = System.nanoTime();
		NumberFormat formatter = new DecimalFormat("#0.00000");
		DB db = new DB();
		try {
			
			db = getDB(context);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		JSONObject jo=new JSONObject();

        String notif="Account already exist";
//        List<String> tenantaccount = new ArrayList<String>();

//        ApiSystemDao maintanance = new ApiSystemDao(db);
//        
//		LogSystem.info(request, "Maintanance : " + maintanance.checkSystem(), uuid);
		
		InetAddress inetAddress = null;
		
		try {
			inetAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//		if(maintanance.checkSystem())
//		{
//			notif="Billing API under maintanance";
//	  	  	jo.put("result", "1122");
//			jo.put("notif", notif);
//			jo.put("log", uuid);
//			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//			LogSystem.info(request, jo.toString(), uuid);
//			return jo;
//		}
        
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
  		if(!jsonRecv.has("data")) 
  	  	{
  	  		notif="JSON data name does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
  	  	}
  		
  		TenantDao tdao = new TenantDao(db);
  		Boolean update = false;
  	  	try {
	  	  	JSONArray sendList = null;
  	  		try
  	  		{
  	  			sendList=(JSONArray) jsonRecv.getJSONArray("data");
  	  		}catch(Exception e)
  	  		{
	  	  		notif="data not JSONArray";
		  	  	jo.put("result", "28");
				jo.put("notif", notif);
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
				LogSystem.info(request, jo.toString(), uuid);
				return jo;
  	  		}
  	  		
	  	  	for(int i=0; i<sendList.length(); i++) 
			{
				JSONObject obj=(JSONObject) sendList.get(i);
				
		  	  	if(!obj.has("tenant")) 
			  	{
			  		notif="JSON tenant does not exist at array" + i;
			  	  	jo.put("result", "28");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
			  	}
		  	  	if(!obj.has("email")) 
			  	{
			  		notif="JSON email does not exist at array" + i;
			  	  	jo.put("result", "28");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
			  	}
		  	  	if(!obj.has("balance")) 
			  	{
			  		notif="JSON balance does not exist at array" + i;
			  	  	jo.put("result", "28");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
			  	}
		  	  	if(!obj.has("set")) 
			  	{
			  		notif="JSON set does not exist at array" + i;
			  	  	jo.put("result", "28");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
			  	}
		  	  		
		  	  	AccountsDao acd = new AccountsDao(db);
		  	  	
		  	  	
		  	  	//Bug Fixing null tenant query from v1.3 to 1.5
		  	  	Tenant tenant = tdao.findByApiKey(obj.getString("tenant"));
		  	  	 
		  	  	if (tenant == null)
		  	  	{
			  	  	notif="Tenant not exist";
			  	  	jo.put("result", "05");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
		  	  	}
		  	  	//end bug fixing
		  	  	
		  	  	Accounts account = acd.findByExternalKey(jsonRecv.getString("externalkey"), tenant.getRecord_id());
		  	  	
		  	  	if (account == null)
		  	  	{
			  	  	notif="Account with tenant " + obj.getString("tenant") + " not exist";
			  	  	jo.put("result", "05");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
		  	  	}
		  	  	
		  	  	account.setEmail(obj.getString("email"));
		  	  	account.setSend_notif_balance(obj.getBoolean("set"));
		  	  	account.setNotif_balance(BigDecimal.valueOf(obj.getLong("balance")));
		  	  	
		  	  	try {
		  	  		update = new AccountsDao(db).updateAccount(account);
		  	  	}catch(Exception e)
		  	  	{
			  	  	notif="Error update account";
			  	  	jo.put("result", "06");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					
					try
					{
						TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
						bot.execute(new SendMessage(213382980, "Error update billing account : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Externalkey : " + jsonRecv.getString("externalkey")));
					}catch(Exception t)
					{
						LogSystem.error(request, "Failed send message telegram", uuid);
						LogSystem.error(getClass(), e, uuid);
					}
		  	  	}
			}
	  	  	
	  	  	if (update)
	  		{
		  	  	jo.put("result", "00");
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));

				LogSystem.info(request, jo.toString(), uuid);
				return jo;
	  		}
	  		else
	  		{
		  	  	jo.put("result", "05");
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));

				LogSystem.info(request, jo.toString(), uuid);
				return jo;
	  		}
	  	  
  	  	}catch(RuntimeException e)
  	  	{
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Error update billing account";
	  	  	jo.put("result", "06");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			
			try
			{
				TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
				bot.execute(new SendMessage(213382980, "Error update billing account : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Externalkey : " + jsonRecv.getString("externalkey")));
			}catch(Exception t)
			{
				LogSystem.error(request, "Failed send message telegram", uuid);
				LogSystem.error(getClass(), e, uuid);
			}
			
			return jo;
  	  	}  	
  	  	finally {
			db.session().close();
			db.close();
	  	}
	}
}