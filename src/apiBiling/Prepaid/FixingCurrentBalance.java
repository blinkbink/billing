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
//import java.math.BigInteger;
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
//public class FixingCurrentBalance extends ActionSupport {
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
//			context.put("trxjson", FixingCurrentBalance(context, request, uuid).toString());
//		} catch (StaleObjectStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	JSONObject FixingCurrentBalance(JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException, StaleObjectStateException{
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
//		List<Accounts> accounts = acd.allAccount();
//
//		LogSystem.info(request, "Fixing current balance all account", uuid);
//
//		try {
//			LogSystem.info(request, "Fixing " + accounts.size() + " account current balance", uuid);
//			for(int j = 0 ; j < accounts.size() ; j++)
//			{
//				LogSystem.info(request, "Fixing account balance " + j + " from " + accounts.size(), uuid);
//				new InvoiceItemsDao(db).FixingCurrentBalanceCBA_ADJ(accounts.get(j).getExternal_key(), BigInteger.valueOf(accounts.get(j).getTenant_record_id().getRecord_id()));
//				new InvoiceItemsDao(db).FixingCurrentBalanceUSAGE(accounts.get(j).getExternal_key(), BigInteger.valueOf(accounts.get(j).getTenant_record_id().getRecord_id()));
//				new InvoiceItemsDao(db).FixingCurrentBalanceITEM_ADJ(accounts.get(j).getExternal_key(), BigInteger.valueOf(accounts.get(j).getTenant_record_id().getRecord_id()));
//				new InvoiceItemsDao(db).FixingCurrentBalanceCREDIT_ADJ(accounts.get(j).getExternal_key(), BigInteger.valueOf(accounts.get(j).getTenant_record_id().getRecord_id()));
//			}
//			LogSystem.info(request, "Fixing is done.....", uuid);
//			
//		}catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		finally {
//			db.session().close();
//			db.close();
//  	  	}
//		
//		return jo;
//	}
//}