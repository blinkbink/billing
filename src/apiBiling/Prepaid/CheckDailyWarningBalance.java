//package apiBiling.Prepaid;
//
//import id.co.keriss.consolidate.action.ActionSupport;
//import id.co.keriss.consolidate.dao.AccountsDao;
//import id.co.keriss.consolidate.dao.InvoiceItemsDao;
//import id.co.keriss.consolidate.ee.InvoiceItems;
//import id.co.keriss.consolidate.ee.Pivot;
//import org.hibernate.HibernateException;
//import org.hibernate.StaleObjectStateException;
//import org.hibernate.Transaction;
//import id.co.keriss.consolidate.util.LogSystem;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.UUID;
//
//import javax.servlet.http.HttpServletRequest;
//import org.apache.log4j.Logger;
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//import org.jpos.ee.Accounts;
//import org.jpos.ee.DB;
//import org.jpublish.JPublishContext;
//import com.anthonyeden.lib.config.Configuration;
//import api.email.SendNotifPeringatanSaldo;
//
//public class CheckDailyWarningBalance extends ActionSupport {
//
//	static String basepath="/opt/data-DS/UploadFile/";
//	static String basepathPreReg="/opt/data-DS/PreReg/";
//	final static Logger log=Logger.getLogger("digisignlogger");
//
//	@Override
//	public void execute(JPublishContext context, Configuration cfg) {
//		HttpServletRequest request = context.getRequest();
//		String uuid = UUID.randomUUID().toString();
//
//        
//		try {
//			context.put("trxjson", CheckDailyWarning(context, request, uuid).toString());
//		} catch (StaleObjectStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	JSONObject CheckDailyWarning(JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException, StaleObjectStateException{
//
//		JSONObject jo = new JSONObject();
//		jo.put("status", "Sukses");
//		int i;
//		int k;
//		
//		DB db = new DB();
//		try {
//			db = getDB(context);
//		}catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		
//		AccountsDao acd = new AccountsDao(db);
//		InvoiceItemsDao ido = new InvoiceItemsDao(db);
//		List<Accounts> accounts = acd.allAccountNotifBalanceTrue();
//
//		LogSystem.info(request, "Running Daily Check Warning Balance", uuid);
//
//		JSONArray saldo = new JSONArray();
//		LogSystem.info(request, "JUMLAH SELURUH AKUN YANG KIRIM NOTIF BALANCE TRUE : "+accounts.size(), uuid);
//		for(i=0 ; i < accounts.size() ; i++)
//		{
////			List<InvoiceItems> balance = ido.singleBalance(accounts.get(i).getId());
//			List<Accounts> accountexternalkey = ido.findNotifBalanceTrue(accounts.get(i).getExternal_key());
//			LogSystem.info(request, "JUMLAH AKUN TENANT YANG TRUE : "+accountexternalkey.size(), uuid);
//			for(k = 0 ; k < accountexternalkey.size() ; k++)
//			{
//				LogSystem.info(request, "CHECKING ACCOUNT BALANCE : " + accountexternalkey.get(k).getName(), uuid);
//				
//	  			BigDecimal balance = null;
//
//  				List<InvoiceItems> invoiceitems = ido.findAccountonInvoiceItems(accountexternalkey.get(k).getTenant_record_id().getRecord_id(), accountexternalkey.get(k).getId().toString());
//  				
//  	  			if(invoiceitems.size() > 0)
//  	  			{
//  	  				Pivot pivote = ido.findPivotRecordID(accountexternalkey.get(k).getExternal_key(), accountexternalkey.get(k).getTenant_record_id().getRecord_id());
//  	  				if(pivote != null)
//  	  				{
//	  	  				BigDecimal pivoteBalance = ido.getPivotBalance(accountexternalkey.get(k).getExternal_key(), accountexternalkey.get(k).getTenant_record_id().getRecord_id(), pivote.getInvoice_items_record_id().getRecord_id());
//	  	  				if (pivoteBalance != null)
//	  	  				{
//		  	  				balance = pivote.getCurrent_balance().add(pivoteBalance);
//	  	  				}
//	  	  				else
//	  	  				{
//	  	  					balance = pivote.getCurrent_balance().add(new BigDecimal(0));
//	  	  				}
//  	  				}
//  	  				else
//  	  				{
//  	  					balance = ido.getSingleBalance(accountexternalkey.get(k).getExternal_key(), accountexternalkey.get(k).getTenant_record_id().getRecord_id());
//  	  				}
//  	  			}
//	  	  		else
//	  			{
//	  				balance = new BigDecimal(0);
//	  			}
//
//				if(accountexternalkey.get(k).getNotif_balance().compareTo(balance) == 1 && accountexternalkey.get(k).getNotif_email() == false && accountexternalkey.get(k).getSend_notif_balance() == true)
//				{
//					try {
//						//Get All Balance
//			  			for(int j = 0; j < accountexternalkey.size() ; j++)
//		  				{
//		  					List<InvoiceItems> invoiceitems1 = ido.findAccountonInvoiceItems(accountexternalkey.get(j).getTenant_record_id().getRecord_id(), accountexternalkey.get(j).getId());
//		  					BigDecimal allBalance = null;
//		  					if(invoiceitems1.size() > 0)
//		  					{
//			  					Pivot pivote = ido.findPivotRecordID(accountexternalkey.get(j).getExternal_key(), accountexternalkey.get(j).getTenant_record_id().getRecord_id());
//			  	  				if(pivote != null)
//			  	  				{
//				  	  				BigDecimal pivoteBalance = ido.getPivotBalance(accountexternalkey.get(j).getExternal_key(), accountexternalkey.get(j).getTenant_record_id().getRecord_id(), pivote.getInvoice_items_record_id().getRecord_id());
//				  	  				if (pivoteBalance != null)
//				  	  				{
//				  	  					allBalance = pivote.getCurrent_balance().add(pivoteBalance);
//				  	  				}
//				  	  				else
//				  	  				{
//				  	  					allBalance = pivote.getCurrent_balance().add(new BigDecimal(0));
//				  	  				}
//			  	  				}
//			  	  				else
//			  	  				{
//			  	  					allBalance = ido.getSingleBalance(accountexternalkey.get(j).getExternal_key(), accountexternalkey.get(j).getTenant_record_id().getRecord_id());
//			  	  				}
//			
//				  				JSONObject jsonObject = new JSONObject();
//				
//			  	  				jsonObject.put("tenant", accountexternalkey.get(j).getTenant_record_id().getApi_key());
//			  	  				jsonObject.put("amount", allBalance);
//			  	  			
//			  	  				saldo.put(jsonObject);
//		  					}
//		  					else
//		  					{
//			  					JSONObject jsonObject = new JSONObject();
//			  					jsonObject.put("tenant", accountexternalkey.get(i).getTenant_record_id().getApi_key());
//			  	  				jsonObject.put("amount", 0);
//		  	  			
//		  	  					saldo.put(jsonObject);
//		  					}
//		  				}
//
//						String ttd = null;
//		  				String doc = null;
//		  				String sms = null;
//		  				String ver = null;
//		  				int b;
//
//		  				Transaction tx = db.session().getTransaction();
//		  				
//		  				tx.setTimeout(100);
//		  				tx.begin();
//
//		  				accountexternalkey.get(k).setNotif_email(true);
//			  			
//			  			db.session().update(accountexternalkey.get(k));
//
//		  				LogSystem.info(request, "SALDO : " + saldo, uuid);
//
//			  			for(b = 0 ; b < saldo.length() ; b++)
//			  			{
//			  				JSONObject obj=(JSONObject) saldo.get(b);
//			  				
//			  				if(obj.getString("tenant").equals("personal"))
//			  				{
//			  					ttd = obj.getString("amount");
//			  					LogSystem.info(request, "PERSONAL : " + obj.getString("amount"), uuid);
//			  				}
//			  				if(obj.getString("tenant").equals("document"))
//			  				{
//			  					doc = obj.getString("amount");
//			  					LogSystem.info(request, "DOCUMENTS : " + obj.getString("amount"), uuid);
//			  				}
//			  				if(obj.getString("tenant").equals("sms"))
//			  				{
//			  					sms = obj.getString("amount");
//			  					LogSystem.info(request, "SMS : " + obj.getString("amount"), uuid);
//			  				}
//			  				if(obj.getString("tenant").equals("verifikasi"))
//			  				{
//			  					ver = obj.getString("amount");
//			  					LogSystem.info(request, "VERIFIKASI : " + obj.getString("amount"), uuid);
//			  				}
//			  			}
//						
//		  				SendNotifPeringatanSaldo action = new SendNotifPeringatanSaldo();
//						LogSystem.info(request, "Sending email warning minimum balance for : " + accountexternalkey.get(k).getName() + " external key " +  accountexternalkey.get(k).getExternal_key() + " with tenant " +  accountexternalkey.get(k).getTenant_record_id().getApi_key(), uuid);
//		  				action.kirim(ttd, doc, sms, ver, accountexternalkey.get(k).getName(), accountexternalkey.get(k).getEmail());	
//
//		  			    tx.commit();
//
//					} catch (HibernateException e) {
//						// TODO Auto-generated catch block
//						
//						e.printStackTrace();
//						jo.put("status", "Gagal");
//		  				return jo;
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						
//						e.printStackTrace();
//						jo.put("status", "Gagal");
//		  				return jo;
//					}
//				}
//			}
//		}
//		jo.put("status", "Sukses checking daily warning balance");
//		return jo;
//	}
//}