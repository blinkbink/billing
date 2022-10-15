package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.dao.MasterBalanceDao;
import id.co.keriss.consolidate.dao.TenantDao;
//import id.co.keriss.consolidate.ee.InvoiceItems;
//import id.co.keriss.consolidate.ee.Invoices;
import id.co.keriss.consolidate.ee.MasterBalance;
import id.co.keriss.consolidate.ee.Tenant;
//import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.LogSystem;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
//import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.Accounts;
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

public class BalanceDashboard extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("billingLog");

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		
		String uuid = UUID.randomUUID().toString().replace("-", "");
		JSONObject obalance = new JSONObject();
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
	        jo=Balance(jsonRecv, context, request, uuid);
	         
	         
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
				jo.put("notif", "Failed get balance");
				jo.put("log", uuid);
				
				InetAddress inetAddress = null;
				try {
					inetAddress = InetAddress.getLocalHost();
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try
				{
					TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
					bot.execute(new SendMessage(213382980, "Failed get Balance : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : " + e));
				}catch(Exception t)
				{
					LogSystem.error(request, "Failed send message telegram", uuid);
					LogSystem.error(getClass(), e, uuid);
				}
				LogSystem.info(request, jo.toString(), uuid);
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject Balance(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException{
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
  		
  	  	try {

  	  		ArrayList<String> listtenant = new ArrayList<String>();
  	  		TenantDao tdof = new TenantDao(db);
	  	  	List<Tenant> dataTenant = tdof.findAllTenantPrepaid();
			
			for (int l = 0 ; l < dataTenant.size() ; l++)
			{
				listtenant.add(dataTenant.get(l).getApi_key());
			}
  	  		
  	  		InvoiceItemsDao invoiceItemsDao = new InvoiceItemsDao(db);
  	  		AccountsDao ado = new AccountsDao(db);

  	  		MasterBalanceDao mbo = new MasterBalanceDao(db);

  	  		if(!jsonRecv.has("tenant"))
  	  		{  	
  	  			List<Accounts> accountexternalkey = invoiceItemsDao.findAccount(jsonRecv.getString("externalkey"));
  	  		
  	  			JSONArray allbalance = new JSONArray();
  	  			
  	  			if(accountexternalkey.size() > 0)
				{
  	  				for(int i = 0; i < accountexternalkey.size() ; i++)
  	  				{
  	  					MasterBalance mb = mbo.Balance(accountexternalkey.get(i).getExternal_key(), accountexternalkey.get(i).getTenant_record_id().getRecord_id());
//  	  					if(accountexternalkey.get(i).getSubscription().equals("prepaid"))
//  	  					{
	  	  					if(mb != null)
	  	  					{
		  	  					JSONObject jsonObject = new JSONObject();
		  	  					LogSystem.info(request, "tenant : "+accountexternalkey.get(i).getTenant_record_id().getApi_key(), uuid);
		  	  					LogSystem.info(request, "amount : "+mb.getBalance(), uuid);
		  	  					jsonObject.put("id", accountexternalkey.get(i).getTenant_record_id().getRecord_id());
		  	  					jsonObject.put("name", accountexternalkey.get(i).getTenant_record_id().getExternal_key());
			  	  				jsonObject.put("tenant", accountexternalkey.get(i).getTenant_record_id().getApi_key());
			  	  				jsonObject.put("amount", mb.getBalance());
			  	  			
			  	  				allbalance.put(jsonObject);
	  	  					}
//	  	  					else
//	  	  					{
//	  	  						JSONObject jsonObject = new JSONObject();
//		  	  					jsonObject.put("tenant", accountexternalkey.get(i).getTenant_record_id().getApi_key());
//			  	  				jsonObject.put("amount", new BigDecimal(0));
//			  	  			
//			  	  				allbalance.put(jsonObject);
//	  	  					}
//  	  					}
//  	  					else
//  	  					{
//	  	  					JSONObject jsonObject = new JSONObject();
//	  	  					jsonObject.put("id", accountexternalkey.get(i).getTenant_record_id().getRecord_id());
//	  	  					jsonObject.put("name", accountexternalkey.get(i).getTenant_record_id().getExternal_key());
//		  	  				jsonObject.put("tenant", accountexternalkey.get(i).getTenant_record_id().getApi_key());
//		  	  				jsonObject.put("amount", 1);
//		  	  			
//		  	  				allbalance.put(jsonObject);
//  	  					}
  	  				}
				}
	  	  		else
	  	  		{
					notif="Account externalkey with that tenant does not exist";
			  	  	jo.put("result", "04");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
	  	  		}

  	  			if(allbalance.length() > 0)
  	  			{
  	  				jo.put("result", "00");
	  	  			jo.put("external_key", jsonRecv.getString("externalkey"));

		  	  		jo.put("balance", allbalance);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
		  	  		LogSystem.info(request, jo.toString(), uuid);
		  	  		return jo;
  	  			}
  	  			else
  	  			{
	  	  			notif="Balance account not exist";
			  	  	jo.put("result", "05");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
  	  			}
  	  		}	
  	  		else
			{
  		  	  	Tenant tenantData = tdof.findByApiKey(jsonRecv.getString("tenant"));
  		  	  	if(tenantData == null)
  		  	  	{
	  		  	  	notif="Tenant not exist";
			  	  	jo.put("result", "05");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
  		  	  	}
  		  	  	
  		  		Accounts account = ado.findByExternalKey(jsonRecv.getString("externalkey"), tenantData.getRecord_id());

	  	  		if(!listtenant.contains(jsonRecv.getString("tenant")))
			  	{
			  	 	LogSystem.info(request, "Tenant not available", uuid);
				  	notif="Tenant available : " + listtenant;
				  	jo.put("result", "01");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(getClass(), jo.toString());
					return jo;
				}
	  	  		
	  			if(account != null)
	  			{
	  				JSONObject obalance = new JSONObject();
//	  				if(account.getSubscription().equals("prepaid"))
//	  				{
		  				MasterBalance mb = mbo.Balance(account.getExternal_key(), account.getTenant_record_id().getRecord_id());
		  				if (mb == null)
		  				{
		  					notif="Account externalkey with that tenant does not exist";
					  	  	jo.put("result", "04");
							jo.put("notif", notif);
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							LogSystem.info(request, jo.toString(), uuid);
							return jo;
		  				}
		  				LogSystem.info(request, "Value balance : " + mb.getBalance(), uuid);
		  	  			
		  				obalance.put("id", tenantData.getRecord_id());
		  				obalance.put("name", tenantData.getExternal_key());
		  	  			obalance.put("tenant", tenantData.getApi_key());
		  	  			obalance.put("amount", mb.getBalance());
//	  				}
//	  				else
//	  				{
//	  					obalance.put("id", tenantData.getRecord_id());
//	  					obalance.put("name", tenantData.getExternal_key());
//		  	  			obalance.put("tenant", tenantData.getApi_key());
//		  	  			obalance.put("amount", 1);
//	  				}
	  				
	  	
		  	  		jo.put("external_key", jsonRecv.getString("externalkey"));
		  	  		jo.put("data", obalance);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					
		  	  		LogSystem.info(request, jo.toString(), uuid);
		  	  		return jo;
	  			}
	  			else
	  			{
	  	  			notif="Balance account not exist";
			  	  	jo.put("result", "05");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
	  			}
				

			}
  	  	}catch(RuntimeException e)
  	  	{
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Failed get balance";
	  	  	jo.put("result", "06");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			try
			{
				TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
				bot.execute(new SendMessage(213382980, "Error get balance : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Externalkey : " + "\n externalkey : " + jsonRecv.getString("externalkey") + "\n Message : " + e));
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