package apiBiling.Postpaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.InvoiceItems;
import id.co.keriss.consolidate.ee.Invoices;
import id.co.keriss.consolidate.ee.PostpaidTransaction;
import id.co.keriss.consolidate.ee.Tenant;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.LogSystem;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
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


public class PostpaidTransactions extends ActionSupport {

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
				jo.put("notif", "Failed create postpaid transaction");
				
				try
				{
					TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
					bot.execute(new SendMessage(213382980, "Failed create postpaid transaction : " + uuid));
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

  	  	try {
  	  		tx.setTimeout(15);
  	  		tx.begin();
  	  		
	  	  	AccountsDao ado = new AccountsDao(db);
	  	  	TenantDao tdo = new TenantDao(db);
	  	  	
	  	  	Tenant tenantData = null;
	  	  	
	  	 	TenantDao tdao = new TenantDao(db);

	  	  	ArrayList<String> listtenant = new ArrayList<String>();
			
			TenantDao tdof = new TenantDao(db);
			List<Tenant> dataTenant = tdof.findAllTenantPrepaid();
			
			LogSystem.info(request, "Arraylist tenant : " + listtenant, uuid);
			
			for (int l = 0 ; l < dataTenant.size() ; l++)
			{
				if(dataTenant.get(l).getApi_key().toLowerCase() != "postpaid")
				{
					listtenant.add(dataTenant.get(l).getApi_key());
				}
			}
			
//			if(!listtenant.contains(jsonRecv.getString("tenant")))
//			{
//				LogSystem.info(request, "Can't user tenant postpaid transaction", uuid);
//			  	notif="Tenant available : " + listtenant;
//			  	jo.put("result", "01");
//				jo.put("notif", notif);
//				jo.put("log", uuid);
//				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//				LogSystem.info(getClass(), jo.toString());
//				return jo;
//			}
			
			if(jsonRecv.getString("tenant").toLowerCase() == "postpaid" || !listtenant.contains(jsonRecv.getString("tenant")))
		  	{
		  	 	LogSystem.info(request, "Can't user tenant "+jsonRecv.getString("tenant")+" transaction", uuid);
			  	notif="Tenant available : " + listtenant;
			  	jo.put("result", "01");
				jo.put("notif", notif);
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
				LogSystem.info(getClass(), jo.toString());
				return jo;
			}
	  	  	
	  	  	if(tdo.findByType("postpaid") != null)
	  	  	{
	  	  		tenantData = tdo.findByType("postpaid");
	  	  	}
	  	  	else
	  	  	{
		  	  	notif="Tenant postpaid not exist";
		  	  	jo.put("result", "05");
				jo.put("notif", notif);
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
				LogSystem.info(request, jo.toString(), uuid);
				return jo;
	  	  	}
	  	  	
		  	if(ado.findByExternalKey(jsonRecv.getString("externalkey"), tenantData.getRecord_id()) == null)
			{
				notif="Account postpaid not exist";
		  	  	jo.put("result", "05");
				jo.put("notif", notif);
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
				LogSystem.info(request, jo.toString(), uuid);
				return jo;
			}
		  	else
		  	{
		  		InvoiceItemsDao invoiceItemsDao = new InvoiceItemsDao(db);
		  		InvoiceItems it = invoiceItemsDao.findPostpaidTransactionActive(jsonRecv.getString("externalkey"));
		  		if(it != null)
		  		{
		  			Tenant tenantApiKey = tdao.findByApiKey(jsonRecv.getString("tenant"));
		  			PostpaidTransaction postpaidTransaction = new PostpaidTransaction();
			  		
			  		postpaidTransaction.setAmount(jsonRecv.getLong("amount"));
			  		postpaidTransaction.setDate(new Date());
			  		postpaidTransaction.setExternal_key(jsonRecv.getString("externalkey"));
			  		postpaidTransaction.setInvoice_items_id(it);
			  		postpaidTransaction.setTenant(tenantApiKey);
			  		
			  		db.session().save(postpaidTransaction);
			  		
			  		tx.commit();
			  		
			  		notif="Success record transaction";
			  	  	jo.put("result", "00");
					jo.put("notif", notif);
					jo.put("invoiceid", it.getId());
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
		  		}
		  		else
		  		{
		  			Invoices invoices = new Invoices();
			  		InvoiceItems invoiceItems = new InvoiceItems();
			  		PostpaidTransaction postpaidTransaction = new PostpaidTransaction();
			  		Tenant tenantApiKey = tdao.findByApiKey(jsonRecv.getString("tenant"));
			  		Accounts ac = ado.findByExternalKey(jsonRecv.getString("externalkey"), tenantData.getRecord_id());
			  		Calendar c = Calendar.getInstance();
			        c.add(Calendar.DATE, 30);
			        Date d = c.getTime();
			        
			  		invoices.setAccount_id(ac.getId());
			  		invoices.setAccount_record_id(ac);
			  		invoices.setCreated_date(new Date());
			  		invoices.setId(UUID.randomUUID().toString());
			  		invoices.setInvoice_date(new Date());
			  		invoices.setTenant_record_id(tenantData);
			  		invoices.setStatus("ACTIVE");
			  		invoices.setTarget_date(d);
			  		
			  		db.session().save(invoices);
			  		
			  		invoiceItems.setAccount_id(ac.getId());
			  		invoiceItems.setAccount_record_id(ac);
			  		invoiceItems.setAmount(BigDecimal.valueOf(0));
			  		invoiceItems.setCreated_date(new Date());
			  		invoiceItems.setDescription("Postpaid Transaction");
			  		invoiceItems.setId(UUID.randomUUID().toString());
			  		invoiceItems.setInvoice_id(invoices.getId());
			  		invoiceItems.setTenant_record_id(tenantData);
			  		invoiceItems.setType("Postpaid");
			  		invoiceItems.setUsage_name("Monthly");
			  		invoiceItems.setPhase_name("Postpaid");
			  		invoiceItems.setStart_date(new Date());
			  		invoiceItems.setEnd_date(d);
			  		invoiceItems.setPlan_name("Digisign");
			  		invoiceItems.setExternal_key(jsonRecv.getString("externalkey"));
			  		invoiceItems.setTrx(5);
			  		invoiceItems.setCurrent_balance(new BigDecimal(0));
			  		if(jsonRecv.has("item"))
			  		{
			  			invoiceItems.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
			  		}
			  		
			  		db.session().save(invoiceItems);
			  		
			  		postpaidTransaction.setAmount(jsonRecv.getLong("amount"));
			  		postpaidTransaction.setDate(new Date());
			  		postpaidTransaction.setExternal_key(jsonRecv.getString("externalkey"));
			  		postpaidTransaction.setInvoice_items_id(invoiceItems);
			  		postpaidTransaction.setTenant(tenantApiKey);
			  		
			  		db.session().save(postpaidTransaction);
			  		
			  		tx.commit();
			  		
			  		notif="Success create postpaid transaction";
			  	  	jo.put("result", "00");
					jo.put("notif", notif);
					jo.put("invoiceid", invoices.getId());
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
	  	  	notif="Failed create postpaid transaction";
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