package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.BatchDao;
import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.Batch;
import id.co.keriss.consolidate.ee.InvoiceItems;
import id.co.keriss.consolidate.ee.Tenant;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.LogSystem;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;


public class Reversal extends ActionSupport {

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
				jo.put("notif", "Failed revers transaction");
				jo.put("log", uuid);
				LogSystem.info(request, jo.toString(), uuid);
				
				try
				{
					TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
					bot.execute(new SendMessage(213382980, "Failed revers transaction : " + uuid + "\n Message : " + e));
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
//		Boolean free = false;
		Boolean reversal = false;
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
        
  	  	if(!jsonRecv.has("invoiceid")) 
  	  	{
  	  		notif="JSON invoiceid does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
  	  	}
  	  	
  	  	try {
	  	  	InvoiceItemsDao itdo = new InvoiceItemsDao(db);
	  	  	List<InvoiceItems> datainv = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
	  	  	
	  	  	if(datainv.size() < 1)
	  	  	{
		  	  	notif="invoiceid does not exist";
		  	  	jo.put("result", "04");
				jo.put("notif", notif);
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
				LogSystem.info(request, jo.toString(), uuid);
				return jo;
	  	  	}
	  	  	else
	  	  	{
//		  	  	if(!datainv.get(0).getPhase_name().equals("Postpaid"))
//		  	  	{
		  	  		List<InvoiceItems> checkreversal =  itdo.findTransactionReversal(jsonRecv.getString("invoiceid"));
		  	  		List<InvoiceItems> usage = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
		  	  		BigDecimal trx = BigDecimal.ZERO;
		  	  		if(checkreversal.size() > 0) 
		  	  		{
		  	  			LogSystem.info(request, "SIZE CHECK REVERSAL :" + checkreversal.size(), uuid);
		  	  			
		  	  			for(int i = 0 ; i < checkreversal.size() ; i++)
		  	  			{
		  	  				LogSystem.info(request, "TRX SEBELUM :" + trx, uuid);
		  	  				trx = trx.add(checkreversal.get(i).getAmount());
		  	  				LogSystem.info(request, "REVERSAL AMOUNT DB :" + checkreversal.get(i).getAmount(), uuid);
		  	  				LogSystem.info(request, "DITAMBAH :" + trx, uuid);
		  	  			}
		  	  			
		  	  			if (trx.compareTo(usage.get(0).getAmount().abs()) == 0)
		  				{
		  					notif="invoiceid already returned for reversal of all amount";
					  	  	jo.put("result", "03");
							jo.put("notif", notif);
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							LogSystem.info(request, jo.toString(), uuid);
							return jo;
		  				}
		  	  			
		  	  			if(trx.add(BigDecimal.valueOf(jsonRecv.getLong("amount"))).compareTo(usage.get(0).getAmount().abs()) == 1)
		  	  			{
			  	  			notif="existing reversal and amount cannot be greater than transaction";
					  	  	jo.put("result", "03");
							jo.put("notif", notif);
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							LogSystem.info(request, jo.toString(), uuid);
							return jo;
		  	  			}
		  	  			else
		  	  			{
			  	  			if(jsonRecv.has("amount"))
			  	  			{
			  	  				if(jsonRecv.has("item"))
			  	  				{
			  	  					if(!datainv.get(0).getItems().equals(BigInteger.valueOf(jsonRecv.getLong("item"))) && datainv.get(0).getItems() != null)
			  	  					{
				  	  					notif="item for invoice id " + datainv.get(0).getInvoice_id() + " not match";
								  	  	jo.put("result", "03");
										jo.put("notif", notif);
										jo.put("log", uuid);
										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
										LogSystem.info(request, jo.toString(), uuid);
										return jo;
			  	  					}
			  	  				}
			  	  				InvoiceItemsDao ido = new InvoiceItemsDao(db);
				  	  			InvoiceItems invReversal = new InvoiceItems();
	
				  	  			AccountsDao ado = new AccountsDao(db);
				  	  			TenantDao tdo = new TenantDao(db);
				  	  			
				  	  			List<InvoiceItems> itm = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
				  	  			
				  	  			
				  	  			Accounts account = ado.findById(itm.get(0).getAccount_record_id().getRecord_id());
				  	  			Tenant tenant = tdo.findByID(itm.get(0).getTenant_record_id().getRecord_id());
		
	//				  			BigDecimal balance = null;
					  	  		Transaction tx = db.session().getTransaction();
						  		tx.setTimeout(23);
					  	  		tx.begin();
				  	  			BigDecimal masterBalanceReversal = null;
				  				try {
//				  					Transaction txPlus = db.session().getTransaction();
//				  					txPlus.setTimeout(18);
//				  					txPlus.begin();
				  					//Potong update saldo ke master balance
				  					long getspbalance = System.nanoTime();
	//			  					if(itm.get(0).getFree())
	//			  					{
	//			  						masterBalanceReversal = ido.SPFreeBalancePlus(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")));
	//			  						free = true;
	//			  					}
	//			  					else
	//			  					{
				  					masterBalanceReversal= ido.SPBalancePlus(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id());
//				  					masterBalanceReversal= ido.SPBalancePlus(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id(), itm.get(0).getBatch().getId().intValue());
	//			  					}
				  				
				  					LogSystem.info(request, "Try reversal : ", uuid);
				  					
//				  					txPlus.commit();
				  					
//				  					reversal = true;
				  					LogSystem.info(request, "Success add balance reversal : ", uuid);
						  			LogSystem.info(request, "Time Get SP Balance : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
				  					LogSystem.info(request, "Balance from SP : " + masterBalanceReversal, uuid);
				  					
				  				}catch(Exception e)
				  				{
				  					if(tx!=null)
				  					{
				  						tx.rollback();
				  					}
						  			LogSystem.error(getClass(), e, uuid);
						  			
//						  			if(reversal) 
//						        	{
//						  				LogSystem.info(request, "Reversal was success before but transaction was failed, abort reversal mines balances : ", uuid);
//						        		
//						        		Transaction txreversal = db.session().getTransaction();
//						        		if(!txreversal.isActive())
//						        		{
//						        			txreversal.begin();
//						        		}
////						        		ido.SPBalanceMines(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")), account.getRecord_id());
//						        		ido.SPBalanceMines(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id());
//										txreversal.commit();
//										LogSystem.info(request, "Success revers transaction", uuid);
//						        	}
//				
									try
									{
										TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
										bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
									}catch(Exception t)
									{
										LogSystem.error(request, "Failed send message telegram", uuid);
										LogSystem.error(getClass(), e, uuid);
									}
						  			
				  					notif="Failed create reversal";
							  	  	jo.put("result", "06");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									jo.put("tenant", tenant.getApi_key());
									LogSystem.info(request, jo.toString(), uuid);
									return jo;
				  				}
				  				
				  				try 
					  			{
				  					
						  	  		
						  			if (masterBalanceReversal != null)
						  			{
//						  				Batch batch = new Batch();
//						  				batch.setId(new BigInteger(masterBalanceReversal[1].toString()));
						  				
						  				
							  			invReversal.setAccount_id(account.getId());
							  			invReversal.setAccount_record_id(account);
							  			invReversal.setAmount(BigDecimal.valueOf(jsonRecv.getLong("amount")));
							  			invReversal.setCreated_date(new Date());
							  			invReversal.setDescription("Failed transaction");
							  			invReversal.setId(UUID.randomUUID().toString());
							  			invReversal.setInvoice_id(jsonRecv.getString("invoiceid"));
							  			invReversal.setTenant_record_id(tenant);
							  			invReversal.setType("ITEM_ADJ");
							  			invReversal.setUsage_name("Reversal");
							  			invReversal.setPhase_name("Prepaid");
							  			invReversal.setStart_date(new Date());
							  			invReversal.setEnd_date(new Date());
							  			invReversal.setPlan_name("Digisign");
							  			invReversal.setCurrent_balance(masterBalanceReversal);
							  			invReversal.setExternal_key(account.getExternal_key());
							  			invReversal.setTrx(3);
	//						  			invReversal.setFree(itm.get(0).getFree());
							  	  		if(jsonRecv.has("item"))
								  		{
							  	  			invReversal.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
								  		}
								  	  	else
							  	  		{
							  	  			invReversal.setItems(datainv.get(0).getItems());
							  	  		}
							  	  		
//							  	  		invReversal.setBatch(batch);
						  	  			
						  	  			db.session().save(invReversal);
			
								  		try {
								  			long commitinsert = System.nanoTime();
								  			
								  			tx.commit();
								  			LogSystem.info(request, "PROCESS INSERT COMMIT REVERSAL : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
								  		}catch(Exception e)
								  		{
								  			LogSystem.error(getClass(), e, uuid);
								  			LogSystem.info(request, "Reversal commit was failed, rever transaction ", uuid);
								  			if(tx!=null)
						  					{
						  						tx.rollback();
						  					}
								  			db.session().clear();
	
//								        	if(reversal) 
//								        	{
//								        		LogSystem.info(request, "Reversal was success before but transaction was failed, abort reversal mines balances : ", uuid);
//								        		
//								        		Transaction txreversal = db.session().getTransaction();
//								        		if(!txreversal.isActive())
//								        		{
//								        			txreversal.begin();
//								        		}
////								        		ido.SPBalanceMines(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")), account.getRecord_id());
//								        		ido.SPBalanceMines(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id());
//								        		txreversal.commit();
//												LogSystem.info(request, "Success revers transaction", uuid);
//								        	}
								        	notif="Failed reversal";
									  	  	jo.put("result", "06");
											jo.put("notif", notif);
											jo.put("log", uuid);
											jo.put("tenant", tenant.getApi_key());
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
					  			}catch(Exception e) {
	
						  			LogSystem.error(getClass(), e, uuid);
						  			
					  				notif="Failed create reversal";
							  	  	jo.put("result", "06");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									jo.put("tenant", tenant.getApi_key());
									LogSystem.info(request, jo.toString(), uuid);
									return jo;
					  			}
				  				
				  				notif="Success reversal";
						  	  	jo.put("result", "00");
								jo.put("notif", notif);
	//							jo.put("free", itm.get(0).getFree());
								jo.put("invoiceid", invReversal.getInvoice_id());
								jo.put("current_balance", invReversal.getCurrent_balance());
								jo.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invReversal.getCreated_date()));
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								jo.put("tenant", tenant.getApi_key());
								jo.put("external_key", invReversal.getExternal_key());
								jo.put("name", invReversal.getAccount_record_id().getName());
								LogSystem.info(request, jo.toString(), uuid);
								
								return jo;
			  	  			}
			  	  			else
			  	  			{
				  	  			if(jsonRecv.has("item"))
			  	  				{
			  	  					if(!datainv.get(0).getItems().equals(BigInteger.valueOf(jsonRecv.getLong("item"))) && datainv.get(0).getItems() != null)
			  	  					{
				  	  					notif="item for invoice id " + datainv.get(0).getInvoice_id() + " not match";
								  	  	jo.put("result", "03");
										jo.put("notif", notif);
										jo.put("log", uuid);
										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
										LogSystem.info(request, jo.toString(), uuid);
										return jo;
			  	  					}
			  	  				}
				  	  			InvoiceItems invReversal = new InvoiceItems();
				  	  			AccountsDao ado = new AccountsDao(db);
				  	  			TenantDao tdo = new TenantDao(db);
				  	  			
				  	  			List<InvoiceItems> itm = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
				  	  			Accounts account = ado.findById(itm.get(0).getAccount_record_id().getRecord_id());
				  	  			Tenant tenant = tdo.findByID(itm.get(0).getTenant_record_id().getRecord_id());
				  	  			
				  	  			InvoiceItemsDao ido = new InvoiceItemsDao(db);
	
					  	  		Transaction tx = db.session().getTransaction();
						  		tx.setTimeout(23);
					  	  		tx.begin();
						  	  	BigDecimal masterBalanceReversal = null;
				  				try {
//				  					Transaction txPlus = db.session().getTransaction();
//			  						txPlus.setTimeout(18);
//			  						txPlus.begin();
				  					//Potong update saldo ke master balance
				  					long getspbalance = System.nanoTime();
	//			  					
	//			  					if(itm.get(0).getFree())
	//			  					{
	//			  						masterBalanceReversal = ido.SPFreeBalancePlus(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), itm.get(0).getAmount().abs());
	//			  						free = true;
	//			  					}
	//			  					else
	//			  					{
//				  					masterBalanceReversal = ido.SPBalancePlus(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), itm.get(0).getAmount().abs(), (int)account.getRecord_id(), itm.get(0).getBatch().getId().intValue());
				  					masterBalanceReversal = ido.SPBalancePlus(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), itm.get(0).getAmount().abs().intValue(), (int)account.getRecord_id());
	//			  					}
				  			
				  					LogSystem.info(request, "Try reversal : ", uuid);
			  						
//				  					txPlus.commit();
				  						
//				  					reversal = true;
				  					LogSystem.info(request, "Success reversal add balances", uuid);
						  			LogSystem.info(request, "Time Get SP Balance : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
				  					LogSystem.info(request, "Balance from SP : " + masterBalanceReversal, uuid);
				  					
				  				}catch(Exception e)
				  				{
			
						  			LogSystem.error(getClass(), e, uuid);
						  			if(tx!=null)
				  					{
				  						tx.rollback();
				  					}
						  			
				  					notif="Failed create reversal";
							  	  	jo.put("result", "06");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									jo.put("tenant", tenant.getApi_key());
									LogSystem.info(request, jo.toString(), uuid);
									
									try
									{
										TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
										bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
									}catch(Exception t)
									{
										LogSystem.error(request, "Failed send message telegram", uuid);
										LogSystem.error(getClass(), t, uuid);
									}
									
									
									return jo;
				  				}
				  				
				  				try 
					  			{
				  					
						  		
						  			if (masterBalanceReversal != null)
						  			{
//						  				Batch batch = new Batch();
//						  				batch.setId(new BigInteger(masterBalanceReversal[1].toString()));
						  				
							  			invReversal.setAccount_id(account.getId());
							  			invReversal.setAccount_record_id(account);
							  			invReversal.setAmount(itm.get(0).getAmount().abs());
							  			invReversal.setCreated_date(new Date());
							  			invReversal.setDescription("Failed transaction");
							  			invReversal.setId(UUID.randomUUID().toString());
							  			invReversal.setInvoice_id(jsonRecv.getString("invoiceid"));
							  			invReversal.setTenant_record_id(tenant);
							  			invReversal.setType("ITEM_ADJ");
							  			invReversal.setUsage_name("Reversal");
							  			invReversal.setPhase_name("Prepaid");
							  			invReversal.setCurrent_balance(masterBalanceReversal);
							  			invReversal.setExternal_key(account.getExternal_key());
							  			invReversal.setTrx(3);
	//						  			invReversal.setFree(itm.get(0).getFree());
							  	  		if(jsonRecv.has("item"))
								  		{
							  	  			invReversal.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
								  		}
							  	  		else
							  	  		{
							  	  			invReversal.setItems(datainv.get(0).getItems());
							  	  		}
							  	  		
//							  	  		invReversal.setBatch(batch);
						  	  			
						  	  			db.session().save(invReversal);
		
								  		try {
								  			long commitinsert = System.nanoTime();
								  		
								  			tx.commit();
								  			LogSystem.info(request, "PROCESS INSERT COMMIT REVERSAL : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
								  		}catch(Exception e)
								  		{
								  			LogSystem.error(getClass(), e, uuid);
								  			LogSystem.info(request, "Reversal commit was failed, rever transaction ", uuid);
								  			if(tx!=null)
						  					{
						  						tx.rollback();
						  					}
								  			db.session().clear();
	
//								  			if(reversal)
//								  			{
//								  				LogSystem.info(request, "Reversal was success before but transaction was failed, abort reversal mines balances : ", uuid);
//								  				
//								  				Transaction txreversal = db.session().getTransaction();
//								  				if(!txreversal.isActive())
//								        		{
//								        			txreversal.begin();
//								        		}
////								  				ido.SPBalanceMines(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")), account.getRecord_id());
//								  				ido.SPBalanceMines(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id());
//								  				txreversal.commit();
//												LogSystem.info(request, "Success revers transaction", uuid);
//								  			}
								        	notif="Failed reversal";
									  	  	jo.put("result", "06");
											jo.put("notif", notif);
											jo.put("log", uuid);
											jo.put("tenant", tenant.getApi_key());
											try
											{
												TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
												bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
											}catch(Exception t)
											{
												LogSystem.error(request, "Failed send message telegram", uuid);
												LogSystem.error(getClass(), t, uuid);
											}
											
											return jo;
								  		}
						  			}
					  	  		}catch(Exception e) {
						  	  		if(tx!=null)
				  					{
				  						tx.rollback();
				  					}
						  			LogSystem.error(getClass(), e, uuid);
						  			
					  				notif="Failed create reversal";
							  	  	jo.put("result", "06");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									LogSystem.info(request, jo.toString(), uuid);
									
									try
									{
										TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
										bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
									}catch(Exception t)
									{
										LogSystem.error(request, "Failed send message telegram", uuid);
										LogSystem.error(getClass(), t, uuid);
									}
									
									return jo;
					  			}
				  				notif="Success reversal";
						  	  	jo.put("result", "00");
	//					  	  	jo.put("free", itm.get(0).getFree());
								jo.put("notif", notif);
								jo.put("invoiceid", invReversal.getInvoice_id());
								jo.put("current_balance", invReversal.getCurrent_balance());
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								jo.put("tenant", tenant.getApi_key());
								jo.put("external_key", invReversal.getExternal_key());
								jo.put("name", invReversal.getAccount_record_id().getName());
								LogSystem.info(request, jo.toString(), uuid);
								
								return jo;
			  	  			}
			  	  		}
		  	  		}
		  	  		else
		  	  		{
		  	  			if(jsonRecv.has("amount"))
		  	  			{
			  	  			if(jsonRecv.has("item"))
		  	  				{
		  	  					if(!datainv.get(0).getItems().equals(BigInteger.valueOf(jsonRecv.getLong("item"))) && datainv.get(0).getItems() != null)
		  	  					{
			  	  					notif="item for invoice id " + datainv.get(0).getInvoice_id() + " not match";
							  	  	jo.put("result", "03");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									LogSystem.info(request, jo.toString(), uuid);
									return jo;
		  	  					}
		  	  				}
			  	  			
			  	  			if(usage.get(0).getAmount().abs().compareTo(BigDecimal.valueOf(jsonRecv.getLong("amount"))) == -1)
			  	  			{
				  	  			notif="amount cannot be greater than transaction";
						  	  	jo.put("result", "03");
								jo.put("notif", notif);
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								LogSystem.info(request, jo.toString(), uuid);
								return jo;
			  	  			}
			  	  			else
			  	  			{
			  	  				InvoiceItemsDao ido = new InvoiceItemsDao(db);
				  	  			InvoiceItems invReversal = new InvoiceItems();
	
				  	  			AccountsDao ado = new AccountsDao(db);
				  	  			TenantDao tdo = new TenantDao(db);
				  	  			
				  	  			List<InvoiceItems> itm = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
				  	  			Accounts account = ado.findById(itm.get(0).getAccount_record_id().getRecord_id());
				  	  			Tenant tenant = tdo.findByID(itm.get(0).getTenant_record_id().getRecord_id());
		
					  	  		Transaction tx = db.session().getTransaction();
						  		tx.setTimeout(23);
					  	  		tx.begin();
						  	  	BigDecimal masterBalanceReversal = null;
				  				try {
//				  					Transaction txPlus = db.session().getTransaction();
//			  						txPlus.setTimeout(18);
//			  						txPlus.begin();
				  					//Potong update saldo ke master balance
				  					long getspbalance = System.nanoTime();
				  					
	//			  					if(itm.get(0).getFree())
	//			  					{
	//			  						masterBalanceReversal = ido.SPFreeBalancePlus(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")));
	//			  						free = true;
	//			  					}
	//			  					else
	//			  					{
				  					masterBalanceReversal= ido.SPBalancePlus(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id());
	//			  					}
				  			
				  					LogSystem.info(request, "Try reversal : ", uuid);
			  					
//				  					txPlus.commit();
				  					
//				  					reversal = true;
		
				  					LogSystem.info(request, "Success reversal add balances ", uuid);
						  			LogSystem.info(request, "Time Get SP Balance : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
				  					LogSystem.info(request, "Balance from SP : " + masterBalanceReversal, uuid);
				  					
				  				}catch(Exception e)
				  				{
		
						  			LogSystem.error(getClass(), e, uuid);
						  			
				  					notif="Failed create reversal";
							  	  	jo.put("result", "06");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									LogSystem.info(request, jo.toString(), uuid);
									
									try
									{
										TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
										bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
									}catch(Exception t)
									{
										LogSystem.error(request, "Failed send message telegram", uuid);
										LogSystem.error(getClass(), t, uuid);
									}
									
									return jo;
				  				}
				  				
				  				try 
					  			{
				  					
						  			if (masterBalanceReversal != null)
						  			{
//						  				Batch batch = new Batch();
//						  				batch.setId(new BigInteger(masterBalanceReversal[1].toString()));
						  				
							  			invReversal.setAccount_id(account.getId());
							  			invReversal.setAccount_record_id(account);
							  			invReversal.setAmount(BigDecimal.valueOf(jsonRecv.getLong("amount")));
							  			invReversal.setCreated_date(new Date());
							  			invReversal.setDescription("Failed transaction");
							  			invReversal.setId(UUID.randomUUID().toString());
							  			invReversal.setInvoice_id(jsonRecv.getString("invoiceid"));
							  			invReversal.setTenant_record_id(tenant);
							  			invReversal.setType("ITEM_ADJ");
							  			invReversal.setUsage_name("Reversal");
							  			invReversal.setPhase_name("Prepaid");
							  			invReversal.setStart_date(new Date());
							  			invReversal.setEnd_date(new Date());
							  			invReversal.setPlan_name("Digisign");
							  			invReversal.setCurrent_balance(masterBalanceReversal);
							  			invReversal.setExternal_key(account.getExternal_key());
							  			invReversal.setTrx(3);
	//						  			invReversal.setFree(itm.get(0).getFree());
							  	  		if(jsonRecv.has("item"))
								  		{
							  	  			invReversal.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
								  		}
							  	  		else
							  	  		{
							  	  			invReversal.setItems(datainv.get(0).getItems());
							  	  		}
							  	  		
//							  	  		invReversal.setBatch(batch);
						  	  			
						  	  			db.session().save(invReversal);
			
								  		try {
								  			long commitinsert = System.nanoTime();
								  			
								  			tx.commit();
								  			LogSystem.info(request, "PROCESS INSERT COMMIT REVERSAL : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
								  		}catch(Exception e)
								  		{
								  			LogSystem.error(getClass(), e, uuid);
								  			LogSystem.info(request, "Reversal commit was failed, rever transaction ", uuid);
								  			
								  			if (tx!= null)
								  			{
								  				tx.rollback();
								  			}
								  			db.session().clear();
	
//								        	if(reversal)
//								        	{
//								        		LogSystem.info(request, "Reversal was success before but transaction was failed, abort reversal mines balances : ", uuid);
//								        		
//								        		Transaction txreversal = db.session().getTransaction();
//								        		if(!txreversal.isActive())
//								        		{
//								        			txreversal.begin();
//								        		}
////								        		ido.SPBalanceMines(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")), account.getRecord_id());
//								        		ido.SPBalanceMines(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id());
//												txreversal.commit();
//												LogSystem.info(request, "Success revers transaction", uuid);
//								        	}
								        	notif="Failed reversal";
									  	  	jo.put("result", "06");
											jo.put("notif", notif);
											jo.put("log", uuid);
											jo.put("tenant", tenant.getApi_key());
											try
											{
												TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
												bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
											}catch(Exception t)
											{
												LogSystem.error(request, "Failed send message telegram", uuid);
												LogSystem.error(getClass(), t, uuid);
											}
											return jo;
								  		}
						  			}
					  			}catch(Exception e) {
					  				if (tx!= null)
						  			{
						  				tx.rollback();
						  			}
	
						  			LogSystem.error(getClass(), e, uuid);
						  			
					  				notif="Failed create reversal";
							  	  	jo.put("result", "06");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									jo.put("tenant", tenant.getApi_key());
									LogSystem.info(request, jo.toString(), uuid);
									
									try
									{
										TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
										bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
									}catch(Exception t)
									{
										LogSystem.error(request, "Failed send message telegram", uuid);
										LogSystem.error(getClass(), t, uuid);
									}
									
									return jo;
					  			}
				  				
				  				notif="Success reversal";
						  	  	jo.put("result", "00");
								jo.put("notif", notif);
	//							jo.put("free", itm.get(0).getFree());
								jo.put("invoiceid", invReversal.getInvoice_id());
								jo.put("current_balance", invReversal.getCurrent_balance());
								jo.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invReversal.getCreated_date()));
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								jo.put("tenant", tenant.getApi_key());
								jo.put("external_key", invReversal.getExternal_key());
								jo.put("name", invReversal.getAccount_record_id().getName());
								LogSystem.info(request, jo.toString(), uuid);
								
								return jo;
			  	  			}
		  	  			}
		  	  			else
		  	  			{
			  	  			if(jsonRecv.has("item"))
		  	  				{
		  	  					if(!datainv.get(0).getItems().equals(BigInteger.valueOf(jsonRecv.getLong("item"))) && datainv.get(0).getItems() != null)
		  	  					{
			  	  					notif="item for invoice id " + datainv.get(0).getInvoice_id() + " not match";
							  	  	jo.put("result", "03");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									LogSystem.info(request, jo.toString(), uuid);
									return jo;
		  	  					}
		  	  				}
			  	  			
			  	  			InvoiceItems invReversal = new InvoiceItems();
			  	  			AccountsDao ado = new AccountsDao(db);
			  	  			TenantDao tdo = new TenantDao(db);
			  	  			
			  	  			List<InvoiceItems> itm = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
			  	  			Accounts account = ado.findById(itm.get(0).getAccount_record_id().getRecord_id());
			  	  			Tenant tenant = tdo.findByID(itm.get(0).getTenant_record_id().getRecord_id());
			  	  			
			  	  			InvoiceItemsDao ido = new InvoiceItemsDao(db);
				  	  		Transaction tx = db.session().getTransaction();
					  		tx.setTimeout(23);
				  	  		tx.begin();
					  	  	BigDecimal masterBalanceReversal = null;
			  				try {
//			  					Transaction txPlus = db.session().getTransaction();
//		  						txPlus.setTimeout(18);
//		  						txPlus.begin();
			  					//Potong update saldo ke master balance
			  					long getspbalance = System.nanoTime();

			  					masterBalanceReversal= ido.SPBalancePlus(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), itm.get(0).getAmount().abs().intValue(), (int)account.getRecord_id());

			  				
			  					LogSystem.info(request, "Try reversal : ", uuid);
		  						
//			  					txPlus.commit();
			  					
//			  					reversal = true;
		
			  					LogSystem.info(request, "Success reversal add balances : ", uuid);
					  			LogSystem.info(request, "Time Get SP Balance : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
			  					LogSystem.info(request, "Balance from SP : " + masterBalanceReversal, uuid);
			  					
			  				}catch(Exception e)
			  				{
			  					if (tx!= null)
					  			{
					  				tx.rollback();
					  			}
					  			LogSystem.error(getClass(), e, uuid);
					  			
			  					notif="Failed create reversal";
						  	  	jo.put("result", "06");
								jo.put("notif", notif);
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								jo.put("tenant", tenant.getApi_key());
								LogSystem.info(request, jo.toString(), uuid);
								
								try
								{
									TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
									bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
								}catch(Exception t)
								{
									LogSystem.error(request, "Failed send message telegram", uuid);
									LogSystem.error(getClass(), t, uuid);
								}
								
								
								return jo;
			  				}
			  				
			  				try 
				  			{
			  					
					  			if (masterBalanceReversal != null)
					  			{
//					  				Batch batch = new Batch();
//					  				batch.setId(new BigInteger(masterBalanceReversal[1].toString()));
					  				
						  			invReversal.setAccount_id(account.getId());
						  			invReversal.setAccount_record_id(account);
						  			invReversal.setAmount(itdo.findByInvoiceId(jsonRecv.getString("invoiceid")).get(0).getAmount().abs());
						  			invReversal.setCreated_date(new Date());
						  			invReversal.setDescription("Failed transaction");
						  			invReversal.setId(UUID.randomUUID().toString());
						  			invReversal.setInvoice_id(jsonRecv.getString("invoiceid"));
						  			invReversal.setTenant_record_id(tenant);
						  			invReversal.setType("ITEM_ADJ");
						  			invReversal.setUsage_name("Reversal");
						  			invReversal.setPhase_name("Prepaid");
						  			invReversal.setCurrent_balance(masterBalanceReversal);
						  			invReversal.setExternal_key(account.getExternal_key());
						  			invReversal.setTrx(3);
	//					  			invReversal.setFree(itm.get(0).getFree());
						  	  		if(jsonRecv.has("item"))
							  		{
						  	  			invReversal.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
							  		}
						  	  		else
						  	  		{
						  	  			invReversal.setItems(datainv.get(0).getItems());
						  	  		}
						  	  		
//						  	  		invReversal.setBatch(batch);
					  	  			
					  	  			db.session().save(invReversal);
	
							  		try {
							  			long commitinsert = System.nanoTime();
							  			
							  			tx.commit();
							  			LogSystem.info(request, "PROCESS INSERT COMMIT REVERSAL : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
							  		}catch(Exception e)
							  		{
							  			LogSystem.error(getClass(), e, uuid);
							  			LogSystem.info(request, "Reversal commit was failed, rever transaction ", uuid);
							  			db.session().clear();
	
//							        	if(reversal)
//							        	{
//							        		LogSystem.info(request, "Reversal was success before but transaction was failed, abort reversal mines balances : ", uuid);
//							        		
//							        		Transaction txreversal = db.session().getTransaction();
//							        		if(!txreversal.isActive())
//							        		{
//							        			txreversal.begin();
//							        		}
////							        		ido.SPBalanceMines(account.getExternal_key(), account.getTenant_record_id().getRecord_id(), itdo.findByInvoiceId(jsonRecv.getString("invoiceid")).get(0).getAmount().abs(), account.getRecord_id());
//							        		ido.SPBalanceMines(account.getExternal_key(), (int)account.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)account.getRecord_id());
//							        		txreversal.commit();
//											LogSystem.info(request, "Success revers transaction", uuid);
//							        	}
							        	notif="Failed reversal";
								  	  	jo.put("result", "06");
										jo.put("notif", notif);
										jo.put("log", uuid);
										jo.put("tenant", tenant.getApi_key());
										try
										{
											TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
											bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
										}catch(Exception t)
										{
											LogSystem.error(request, "Failed send message telegram", uuid);
											LogSystem.error(getClass(), t, uuid);
										}
	
										return jo;
							  		}
					  			}
				  	  		}catch(Exception e) {
					  	  		if (tx!= null)
					  			{
					  				tx.rollback();
					  			}
					  			LogSystem.error(getClass(), e, uuid);
					  			
				  				notif="Failed create reversal";
						  	  	jo.put("result", "06");
								jo.put("notif", notif);
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								jo.put("tenant", tenant.getApi_key());
								LogSystem.info(request, jo.toString(), uuid);
								
								try
								{
									TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
									bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
								}catch(Exception t)
								{
									LogSystem.error(request, "Failed send message telegram", uuid);
									LogSystem.error(getClass(), t, uuid);
								}
								
								return jo;
				  			}
			  				notif="Success reversal";
					  	  	jo.put("result", "00");
							jo.put("notif", notif);
	//						jo.put("free", itm.get(0).getFree());
							jo.put("invoiceid", invReversal.getInvoice_id());
							jo.put("current_balance", invReversal.getCurrent_balance());
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							jo.put("tenant", tenant.getApi_key());
							jo.put("external_key", invReversal.getExternal_key());
							jo.put("name", invReversal.getAccount_record_id().getName());
							LogSystem.info(request, jo.toString(), uuid);
							
							return jo;
		  	  			}
		  	  		}
//		  	  	}//end if akun adalah prepaid
//		  	  	else
//		  		{
//			  	  	List<InvoiceItems> checkreversal =  itdo.findTransactionReversal(jsonRecv.getString("invoiceid"));
//		  	  		List<InvoiceItems> usage = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
//		  	  		BigDecimal trx = BigDecimal.ZERO;
//		  	  		if(checkreversal.size() > 0) 
//		  	  		{
//		  	  			LogSystem.info(request, "SIZE CHECK REVERSAL :" + checkreversal.size(), uuid);
//		  	  			
//		  	  			for(int i = 0 ; i < checkreversal.size() ; i++)
//		  	  			{
//		  	  				LogSystem.info(request, "TRX SEBELUM :" + trx, uuid);
//		  	  				trx = trx.add(checkreversal.get(i).getAmount());
//		  	  				LogSystem.info(request, "REVERSAL AMOUNT DB :" + checkreversal.get(i).getAmount(), uuid);
//		  	  				LogSystem.info(request, "DITAMBAH :" + trx, uuid);
//		  	  			}
//		  	  			
//		  	  			if (trx.compareTo(usage.get(0).getAmount().abs()) == 0)
//		  				{
//		  					notif="invoiceid already returned for reversal of all amount";
//					  	  	jo.put("result", "03");
//							jo.put("notif", notif);
//							jo.put("log", uuid);
//							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//							LogSystem.info(request, jo.toString(), uuid);
//							return jo;
//		  				}
//		  	  			
//		  	  			if(trx.add(BigDecimal.valueOf(jsonRecv.getLong("amount"))).compareTo(usage.get(0).getAmount().abs()) == 1)
//		  	  			{
//			  	  			notif="existing reversal and amount cannot be greater than transaction";
//					  	  	jo.put("result", "03");
//							jo.put("notif", notif);
//							jo.put("log", uuid);
//							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//							LogSystem.info(request, jo.toString(), uuid);
//							return jo;
//		  	  			}
//		  	  			else
//		  	  			{
//			  	  			if(jsonRecv.has("amount"))
//			  	  			{
//			  	  				if(jsonRecv.has("item"))
//			  	  				{
//			  	  					if(!datainv.get(0).getItems().equals(BigInteger.valueOf(jsonRecv.getLong("item"))) && datainv.get(0).getItems() != null)
//			  	  					{
//				  	  					notif="item for invoice id " + datainv.get(0).getInvoice_id() + " not match";
//								  	  	jo.put("result", "03");
//										jo.put("notif", notif);
//										jo.put("log", uuid);
//										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//										LogSystem.info(request, jo.toString(), uuid);
//										return jo;
//			  	  					}
//			  	  				}
//				  	  			InvoiceItems invReversal = new InvoiceItems();
//				  	  			
//				  	  			List<InvoiceItems> itm = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
//				  	  			BatchDao qbatch = new BatchDao(db);
//
//				  	  			synchronized(this)
//				  	  			{
//				  	  				Batch batch_data = qbatch.findPostpaidByBatchId(itm.get(0).getBatch().getId());
//				  	  				
//					  				try 
//						  			{
//					  					Transaction tx = db.session().getTransaction();
//								  		tx.setTimeout(18);
//							  	  		tx.begin();
//							  	  		
//							  	  		batch_data.setUsage(batch_data.getUsage()-itm.get(0).getAmount().intValue());
//							  	  		db.session().update(batch_data);
//		
//							  			invReversal.setAccount_id(checkreversal.get(0).getAccount_id());
//							  			invReversal.setAccount_record_id(checkreversal.get(0).getAccount_record_id());
//							  			invReversal.setAmount(BigDecimal.valueOf(jsonRecv.getLong("amount")));
//							  			invReversal.setCreated_date(new Date());
//							  			invReversal.setDescription("Failed transaction");
//							  			invReversal.setId(UUID.randomUUID().toString());
//							  			invReversal.setInvoice_id(jsonRecv.getString("invoiceid"));
//							  			invReversal.setTenant_record_id(checkreversal.get(0).getTenant_record_id());
//							  			invReversal.setType("ITEM_ADJ");
//							  			invReversal.setUsage_name("Reversal");
//							  			invReversal.setPhase_name("Postpaid");
//							  			invReversal.setStart_date(new Date());
//							  			invReversal.setEnd_date(new Date());
//							  			invReversal.setPlan_name("Digisign");
//							  			invReversal.setCurrent_balance(BigDecimal.valueOf(batch_data.getUsage()));
//							  			invReversal.setExternal_key(checkreversal.get(0).getExternal_key());
//							  			invReversal.setTrx(3);
//							  			invReversal.setBatch(batch_data);
//							  			
//							  	  		if(jsonRecv.has("item"))
//								  		{
//							  	  			invReversal.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
//								  		}
//								  	  	else
//							  	  		{
//							  	  			invReversal.setItems(datainv.get(0).getItems());
//							  	  		}
//						  	  			
//						  	  			db.session().save(invReversal);
//			
//								  		try {
//								  			long commitinsert = System.nanoTime();
//								  			
//								  			tx.commit();
//								  			LogSystem.info(request, "PROCESS INSERT COMMIT REVERSAL : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
//								  		}catch(Exception e)
//								  		{
//								  			
//								        	notif="Failed reversal postpaid";
//									  	  	jo.put("result", "06");
//											jo.put("notif", notif);
//											jo.put("log", uuid);
//											try
//											{
//												TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
//												bot.execute(new SendMessage(213382980, "Failed Reversal Postpaid : " + uuid));
//											}catch(Exception t)
//											{
//												LogSystem.error(request, "Failed send message telegram", uuid);
//												LogSystem.error(getClass(), e, uuid);
//											}
//											
//											return jo;
//								  		}
//							  			
//						  			}catch(Exception e) {
//		
//							  			LogSystem.error(getClass(), e, uuid);
//							  			
//						  				notif="Failed create reversal postpaid";
//								  	  	jo.put("result", "06");
//										jo.put("notif", notif);
//										jo.put("log", uuid);
//										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//										LogSystem.info(request, jo.toString(), uuid);
//										return jo;
//						  			}
//				  	  			}
//				  				
//				  				notif="Success reversal postpaid";
//						  	  	jo.put("result", "00");
//								jo.put("notif", notif);
//								jo.put("invoiceid", invReversal.getInvoice_id());
//								jo.put("current_balance", invReversal.getCurrent_balance());
//								jo.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invReversal.getCreated_date()));
//								jo.put("log", uuid);
//								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//								LogSystem.info(request, jo.toString(), uuid);
//								
//								return jo;
//			  	  			}
//			  	  			else
//			  	  			{
//				  	  			if(jsonRecv.has("item"))
//			  	  				{
//			  	  					if(!datainv.get(0).getItems().equals(BigInteger.valueOf(jsonRecv.getLong("item"))) && datainv.get(0).getItems() != null)
//			  	  					{
//				  	  					notif="item for invoice id " + datainv.get(0).getInvoice_id() + " not match";
//								  	  	jo.put("result", "03");
//										jo.put("notif", notif);
//										jo.put("log", uuid);
//										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//										LogSystem.info(request, jo.toString(), uuid);
//										return jo;
//			  	  					}
//			  	  				}
//				  	  			
//				  	  			InvoiceItems invReversal = new InvoiceItems();
//				  	  			
//				  	  			List<InvoiceItems> itm = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
//					  	  		BatchDao qbatch = new BatchDao(db);
//
//				  	  			synchronized(this)
//				  	  			{
//				  	  				Batch batch_data = qbatch.findPostpaidByBatchId(itm.get(0).getBatch().getId());
//				  	  				
//					  				try 
//						  			{
//							  			Transaction tx = db.session().getTransaction();
//								  		tx.setTimeout(18);
//							  	  		tx.begin();
//							  	  		
//								  	  	batch_data.setUsage(batch_data.getUsage()-itm.get(0).getAmount().intValue());
//							  	  		db.session().update(batch_data);
//							  		
//							  			invReversal.setAccount_id(itm.get(0).getAccount_id());
//							  			invReversal.setAccount_record_id(itm.get(0).getAccount_record_id());
//							  			invReversal.setAmount(itm.get(0).getAmount().abs());
//							  			invReversal.setCreated_date(new Date());
//							  			invReversal.setDescription("Failed transaction");
//							  			invReversal.setId(UUID.randomUUID().toString());
//							  			invReversal.setInvoice_id(jsonRecv.getString("invoiceid"));
//							  			invReversal.setTenant_record_id(itm.get(0).getTenant_record_id());
//							  			invReversal.setType("ITEM_ADJ");
//							  			invReversal.setUsage_name("Reversal");
//							  			invReversal.setPhase_name("Postpaid");
//							  			invReversal.setCurrent_balance(BigDecimal.valueOf(batch_data.getUsage()));
//							  			invReversal.setExternal_key(itm.get(0).getExternal_key());
//							  			invReversal.setTrx(3);
//							  			invReversal.setBatch(batch_data);
//							  			
//							  	  		if(jsonRecv.has("item"))
//								  		{
//							  	  			invReversal.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
//								  		}
//							  	  		else
//							  	  		{
//							  	  			invReversal.setItems(datainv.get(0).getItems());
//							  	  		}
//						  	  			
//						  	  			db.session().save(invReversal);
//		
//								  		try {
//								  			long commitinsert = System.nanoTime();
//								  		
//								  			tx.commit();
//								  			LogSystem.info(request, "PROCESS INSERT COMMIT REVERSAL : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
//								  		}catch(Exception e)
//								  		{
//								  			LogSystem.error(getClass(), e, uuid);
//								  			
//								        	notif="Failed reversal postpaid";
//									  	  	jo.put("result", "06");
//											jo.put("notif", notif);
//											jo.put("log", uuid);
//											try
//											{
//												TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
//												bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
//											}catch(Exception t)
//											{
//												LogSystem.error(request, "Failed send message telegram", uuid);
//												LogSystem.error(getClass(), t, uuid);
//											}
//											
//											return jo;
//								  		}
//							  			
//						  	  		}catch(Exception e) {
//					
//							  			LogSystem.error(getClass(), e, uuid);
//							  			
//						  				notif="Failed create reversal";
//								  	  	jo.put("result", "06");
//										jo.put("notif", notif);
//										jo.put("log", uuid);
//										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//										LogSystem.info(request, jo.toString(), uuid);
//										
//										try
//										{
//											TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
//											bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
//										}catch(Exception t)
//										{
//											LogSystem.error(request, "Failed send message telegram", uuid);
//											LogSystem.error(getClass(), t, uuid);
//										}
//										
//										return jo;
//						  			}
//				  	  			}
//				  				notif="Success reversal postpaid";
//						  	  	jo.put("result", "00");
//								jo.put("notif", notif);
//								jo.put("invoiceid", invReversal.getInvoice_id());
//								jo.put("current_balance", invReversal.getCurrent_balance());
//								jo.put("log", uuid);
//								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//								LogSystem.info(request, jo.toString(), uuid);
//								
//								return jo;
//			  	  			}
//			  	  		}
//		  	  		}
//		  	  		else
//		  	  		{
//		  	  			if(jsonRecv.has("amount"))
//		  	  			{
//			  	  			if(jsonRecv.has("item"))
//		  	  				{
//		  	  					if(!datainv.get(0).getItems().equals(BigInteger.valueOf(jsonRecv.getLong("item"))) && datainv.get(0).getItems() != null)
//		  	  					{
//			  	  					notif="item for invoice id " + datainv.get(0).getInvoice_id() + " not match";
//							  	  	jo.put("result", "03");
//									jo.put("notif", notif);
//									jo.put("log", uuid);
//									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//									LogSystem.info(request, jo.toString(), uuid);
//									return jo;
//		  	  					}
//		  	  				}
//			  	  			
//			  	  			if(usage.get(0).getAmount().abs().compareTo(BigDecimal.valueOf(jsonRecv.getLong("amount"))) == -1)
//			  	  			{
//				  	  			notif="amount cannot be greater than transaction";
//						  	  	jo.put("result", "03");
//								jo.put("notif", notif);
//								jo.put("log", uuid);
//								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//								LogSystem.info(request, jo.toString(), uuid);
//								return jo;
//			  	  			}
//			  	  			else
//			  	  			{
//				  	  			InvoiceItems invReversal = new InvoiceItems();
//				  	  			
//				  	  			List<InvoiceItems> itm = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
//					  	  		BatchDao qbatch = new BatchDao(db);
//
//				  	  			synchronized(this)
//				  	  			{
//				  	  				Batch batch_data = qbatch.findPostpaidByBatchId(itm.get(0).getBatch().getId());
//				  	  				
//					  				try 
//						  			{
//					  					Transaction tx = db.session().getTransaction();
//								  		tx.setTimeout(18);
//							  	  		tx.begin();
//							  	  		
//								  	  	batch_data.setUsage(batch_data.getUsage()-itm.get(0).getAmount().intValue());
//							  	  		db.session().update(batch_data);
//							  	
//							  			invReversal.setAccount_id(itm.get(0).getAccount_id());
//							  			invReversal.setAccount_record_id(itm.get(0).getAccount_record_id());
//							  			invReversal.setAmount(BigDecimal.valueOf(jsonRecv.getLong("amount")));
//							  			invReversal.setCreated_date(new Date());
//							  			invReversal.setDescription("Failed transaction");
//							  			invReversal.setId(UUID.randomUUID().toString());
//							  			invReversal.setInvoice_id(jsonRecv.getString("invoiceid"));
//							  			invReversal.setTenant_record_id(itm.get(0).getTenant_record_id());
//							  			invReversal.setType("ITEM_ADJ");
//							  			invReversal.setUsage_name("Reversal");
//							  			invReversal.setPhase_name("Postpaid");
//							  			invReversal.setStart_date(new Date());
//							  			invReversal.setEnd_date(new Date());
//							  			invReversal.setPlan_name("Digisign");
//							  			invReversal.setCurrent_balance(BigDecimal.valueOf(batch_data.getUsage()));
//							  			invReversal.setExternal_key(itm.get(0).getExternal_key());
//							  			invReversal.setTrx(3);
//							  			invReversal.setBatch(batch_data);
//							  			
//							  			
//							  	  		if(jsonRecv.has("item"))
//								  		{
//							  	  			invReversal.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
//								  		}
//							  	  		else
//							  	  		{
//							  	  			invReversal.setItems(datainv.get(0).getItems());
//							  	  		}
//						  	  			
//						  	  			db.session().save(invReversal);
//			
//								  		try {
//								  			long commitinsert = System.nanoTime();
//								  			
//								  			tx.commit();
//								  			LogSystem.info(request, "PROCESS INSERT COMMIT REVERSAL : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
//								  		}catch(Exception e)
//								  		{
//								  			LogSystem.error(getClass(), e, uuid);
//								  			
//								        	notif="Failed reversal postpaid";
//									  	  	jo.put("result", "06");
//											jo.put("notif", notif);
//											jo.put("log", uuid);
//											try
//											{
//												TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
//												bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
//											}catch(Exception t)
//											{
//												LogSystem.error(request, "Failed send message telegram", uuid);
//												LogSystem.error(getClass(), t, uuid);
//											}
//											return jo;
//								  		}
//							  			
//						  			}catch(Exception e) {
//		
//							  			LogSystem.error(getClass(), e, uuid);
//							  			
//						  				notif="Failed create reversal";
//								  	  	jo.put("result", "06");
//										jo.put("notif", notif);
//										jo.put("log", uuid);
//										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//										LogSystem.info(request, jo.toString(), uuid);
//										
//										try
//										{
//											TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
//											bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
//										}catch(Exception t)
//										{
//											LogSystem.error(request, "Failed send message telegram", uuid);
//											LogSystem.error(getClass(), t, uuid);
//										}
//										
//										return jo;
//						  			}
//				  	  			}
//				  				
//				  				notif="Success reversal";
//						  	  	jo.put("result", "00");
//								jo.put("notif", notif);
//								jo.put("invoiceid", invReversal.getInvoice_id());
//								jo.put("current_balance", invReversal.getCurrent_balance());
//								jo.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invReversal.getCreated_date()));
//								jo.put("log", uuid);
//								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//								LogSystem.info(request, jo.toString(), uuid);
//								
//								return jo;
//			  	  			}
//		  	  			}
//		  	  			else
//		  	  			{
//			  	  			if(jsonRecv.has("item"))
//		  	  				{
//		  	  					if(!datainv.get(0).getItems().equals(BigInteger.valueOf(jsonRecv.getLong("item"))) && datainv.get(0).getItems() != null)
//		  	  					{
//			  	  					notif="item for invoice id " + datainv.get(0).getInvoice_id() + " not match";
//							  	  	jo.put("result", "03");
//									jo.put("notif", notif);
//									jo.put("log", uuid);
//									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//									LogSystem.info(request, jo.toString(), uuid);
//									return jo;
//		  	  					}
//		  	  				}
//			  	  			
//			  	  			InvoiceItems invReversal = new InvoiceItems();
//			  	  			
//			  	  			List<InvoiceItems> itm = itdo.findByInvoiceId(jsonRecv.getString("invoiceid"));
//				  	  		BatchDao qbatch = new BatchDao(db);
//
//			  	  			synchronized(this)
//			  	  			{
//			  	  				Batch batch_data = qbatch.findPostpaidByBatchId(itm.get(0).getBatch().getId());
//			  	  				
//				  				try 
//					  			{
//				  					Transaction tx = db.session().getTransaction();
//							  		tx.setTimeout(18);
//						  	  		tx.begin();
//						  	  		
//							  	  	batch_data.setUsage(batch_data.getUsage()-itm.get(0).getAmount().intValue());
//						  	  		db.session().update(batch_data);
//						  			
//						  			invReversal.setAccount_id(itm.get(0).getAccount_id());
//						  			invReversal.setAccount_record_id(itm.get(0).getAccount_record_id());
//						  			invReversal.setAmount(itdo.findByInvoiceId(jsonRecv.getString("invoiceid")).get(0).getAmount().abs());
//						  			invReversal.setCreated_date(new Date());
//						  			invReversal.setDescription("Failed transaction");
//						  			invReversal.setId(UUID.randomUUID().toString());
//						  			invReversal.setInvoice_id(jsonRecv.getString("invoiceid"));
//						  			invReversal.setTenant_record_id(itm.get(0).getTenant_record_id());
//						  			invReversal.setType("ITEM_ADJ");
//						  			invReversal.setUsage_name("Reversal");
//						  			invReversal.setPhase_name("Postpaid");
//						  			invReversal.setCurrent_balance(BigDecimal.valueOf(batch_data.getUsage()));
//						  			invReversal.setExternal_key(itm.get(0).getExternal_key());
//						  			invReversal.setTrx(3);
//						  			invReversal.setBatch(batch_data);
//						  			
//						  			
//						  	  		if(jsonRecv.has("item"))
//							  		{
//						  	  			invReversal.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
//							  		}
//						  	  		else
//						  	  		{
//						  	  			invReversal.setItems(datainv.get(0).getItems());
//						  	  		}
//					  	  			
//					  	  			db.session().save(invReversal);
//		
//							  		try {
//							  			long commitinsert = System.nanoTime();
//							  			
//							  			tx.commit();
//							  			LogSystem.info(request, "PROCESS INSERT COMMIT REVERSAL : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
//							  		}catch(Exception e)
//							  		{
//							  			LogSystem.error(getClass(), e, uuid);
//		
//							        	notif="Failed reversal postpaid";
//								  	  	jo.put("result", "06");
//										jo.put("notif", notif);
//										jo.put("log", uuid);
//										try
//										{
//											TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
//											bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
//										}catch(Exception t)
//										{
//											LogSystem.error(request, "Failed send message telegram", uuid);
//											LogSystem.error(getClass(), t, uuid);
//										}
//		
//										return jo;
//							  		}
//					  			
//					  	  		}catch(Exception e) {
//				
//						  			LogSystem.error(getClass(), e, uuid);
//						  			
//					  				notif="Failed create reversal postpaid";
//							  	  	jo.put("result", "06");
//									jo.put("notif", notif);
//									jo.put("log", uuid);
//									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//									LogSystem.info(request, jo.toString(), uuid);
//									
//									try
//									{
//										TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
//										bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
//									}catch(Exception t)
//									{
//										LogSystem.error(request, "Failed send message telegram", uuid);
//										LogSystem.error(getClass(), t, uuid);
//									}
//									
//									return jo;
//					  			}
//			  	  			}
//			  				notif="Success reversal";
//					  	  	jo.put("result", "00");
//							jo.put("notif", notif);
//							jo.put("invoiceid", invReversal.getInvoice_id());
//							jo.put("current_balance", invReversal.getCurrent_balance());
//							jo.put("log", uuid);
//							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//							LogSystem.info(request, jo.toString(), uuid);
//							
//							return jo;
//		  	  			}
//		  	  		}
//		  		}//end else akun adalah prepaid
	  	  	}
	  		
  	  	}catch(RuntimeException e)
  	  	{
  	  	
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Error revers transaction";
	  	  	jo.put("result", "06");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			
			try
			{
				TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
				bot.execute(new SendMessage(213382980, "Failed create reversal log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n invoiceid : " + jsonRecv.getString("invoiceid") + "\n Message : " + e));
			}catch(Exception t)
			{
				LogSystem.error(request, "Failed send message telegram", uuid);
				LogSystem.error(getClass(), t, uuid);
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