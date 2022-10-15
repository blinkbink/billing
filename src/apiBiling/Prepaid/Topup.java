package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.dao.PlanDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.InvoiceItems;
import id.co.keriss.consolidate.ee.Invoices;
import id.co.keriss.consolidate.ee.Plan;
import id.co.keriss.consolidate.ee.Tenant;

import org.hibernate.HibernateException;
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
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;


public class Topup extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/billing/data";
	final static Logger log=LogManager.getLogger("digisignlogger");

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
					jo.put("notif", "Format request API bukan multipart");
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
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject Topup(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, FileItem filedata, String uuid) throws JSONException{
		long startTime = System.nanoTime();
		NumberFormat formatter = new DecimalFormat("#0.00000");
		DB db = new DB();
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
  		if(!jsonRecv.has("plan_id")) 
  	  	{
  	  		notif="JSON plan_id does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
  	  	}

  	  	Transaction tx = db.session().getTransaction();
  	  	
  	  	try {
  	  		tx.setTimeout(20);
  	  		tx.begin();
  	  		
  	  		PlanDao pldo = new PlanDao(db);
  	  		LogSystem.info(request, "Checking plan id", uuid);
  	  		
  	  		TenantDao tdao = new TenantDao(db);
  	  		Tenant tenantApiKey = tdao.findByApiKey(jsonRecv.getString("tenant"));
  	  		AccountsDao ado = new AccountsDao(db);
  	  		
	  	  	ArrayList<String> listtenant = new ArrayList<String>();
			
			TenantDao tdof = new TenantDao(db);
			List<Tenant> dataTenant = tdof.findAllTenantPrepaid();
			
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
  				if(!jsonRecv.getString("plan_id").startsWith("default"))
  				{
  					// Check plan id on account
  	  				if(pldo.findByPlanId(jsonRecv.getString("externalkey"), jsonRecv.getString("plan_id"), tenantApiKey.getRecord_id()) != null)
  	  	  	  		{
  	  	  	  			Plan pl = pldo.findByPlanId(jsonRecv.getString("externalkey"), jsonRecv.getString("plan_id"), tenantApiKey.getRecord_id());
  	  	  	  			InvoiceItemsDao ido = new InvoiceItemsDao(db);
  	  	  	  			Invoices invoice = new Invoices();
  	  	  	  			InvoiceItems inv = new InvoiceItems();
  	  	  	  			InvoiceItems invTopup = new InvoiceItems();
  	  	  	  			TenantDao tdo = new TenantDao(db);
  	  	  	  			LogSystem.info(request, "Checking tenant", uuid);
  	  	  	  			
  	  	  	  			Tenant tenant = tdo.findByID(tenantApiKey.getRecord_id());
  	  	  	  			
  	  	  	  			BigDecimal SPTopup = null;
						try 
						{
							long gettoptupbalance = System.nanoTime();
	
							SPTopup = ido.SPTopup(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(pl.getAmount()));
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
							LogSystem.info(request, jo.toString(), uuid);
							return jo;
		  				}
						
	  					try 
	  					{
	  						if (SPTopup != null)
	  			  			{
		  	  	  	  			if(tenant != null)
		  	  	  	  			{
		  				  			String accountid = ido.getAccountId(jsonRecv.getString("externalkey"), tenantApiKey.getRecord_id());
		
			  				  		BigDecimal balance = null;
			  			  			InvoiceItemsDao invoiceItemsDao = new InvoiceItemsDao(db);
			  			  			List<InvoiceItems> invoiceitems = invoiceItemsDao.findAccountonInvoiceItems(account.getTenant_record_id().getRecord_id(), account.getId().toString());
		
		  	  	  	  				invoice.setId(UUID.randomUUID().toString());
		  	  	  	  				invoice.setInvoice_date(new Date());
		  	  	  	  				invoice.setTenant_record_id(tenant);
		  	  	  	  				invoice.setAccount_id(account.getId());
		  	  	  	  				invoice.setAccount_record_id(account);
		  	  	  	  				invoice.setCreated_date(new Date());
		  	  	  	  				invoice.setTarget_date(new Date());
		  	  	  	  				invoice.setStatus("COMMITED");
		  	  	  	  				
		  	  	  	  				db.session().save(invoice);
		  	  	  	  				
		  		  	  	  			invTopup.setAccount_id(account.getId());
		  		  	  	  			invTopup.setAccount_record_id(account);
		  			  	  			invTopup.setCreated_date(new Date());
		  			  	  			invTopup.setInvoice_id(invoice.getId());
		  			  	  			invTopup.setType("CREDIT_ADJ");
		  			  	  			invTopup.setAmount(BigDecimal.valueOf(pl.getAmount()));
		  			  	  			invTopup.setDescription("Topup Balance");
		  			  	  			invTopup.setTenant_record_id(tenant);
		  			  	  			invTopup.setId(UUID.randomUUID().toString());
		  			  	  			invTopup.setPlan_name(Long.toString(pl.getId()));
		  			  	  			invTopup.setUsage_name("Topup");
		  			  	  			invTopup.setPhase_name("Prepaid");
		  			  	  			invTopup.setStart_date(new Date());
		  			  	  			invTopup.setEnd_date(new Date());
		  			  	  			invTopup.setCurrent_balance(SPTopup);
		  			  	  			invTopup.setExternal_key(account.getExternal_key());
		  			  	  			invTopup.setTrx(1);
		  		  	  				
		  		  	  				db.session().save(invTopup);
		   	  	  	  				tx.commit();
		
		  		  	  	  			notif="Success topup";
		  			  		  	  	jo.put("result", "00");
		  			  				jo.put("notif", notif);
		  			  				jo.put("invoiceid", invoice.getId());
		  			  				jo.put("current_balance", balance.add(BigDecimal.valueOf(pl.getAmount())));
		  			  				jo.put("log", uuid);
		  			  				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
		  			  				LogSystem.info(request, jo.toString(), uuid);
		  			  				return jo;
		  	  	  	  			}
		  	  	  	  			else
		  	  	  	  			{
		  	  		  	  			notif="tenant does not exist";
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
				  				LogSystem.info(request, "SP Balance null", uuid);
				  				notif="Failed create topup plan";
						  	  	jo.put("result", "06");
								jo.put("notif", notif);
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								LogSystem.info(request, jo.toString(), uuid);
								return jo;
				  			}
	  					}catch(Exception e) {

	  			  			LogSystem.error(getClass(), e, uuid);
	  			  			LogSystem.error(request, e.getMessage().toString(), uuid);
	  			  			LogSystem.error(request, e.getCause().toString(), uuid);
	  			  			
	  		  				notif="Failed create topup plan";
	  				  	  	jo.put("result", "06");
	  						jo.put("notif", notif);
	  						jo.put("log", uuid);
	  						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
	  						LogSystem.info(request, jo.toString(), uuid);
	  						return jo;
	  		  			}
  	  	  	  		}
	  	  			else
	  				{
	  		  			notif="Account plan id does not exist";
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
  					Plan planDefault = pldo.findByPlanId("0", jsonRecv.getString("plan_id"), tenantApiKey.getRecord_id());
  					if(planDefault != null)
  	  	  	  		{
  	  	  	  			Invoices invoice = new Invoices();
  	  	  	  			InvoiceItems inv = new InvoiceItems();
  	  	  	  			InvoiceItems invTopup = new InvoiceItems();
  	  	  	  			TenantDao tdo = new TenantDao(db);
  	  	  	  			LogSystem.info(request, "Checking tenant", uuid);
  	  	  	  			
  	  	  	  			Tenant tenant = tdo.findByID(tenantApiKey.getRecord_id());

  	  	  	  			if(tenant != null)
  	  	  	  			{
  	  	  	  				InvoiceItemsDao ido = new InvoiceItemsDao(db);
  			  			
  				  			String accountid = ido.getAccountId(jsonRecv.getString("externalkey"), tenantApiKey.getRecord_id());
  				  			
	  				  		BigDecimal balance = null;
	  			  			InvoiceItemsDao invoiceItemsDao = new InvoiceItemsDao(db);

  	  	  	  			
	  	  	  	  			BigDecimal SPTopup = null;
							try 
							{
								long gettoptupbalance = System.nanoTime();
		
								SPTopup = ido.SPTopup(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(planDefault.getAmount()));
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
								LogSystem.info(request, jo.toString(), uuid);
								return jo;
			  				}
							
		  					try 
		  					{
		  						if (SPTopup != null)
		  			  			{
			  	  	  	  			if(tenant != null)
			  	  	  	  			{
			  	  	  	  				invoice.setId(UUID.randomUUID().toString());
			  	  	  	  				invoice.setInvoice_date(new Date());
			  	  	  	  				invoice.setTenant_record_id(tenant);
			  	  	  	  				invoice.setAccount_id(account.getId());
			  	  	  	  				invoice.setAccount_record_id(account);
			  	  	  	  				invoice.setCreated_date(new Date());
			  	  	  	  				invoice.setTarget_date(new Date());
			  	  	  	  				invoice.setStatus("COMMITED");
			  	  	  	  				
			  	  	  	  				db.session().save(invoice);
			  	  	  	  				
			  		  	  	  			invTopup.setAccount_id(account.getId());
			  		  	  	  			invTopup.setAccount_record_id(account);
			  			  	  			invTopup.setCreated_date(new Date());
			  			  	  			invTopup.setInvoice_id(invoice.getId());
			  			  	  			invTopup.setType("CREDIT_ADJ");
			  			  	  			invTopup.setAmount(BigDecimal.valueOf(planDefault.getAmount()));
			  			  	  			invTopup.setDescription("Topup Balance");
			  			  	  			invTopup.setTenant_record_id(tenant);
			  			  	  			invTopup.setId(UUID.randomUUID().toString());
			  			  	  			invTopup.setPlan_name(Long.toString(planDefault.getId()));
			  			  	  			invTopup.setUsage_name("Topup");
			  			  	  			invTopup.setPhase_name("Prepaid");
			  			  	  			invTopup.setStart_date(new Date());
			  			  	  			invTopup.setEnd_date(new Date());
			  			  	  			invTopup.setCurrent_balance(SPTopup);
			  			  	  			invTopup.setExternal_key(account.getExternal_key());
			  			  	  			invTopup.setTrx(1);
			  		  	  				
			  		  	  				db.session().save(invTopup);
			  		  	  				
				  		  	  			try {
								  			long commitinsert = System.nanoTime();
								  			tx.commit();
								  			
								  			LogSystem.info(request, "PROCESS INSERT COMMIT : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
								  		}catch(HibernateException e)
								  		{
//								  			Transaction txdelete = db.session().getTransaction();
//								  			txdelete.setTimeout(13);
//								        	txdelete.begin();
//								        	
//								        	db.session().delete(invoice);
//								        	db.session().delete(invTopup);
//								        	
//								        	txdelete.commit();
//								        	
//								        	ido.SPBalancePlus(jsonRecv.getString("externalkey"), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")));
								  			
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
			  			  				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			  			  				LogSystem.info(request, jo.toString(), uuid);
			  			  				return jo;
			  	  	  	  			}
			  	  	  	  			else
			  	  	  	  			{
			  	  		  	  			notif="tenant does not exist";
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
					  				LogSystem.info(request, "SP Balance null", uuid);
					  				notif="Failed create topup plan";
							  	  	jo.put("result", "06");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									LogSystem.info(request, jo.toString(), uuid);
									return jo;
					  			}
		  					}catch(Exception e) {

		  			  			LogSystem.error(getClass(), e, uuid);
		  			  			LogSystem.error(request, e.getMessage().toString(), uuid);
		  			  			LogSystem.error(request, e.getCause().toString(), uuid);
		  			  			
		  		  				notif="Failed create topup plan";
		  				  	  	jo.put("result", "06");
		  						jo.put("notif", notif);
		  						jo.put("log", uuid);
		  						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
		  						LogSystem.info(request, jo.toString(), uuid);
		  						return jo;
		  		  			}
  	  	  	  			}
	  					else
  	  	  	  			{
  	  		  	  			notif="tenant does not exist";
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
  						notif="Default plan id does not exist";
	  			  	  	jo.put("result", "05");
	  					jo.put("notif", notif);
	  					jo.put("log", uuid);
	  					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
	  					LogSystem.info(request, jo.toString(), uuid);
	  					return jo;
  					}
  				}
  			}
  	  	}catch(RuntimeException e)
  	  	{
  	  		tx.rollback();
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Error Topup";
	  	  	jo.put("result", "06");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			
			return jo;
  	  	}
  	  	finally {
			db.session().close();
			db.close();
	  	}
	}
}