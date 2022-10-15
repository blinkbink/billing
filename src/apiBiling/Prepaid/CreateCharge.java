package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.InvoiceItems;
import id.co.keriss.consolidate.ee.Invoices;
import id.co.keriss.consolidate.ee.Tenant;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.LogSystem;
import java.math.BigDecimal;
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


public class CreateCharge extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

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
					}
				}
			

	        LogSystem.info(request, "PATH :"+request.getRequestURI(), uuid);
	        LogSystem.request(request, fileItems, uuid);
			if(jsonString==null) return;	         
	        JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");         
	         
	        JSONObject jo = null;
	        jo=CreateCharge(jsonRecv, context, request, uuid);
	         
	         
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
				jo.put("notif", "Failed create external charge");
				jo.put("log", uuid);
				LogSystem.info(request, jo.toString(), uuid);
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject CreateCharge(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException{
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
  		if(!jsonRecv.has("description")) 
	  	{
	  		notif="JSON description does not exist";
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
  	  		
	  	  	AccountsDao ado = new AccountsDao(db);
	  	  	TenantDao tdo = new TenantDao(db);
	  	  	
	  	  	TenantDao tdao = new TenantDao(db);
	  	  	Tenant tenantApiKey = tdao.findByApiKey(jsonRecv.getString("tenant"));
	  		
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
				notif="Account externalkey with that tenant does not exist";
		  	  	jo.put("result", "05");
				jo.put("notif", notif);
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
				LogSystem.info(request, jo.toString(), uuid);
				return jo;
			}
		  	else
		  	{
		  		InvoiceItemsDao ido = new InvoiceItemsDao(db);
		  		
		  		BigDecimal masterBalancemines = null;
  				try {
  					//Potong update saldo ke master balance
  					long getspbalance = System.nanoTime();

  					masterBalancemines= ido.SPBalanceMines(jsonRecv.getString("externalkey"), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")), account.getRecord_id());
//  					masterBalancemines = ido.SPBalanceMines(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id());
		  			LogSystem.info(request, "Time Get SP Balance : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
  					LogSystem.info(request, "Balance from SP : " + masterBalancemines, uuid);
  					
  				}catch(Exception e)
  				{
		  			LogSystem.error(getClass(), e, uuid);
		  			LogSystem.error(request, e.getMessage().toString(), uuid);
		  			LogSystem.error(request, e.getCause().toString(), uuid);
		  			
  					notif="Failed create charge";
			  	  	jo.put("result", "06");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
  				}
  				
  				try 
	  			{
		  			if (masterBalancemines != null)
		  			{
			  			if (masterBalancemines.intValue() < 0)
			  			{
			  				notif="Balance not enought";
					  	  	jo.put("result", "05");
							jo.put("notif", notif);
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							LogSystem.info(request, jo.toString(), uuid);
							return jo;
			  			}
			  			else
			  			{
				  			Invoices invoices = new Invoices();
					  		InvoiceItems invCharge = new InvoiceItems();
					  		Tenant tn = tdo.findByID(tenantApiKey.getRecord_id());
					  		
					  		invoices.setAccount_id(account.getId());
					  		invoices.setAccount_record_id(account);
					  		invoices.setCreated_date(new Date());
					  		invoices.setId(UUID.randomUUID().toString());
					  		invoices.setInvoice_date(new Date());
					  		invoices.setTenant_record_id(tn);
					  		invoices.setTarget_date(new Date());
					  		invoices.setStatus("COMMITTED");
					  		
					  		db.session().save(invoices);
					  		
					  		invCharge.setAccount_id(account.getId());
					  		invCharge.setAccount_record_id(account);
					  		invCharge.setAmount(BigDecimal.valueOf(jsonRecv.getLong("amount")).negate());
					  		invCharge.setCreated_date(new Date());
					  		invCharge.setDescription(jsonRecv.getString("description"));
					  		invCharge.setId(UUID.randomUUID().toString());
					  		invCharge.setInvoice_id(invoices.getId());
					  		invCharge.setTenant_record_id(tn);
					  		invCharge.setType("EXTERNAL_CHARGE");
					  		invCharge.setUsage_name("External Charge");
					  		invCharge.setPhase_name("Prepaid");
					  		invCharge.setStart_date(new Date());
					  		invCharge.setEnd_date(new Date());
					  		invCharge.setPlan_name("Digisign");
					  		invCharge.setCurrent_balance(masterBalancemines);
					  		invCharge.setExternal_key(account.getExternal_key());
					  		invCharge.setTrx(2);
					  		
					  		db.session().save(invCharge);
					  		
					  		try {
					  			long commitinsert = System.nanoTime();
					  			tx.commit();
					  			LogSystem.info(request, "PROCESS INSERT COMMIT REVERSAL : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
					  			notif="Success create charge";
						  	  	jo.put("result", "00");
								jo.put("notif", notif);
								jo.put("invoiceid", invoices.getId());
								jo.put("current_balance", invCharge.getCurrent_balance());
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								LogSystem.info(request, jo.toString(), uuid);
								return jo;
								
					  		}catch(Exception e)
					  		{
					  			Transaction txdelete = db.session().getTransaction();
					  			txdelete.setTimeout(15);
					        	txdelete.begin();				  		
					        	
					        	db.session().delete(invCharge);
					        	
					        	txdelete.commit();
//					        	ido.SPBalancePlus(jsonRecv.getString("externalkey"), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id());
//					        	ido.SPBalancePlus(jsonRecv.getString("externalkey"), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id(), (int)masterBalancemines[1]);
					        	
					        	notif="Failed reversal";
						  	  	jo.put("result", "06");
								jo.put("notif", notif);
								jo.put("log", uuid);
								try
								{
									TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
									bot.execute(new SendMessage(213382980, "Failed Reversal : " + uuid));
								}catch(Exception t)
								{
									LogSystem.error(request, "Failed send message telegram", uuid);
									LogSystem.error(getClass(), e, uuid);
								}
								return jo;
					  		}
			  			}
		  			}
		  			else
		  			{
		  				LogSystem.info(request, "SP Balance null", uuid);
		  				notif="Failed create charge";
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
		  			
	  				notif="Failed create charge";
			  	  	jo.put("result", "06");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
	  			}
	  		}
  	  	}catch(RuntimeException e)
  	  	{
  	  		tx.rollback();
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Error create external charge";
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