package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.BatchDao;
import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.dao.MasterBalanceDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.Batch;
import id.co.keriss.consolidate.ee.InvoiceItems;
import id.co.keriss.consolidate.ee.Invoices;
import id.co.keriss.consolidate.ee.MasterBalance;
import id.co.keriss.consolidate.ee.Tenant;

import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.LogSystem;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

import api.email.SendNotifPeringatanSaldo;

public class CreateTransaction extends ActionSupport {


	final static Logger log=LogManager.getLogger("digisignlogger");

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg)  {

		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		
		String uuid = UUID.randomUUID().toString().replace("-", "");

		List<FileItem> fileItems=null;

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
	        jo=CreateTransaction(jsonRecv, context, request, uuid);
	        
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
				jo.put("notif", "Failed create transaction");
				jo.put("log", uuid);
				LogSystem.info(request, jo.toString(), uuid);
				
				try
				{
					TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
					bot.execute(new SendMessage(213382980, "Failed create transaction : " + uuid + "\n Message : " + e));
				}catch(Exception t)
				{
					LogSystem.error(request, "Failed send message telegram", uuid);
					LogSystem.error(getClass(), t, uuid);
				}
				
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject CreateTransaction(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException, StaleObjectStateException, HibernateException{
		long startTime = System.nanoTime();
		NumberFormat formatter = new DecimalFormat("#0.00000");

		Boolean charge = false;
		Boolean chargenext = false;
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

        String notif="Account already exist";

//        ApiSystemDao maintanance = new ApiSystemDao(db);
        
//		LogSystem.info(request, "Maintanance : " + maintanance.checkSystem(), uuid);
        
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
  	  	
  	  	if(jsonRecv.getString("amount").equals("0")) 
	  	{
	  		notif="Amount can't 0";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
	  	}
  	  
  	  	Invoices invoices = new Invoices();
		InvoiceItems invtrx = new InvoiceItems();
		InvoiceItems invtrxnext = new InvoiceItems();
		
		BigDecimal masterBalancemines = null;
		Object[] masterBalanceminesnext = null;
		Object[] balanceplus = null;
		InvoiceItemsDao ido = new InvoiceItemsDao(db);
		Accounts acc = null;
  	  	try {

		  	AccountsDao ado = new AccountsDao(db);
		
	  	  	TenantDao tdao = new TenantDao(db);
	  		Tenant tenantApiKey = tdao.findByApiKey(jsonRecv.getString("tenant"));
	  		
	  	  	ArrayList<String> listtenant = new ArrayList<String>();
			
			TenantDao tdof = new TenantDao(db);
			List<Tenant> dataTenant = tdof.findAllTenantPrepaid();
			  	
			for (int l = 0 ; l < dataTenant.size() ; l++)
			{
				listtenant.add(dataTenant.get(l).getApi_key());
			}
			
			LogSystem.info(request, "Arraylist tenant : " + listtenant, uuid);

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
	  	  	
			acc = ado.findByExternalKey(jsonRecv.getString("externalkey"), tenantApiKey.getRecord_id());

		  	if(acc == null)
			{
				notif="Account externalkey with that tenant does not exist";
		  	  	jo.put("result", "04");
				jo.put("notif", notif);
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
				LogSystem.info(request, jo.toString(), uuid);
				return jo;
			}
		  	else
		  	{
//		  		if(acc.getSubscription().equals("prepaid"))
//		  		{	
//		  			
//		  			Transaction txMines = db.session().getTransaction();
//		  			
//					txMines.setTimeout(18);
//					txMines.begin();
//					
//		  			TimeLimiter timeLimiter = new SimpleTimeLimiter();
		  			
			  		if(jsonRecv.has("item"))
			  		{
			  			if(jsonRecv.getString("tenant") == "document" || jsonRecv.getString("tenant").equals("document"))
		  				{
				  			//cek dokumen id
			  				LogSystem.info(request, "Document ID : "+jsonRecv.getLong("item"), uuid);
			  				List<InvoiceItems> existingTransaction;
			  				List<InvoiceItems> existingReversal;
			  				List<InvoiceItems> existingTransactioncheck;
			  				
			  				existingTransactioncheck = ido.getItemcheck(jsonRecv.getLong("item"));
			  				if(existingTransactioncheck.size() != 0)
			  				{
			  					LogSystem.info(request, "TRX 1", uuid);
			  					existingTransaction = ido.getItem(jsonRecv.getLong("item"), existingTransactioncheck.get(0).getInvoice_id());
			  					
			  					existingReversal = ido.getItemReversal(jsonRecv.getLong("item"), existingTransaction.get(0).getInvoice_id());
			  					LogSystem.info(request, "REVERSAL size :" + existingReversal.size(), uuid);
			  				}
			  				else
			  				{
			  					LogSystem.info(request, "TRX 0", uuid);
			  					LogSystem.info(request, "REVERSAL 0", uuid);
			  					existingTransaction = existingTransactioncheck;
			  					existingReversal = ido.getItemReversalcheck(jsonRecv.getLong("item"));
			  				}
			  				
			  				LogSystem.info(request, "existingTransaction size : "+existingTransaction.size(), uuid);
			  				LogSystem.info(request, "existingReversal size : "+existingReversal.size(), uuid);

				  			if(existingTransaction.size() >= 1 && existingReversal.size() <= 0)
				  			{
				  				LogSystem.info(request, "Current balance existing : "+existingTransaction.get(0).getCurrent_balance(), uuid);
				  				notif="Success create transaction";
						  	  	jo.put("result", "00");
								jo.put("notif", notif);
								jo.put("current_balance", existingTransaction.get(0).getCurrent_balance());
								jo.put("invoiceid", existingTransaction.get(0).getInvoice_id());
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								jo.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(existingTransaction.get(0).getCreated_date()));
								jo.put("tenant", jsonRecv.getString("tenant"));
								jo.put("external_key", jsonRecv.getString("externalkey"));
								jo.put("name", existingTransaction.get(0).getAccount_record_id().getName());
		
								LogSystem.info(request, jo.toString(), uuid);
								return jo;
				  			}
		  				}
			  		}
			  		

//		  			Transaction txMines = db.session().getTransaction();
//		  			
//					txMines.setTimeout(18);
//					txMines.begin();
			  		Transaction tx = db.session().getTransaction();
			  		tx.setTimeout(21);
			        tx.begin();
		  		
	  				try {
	  					//Potong update saldo ke master balance
	  					long getspbalance = System.nanoTime();

						masterBalancemines = ido.SPBalanceMines(jsonRecv.getString("externalkey"), acc.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")), acc.getRecord_id());
//	  					masterBalancemines = ido.SPBalanceMines(jsonRecv.getString("externalkey"), (int)acc.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)acc.getRecord_id());

						LogSystem.info(request, "Try create transaction charge amount : ", uuid);
		
//						txMines.commit();
						
//	  					charge = true;
	  					
	  					LogSystem.info(request, "Success charge amount : " + masterBalancemines, uuid);
			  			LogSystem.info(request, "Time Get SP Balance : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
	  					LogSystem.info(request, "Balance from SP : " + masterBalancemines, uuid);
	  					
	  				}catch(Exception e)
		  			{
	  					if(tx!=null)
	  					{
	  						tx.rollback();
	  					}
		  					LogSystem.error(getClass(), e, uuid);
//		  					if(charge)
//				        	{
//		  						LogSystem.info(request, "Balance has charge, do reversal balance", uuid);
//	
//		  						Transaction txreversal = db.session().getTransaction();
//				        		if(!txreversal.isActive())
//				        		{
//				        			txreversal.begin();
//				        		}
//					        	balanceplus = ido.SPBalancePlus(jsonRecv.getString("externalkey"), (int)acc.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)acc.getRecord_id(), (int)masterBalancemines[1]);
//	
//				        		LogSystem.info(request, "Reversal amount from "+ masterBalancemines[0] +" balance to : " + balanceplus, uuid);
//				        		LogSystem.info(request, "Success reversal", uuid);
//				        		try
//				        		{
//				        			txreversal.commit();
//				        			charge = false;
//				        		}
//				        		catch(Exception a)
//				        		{
//				        			LogSystem.info(request, "Failed reversal commit, send data to message broker", uuid);
//				        			LogSystem.error(getClass(), a, uuid);
//				        		}
//				        	}
			  					
		  					notif="Failed create transaction";
					  	  	jo.put("result", "06");
							jo.put("notif", notif);
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							
							LogSystem.info(request, jo.toString(), uuid);
			
							try
							{
								TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
								bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed create transaction log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\nMessage : " + e));
							}catch(Exception t)
							{
								LogSystem.error(request, "Failed send message telegram", uuid);
								LogSystem.error(getClass(), t, uuid);
							}
							
							return jo;
		  				}
		  				
			  			try 
			  			{
				  			if (masterBalancemines != null)
				  			{
					  			if (masterBalancemines.intValue() < 0)
					  			{
					  				if(masterBalancemines.intValue() == -2)
					  				{
					  					notif="Failed create transaction";
								  	  	jo.put("result", "05");
										jo.put("notif", notif);
										jo.put("log", uuid);
										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
										jo.put("tenant", jsonRecv.getString("tenant"));
										jo.put("external_key", jsonRecv.getString("externalkey"));
										LogSystem.info(request, jo.toString(), uuid);
										
										try
										{
											TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
											bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed create transaction log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : SP -2"));
										}catch(Exception t)
										{
											LogSystem.error(request, "Failed send message telegram", uuid);
											LogSystem.error(getClass(), t, uuid);
										}
										
										return jo;
					  				}
					  				else
					  				{
					  				//Try send email notif balance
								  		long queryemail = System.nanoTime();
						  				try {
						  					JSONArray saldo = new JSONArray();
								  			
									  		if(jsonRecv.getString("externalkey").startsWith("MT"))
									  		{
									  			if(acc.getNotif_balance().compareTo(new BigDecimal(0)) == 1 && acc.getNotif_email() == false && acc.getSend_notif_balance() == true)
							  					{
									  				InvoiceItemsDao invoiceItemsDao = new InvoiceItemsDao(db);
									  				SendNotifPeringatanSaldo action = new SendNotifPeringatanSaldo();
									  				
									  				List<Accounts> accountlist = invoiceItemsDao.findAccount(jsonRecv.getString("externalkey"));
									  				if(accountlist.size() > 0)
													{
									  	  				for(int i = 0; i < accountlist.size() ; i++)
									  	  				{
									  	  					MasterBalanceDao mbd = new MasterBalanceDao(db);
										  	  				if(accountlist.get(i).getSend_notif_balance())
															{
										  	  					MasterBalance mb = mbd.Balance(accountlist.get(i).getExternal_key(), accountlist.get(i).getTenant_record_id().getRecord_id());
										  	  					
											  	  				JSONObject jsonObject = new JSONObject();
															
											  	  				jsonObject.put("tenant", accountlist.get(i).getTenant_record_id().getApi_key());
											  	  				if(mb != null)
											  	  				{
											  	  					jsonObject.put("amount", mb.getBalance());
											  	  				}
											  	  				else
											  	  				{
											  	  					jsonObject.put("amount", 0);
											  	  				}
											  	  				saldo.put(jsonObject);
															}
										  	  			
									  	  				}
													}

									  				acc.setNotif_email(true);
									  				acc.setUpdated_date(new Date());

									  				LogSystem.info(request, "SALDO : " + saldo, uuid);
										  			
										  			LogSystem.info(request, "Sending email warning minimum balance to : " + jsonRecv.getString("externalkey") + " " + acc.getEmail(), uuid);
										  			
										  			LogSystem.info(request, "List saldo yang dikirim peringatan saldo : " +  saldo, uuid);
										  			try {
										  				action.kirim(acc.getName(), acc.getEmail(), saldo);
										  				new InvoiceItemsDao(db).updateEmail(acc);
										  				LogSystem.info(request, "Sukses kirim notifikasi saldo", uuid);
										  			}catch (Exception e)
										  			{
										  				LogSystem.info(request, "Gagal kirim notifikasi saldo", uuid);
										  				e.printStackTrace();
										  				
										  				try
														{
															TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
															bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed send email notification minimum balance log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : " + e));
														}catch(Exception t)
														{
															LogSystem.error(request, "Failed send message telegram", uuid);
															LogSystem.error(getClass(), t, uuid);
														}
										  			}
							  					}
									  		}
									  		LogSystem.info(request, "Running email checking : " + formatter.format((System.nanoTime() - queryemail)/1000000000d), uuid);
					  					}catch(Exception e)
					  					{
					  						LogSystem.error(getClass(), e, uuid);
								  			
											try
											{
												TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
												bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed send email notification minimum balance log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : " + e));
											}catch(Exception t)
											{
												LogSystem.error(request, "Failed send message telegram", uuid);
												LogSystem.error(getClass(), t, uuid);
											}
					  					}
						  				
					  					notif="Balance not enought";
								  	  	jo.put("result", "05");
										jo.put("notif", notif);
										jo.put("log", uuid);
										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
										jo.put("tenant", jsonRecv.getString("tenant"));
										jo.put("external_key", jsonRecv.getString("externalkey"));
										LogSystem.info(request, jo.toString(), uuid);
									
										return jo;
					  				}
					  				
					  			}
					  			else
					  			{
					  				try {
						  			
//								        Batch batch = new Batch();
//								        batch.setId(new BigInteger(masterBalancemines[1].toString()));    
								     
								  		invoices.setAccount_id(acc.getId());
								  		invoices.setAccount_record_id(acc);
								  		invoices.setCreated_date(new Date());
								  		invoices.setId(UUID.randomUUID().toString());
								  		invoices.setInvoice_date(new Date());
								  		invoices.setTenant_record_id(tenantApiKey);
								  		invoices.setTarget_date(new Date());
								  		invoices.setStatus("COMMITTED");
								  		
								  		db.session().save(invoices);
							  			
								  		
								  		LogSystem.info(request, "Generate invoiceid : "+invoices.getId(), uuid);
								  		
								  		invtrx.setAccount_id(acc.getId());
								  		invtrx.setAccount_record_id(acc);
								  		invtrx.setAmount(BigDecimal.valueOf(jsonRecv.getLong("amount")).negate());
								  		invtrx.setCreated_date(new Date());
								  		invtrx.setDescription("Transaction DS");
								  		invtrx.setId(UUID.randomUUID().toString());
								  		invtrx.setInvoice_id(invoices.getId());
								  		invtrx.setTenant_record_id(tenantApiKey);
								  		invtrx.setType("USAGE");
								  		invtrx.setUsage_name("Usage");
								  		invtrx.setPhase_name("Prepaid");
								  		invtrx.setStart_date(new Date());
								  		invtrx.setEnd_date(new Date());
								  		invtrx.setCurrent_balance(masterBalancemines);
								  		invtrx.setExternal_key(acc.getExternal_key());
								  		invtrx.setPlan_name("Digisign");
								  		invtrx.setTrx(2);
//								  		invtrx.setBatch(batch);

								  		if(jsonRecv.has("item"))
								  		{
								  			invtrx.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
								  		}
								  		
								  		db.session().save(invtrx);
								  		
//								  		// cek jika ada transaksi lebih dari sisa batch
//								  		if((int)masterBalancemines[2] > 0)
//								  		{
//								  			Transaction txMinesnext = db.session().getTransaction();
//
//								  			if(!txMinesnext.isActive())
//								  			{
//								  				txMinesnext.setTimeout(18);
//								  				txMinesnext.begin();
//								  			}
//								  		
//							  				try {
//							  					//Potong update saldo ke master balance
//							  					long getspbalance = System.nanoTime();
//
//												//masterBalancemines = ido.SPBalanceMines(jsonRecv.getString("externalkey"), acc.getTenant_record_id().getRecord_id(), BigDecimal.valueOf(jsonRecv.getLong("amount")), acc.getRecord_id());
//							  					masterBalanceminesnext = ido.SPBalanceMines(jsonRecv.getString("externalkey"), (int)acc.getTenant_record_id().getRecord_id(), (int)masterBalancemines[2], (int)acc.getRecord_id());
//
//												LogSystem.info(request, "Try create transaction charge amount next : ", uuid);
//								
//												txMinesnext.commit();
//												
//							  					chargenext = true;
//							  					
//							  					LogSystem.info(request, "Success charge amount : " + masterBalanceminesnext[0], uuid);
//									  			LogSystem.info(request, "Time Get SP Balance : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
//							  					LogSystem.info(request, "Balance from SP : " + masterBalanceminesnext[0], uuid);
//							  				}catch(Exception e)
//								  			{
//								  					LogSystem.error(getClass(), e, uuid);
//								  					if(chargenext)
//										        	{
//								  						LogSystem.info(request, "Balance has charge on next, do reversal balance", uuid);
//							
//								  						Transaction txreversal = db.session().getTransaction();
//										        		if(!txreversal.isActive())
//										        		{
//										        			txreversal.begin();
//										        		}
//										        		
//											        	balanceplus = ido.SPBalancePlus(jsonRecv.getString("externalkey"), (int)acc.getTenant_record_id().getRecord_id(), (int)masterBalancemines[2],(int)acc.getRecord_id(), (int)masterBalancemines[1]);
//							
//										        		LogSystem.info(request, "Reversal next amount from "+ masterBalancemines[0] +" balance to : " + balanceplus, uuid);
//										        		LogSystem.info(request, "Success reversal next", uuid);
//										        		try
//										        		{
//										        			txreversal.commit();
//										        			chargenext = false;
//										        		}
//										        		catch(Exception a)
//										        		{
//										        			LogSystem.info(request, "Failed reversal commit next, send data to message broker", uuid);
//										        			LogSystem.error(getClass(), a, uuid);
//										        		}
//										        	}
//								  					
//								  					if(charge)
//										        	{
//										        		long getspbalance = System.nanoTime();
//										        		LogSystem.info(request, "Balance has charge, do reversal balance", uuid);
//
//										        		Transaction txreversal = db.session().getTransaction();
//										        		if(!txreversal.isActive())
//										        		{
//										        			txreversal.begin();
//										        		}
//										        		
//										        		balanceplus = ido.SPBalancePlus(jsonRecv.getString("externalkey"), (int)acc.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)acc.getRecord_id(), (int)masterBalancemines[1]);
//										        		LogSystem.info(request, "Reversal amount from "+ masterBalancemines[0] +" balance to : " + balanceplus, uuid);
//
//										    			txreversal.commit();
//										    			
//										    			charge = false;
//										        		LogSystem.info(request, "Success reversal", uuid);	
//										        		LogSystem.info(request, "Time Get SP Balance on catch : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
//										  			}
//									  				
//								  					notif="Failed create transaction";
//											  	  	jo.put("result", "06");
//													jo.put("notif", notif);
//													jo.put("log", uuid);
//													jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//													LogSystem.info(request, jo.toString(), uuid);
//									
//													try
//													{
//														TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
//														bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed create transaction log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\nMessage : " + e));
//													}catch(Exception t)
//													{
//														LogSystem.error(request, "Failed send message telegram", uuid);
//														LogSystem.error(getClass(), t, uuid);
//													}
//													
//													return jo;
//								  				}
//							  				
//							  				Batch batchnext = new Batch();
//							  				batchnext.setId(new BigInteger(masterBalanceminesnext[1].toString()));
//							  				
//								  			invtrxnext.setAccount_id(acc.getId());
//								  			invtrxnext.setAccount_record_id(acc);
//								  			invtrxnext.setAmount(new BigDecimal(masterBalancemines[2].toString()).negate());
//								  			invtrxnext.setCreated_date(new Date());
//								  			invtrxnext.setDescription("Transaction DS");
//								  			invtrxnext.setId(UUID.randomUUID().toString());
//								  			invtrxnext.setInvoice_id(invoices.getId());
//								  			invtrxnext.setTenant_record_id(tenantApiKey);
//								  			invtrxnext.setType("USAGE");
//								  			invtrxnext.setUsage_name("Usage");
//								  			invtrxnext.setPhase_name("Prepaid");
//								  			invtrxnext.setStart_date(new Date());
//								  			invtrxnext.setEnd_date(new Date());
//								  			invtrxnext.setCurrent_balance(new BigDecimal(masterBalanceminesnext[0].toString()));
//								  			invtrxnext.setExternal_key(acc.getExternal_key());
//								  			invtrxnext.setPlan_name("Digisign");
//								  			invtrxnext.setTrx(2);
//								  			invtrxnext.setBatch(batchnext);
//
//									  		if(jsonRecv.has("item"))
//									  		{
//									  			invtrxnext.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
//									  		}
//									  		
//									  		db.session().save(invtrxnext);
//								  		}

							  			long commitinsert = System.nanoTime();
							  			if(!tx.isActive())
							  			{
							  				tx.setTimeout(18);
							  				tx.begin();
							  			}
							  			
							  			Random rand = new Random(); 
							  			int value = rand.nextInt(3);
							  			
							  			LogSystem.info(request, "value : " + value, uuid);

							  			tx.commit();
							  			
							  			LogSystem.info(request, "PROCESS INSERT COMMIT: " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
//							  			LogSystem.info(request, "LEBIH AMOUNT TRANSAKSI " + (int)masterBalancemines[2], uuid);
					
							  		}catch(Exception e)
							  		{
							  			LogSystem.info(request, "Transaction commit was failed, do reversal : ", uuid);
							  			if (tx!= null)
							  			{
							  				tx.rollback();
							  			}
							  			db.session().clear();
							  			

//							        	if(charge)
//							        	{
//							        		long getspbalance = System.nanoTime();
//							        		LogSystem.info(request, "Balance has charge, do reversal balance", uuid);
//
//							        		Transaction txreversal = db.session().getTransaction();
//							        		if(!txreversal.isActive())
//							        		{
//							        			txreversal.begin();
//							        		}
//							        		
//							        		balanceplus = ido.SPBalancePlus(jsonRecv.getString("externalkey"), (int)acc.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)acc.getRecord_id(), (int)masterBalancemines[1]);
//							        		LogSystem.info(request, "Reversal amount from "+ masterBalancemines[0] +" balance to : " + balanceplus, uuid);
//
//							    			txreversal.commit();
//							    			
//							    			charge = false;
//							        		LogSystem.info(request, "Success reversal", uuid);	
//							        		LogSystem.info(request, "Time Get SP Balance on catch : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
//							  			}
								       
							  			LogSystem.error(getClass(), e, uuid);
							  			
								  	  	notif="Failed create transaction";
								  	  	jo.put("result", "06");
										jo.put("notif", notif);
										jo.put("log", uuid);
										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
										jo.put("tenant", jsonRecv.getString("tenant"));
										jo.put("external_key", jsonRecv.getString("externalkey"));
										try
										{
											TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
											bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed create transaction log id : " + uuid + "\nAddress : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\nMessage : " + e));
										}catch(Exception t)
										{
											LogSystem.error(request, "Failed send message telegram", uuid);
											LogSystem.error(getClass(), t, uuid);
										}
										return jo;
							  		}
							  		
							  		//Try send email notif balance
							  		long queryemail = System.nanoTime();
					  				try {
					  					JSONArray saldo = new JSONArray();
							  			
								  		if(jsonRecv.getString("externalkey").startsWith("MT"))
								  		{
								  			if(acc.getNotif_balance().compareTo(invtrx.getCurrent_balance()) == 1 && acc.getNotif_email() == false && acc.getSend_notif_balance() == true)
						  					{
								  				InvoiceItemsDao invoiceItemsDao = new InvoiceItemsDao(db);
								  				SendNotifPeringatanSaldo action = new SendNotifPeringatanSaldo();
								  				
								  				List<Accounts> accountlist = invoiceItemsDao.findAccount(jsonRecv.getString("externalkey"));
								  				if(accountlist.size() > 0)
												{
								  	  				for(int i = 0; i < accountlist.size() ; i++)
								  	  				{
								  	  					MasterBalanceDao mbd = new MasterBalanceDao(db);
									  	  				if(accountlist.get(i).getSend_notif_balance())
														{
									  	  					MasterBalance mb = mbd.Balance(accountlist.get(i).getExternal_key(), accountlist.get(i).getTenant_record_id().getRecord_id());
									  	  					
										  	  				JSONObject jsonObject = new JSONObject();
														
										  	  				jsonObject.put("tenant", accountlist.get(i).getTenant_record_id().getApi_key());
										  	  				if(mb != null)
										  	  				{
										  	  					jsonObject.put("amount", mb.getBalance());
										  	  				}
										  	  				else
										  	  				{
										  	  					jsonObject.put("amount", 0);
										  	  				}
										  	  				saldo.put(jsonObject);
														}
								  	  				}
												}

								  				acc.setNotif_email(true);
								  				acc.setUpdated_date(new Date());

								  				LogSystem.info(request, "SALDO : " + saldo, uuid);
									  			
									  			LogSystem.info(request, "Sending email warning minimum balance to : " + jsonRecv.getString("externalkey") + " " + acc.getEmail(), uuid);
									  			
									  			LogSystem.info(request, "List saldo yang dikirim peringatan saldo : " +  saldo, uuid);
									  			try {
									  				action.kirim(acc.getName(), acc.getEmail(), saldo);
									  				new InvoiceItemsDao(db).updateEmail(acc);
									  				LogSystem.info(request, "Sukses kirim notifikasi saldo", uuid);
									  			}catch (Exception e)
									  			{
									  				LogSystem.info(request, "Gagal kirim notifikasi saldo", uuid);
									  				e.printStackTrace();
									  				
									  				try
													{
														TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
														bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed send email notification minimum balance log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : " + e));
													}catch(Exception t)
													{
														LogSystem.error(request, "Failed send message telegram", uuid);
														LogSystem.error(getClass(), t, uuid);
													}
									  			}
						  					}
								  		}
								  		LogSystem.info(request, "Running email checking : " + formatter.format((System.nanoTime() - queryemail)/1000000000d), uuid);
				  					}catch(Exception e)
				  					{
				  						LogSystem.error(getClass(), e, uuid);
							  			
										try
										{
											TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
											bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed send email notification minimum balance log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : " + e));
										}catch(Exception t)
										{
											LogSystem.error(request, "Failed send message telegram", uuid);
											LogSystem.error(getClass(), t, uuid);
										}
				  					}
					  				
					  				notif="Success create transaction";
							  	  	jo.put("result", "00");
									jo.put("notif", notif);
									
									if(invtrxnext.getId() != null)
									{
										jo.put("current_balance", invtrxnext.getCurrent_balance());
									}
									else
									{
										jo.put("current_balance", invtrx.getCurrent_balance());
									}
									LogSystem.info(request, "ISI INVTRXNEXT " + invtrxnext.getId(), uuid);
									jo.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(invtrx.getCreated_date()));
									
									jo.put("invoiceid", invoices.getId());
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									jo.put("tenant", jsonRecv.getString("tenant"));
									jo.put("external_key", jsonRecv.getString("externalkey"));
									jo.put("name", invoices.getAccount_record_id().getName());
									LogSystem.info(request, jo.toString(), uuid);
									return jo;
					  			}
				  			}
				  			else
				  			{
				  				LogSystem.info(request, "SP Balance null", uuid);
				  				notif="Failed create transaction";
						  	  	jo.put("result", "06");
								jo.put("notif", notif);
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								jo.put("tenant", jsonRecv.getString("tenant"));
								jo.put("external_key", jsonRecv.getString("externalkey"));
								LogSystem.info(request, jo.toString(), uuid);
								try
								{
									TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
									bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed create transaction log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : SP Balance null "));
								}catch(Exception t)
								{
									LogSystem.error(request, "Failed send message telegram", uuid);
									LogSystem.error(getClass(), t, uuid);
								}
								
								return jo;
				  			}
			  			}catch(Exception e) {
			  				LogSystem.error(getClass(), e, uuid);
			  				if(tx!=null)
		  					{
		  						tx.rollback();
		  					}
			  				db.session().clear();

//			  				if(charge)
//				        	{
//			  					long getspbalance = System.nanoTime();
//		  						LogSystem.info(request, "Balance has charge, do reversal balance", uuid);
//
//		  						Transaction txreversal = db.session().getTransaction();
//				        		if(!txreversal.isActive())
//				        		{
//				        			txreversal.begin();
//				        		}
//				        		balanceplus = ido.SPBalancePlus(jsonRecv.getString("externalkey"), (int)acc.getTenant_record_id().getRecord_id(), jsonRecv.getInt("amount"), (int)acc.getRecord_id(), (int)masterBalancemines[1]);
//
//				        		LogSystem.info(request, "Reversal amount from "+ masterBalancemines[0] +" balance to : " + balanceplus, uuid);
//				        		
//				    			txreversal.commit();
//				    			
//				    			charge = false;
//				    			
//				        		LogSystem.info(request, "Success reversal", uuid);	
//				        		LogSystem.info(request, "Time Get SP Balance on catch : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
//				        	}
			  				
				  			LogSystem.error(getClass(), e, uuid);
				  			
			  				notif="Failed create transaction";
					  	  	jo.put("result", "06");
							jo.put("notif", notif);
							jo.put("log", uuid);
							jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							jo.put("tenant", jsonRecv.getString("tenant"));
							jo.put("external_key", jsonRecv.getString("externalkey"));
							LogSystem.info(request, jo.toString(), uuid);
							
							try
							{
								TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
								bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed create transaction log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : " + e));
							}catch(Exception t)
							{
								LogSystem.error(request, "Failed send message telegram", uuid);
								LogSystem.error(getClass(), t, uuid);
							}
							
							return jo;
			  			}
			  	
//		  		}//end if akun adalah prepaid
//		  		else
//		  		{
//		  			BatchDao qbatch = new BatchDao(db);
//		  			InvoiceItems postpaidItems = new InvoiceItems();
//		  			
//		  			synchronized(this){
//			  			Batch batch_data = qbatch.findPostpaidByAccountRecord(acc.getRecord_id());
//			  			Date now = new Date();
//			  			if(batch_data != null)
//			  			{
//			  				SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
//			  				LogSystem.info(request, "Cek hari ini apa sudah melewati tanggal closing", uuid);
//			  				
//			  				String convertNow = myFormat.format(now);
//			  				String convertClosing = myFormat.format(batch_data.getClosing_date());
//			  				
//			  				LogSystem.info(request, "Tanggal hari ini " + convertNow, uuid);
//			  				LogSystem.info(request, "Tanggal closing " + convertClosing, uuid);
//			  				
//			  				Date parseNow = null;
//			  				Date parseClosing = null;
//			  				try {
//			  					parseNow = myFormat.parse(convertNow);
//			  					parseClosing = myFormat.parse(convertClosing);
//			  				}
//			  				catch(Exception e)
//			  				{
//			  					LogSystem.error(request, "Error parse date now", uuid);
//			  					notif="Failed create transaction";
//			  			  	  	jo.put("result", "06");
//			  					jo.put("notif", notif);
//			  					jo.put("log", uuid);
//			  					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//			  					return jo;
//			  				}
//
//			  				long diff =  parseNow.getTime() - parseClosing.getTime();
//			  				long selisih = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
//			  				
//			  				LogSystem.info(request, "Selisih " + selisih, uuid);
//			  				
//			  				if(selisih > 0)
//			  				{
//			  					LogSystem.info(request, "Tutup dan buat batch baru", uuid);
//			  					Transaction txBatch = db.session().getTransaction();
//					  			
//				  				txBatch.setTimeout(18);
//				  				txBatch.begin();
//			  					batch_data.setSettled(true);
//			  					db.session().update(batch_data);
//			  					txBatch.commit();
//			  					LogSystem.info(request, "Selesai tutup dan buat batch baru", uuid);
//			  					
//			  					batch_data = null;
//			  				}
//			  			}
//			  			
//			  			//Set Closing date
//		  				Calendar cal = Calendar.getInstance(); 
//		  				if(cal.get(Calendar.DAY_OF_MONTH) > acc.getBill_period())
//		  				{
//			  				cal.add(Calendar.MONTH, 1);
//			  				cal.set(Calendar.DAY_OF_MONTH, acc.getBill_period());
//		  				}
//		  				else
//		  				{
//		  					cal.add(Calendar.MONTH, 0);
//			  				cal.set(Calendar.DAY_OF_MONTH, acc.getBill_period());
//		  				}
//			  			
//			  			if(batch_data == null)
//			  			{
//			  				//Create new batch
//			  				Transaction txpostpaid = db.session().getTransaction();
//				  			
//			  				txpostpaid.setTimeout(18);
//			  				txpostpaid.begin();
//			  				
//			  				Batch batch = new Batch();
//			  				batch.setAccount_record_id(acc);
//			  				batch.setOpen_date(now);
//			  				batch.setClosing_date(cal.getTime());
//			  				batch.setName_batch("POST<" + jsonRecv.getString("externalkey") + "_" + tenantApiKey.getApi_key() + ">");
//			  				batch.setSettled(false);
//			  				batch.setUsage(jsonRecv.getInt("amount"));
//			  				batch.setQuota(0);
//			  				batch.setRemaining_balance(0);
//			  				batch.setPrice(0);
//			  				
//			  				db.session().save(batch);
//					        
//					  		invoices.setAccount_id(acc.getId());
//					  		invoices.setAccount_record_id(acc);
//					  		invoices.setCreated_date(new Date());
//					  		invoices.setId(UUID.randomUUID().toString());
//					  		invoices.setInvoice_date(now);
//					  		invoices.setTenant_record_id(tenantApiKey);
//					  		invoices.setTarget_date(now);
//					  		invoices.setStatus("COMMITTED");
//	
//					  		db.session().save(invoices);
//					  		
//			  				//Use existing batch
//			  				postpaidItems.setAccount_id(acc.getId());
//			  				postpaidItems.setAccount_record_id(acc);
//			  				postpaidItems.setAmount(BigDecimal.valueOf(jsonRecv.getLong("amount")));
//			  				postpaidItems.setCreated_date(now);
//			  				postpaidItems.setDescription("Transaction DS");
//			  				postpaidItems.setId(UUID.randomUUID().toString());
//			  				postpaidItems.setInvoice_id(invoices.getId());
//			  				postpaidItems.setTenant_record_id(tenantApiKey);
//			  				postpaidItems.setType("USAGE");
//			  				postpaidItems.setUsage_name("Usage");
//			  				postpaidItems.setPhase_name("Postpaid");
//			  				postpaidItems.setStart_date(now);
//			  				postpaidItems.setEnd_date(cal.getTime());
//			  				postpaidItems.setCurrent_balance(BigDecimal.valueOf(batch.getUsage()));
//			  				postpaidItems.setExternal_key(acc.getExternal_key());
//			  				postpaidItems.setPlan_name("Digisign");
//			  				postpaidItems.setTrx(2);
//			  				postpaidItems.setBatch(batch);
//			  				
//			  				db.session().save(postpaidItems);
//			  				
//			  				if(jsonRecv.has("item"))
//					  		{
//			  					postpaidItems.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
//					  		}
//	
//			  				try {
//					  			long commitinsert = System.nanoTime();
//		
//					  			txpostpaid.commit();
//					  			LogSystem.info(request, "PROCESS INSERT COMMIT : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
//			  				}catch(Exception e)
//			  				{
//			  					notif="Failed create transaction postpaid";
//			  			  	  	jo.put("result", "06");
//			  					jo.put("notif", notif);
//			  					jo.put("log", uuid);
//			  					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//			  					try
//			  					{
//			  						TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
//			  						bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed create transaction postpaid log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : " + e));
//			  					}catch(Exception t)
//			  					{
//			  						LogSystem.error(request, "Failed send message telegram", uuid);
//			  						LogSystem.error(getClass(), t, uuid);
//			  					}
//			  				}
//			  			}
//			  			else
//			  			{
//			  				Transaction txpostpaid = db.session().getTransaction();
//				  			
//			  				txpostpaid.setTimeout(18);
//			  				txpostpaid.begin();
//
//		  					batch_data.setUsage(batch_data.getUsage()+jsonRecv.getInt("amount"));
//		  					db.session().update(batch_data);
//
//					  		invoices.setAccount_id(acc.getId());
//					  		invoices.setAccount_record_id(acc);
//					  		invoices.setCreated_date(new Date());
//					  		invoices.setId(UUID.randomUUID().toString());
//					  		invoices.setInvoice_date(now);
//					  		invoices.setTenant_record_id(tenantApiKey);
//					  		invoices.setTarget_date(now);
//					  		invoices.setStatus("COMMITTED");
//	
//					  		db.session().save(invoices);
//					  		
//			  				//Use existing batch
//			  				postpaidItems.setAccount_id(acc.getId());
//			  				postpaidItems.setAccount_record_id(acc);
//			  				postpaidItems.setAmount(BigDecimal.valueOf(jsonRecv.getLong("amount")));
//			  				postpaidItems.setCreated_date(now);
//			  				postpaidItems.setDescription("Transaction DS");
//			  				postpaidItems.setId(UUID.randomUUID().toString());
//			  				postpaidItems.setInvoice_id(invoices.getId());
//			  				postpaidItems.setTenant_record_id(tenantApiKey);
//			  				postpaidItems.setType("USAGE");
//			  				postpaidItems.setUsage_name("Usage");
//			  				postpaidItems.setPhase_name("Postpaid");
//			  				postpaidItems.setStart_date(now);
//			  				postpaidItems.setEnd_date(cal.getTime());
//			  				postpaidItems.setCurrent_balance(BigDecimal.valueOf(batch_data.getUsage()));
//			  				postpaidItems.setExternal_key(acc.getExternal_key());
//			  				postpaidItems.setPlan_name("Digisign");
//			  				postpaidItems.setTrx(2);
//			  				postpaidItems.setBatch(batch_data);
//			  				
//			  				db.session().save(postpaidItems);
//			  				
//			  				if(jsonRecv.has("item"))
//					  		{
//			  					postpaidItems.setItems(BigInteger.valueOf(jsonRecv.getLong("item")));
//					  		}
//	
//			  				try {
//					  			long commitinsert = System.nanoTime();
//		
//					  			txpostpaid.commit();
//					  			LogSystem.info(request, "PROCESS INSERT COMMIT : " + formatter.format((System.nanoTime() - commitinsert)/1000000000d), uuid);
//			  				}catch(Exception e)
//			  				{
//			  					notif="Failed create transaction postpaid";
//			  			  	  	jo.put("result", "06");
//			  					jo.put("notif", notif);
//			  					jo.put("log", uuid);
//			  					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//			  					try
//			  					{
//			  						TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
//			  						bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed create transaction postpaid log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : " + e));
//			  					}catch(Exception t)
//			  					{
//			  						LogSystem.error(request, "Failed send message telegram", uuid);
//			  						LogSystem.error(getClass(), t, uuid);
//			  					}
//			  				}
//			  			}
//		  			}
//
//		  			notif="Success create transaction postpaid";
//			  	  	jo.put("result", "00");
//					jo.put("notif", notif);
//					jo.put("current_balance", postpaidItems.getCurrent_balance());
//					jo.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(postpaidItems.getCreated_date()));
//					
//					jo.put("invoiceid", invoices.getId());
//					jo.put("log", uuid);
//					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//					LogSystem.info(request, jo.toString(), uuid);
//					
//					return jo;			
//		  
//		  		}//end else akun adalah postpaid
//		  		
	  		}
  	  	}catch(RuntimeException e)
  	  	{
			LogSystem.error(getClass(), e, uuid);
			db.session().clear();
	
//			if(charge)
//        	{
//				long getspbalance = System.nanoTime();
//				LogSystem.info(request, "Balance has charge, do reversal balance", uuid);
//
//				Transaction txreversal = db.session().getTransaction();
//        		if(!txreversal.isActive())
//        		{
//        			txreversal.begin();
//        		}
//        		
//        		balanceplus = ido.SPBalancePlus(jsonRecv.getString("externalkey"), (int)acc.getTenant_record_id().getRecord_id(),jsonRecv.getInt("amount"), (int)acc.getRecord_id(), (int)masterBalancemines[1]);
//        		
//        		LogSystem.info(request, "Reversal amount from "+ masterBalancemines[0] +" balance to : " + balanceplus, uuid);
//        		
//    			txreversal.commit();
//    			
//    			charge = false;
//        		LogSystem.info(request, "Success reversal", uuid);	
//        		LogSystem.info(request, "Time Get SP Balance on catch : " + formatter.format((System.nanoTime() - getspbalance)/1000000000d), uuid);
//        		
//        	}
	
	  	  	notif="Failed create transaction";
	  	  	jo.put("result", "06");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//			jo.put("tenant", jsonRecv.getString("tenant"));
			try
			{
				TelegramBot bot=new TelegramBot("912934463:AAGOhuRQyFtd5huj0mqsOjkdR8IARrdREYE");
				bot.execute(new SendMessage(213382980, jsonRecv.getString("externalkey") + " " + jsonRecv.getString("tenant") + "\nFailed create transaction log id : " + uuid + "\n Address : " + inetAddress.getHostAddress() + "@" + inetAddress.getHostName() + "\n Message : " + e));
			}catch(Exception t)
			{
				LogSystem.error(request, "Failed send message telegram", uuid);
				LogSystem.error(getClass(), t, uuid);
			}

			return jo;
  	  	} finally {
  	  		LogSystem.info(request, "Close session connection to db", uuid);
			db.session().close();
			LogSystem.info(request, "Close connection db", uuid);
			db.close();
  	  	}
	}
}