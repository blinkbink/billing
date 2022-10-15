package id.co.keriss.consolidate.dao;

import java.util.List;
import java.util.UUID;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.Accounts;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.util.LogSystem;

public class AccountsDao {
	Session session;
	DB db;
	Log log;
	
	public AccountsDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	public List<Accounts> allAccount()
	{
		return (List<Accounts>) session.createQuery("from Accounts where external_key like '%MT%'").list();
	}
	
	public List<Accounts> allAccountNotifBalanceTrue()
	{
		return (List<Accounts>) session.createQuery("from Accounts where external_key like '%MT%' and send_notif_balance is true").list();
	}

	public Accounts findByExternalKey(String external_key, long tenant)
	{
		return (Accounts) session.createQuery("from Accounts where external_key = '"+external_key+"' AND tenant_record_id = '"+tenant+"'").uniqueResult();
	}
	
	public List<Accounts> findSingleAccount(String external_key)
	{
		return (List<Accounts>) session.createQuery("from Accounts a where external_key = '"+external_key+"'").list();
	}
	
	public Object updateNotifTodayTrueMT()
	{
		return session.createSQLQuery("UPDATE accounts set notif_email = false WHERE notif_email = true").uniqueResult();
	}
	
	public JSONArray allAccounts(String external_key) throws HibernateException, JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONArray list = new JSONArray();
        
		List<Object[]> q = session.createSQLQuery("SELECT a.id, a.name, a.external_key, i.tenant_record_id, SUM(i.amount), t.api_key FROM accounts a, invoice_items i, tenants t WHERE t.record_id = a.tenant_record_id AND a.id = i.account_id AND i.type='CBA_ADJ' AND a.external_key = '"+external_key+"' GROUP BY a.name, a.id, a.external_key, a.tenant_record_id, i.tenant_record_id, t.api_key ORDER BY a.external_key asc").list();
	
		for(Object[] a : q)
		{
			jsonArray.put(a[5] + " : " + a[4] + " : " + a[3] + " : " + a[2] + " : " + a[1] + " : " + a[0]);
		}
		
		for(int i = 0 ; i < jsonArray.length() ; i++)
		{
			JSONObject accountData = new JSONObject();
	
			accountData.put("amount", jsonArray.get(i).toString().split(":")[1].replaceAll(" ", ""));
			accountData.put("tenant", jsonArray.get(i).toString().split(":")[0].replaceAll(" ", ""));
			accountData.put("externalkey", jsonArray.get(i).toString().split(":")[3].replaceAll(" ", ""));
			accountData.put("id", jsonArray.get(i).toString().split(":")[5].replaceAll(" ", ""));
  			accountData.put("name", jsonArray.get(i).toString().split(":")[4]);
  			
  			if(jsonArray.get(i).toString().split(":")[0].replaceAll(" ", "").equals("personal"))
  			{
  				accountData.put("personal", jsonArray.get(i).toString().split(":")[1].replaceAll(" ", ""));
  			}
  			if(jsonArray.get(i).toString().split(":")[0].replaceAll(" ", "").equals("document"))
  			{
  				accountData.put("document", jsonArray.get(i).toString().split(":")[1].replaceAll(" ", ""));
  			}
  			if(jsonArray.get(i).toString().split(":")[0].replaceAll(" ", "").equals("sms"))
  			{
  				accountData.put("sms", jsonArray.get(i).toString().split(":")[1].replaceAll(" ", ""));
  			}
  			if(jsonArray.get(i).toString().split(":")[0].replaceAll(" ", "").equals("verifikasi"))
  			{
  				accountData.put("verifikasi", jsonArray.get(i).toString().split(":")[1].replaceAll(" ", ""));
  			}
		
	  		list.put(accountData);  	  		
		}
		
		return list;
	}
	
	public Accounts findById(long record_id)
	{
		return (Accounts) session.createQuery("from Accounts where record_id = '"+record_id+"'").uniqueResult();
	}
	
	public Boolean updateAccount(Accounts account) {
	    try {
	    	Transaction tx=db.session().getTransaction();
	    	tx.setTimeout(30);
	    	tx.begin();
	    	session.update(account);
	    	
	    	tx.commit();
	    	return true;
	    } catch (RuntimeException e) {
	    	e.printStackTrace();
            LogSystem.error(getClass(), e, UUID.randomUUID().toString());
            return false;
	    }
	  }

}