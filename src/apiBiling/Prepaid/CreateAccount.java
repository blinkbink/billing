package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.MasterBalance;
import id.co.keriss.consolidate.ee.Tenant;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.Accounts;
import org.jpos.ee.AccountsManager;
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;


public class CreateAccount extends ActionSupport {

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
				jo.put("notif", "Error create billing account");
				jo.put("log", uuid);
				LogSystem.info(request, jo.toString(), uuid);
				
				try
				{
					TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
					bot.execute(new SendMessage(213382980, "Failed create billing account : " + uuid + "\n Message : " + e));
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
	
	JSONObject CreateBilling(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException{
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
        List<String> tenantaccount = new ArrayList<String>();

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
  		if(!jsonRecv.has("company_name")) 
  	  	{
  	  		notif="JSON company name does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
  	  	}
  	  	if(!jsonRecv.has("name")) 
	  	{
	  		notif="JSON name does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
	  	}
  	  	if(!jsonRecv.has("email")) 
	  	{
	  		notif="JSON email does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
	  	}
  	  	if(!jsonRecv.has("address")) 
	  	{
	  		notif="JSON address does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
	  	}
  	  	if(!jsonRecv.has("phone")) 
	  	{
	  		notif="JSON phone does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
	  	}
  	
  	  	Transaction tx = db.session().getTransaction();

  	  	try {
  	  		tx.setTimeout(18);
  	  		tx.begin();
  	  		
  	  		ArrayList<String> listtenant = new ArrayList<String>();
  	  		ArrayList<Long> idlisttenant = new ArrayList<Long>();

  	  		AccountsManager am = new AccountsManager(db);
			TenantDao tdof = new TenantDao(db);
			
			List<Tenant> dataTenant = tdof.findAllTenantPrepaid();
			
			LogSystem.info(request, "Arraylist tenant : " + listtenant, uuid);
			
			for (int l = 0 ; l < dataTenant.size() ; l++)
			{
				listtenant.add(dataTenant.get(l).getApi_key());
				idlisttenant.add(dataTenant.get(l).getRecord_id());
			}
			
//  	 		String subscription = null;
//  	 		
//  	 		if(jsonRecv.has("subscription"))
//  	 		{
//  	 			subscription = jsonRecv.getString("subscription");
//  	 		}
//  	 		else
//  	 		{
//  	 			subscription = "prepaid";
//  	 		}
			
  	  		if(jsonRecv.has("tenant"))
  	  		{
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
	  	  	 	else
	  	  	 	{
	  	  	 		LogSystem.info(request, "Create account", uuid);
		  	  	 	Accounts account = new Accounts();
		
	  	  	 		TenantDao tdo = new TenantDao(db);
	  	  	 		Tenant tenantData = tdo.findByApiKey(jsonRecv.getString("tenant"));
	  	  	 		
	  	  	 		MasterBalance mb = new MasterBalance();
	  	  	 		
	  	  	 		if(am.findByKeyTenant(jsonRecv.getString("externalkey"), tenantData.getRecord_id()) == null)
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
				  		
				  		if(jsonRecv.has("notifminimum"))
				  		{
				  			account.setNotif_balance(new BigDecimal(jsonRecv.getInt("notifminimum")));
				  		}
				  		else
				  		{
				  			account.setNotif_balance(new BigDecimal(200));
				  		}
				  		
				  		account.setNotif_email(false);
				  		account.setSend_notif_balance(false);
				  		
//				  		if(jsonRecv.has("bill_period"))
//				  		{
//				  			account.setBill_period(jsonRecv.getInt("bill_period"));
//				  		}
//				  		else
//				  		{
//				  			account.setBill_period(25);
//				  		}
//
//				  	
//		  	  			if(subscription.equals("postpaid"))
//		  	  			{
//		  	  				account.setSubscription(subscription);
//		  	  			}
//		  	  			else
//		  	  			{
//		  	  				account.setSubscription(subscription);
			  	  			mb.setBalance(new BigDecimal(0));
					  		mb.setExternalkey(account.getExternal_key());
					  		mb.setTenant_record_id(account.getTenant_record_id());

		  	  				mb.setBalance(new BigDecimal(0));
		  	  				db.session().save(mb);
//		  	  			}
	  	  	 		
				  		db.session().save(account);
				  		
			  	  		
				  		tx.commit ();
			  	  		
				  	  	notif="Success create billing account";
				  	  	jo.put("result", "00");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(getClass(), jo.toString());
						return jo;
	  	  	 		}
	  	  	 		else
	  	  	 		{
	  	  	 			notif="Billing account external key "+ jsonRecv.getString("externalkey")+" with tenant "+ jsonRecv.getString("tenant") +" already exist";
					  	jo.put("result", "02");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(request, jo.toString(), uuid);
						return jo;
	  	  	 		}
	  	  	 		
	  	  	 	}
  	  		}
  	  		else
  	  		{
  	  			LogSystem.info(request, "Create default accounts", uuid);
  	  			
  	  			List<Accounts> existingAccount = am.findByExternalKey(jsonRecv.getString("externalkey"));
//
//  	  			if(existingAccount.size() < 4)
//  	  			{
  	  				for(int b = 0 ; b < idlisttenant.size() ; b++)
		  	  		{
		  	  			Accounts account = new Accounts();
		  	  			TenantDao tdo = new TenantDao(db);
		  	  	 		List<Tenant> tenantData = tdo.findAllTenantPrepaid();
		  	  	 		MasterBalance mb = new MasterBalance();
		  	  	 		
		  	  	 		if(am.findByKeyTenant(jsonRecv.getString("externalkey"), Long.parseLong(idlisttenant.get(b).toString())) == null)
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
				  	  		account.setTenant_record_id(tenantData.get(b));
					  	  	account.setNotif_balance(new BigDecimal(200));
					  		account.setNotif_email(false);
					  		account.setSend_notif_balance(false);
					  		
//					  		if(jsonRecv.has("bill_period"))
//					  		{
//					  			account.setBill_period(jsonRecv.getInt("bill_period"));
//					  		}
//					  		else
//					  		{
//					  			account.setBill_period(25);
//					  		}
					  		
					  		tenantaccount.add(tenantData.get(b).getApi_key());

//				  	  		db.session().save(account);

//			  	  			if(subscription.equals("postpaid"))
//			  	  			{
//			  	  				account.setSubscription(subscription);
//			  	  			}
//			  	  			else
//			  	  			{
//			  	  				account.setSubscription(subscription);
			  	  				mb.setBalance(new BigDecimal(0));
				  	  			mb.setExternalkey(account.getExternal_key());
						  		mb.setTenant_record_id(account.getTenant_record_id());
				  	  			db.session().save(mb);
//			  	  			}
				  	  	
					  		db.session().save(account);
		  	  	 		}
		  	  		}
  	  				
  	  				
	  	  			if (tenantaccount.size() > 0) 
	  	  			{
	  	  				tx.commit ();
		  	  			notif="Success create billing account";
				  	  	jo.put("result", "00");
						jo.put("notif", notif);
						jo.put("tenant", tenantaccount);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(getClass(), jo.toString());
						return jo;
	  	  			}
	  	  			else
	  	  			{
		  	  			notif="All account billing already exist";
				  	  	jo.put("result", "00");
						jo.put("notif", notif);
//						jo.put("tenant", tenantaccount);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(getClass(), jo.toString());
						return jo;
	  	  			}
		  	  		
			  	  	
//  	  			}
//  	  			else
//  	  			{
//	  	  			notif="All account billing already exist";
//				  	jo.put("result", "02");
//					jo.put("notif", notif);
//					jo.put("log", uuid);
//					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//					LogSystem.info(getClass(), jo.toString());
//					return jo;
//  	  			}
  	  		}
  	  		
  	  	}catch(RuntimeException e)
  	  	{
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Error create billing account";
	  	  	jo.put("result", "06");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			
			try
			{
				TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
				bot.execute(new SendMessage(213382980, "Error create billing account : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Externalkey : " + jsonRecv.getString("externalkey") + "\n Message : " + e));
			}catch(Exception t)
			{
				LogSystem.error(request, "Failed send message telegram", uuid);
				LogSystem.error(getClass(), e, uuid);
			}
			
			return jo;
  	  	}  	
  	  	finally {
  	  		LogSystem.info(request, "All process done, close connection ", uuid);
			db.session().close();
			db.close();
	  	}
	}
}