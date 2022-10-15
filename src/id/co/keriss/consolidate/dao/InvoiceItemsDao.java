package id.co.keriss.consolidate.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;

import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.Accounts;
import org.jpos.ee.DB;
import org.jpos.util.DateUtil;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.CurrentBalance;
import id.co.keriss.consolidate.ee.InvoiceItems;
import id.co.keriss.consolidate.util.LogSystem;

public class InvoiceItemsDao {
	private static final String DATE_FORMAT = "yyyy-mm-dd";
	Session session;
	DB db;
	Log log;
	private EntityManager manager;
	
	public InvoiceItemsDao(DB db) {
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	
	
	 public  void update(InvoiceItems invoiceItems) {
		    try {
		    	Transaction tx=db.session().getTransaction();
		    	tx.setTimeout(30);
		    	tx.begin();
		    	session.update(invoiceItems);
		    	tx.commit();
		    } catch (RuntimeException e) {
//		        log.debug(e);
		    	e.printStackTrace();
	            LogSystem.error(getClass(), e, UUID.randomUUID().toString());
		    }
		  }
	 
	 public  void updateEmail(Accounts account) {
		    try {
		    	
		    	Transaction tx=db.session().getTransaction();
		    	
		    	if(!tx.isActive())
	  			{
	  				tx.setTimeout(18);
	  				tx.begin();
	  			}
		    	
		    	session.update(account);
		    	
		    	tx.commit();
		    } catch (RuntimeException e) {
//		        log.debug(e);
		    	e.printStackTrace();
	            LogSystem.error(getClass(), e, UUID.randomUUID().toString());
		    }
		  }
	
	public List<InvoiceItems> getItem(Long item, String invoiceid)
	{
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems WHERE items='"+item+"' and type='USAGE' and invoice_id='"+invoiceid+"' ORDER BY created_date DESC").list();
	}
	
	public List<InvoiceItems> getItemReversal(Long item, String invoiceid)
	{
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems WHERE items='"+item+"' and type='ITEM_ADJ' and invoice_id='"+invoiceid+"' ORDER BY created_date DESC").list();
	}
	
	public List<InvoiceItems> getItemcheck(Long item)
	{
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems WHERE items='"+item+"' and type='USAGE' ORDER BY created_date DESC").list();
	}
	
	public List<InvoiceItems> getItemReversalcheck(Long item)
	{
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems WHERE items='"+item+"' and type='ITEM_ADJ' ORDER BY created_date DESC").list();
	}


	public List<Accounts> findAccount(String externalkey)
	{
		return (List<Accounts>) session.createQuery("from Accounts WHERE external_key='"+externalkey+"'").list();
	}
	
	public List<Accounts> findNotifBalanceTrue(String externalkey)
	{
		return (List<Accounts>) session.createQuery("from Accounts WHERE external_key='"+externalkey+"' and send_notif_balance is true").list();
	}
	
	public List<InvoiceItems> singleBalance(String account_id)
	{
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems where account_id = '"+account_id+"' ORDER BY created_date DESC").setMaxResults(1).list();
	}
	
	public int updateInvoiceItems(BigDecimal current_balance, BigInteger record_id)
	{
//		return EntityManager.createNativeQuery("UPDATE invoice_items SET current_balance = ""' WHERE record_id = :record_id").executeUpdate();
			
			Transaction txn = session.beginTransaction();
			Query query = session.createNativeQuery(
					"UPDATE invoice_items SET current_balance = :current_balance WHERE record_id = :record_id");
			query.setParameter("current_balance", current_balance);
			query.setParameter("record_id", record_id);
			query.executeUpdate();
			txn.commit();
//			session.getTransaction().commit();
			return 0;
//			return session.createNativeQuery("UPDATE invoice_items SET current_balance = '"+current_balance+"' WHERE record_id = '"+record_id+"'").executeUpdate();
	}
	
	public Boolean updateInvoiceItemsCommit(InvoiceItems invoiceitems)
	{
		try
		{
	  		
	  		
	  		for(int i=0; i<=1; i++){
	  		      new Thread("" + i){
	  		        public void run(){
	  		        	System.out.print("Update Invoice Items");
	  		        	long commitupdate = System.nanoTime();
	  		        	NumberFormat formatter = new DecimalFormat("#0.00000");
	  		        	
		  		      	Transaction txupdate = db.session().getTransaction();
		  		      	db.session().update(invoiceitems);
		  		  		
		  		  		txupdate.setTimeout(270);
		  		  		txupdate.begin();
		  		  		txupdate.commit();
		  		  		System.out.print("PROCESS UPDATE COMMIT : " + formatter.format((System.nanoTime() - commitupdate)/1000000000d));
	  		        }
	  		      }.start();
	  		    }
	  	
	  		return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
  		
	}
	
	public List<InvoiceItems> listTransaction(String accountid)
	{
		try {
			System.out.println("LAST MONTH LAST DAY : " + new SimpleDateFormat("yyyy-MM-dd").parse(DateUtil.getLastMonthLastDay()));

			System.out.println("LAST MONTH FIRST DAY : " + new SimpleDateFormat("yyyy-MM-dd").parse(DateUtil.getLastMonthFirstDay()));
			return (List<InvoiceItems>) session.createQuery("from InvoiceItems WHERE account_id='"+accountid+"' AND (type='USAGE' OR type='ITEM_ADJ' OR type ='EXTERNAL_CHARGE' OR type ='CREDIT_ADJ') AND created_date > :starttime and created_date < :endtime ORDER BY record_id desc").setParameter("starttime", new SimpleDateFormat("yyyy-MM-dd").parse(DateUtil.getLastMonthFirstDay())).setParameter("endtime", new SimpleDateFormat("yyyy-MM-dd").parse(DateUtil.getLastMonthLastDay())).list();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Boolean FixingCurrentBalanceCBA_ADJ(String external_key, BigInteger tenant)
	{
		try 
		{
			session.beginTransaction();
		      String  qString="";
				Query query=session.createNativeQuery("update invoice_items set current_balance = balances\n" + 
						"from (SELECT i.id, SUM(case when i.amount >= 0 then i.amount when i.amount <= 0 then i.amount end) OVER (ORDER BY i.created_date, i.id DESC) AS balances " + 
						"  FROM accounts a, tenants t, invoice_items i where  a.record_id = i.account_record_id and t.record_id = a.tenant_record_id and a.external_key=:external_key and i.tenant_record_id=:tenant and type='CBA_ADJ') as foo " + 
						"WHERE invoice_items.id = foo.id and external_key=:external_key and tenant_record_id=:tenant and type='CBA_ADJ' ");
				query.setString("external_key", external_key);
				query.setBigInteger("tenant", tenant);
				query.executeUpdate();
				session.getTransaction().commit();
				
			return true;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean FixingCurrentBalanceUSAGE(String external_key, BigInteger tenant)
	{
		try 
		{
			session.beginTransaction();
		      String  qString="";
				Query query=session.createNativeQuery("update invoice_items inv set current_balance = balances\n" + 
						"from (select invoice_id, current_balance as balances from invoice_items where external_key=:external_key and tenant_record_id=:tenant and type='CBA_ADJ' order by record_id asc) as foo\n" + 
						"where tenant_record_id=:tenant and type='USAGE' and inv.invoice_id = foo.invoice_id");
				query.setString("external_key", external_key);
				query.setBigInteger("tenant", tenant);
				query.executeUpdate();
				session.getTransaction().commit();
				
			return true;	
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean FixingCurrentBalanceITEM_ADJ(String external_key, BigInteger tenant)
	{
		try 
		{
			session.beginTransaction();
		      String  qString="";
				Query query=session.createNativeQuery("update invoice_items inv set current_balance = balances\n" + 
						"from (select invoice_id, current_balance as balances from invoice_items where external_key=:external_key and tenant_record_id=:tenant and type='CBA_ADJ' order by record_id desc) as foo\n" + 
						"where tenant_record_id=:tenant and type='ITEM_ADJ' and inv.invoice_id = foo.invoice_id");
				query.setString("external_key", external_key);
				query.setBigInteger("tenant", tenant);
				query.executeUpdate();
				session.getTransaction().commit();
				
			return true;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public Boolean FixingCurrentBalanceCREDIT_ADJ(String external_key, BigInteger tenant)
	{
		try 
		{
			session.beginTransaction();
		      String  qString="";
				Query query=session.createNativeQuery("update invoice_items inv set current_balance = balances\n" + 
						"from (select invoice_id, current_balance as balances from invoice_items where external_key=:external_key and tenant_record_id=:tenant and type='CBA_ADJ' order by record_id desc) as foo\n" + 
						"where tenant_record_id=:tenant and type='CREDIT_ADJ' and inv.invoice_id = foo.invoice_id");
				query.setString("external_key", external_key);
				query.setBigInteger("tenant", tenant);
				query.executeUpdate();
				session.getTransaction().commit();
				
			return true;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public List<InvoiceItems> listInvoice(String invoiceid)
	{
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems WHERE invoice_id='"+invoiceid+"' AND (type='USAGE' OR type='ITEM_ADJ' OR type ='EXTERNAL_CHARGE' OR type ='CREDIT_ADJ')").list();
	}
	
	public List<CurrentBalance> getCurrentBalance(String invoice_id)
	{
		return (List<CurrentBalance>) session.createQuery("from CurrentBalance WHERE invoice_id='"+invoice_id+"'").list();
	}
	
	public String getAccountId(String externalkey, long tenantid)
	{
		return (String) session.createQuery("select id from Accounts WHERE external_key='"+externalkey+"' AND tenant_record_id ='"+tenantid+"'").uniqueResult();
	}
	
//	public BigDecimal getBalance(String accountid) {
////        return (BigDecimal)this.session.createSQLQuery("select SUM(amount) from invoice_items WHERE account_id = '" + accountid + "' AND type='CBA_ADJ' ").uniqueResult();
//		return (BigDecimal)this.session.createQuery("select SUM(amount) from InvoiceItems WHERE account_id = '" + accountid + "' AND type='CBA_ADJ' ").uniqueResult();
////		return (BigDecimal)this.session.createQuery("select SUM(amount) from InvoiceItems I WHERE account_id = '" + accountid + "' AND type='CBA_ADJ' ").setLockMode("I", LockMode.).getSingleResult();
//    }
	
	public InvoiceItems findByRecordIdCBA_ADJ(BigInteger record_id) {
		return (InvoiceItems)this.session.createQuery("from InvoiceItems WHERE record_id = '"+ record_id +"' and type='CBA_ADJ'").uniqueResult();
	}
	
	public InvoiceItems findByRecordIdUSAGE(BigInteger record_id) {
		return (InvoiceItems)this.session.createQuery("from InvoiceItems WHERE record_id = '"+ record_id +"' and type='USAGE'").uniqueResult();
	}
	
	public BigDecimal updateBalance(String accountid, BigInteger record_id) {
		return (BigDecimal)this.session.createSQLQuery("SELECT SUM(amount) from invoice_items I WHERE I.account_id = '" + accountid + "' AND I.record_id <= '"+ record_id +"' AND I.type != 'CBA_ADJ' ").getSingleResult();
	}
	
	public BigDecimal SPFreeBalanceMines(String externalkey, long tenant, BigDecimal amount) {
		return (BigDecimal)this.session.createNativeQuery("SELECT * FROM freebalancemines(:externalkey, :tenant, :amount)")
				.setParameter("externalkey", externalkey)
				.setParameter("tenant", tenant)
				.setParameter("amount", amount).getSingleResult();
	}
	
//	public Object[] SPBalanceMines(String externalkey, int tenant, int amount, int account_record_id) {
//		Object[] q = (Object[]) session.createNativeQuery("SELECT * FROM testfunction2(:externalkey, :tenant, :amount, :accountrecord)")
//				.setParameter("externalkey", externalkey)
//				.setParameter("tenant", tenant)
//				.setParameter("amount", amount)
//				.setParameter("accountrecord", account_record_id).getSingleResult();
//  	
//		return q;
//	}
//	
//	public BigDecimal SPFreeBalancePlus(String externalkey, int tenant, int amount, int account_record_id, int batch) {
//		return (BigDecimal)this.session.createNativeQuery("SELECT * FROM testfunction3(:externalkey, :tenant, :amount, :accountrecord, :batch")
//				.setParameter("externalkey", externalkey)
//				.setParameter("tenant", tenant)
//				.setParameter("amount", amount)
//				.setParameter("accountrecord", account_record_id)
//				.setParameter("batch", batch).getSingleResult();
//	}
	
	public BigDecimal SPBalanceMines(String externalkey, long tenant, BigDecimal amount, long account_record_id) {
		return (BigDecimal)this.session.createNativeQuery("SELECT * FROM balancemines(:externalkey, :tenant, :amount)")
				.setParameter("externalkey", externalkey)
				.setParameter("tenant", tenant)
				.setParameter("amount", amount).getSingleResult();
	}
	
	public BigDecimal SPBalancePlus(String externalkey, int tenant, int amount, int account_record_id) {
		return (BigDecimal)this.session.createNativeQuery("SELECT * FROM balanceplus(:externalkey, :tenant, :amount)")
				.setParameter("externalkey", externalkey)
				.setParameter("tenant", tenant)
				.setParameter("amount", amount).getSingleResult();
	}

//	public Object[] SPBalancePlus(String externalkey, int tenant, int amount, int account_record_id, int batch) {
//		return (Object[])this.session.createNativeQuery("SELECT * FROM testfunction3(:externalkey, :tenant, :amount, :accountrecord, :batch)")
//				.setParameter("externalkey", externalkey)
//				.setParameter("tenant", tenant)
//				.setParameter("amount", amount)
//				.setParameter("accountrecord", account_record_id)
//				.setParameter("batch", batch).getSingleResult();
//	}
	
	public BigDecimal SPTopupFree(String externalkey, long tenant, BigDecimal amount) {
		return (BigDecimal)this.session.createNativeQuery("SELECT * FROM topupfree(:externalkey, :tenant, :amount)")
				.setParameter("externalkey", externalkey)
				.setParameter("tenant", tenant)
				.setParameter("amount", amount).getSingleResult();
	}
	
	public BigDecimal SPTopup(String externalkey, long tenant, BigDecimal amount) {
		return (BigDecimal)this.session.createNativeQuery("SELECT * FROM topup(:externalkey, :tenant, :amount)")
				.setParameter("externalkey", externalkey)
				.setParameter("tenant", tenant)
				.setParameter("amount", amount).getSingleResult();
	}

	public BigDecimal updateNewBalance(String accountid, BigInteger minRecord_id, BigInteger maxRecord_id) {
		return (BigDecimal)this.session.createSQLQuery("SELECT SUM(amount) from invoice_items I WHERE I.account_id = '" + accountid + "' AND I.record_id > '"+ minRecord_id +"' AND I.record_id <= '"+ maxRecord_id +"' AND I.type != 'CBA_ADJ'").getSingleResult();
	}
	
	public InvoiceItems getBalanceObject(String accountid) {
//      return (BigDecimal)this.session.createSQLQuery("select SUM(amount) from invoice_items WHERE account_id = '" + accountid + "' AND type='CBA_ADJ' ").uniqueResult();
		return (InvoiceItems) this.session.createQuery("select SUM(amount) as amount from InvoiceItems WHERE account_id = '" + accountid + "' AND type='CBA_ADJ' ").uniqueResult();
//		return (BigDecimal)this.session.createQuery("select SUM(amount) from InvoiceItems I WHERE account_id = '" + accountid + "' AND type='CBA_ADJ' ").setLockMode("I", LockMode.).getSingleResult();
  }
	
	public int createTransaction()
	{
		Query query = session.createQuery("insert into "
				+ "InvoiceItems(id, type, invoice_id, account_id, description, plan_name, phase_name, usage_name"
				+ "start_date, end_date, amount, account_record_id, tenant_record_id, current_balance, external_key) "
				+ "'asdasda','CBA_ADJ'. '23qsda', 'asdas', 'test insert', null, 'Prepaid', 'Usage DS', '2019-01-01', '2019-01-01', '1', '12', '1', '10', 'MT1'");
		return query.executeUpdate();
	}
	
	
	public Boolean insert(String external_key, String type, String account_id, String invoice_items_id, BigDecimal amount)
	{
		StoredProcedureQuery query = session
				.createStoredProcedureQuery("MyInsert")
				.registerStoredProcedureParameter(
				    "balance",
				    BigDecimal.class,	
				    ParameterMode.IN
				)
				.registerStoredProcedureParameter(
				    "invoice_items_id",
				    String.class,
				    ParameterMode.IN
				)
				.setParameter("balance", amount)
			    .setParameter("invoice_items_id", invoice_items_id);
				     
				return query.execute();
//		StoredProcedureQuery query = session.createStoredProcedureQuery("doInsert");
//		query.registerStoredProcedureParameter("balance", BigDecimal.class, ParameterMode.IN);
//		query.registerStoredProcedureParameter("invoice_items_id", String.class, ParameterMode.IN);
//		query.setParameter("balance", new BigDecimal(100));
//		query.setParameter("invoice_items_id", "asda12-123");
//		return query.execute();
				
		
//		return  session.createQuery("insert into InvoiceItems(id,type,invoice_id, account_id, description, plan_name, phase_name, start_date, end_date, amount, account_record_id, tenant_record_id, current_balance, external_key) "
//				+ "select id,type,invoice_id, account_id, description, plan_name, phase_name, start_date, end_date, amount, account_record_id, tenant_record_id, current_balance, external_key WHERE account_id='6015ec0f-4d30-4802-bbcc-ea35c12f93a2'").executeUpdate();
//				
//		
//		return session.createNativeQuery(
//			    "PERFORM 1 FROM invoice_items\n" + 
//			    "WHERE type = ? and external_key = ?\n" + 
//			    "ORDER BY id, created_date DESC LIMIT 1\n" + 
//			    "FOR UPDATE OF invoice_items;"
//			    + "INSERT INTO balance (balance, invoice_items_id) VALUES ((SELECT SUM(amount) - ? FROM invoice_items WHERE account_id=? and type='CBA_ADJ'), ?);")
//				.setParameter(1, type)
//				.setParameter(2, external_key)
//				.setParameter(3, amount)
//				.setParameter(4, account_id)
//				.setParameter(5, invoice_items_id)
//			    .executeUpdate();
	}
	
	public List<InvoiceItems> findAccountonInvoiceItems(long tenant_record_id, String account_id)
	{
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems WHERE account_id ='"+account_id+"' AND tenant_record_id ='"+tenant_record_id+"'").setMaxResults(1).list();
//		return (List<InvoiceItems>) session.createSQLQuery("select i from invoice_items i WHERE i.account_id ='"+account_id+"' AND i.tenant_record_id ='"+tenant_record_id+"'").setMaxResults(1).list();
	}
	
	public InvoiceItems findAccountonInvoiceItemsUnique(long tenant_record_id, String account_id)
	{
		return (InvoiceItems) session.createQuery("from InvoiceItems WHERE account_id ='"+account_id+"' AND tenant_record_id ='"+tenant_record_id+"'").uniqueResult();
	}

	public JSONArray findAllBalance(String external_key) throws HibernateException, JSONException {

        JSONArray jsonArray = new JSONArray();
        JSONArray list = new JSONArray();
        
		List<Object[]> q = session.createSQLQuery("select SUM(i.amount) as amount, t.api_key from accounts a, invoice_items i, tenants t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.type='CBA_ADJ' group by t.api_key, t.record_id").list();
	
		for(Object[] a : q)
		{
			jsonArray.put(a[1] + " : " + a[0]);
		}
		
		for(int b = 0 ; b < jsonArray.length() ; b++)
		{
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("tenant", jsonArray.get(b).toString().split(":")[0].replaceAll(" ", ""));
			jsonObject.put("amount", jsonArray.get(b).toString().split(":")[1].replaceAll(" ", ""));
			
			list.put(jsonObject);
		}
		
		return list;
	}
	
	
	public JSONArray findTotalUsage(String external_key) throws HibernateException, JSONException {

        JSONArray jsonArray = new JSONArray();
        JSONArray list = new JSONArray();
        
		List<Object[]> q = session.createSQLQuery("select SUM(pu.amount), t.api_key\n" + 
				"from invoice_items i,\n" + 
				"     postpaid_usage pu,\n" + 
				"     invoices inv,\n" + 
				"       tenants t\n" + 
				"WHERE inv.id = i.invoice_id\n" + 
				"  AND pu.tenant = t.record_id\n" +
				"  AND pu.external_key = '"+external_key+"'\n" + 
				"  AND inv.status = 'ACTIVE'\n" + 
				"  AND i.phase_name = 'Postpaid'\n" + 
				"GROUP BY t.api_key;\n" + 
				"\n" +
				"").list();
	
		for(Object[] a : q)
		{
			jsonArray.put(a[1] + " : " + a[0]);
		}
		
		for(int b = 0 ; b < jsonArray.length() ; b++)
		{
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("amount", jsonArray.get(b).toString().split(":")[1].replaceAll(" ", ""));
			jsonObject.put("tenant", jsonArray.get(b).toString().split(":")[0].replaceAll(" ", ""));
			
			list.put(jsonObject);
		}
		
		return list;
	}
	
	
	
	public List<InvoiceItems> getLastDate(String external_key, long tenant)
	{
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems WHERE external_key ='"+external_key+"' AND tenant_record_id ='"+tenant+"' AND type='CBA_ADJ' ORDER BY created_date DESC").setMaxResults(1).list();
	}
	
	public List<InvoiceItems> getThreeDaysBeforeRecordId(String external_key, long tenant)
	{
		List<InvoiceItems> data = getLastDate(external_key, tenant);
		Date date = DateUtils.addDays(data.get(0).getCreated_date(), -3);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-M-dd");
		
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems WHERE external_key ='"+external_key+"' AND tenant_record_id ='"+tenant+"' AND type='CBA_ADJ' AND created_date >= '"+dateFormat.format(date)+"' ORDER BY created_date ASC").setMaxResults(1).list();
	}
	
	public BigDecimal getLastThreeDaysBalance(String external_key, long tenant)
	{
		List<InvoiceItems> data = getThreeDaysBeforeRecordId(external_key, tenant);
		
		return (BigDecimal) session.createQuery("select current_balance from InvoiceItems i WHERE i.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' AND record_id >= '"+data.get(0).getRecord_id()+"' ORDER BY created_date ASC").setMaxResults(1).list().get(0);
	}
	
//	public BigDecimal getCurrentBalance(String external_key, long tenant)
//	{
//		try {
//		return (BigDecimal) session.createNativeQuery("select SUM(i.amount) as amount " + 
//					"from accounts a, " + 
//				"invoice_items i, " + 
//				"tenants t " + 
//				"WHERE a.record_id = i.account_record_id " + 
//				"  AND t.record_id = a.tenant_record_id " + 
//				"  AND a.external_key = '"+external_key+"' " + 
//				"  AND i.tenant_record_id = '"+tenant+"' " + 
//				"  AND i.type = 'CBA_ADJ' " + 
//				"AND i.record_id > (SELECT record_id" + 
//				"                                 FROM invoice_items " + 
//				"                                 WHERE external_key = '"+external_key+"'" + 
//				"                                   AND tenant_record_id = '"+tenant+"' " + 
//				"                                   AND type = 'CBA_ADJ'  " + 
//				"                                   AND created_date >= (SELECT created_date " + 
//				"                                                        FROM invoice_items " + 
//				"                                                        WHERE external_key = ' "+external_key+"' " + 
//				"                                                          and tenant_record_id = '"+tenant+"' " + 
//				"                                                          AND created_date >= (SELECT created_date - INTERVAL '3 DAY' " + 
//				"                                                                               FROM invoice_items " + 
//				"                                                                               WHERE external_key = '"+external_key+"' " + 
//				"                                                                                 and tenant_record_id = '"+tenant+"' " + 
//				"                                                                                 and type = 'CBA_ADJ' " + 
//				"                                                                               ORDER BY created_date DESC " + 
//				"                                                                               LIMIT 1) " + 
//				"                                                        ORDER BY created_date ASC " + 
//				"                                                        LIMIT 1) " + 
//				"                                 ORDER BY created_date ASC " + 
//				"                                 LIMIT 1) " + 
//				"group by i.tenant_record_id," + 
//				"         t.record_id" + 
//				"").getSingleResult();
//		}catch(NoResultException nre){
//			//Ignore this because as per your logic this is ok!
//			return null;
//		}
//		
//	}
//	
////	public BigDecimal getThreeDaysBalance(String external_key, long tenant)
//	{
//		List<InvoiceItems> data = getThreeDaysBeforeRecordId(external_key, tenant);
//	
//		return (BigDecimal) session.createQuery("select SUM(i.amount) as amount from Accounts a, InvoiceItems i, Tenant t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' and i.record_id > '"+data.get(0).getRecord_id()+"' group by i.tenant_record_id, t.record_id").uniqueResult();
//	}
//	
	public BigDecimal getPivotBalance(String account_id, long tenant, BigInteger record_id)
	{
//		return (BigDecimal) session.createQuery("select SUM(i.amount) as amount from Accounts a, InvoiceItems i, Tenant t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' AND i.record_id > '"+record_id+"' group by i.tenant_record_id, t.record_id").uniqueResult();
//		return (BigDecimal) session.createSQLQuery("select SUM(i.amount) as amount from accounts a, invoice_items i, tenants t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' AND i.record_id > '"+record_id+"' group by i.tenant_record_id, t.record_id").uniqueResult();
		return (BigDecimal) session.createSQLQuery("select SUM(i.amount) as amount from invoice_items i WHERE i.account_id='"+account_id+"' AND i.type != 'CBA_ADJ' AND i.record_id > '"+record_id+"'").getSingleResult();
		
//		return (BigDecimal) session.createQuery("select SUM(i.amount) as amount from Accounts a, InvoiceItems i, Tenant t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' group by i.tenant_record_id, t.record_id").setLockMode(LockModeType.PESSIMISTIC_READ).getSingleResult();
		//		return (BigDecimal) session.createQuery("select SUM(i.amount) as amount from Accounts a, InvoiceItems i, Tenant t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' group by i.tenant_record_id, t.record_id")
//        .setLockMode(LockModeType.PESSIMISTIC_READ)
//        .setHint("javax.persistence.lock.timeout", 10000)
//        .getSingleResult();
	}
	
	public List<InvoiceItems> getPivotBalancedata(String account_id, long tenant, BigInteger record_id)
	{
//		return (BigDecimal) session.createQuery("select SUM(i.amount) as amount from Accounts a, InvoiceItems i, Tenant t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' AND i.record_id > '"+record_id+"' group by i.tenant_record_id, t.record_id").uniqueResult();
//		return (BigDecimal) session.createSQLQuery("select SUM(i.amount) as amount from accounts a, invoice_items i, tenants t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' AND i.record_id > '"+record_id+"' group by i.tenant_record_id, t.record_id").uniqueResult();
		return (List<InvoiceItems>) session.createSQLQuery("select SUM(i.amount) as amount from invoice_items i WHERE i.account_id='"+account_id+"' AND i.record_id > '"+record_id+"'").setMaxResults(1).list();
		
//		return (BigDecimal) session.createQuery("select SUM(i.amount) as amount from Accounts a, InvoiceItems i, Tenant t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' group by i.tenant_record_id, t.record_id").setLockMode(LockModeType.PESSIMISTIC_READ).getSingleResult();
		//		return (BigDecimal) session.createQuery("select SUM(i.amount) as amount from Accounts a, InvoiceItems i, Tenant t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' group by i.tenant_record_id, t.record_id")
//        .setLockMode(LockModeType.PESSIMISTIC_READ)
//        .setHint("javax.persistence.lock.timeout", 10000)
//        .getSingleResult();
	}
	
	public InvoiceItems lastBalance(String accountID)
	{
		Query query = session.createQuery("from InvoiceItems I where I.account_id='"+accountID+"' order by record_id DESC");
		query.setMaxResults(1);
//		query.setLockMode(LockModeType.OPTIMISTIC);
		InvoiceItems last = (InvoiceItems) query.uniqueResult();
		return last;
	}
	
	public BigDecimal getSingleBalance(String account_id, long tenant)
	{
//		return (BigDecimal) session.createQuery("select SUM(i.amount) as amount from Accounts a, InvoiceItems i, Tenant t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' group by i.tenant_record_id, t.record_id").uniqueResult();
//		return (BigDecimal) session.createSQLQuery("select SUM(i.amount) as amount from accounts a, invoice_items i, tenants t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' group by i.tenant_record_id, t.record_id").uniqueResult();
		
		return (BigDecimal) session.createSQLQuery("select SUM(i.amount) as amount from invoice_items i WHERE i.account_id='"+account_id+"' AND i.type != 'CBA_ADJ'").getSingleResult();
//		return (BigDecimal) session.createQuery("select SUM(i.amount) as amount from Accounts a, InvoiceItems i, Tenant t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' group by i.tenant_record_id, t.record_id").setLockMode(LockModeType.PESSIMISTIC_READ).getSingleResult();
		//		return (BigDecimal) session.createQuery("select SUM(i.amount) as amount from Accounts a, InvoiceItems i, Tenant t WHERE a.record_id = i.account_record_id AND t.record_id=a.tenant_record_id  AND a.external_key='"+external_key+"' AND i.tenant_record_id = '"+tenant+"' AND i.type='CBA_ADJ' group by i.tenant_record_id, t.record_id")
//        .setLockMode(LockModeType.PESSIMISTIC_READ)
//        .setHint("javax.persistence.lock.timeout", 10000)
//        .getSingleResult();
	}
	
	public List<InvoiceItems> findByInvoiceId(String invoiceid)
	{
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems i where invoice_id = '"+invoiceid+"' and type='USAGE' order by record_id asc").list();
	}
	
	public List<InvoiceItems> findTransactionReversal(String invoiceid)
	{
		return (List<InvoiceItems>) session.createQuery("from InvoiceItems i where invoice_id = '"+invoiceid+"' AND type = 'ITEM_ADJ'").list();
	}
	
	public InvoiceItems findPostpaidTransactionActive(String externalkey)
	{
		return (InvoiceItems) session.createQuery("SELECT i from InvoiceItems i, Accounts a, Invoices b where i.invoice_id = b.id AND i.account_record_id = a.record_id AND a.external_key = '"+externalkey+"' AND i.phase_name = 'Postpaid' AND b.status='ACTIVE'").uniqueResult();
	}
	
	public BigDecimal getTotalBalance(String invoiceid)
	{
		BigDecimal totalBalance = (BigDecimal) session.createSQLQuery("select SUM(amount) from invoice_items where invoice_id = '"+invoiceid+"'").getSingleResult();
		return totalBalance;
	}
	
	 public  BigInteger create(InvoiceItems invoiceItems) {
		 BigInteger id=null;
	    try {
	      Transaction tx=session.beginTransaction();
	      id=(BigInteger) session.save(invoiceItems);
	      tx.commit();
	    } catch (RuntimeException e) {
           LogSystem.error(getClass(), e, UUID.randomUUID().toString());

	    }
		  return id;
	  }
	
}
