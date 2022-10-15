package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.dao.MasterBalanceDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.Batch;
import id.co.keriss.consolidate.ee.InvoiceItems;
import id.co.keriss.consolidate.ee.Invoices;
import id.co.keriss.consolidate.ee.MasterBalance;
import id.co.keriss.consolidate.ee.Tenant;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.LogSystem;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

public class TopupCustom extends ActionSupport {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
//		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;

		String uuid = UUID.randomUUID().toString();

		FileItem filedata=null;
		List<FileItem> fileItems=null;
//		LogSystem.info(request, "DATA DEBUG :"+(i++), uuid);

		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				
				// no multipart form
				if (!isMultipart) {
					LogSystem.info(request, "Bukan multipart", uuid);
					JSONObject jo=new JSONObject();
					jo.put("result", "30");
					jo.put("notif", "Format request API bukan multipart.");
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
						else
						{
							 if(fileItem.getFieldName().equalsIgnoreCase("file")){
								 filedata=fileItem;
							 }
						}
					}
				}
			
	        LogSystem.info(request, "PATH :"+request.getRequestURI(), uuid);
	        LogSystem.request(request, fileItems, uuid);
			if(jsonString==null) return;	         
	        JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");         
	         
	        JSONObject jo = null;
	        jo=Topup(jsonRecv, context, request, filedata, uuid);
	         
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
				jo.put("notif", "Failed topup");
				jo.put("log", uuid);
				LogSystem.info(request, jo.toString(), uuid);
				
				try
				{
					TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
					bot.execute(new SendMessage(213382980, "Failed topup : " + uuid + "\n Message : " + e));
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
	
	JSONObject Topup(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, FileItem filedata, String uuid) throws JSONException{
		long startTime = System.nanoTime();
		NumberFormat formatter = new DecimalFormat("#0.00000");
		Boolean batchtopup = false;
		DB db = new DB();
		Date now = new Date();
		try {
			db = getDB(context);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		LogSystem.info(request, "DB Connection status : " + db.session().isConnected(), uuid);

		InetAddress inetAddress = null;
		
		try {
			inetAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		JSONObject jo=new JSONObject();
      
        String notif="Error topup";

//        ApiSystemDao maintanance = new ApiSystemDao(db);
//        
//		LogSystem.info(request, "Maintanance : " + maintanance.checkSystem(), uuid);
//		
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
//        
  	  	
  		if(jsonRecv.has("batchtopup")) 
  	  	{
  			if(jsonRecv.getBoolean("batchtopup"))
  			{
  	  	  		batchtopup=true;
  			}
  	  	}

  	  	try {
  	  		AccountsDao ado = new AccountsDao(db);
  	  		
  	  		TenantDao tdao = new TenantDao(db);
  	  		List<Tenant> dataTenant = tdao.findAllTenantPrepaid();
  	  		
  	  		
  	  		ArrayList<String> listtenant = new ArrayList<String>();
		  	
  	  		if(!batchtopup)
  	  		{
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
		  		if(!jsonRecv.has("tenant")) 
		  	  	{
		  	  		notif="JSON tenant does not exist";
			  	  	jo.put("result", "28");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
		  	  	}
		  		if(!jsonRecv.has("amount")) 
		  	  	{
		  	  		notif="JSON amount does not exist";
			  	  	jo.put("result", "28");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
		  	  	}
  	  			
  		  		Transaction tx = db.session().getTransaction();
  	  	  		tx.setTimeout(20);
  	  	  		tx.begin();
  	  	  		
	  	  		Tenant tenantApiKey = tdao.findByApiKey(jsonRecv.getString("tenant"));
				
				LogSystem.info(request, "Arraylist tenant : " + listtenant, uuid);
				
				for (int l = 0 ; l < dataTenant.size() ; l++)
				{
					listtenant.add(dataTenant.get(l).getApi_key());
				}
				
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
				
				Accounts account = ado.findByExternalKey(jsonRecv.getString("externalkey"), tenantApiKey.getRecord_id());
	  	  		if(account == null)
		  		{
		  	  		notif="account externalkey with that tenant does not exist";
			  	  	jo.put("result", "04");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
	  			}
	  			else
	  			{	
	  	  			Invoices invoice = new Invoices();
	  	  			InvoiceItems invTopup = new InvoiceItems();
	  	  			InvoiceItemsDao ido = new InvoiceItemsDao(db);
	  	  			TenantDao tdo = new TenantDao(db);
	  	  			LogSystem.info(request, "Checking tenant", uuid);
	  	  			Tenant tenant = tdo.findByID(tenantApiKey.getRecord_id());
	
	  	  			BigDecimal SPTopup = null;
					try 
					{
						long gettoptupbalance = System.nanoTime();
//						if(jsonRecv.has("free"))
//						{
//							if(jsonRecv.getBoolean("free"))
//							{
//								SPTopup = ido.SPTopupFree(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")));	
//							}
//							else
//							{
//								SPTopup = ido.SPTopup(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")));
//							}
//						}
//						else
//						{
							SPTopup = ido.SPTopup(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")));
//						}
							
						
						LogSystem.info(request, "Time Get SP Balance : " + formatter.format((System.nanoTime() - gettoptupbalance)/1000000000d), uuid);
					}
					catch(Exception e)
	  				{
			  			LogSystem.error(getClass(), e, uuid);
			  			LogSystem.error(request, e.getMessage().toString(), uuid);
			  			LogSystem.error(request, e.getCause().toString(), uuid);
			  			
	  					notif="Failed topup balance";
				  	  	jo.put("result", "06");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						jo.put("tenant", jsonRecv.getString("tenant"));
						LogSystem.info(request, jo.toString(), uuid);
						return jo;
	  				}
					
					try 
					{
						if (SPTopup != null)
			  			{
			  	  			if(tenant != null)
			  	  			{
//				  				Batch batch = new Batch();
//				  				batch.setAccount_record_id(account);
//				  				batch.setOpen_date(now);
//				  				batch.setName_batch("PRE<" + jsonRecv.getString("externalkey") + "_" + tenantApiKey.getApi_key() + ">");
//				  				batch.setSettled(false);
//				  				batch.setQuota(jsonRecv.getInt("amount"));
//				  				if(jsonRecv.has("price"))
//				  				{
//				  					batch.setPrice(jsonRecv.getInt("price"));
//				  				}
//				  				else
//				  				{
//				  					batch.setPrice(0);
//				  				}
//				  				
//				  				batch.setRemaining_balance(jsonRecv.getInt("amount"));
//				  				batch.setUsage(0);
//				  				
//				  				db.session().save(batch);
			  	  				
			  	  				invoice.setId(UUID.randomUUID().toString());
			  	  				invoice.setInvoice_date(now);
			  	  				invoice.setTenant_record_id(tenant);
			  	  				invoice.setAccount_id(account.getId());
			  	  				invoice.setAccount_record_id(account);
			  	  				invoice.setCreated_date(now);
			  	  				invoice.setTarget_date(now);
			  	  				invoice.setStatus("COMMITTED");
			  	  				
			  	  				db.session().save(invoice);
			  	  				
			  	  				invTopup.setAccount_id(account.getId());
			  	  				invTopup.setAccount_record_id(account);
			  	  				invTopup.setCreated_date(now);
			  	  				invTopup.setInvoice_id(invoice.getId());
			  	  				invTopup.setType("CREDIT_ADJ");
			  	  				invTopup.setAmount(BigDecimal.valueOf(jsonRecv.getLong("amount")));
			  	  				invTopup.setDescription("Topup Balance Custom");
			  	  				invTopup.setTenant_record_id(tenant);
			  	  				invTopup.setId(UUID.randomUUID().toString());
			  	  				invTopup.setPlan_name("Digisign");
			  	  				invTopup.setUsage_name("Topup");
			  	  				invTopup.setPhase_name("Prepaid");
			  	  				invTopup.setStart_date(now);
			  	  				invTopup.setEnd_date(now);
			  	  				invTopup.setCurrent_balance(SPTopup);
			  	  				invTopup.setExternal_key(account.getExternal_key());
			  	  				invTopup.setTrx(1);
//			  	  				invTopup.setBatch(batch);

			  	  				db.session().save(invTopup);
			  	  				
				  	  			try {
						  			long commitinsert = System.nanoTime();
						  			tx.commit();
						  			
						  			LogSystem.info(request, "PROCESS INSERT COMMIT : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
						  		}catch(HibernateException e)
						  		{
//						  			Transaction txdelete = db.session().getTransaction();
//						  			txdelete.setTimeout(13);
//						        	txdelete.begin();
//						        	
//						        	db.session().delete(invoice);
//						        	db.session().delete(invTopup);
//						        	
//						        	txdelete.commit();
//						        	
//						        	ido.SPBalancePlus(jsonRecv.getString("externalkey"), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")));
						  			
						  			LogSystem.error(getClass(), e, uuid);
						  			LogSystem.error(request, e.getMessage().toString(), uuid);
						  			LogSystem.error(request, e.getCause().toString(), uuid);
						  			
							  	  	notif="Failed topup";
							  	  	jo.put("result", "06");
									jo.put("notif", notif);
									jo.put("log", uuid);
									try
									{
										TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
										bot.execute(new SendMessage(213382980, "Failed create topup log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
									}catch(Exception t)
									{
										LogSystem.error(request, "Failed send message telegram", uuid);
										LogSystem.error(getClass(), e, uuid);
									}
									return jo;
						  		}
				  	  			
			  	  	  			notif="Success topup";
				  		  	  	jo.put("result", "00");
				  				jo.put("notif", notif);
				  				jo.put("invoiceid", invoice.getId());
				  				jo.put("current_balance", invTopup.getCurrent_balance());
				  				jo.put("log", uuid);
				  				jo.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invTopup.getCreated_date()));
				  				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
				  				jo.put("tenant", jsonRecv.getString("tenant"));
				  				jo.put("external_key", jsonRecv.getString("externalkey"));
				  				jo.put("name", invoice.getAccount_record_id().getName());
				  				LogSystem.info(request, jo.toString(), uuid);
				  				return jo;
			  	  			}
			  	  			else
			  	  			{
				  	  			notif="tenant does not exist";
				  		  	  	jo.put("result", "05");
				  				jo.put("notif", notif);
				  				jo.put("log", uuid);
				  				jo.put("tenant", jsonRecv.getString("tenant"));
				  				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
				  				LogSystem.info(request, jo.toString(), uuid);
				  				
				  				return jo;
			  	  			}
			  			}
			  			else
			  			{
			  				LogSystem.info(request, "SP Balance null", uuid);
			  				notif="Failed create topup";
					  	  	jo.put("result", "06");
							jo.put("notif", notif);
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							jo.put("tenant", jsonRecv.getString("tenant"));
							LogSystem.info(request, jo.toString(), uuid);
							return jo;
			  			}
					}catch(Exception e) {
	
			  			LogSystem.error(getClass(), e, uuid);
			  			LogSystem.error(request, e.getMessage().toString(), uuid);
			  			LogSystem.error(request, e.getCause().toString(), uuid);
			  			
		  				notif="Failed create topup";
				  	  	jo.put("result", "06");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						jo.put("tenant", jsonRecv.getString("tenant"));
						LogSystem.info(request, jo.toString(), uuid);
						return jo;
		  			}
	  			}
  	  		}
			else
			{
				if(!jsonRecv.has("data"))
				{
	  				notif="Failed process batch topup, JSONArray 'data' not exist";
			  	  	jo.put("result", "03");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
				}
				else
				{
					JSONArray arraytopup = null;
					JSONArray arraydatasuccess = new JSONArray();
					
		  	  		try
		  	  		{
		  	  			arraytopup=(JSONArray) jsonRecv.getJSONArray("data");
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
		  	  		
			  	  	for(int i=0; i<arraytopup.length(); i++) 
					{
				  	  	
			  	  		long startTimeBatch = System.nanoTime();
			  	  		JSONObject datasuccess = new JSONObject();
						JSONObject obj=(JSONObject) arraytopup.get(i);
						
						if(!obj.has("externalkey")) 
				  	  	{
				  	  		notif="externalkey on JSONArray data does not exist";
					  	  	jo.put("result", "28");
							jo.put("notif", notif);
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							LogSystem.info(request, jo.toString(), uuid);
							return jo;
				  	  	}
				  		if(!obj.has("tenant")) 
				  	  	{
				  	  		notif="tenant on JSONArray data does not exist";
					  	  	jo.put("result", "28");
							jo.put("notif", notif);
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							LogSystem.info(request, jo.toString(), uuid);
							return jo;
				  	  	}
				  		if(!obj.has("amount")) 
				  	  	{
				  	  		notif="amount on JSONArray data does not exist";
					  	  	jo.put("result", "28");
							jo.put("notif", notif);
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							LogSystem.info(request, jo.toString(), uuid);
							return jo;
				  	  	}
						
						Tenant tenantApiKey = tdao.findByApiKey(obj.getString("tenant"));
						
						LogSystem.info(request, "Arraylist tenant : " + listtenant, uuid);
						
						for (int l = 0 ; l < dataTenant.size() ; l++)
						{
							listtenant.add(dataTenant.get(l).getApi_key());
						}
						
						if(!listtenant.contains(obj.getString("tenant")))
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
						
						Accounts account = ado.findByExternalKey(obj.getString("externalkey"), tenantApiKey.getRecord_id());
			  	  		if(account == null)
				  		{
							datasuccess.put("externalkey", obj.getString("externalkey"));
			  	  			datasuccess.put("tenant", tenantApiKey.getRecord_id());
			  	  			datasuccess.put("status", false);
			  	  			datasuccess.put("info", "account externalkey with that tenant does not exist");
			  	  			datasuccess.put("ptime", formatter.format((System.nanoTime() - startTimeBatch)/1000000000d));
			  	  			arraydatasuccess.put(datasuccess);
			  	  			LogSystem.info(request, datasuccess.toString(), uuid);
			  			}
			  			else
			  			{	
			  	  			Invoices invoice = new Invoices();
			  	  			InvoiceItems invTopup = new InvoiceItems();
			  	  			InvoiceItemsDao ido = new InvoiceItemsDao(db);
			  	  			TenantDao tdo = new TenantDao(db);
			  	  			LogSystem.info(request, "Checking tenant", uuid);
			  	  			Tenant tenant = tdo.findByID(tenantApiKey.getRecord_id());
			
			  	  			BigDecimal SPTopup = null;
							try 
							{
								long gettoptupbalance = System.nanoTime();

								SPTopup = ido.SPTopup(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(obj.getLong("amount")));

								LogSystem.info(request, "Time Get SP Balance : " + formatter.format((System.nanoTime() - gettoptupbalance)/1000000000d), uuid);
							}
							catch(Exception e)
			  				{
					  			LogSystem.error(getClass(), e, uuid);
					  			LogSystem.error(request, e.getMessage().toString(), uuid);
					  			LogSystem.error(request, e.getCause().toString(), uuid);
					  			
					  			datasuccess.put("externalkey", account.getExternal_key());
				  	  			datasuccess.put("tenant", account.getTenant_record_id());
				  	  			datasuccess.put("status", false);
				  	  			datasuccess.put("info", "Failed topup by system");
				  	  			datasuccess.put("ptime", formatter.format((System.nanoTime() - startTimeBatch)/1000000000d));
				  	  			datasuccess.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invTopup.getCreated_date()));
				  	  			arraydatasuccess.put(datasuccess);
				  	  			LogSystem.info(request, datasuccess.toString(), uuid);
			  				}
							
							try 
							{
							
								if (SPTopup != null)
					  			{
					  	  			if(tenant != null)
					  	  			{
						  	  			Transaction txbatch = db.session().getTransaction();
							  	  		txbatch.setTimeout(20);
							  	  		txbatch.begin();
							  	  		
//								  	  	Batch batch = new Batch();
//						  				batch.setAccount_record_id(account);
//						  				batch.setOpen_date(now);
//						  				batch.setName_batch("PRE<" + jsonRecv.getString("externalkey") + "_" + tenantApiKey.getApi_key() + ">");
//						  				batch.setSettled(false);
//						  				batch.setQuota(jsonRecv.getInt("amount"));
//						  				if(jsonRecv.has("price"))
//						  				{
//						  					batch.setPrice(jsonRecv.getInt("price"));
//						  				}
//						  				else
//						  				{
//						  					batch.setPrice(0);
//						  				}
//						  				
//						  				batch.setRemaining_balance(jsonRecv.getInt("amount"));
//						  				batch.setUsage(0);
//						  				
//						  				db.session().save(batch);
					  	  				
					  	  				invoice.setId(UUID.randomUUID().toString());
					  	  				invoice.setInvoice_date(now);
					  	  				invoice.setTenant_record_id(tenant);
					  	  				invoice.setAccount_id(account.getId());
					  	  				invoice.setAccount_record_id(account);
					  	  				invoice.setCreated_date(now);
					  	  				invoice.setTarget_date(now);
					  	  				invoice.setStatus("COMMITTED");
					  	  				
					  	  				db.session().save(invoice);
					  	  				
					  	  				invTopup.setAccount_id(account.getId());
					  	  				invTopup.setAccount_record_id(account);
					  	  				invTopup.setCreated_date(now);
					  	  				invTopup.setInvoice_id(invoice.getId());
					  	  				invTopup.setType("CREDIT_ADJ");
					  	  				invTopup.setAmount(BigDecimal.valueOf(obj.getLong("amount")));
					  	  				invTopup.setDescription("Topup Balance Custom");
					  	  				invTopup.setTenant_record_id(tenant);
					  	  				invTopup.setId(UUID.randomUUID().toString());
					  	  				invTopup.setPlan_name("Digisign");
					  	  				invTopup.setUsage_name("Topup");
					  	  				invTopup.setPhase_name("Prepaid");
					  	  				invTopup.setStart_date(now);
					  	  				invTopup.setEnd_date(now);
					  	  				invTopup.setCurrent_balance(SPTopup);
					  	  				invTopup.setExternal_key(account.getExternal_key());
					  	  				invTopup.setTrx(1);
//					  	  				invTopup.setBatch(batch);
					  	  				
					  	  				db.session().save(invTopup);
					  	  
						  	  			try {
								  			long commitinsert = System.nanoTime();
								  			txbatch.commit();
								  			
								  			LogSystem.info(request, "PROCESS INSERT COMMIT : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
								  		}catch(HibernateException e)
								  		{
//								  			Transaction txdeletebatch = db.session().getTransaction();
//								  			txdeletebatch.setTimeout(13);
//								  			txdeletebatch.begin();
//								        	
//								        	db.session().delete(invoice);
//								        	db.session().delete(invTopup);
//								        	
//								        	txdeletebatch.commit();
//								        	
//								        	ido.SPBalancePlus(obj.getString("externalkey"), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")));
								  			
								  			LogSystem.error(getClass(), e, uuid);
								  			LogSystem.error(request, e.getMessage().toString(), uuid);
								  			LogSystem.error(request, e.getCause().toString(), uuid);
								  			
								  			datasuccess.put("externalkey", account.getExternal_key());
							  	  			datasuccess.put("tenant", account.getTenant_record_id());
							  	  			datasuccess.put("status", false);
							  	  			datasuccess.put("info", "Failed topup SP");
							  	  			datasuccess.put("ptime", formatter.format((System.nanoTime() - startTimeBatch)/1000000000d));
							  	  			datasuccess.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invTopup.getCreated_date()));
							  	  			arraydatasuccess.put(datasuccess);
							  	  			LogSystem.info(request, datasuccess.toString(), uuid);
							  	  			
											try
											{
												TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
												bot.execute(new SendMessage(213382980, "Failed create topup log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
											}catch(Exception t)
											{
												LogSystem.error(request, "Failed send message telegram", uuid);
												LogSystem.error(getClass(), e, uuid);
											}
											return jo;
								  		}
						  	  			
						  	  			datasuccess.put("externalkey", account.getExternal_key());
						  	  			datasuccess.put("tenant", account.getTenant_record_id().getApi_key());
						  	  			datasuccess.put("status", true);
						  	  			datasuccess.put("info", "Success");
						  	  			datasuccess.put("current_balance", invTopup.getCurrent_balance());
						  	  			datasuccess.put("ptime", formatter.format((System.nanoTime() - startTimeBatch)/1000000000d));
						  	  			datasuccess.put("invoiceid", invoice.getId());
						  	  			datasuccess.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invTopup.getCreated_date()));
						  	  			arraydatasuccess.put(datasuccess);
						  	  			LogSystem.info(request, datasuccess.toString(), uuid);
					  	  			}
					  	  			else
					  	  			{
						  	  			datasuccess.put("externalkey", account.getExternal_key());
						  	  			datasuccess.put("tenant", account.getTenant_record_id());
						  	  			datasuccess.put("status", false);
						  	  			datasuccess.put("info", "Tenant does not exist");
						  	  			datasuccess.put("ptime", formatter.format((System.nanoTime() - startTimeBatch)/1000000000d));
						  	  			datasuccess.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invTopup.getCreated_date()));
						  	  			arraydatasuccess.put(datasuccess);
						  	  			LogSystem.info(request, datasuccess.toString(), uuid);
					  	  			}
					  			}
					  			else
					  			{
					  				datasuccess.put("externalkey", account.getExternal_key());
					  	  			datasuccess.put("tenant", account.getTenant_record_id());
					  	  			datasuccess.put("status", false);
					  	  			datasuccess.put("info", "Balance Account not exist");
					  	  			datasuccess.put("ptime", formatter.format((System.nanoTime() - startTimeBatch)/1000000000d));
					  	  			datasuccess.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
					  	  			arraydatasuccess.put(datasuccess);
					  	  			LogSystem.info(request, datasuccess.toString(), uuid);
					  			}
							}catch(Exception e) {
			
					  			LogSystem.error(getClass(), e, uuid);
					  			LogSystem.error(request, e.getMessage().toString(), uuid);
					  			
					  			datasuccess.put("externalkey", account.getExternal_key());
				  	  			datasuccess.put("tenant", account.getTenant_record_id());
				  	  			datasuccess.put("status", false);
				  	  			datasuccess.put("info", "Failed topup by system");
				  	  			datasuccess.put("ptime", formatter.format((System.nanoTime() - startTimeBatch)/1000000000d));
				  	  			datasuccess.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invTopup.getCreated_date()));
				  	  			arraydatasuccess.put(datasuccess);
				  	  			LogSystem.info(request, datasuccess.toString(), uuid);
				  			}
			  			}		
					}
			  	  	
  	  	  			notif="Success topup";
  	  	  			jo.put("batchdata", arraydatasuccess);
	  		  	  	jo.put("result", "00");
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
	  	  	notif="Error Topup";
	  	  	jo.put("result", "06");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			
			try
			{
				TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
				bot.execute(new SendMessage(213382980, "Failed topup : " + uuid + "\n Message : " + e));
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