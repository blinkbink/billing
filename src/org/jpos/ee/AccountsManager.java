package org.jpos.ee;

import id.co.keriss.consolidate.ee.Tenant;
import id.co.keriss.consolidate.ee.Terminal;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.jpos.util.Log;

public class AccountsManager {
	Session session;
	DB db;
	Log log;
	
	public AccountsManager(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	  @SuppressWarnings("unchecked")
	  public List findAll () throws HibernateException {
        return session.createCriteria (Accounts.class).list();
      }
	  
	  public List findByExternalKey(String externalkey) throws HibernateException {
		    return  (List) session.createQuery("from Accounts a where external_key='"+externalkey+"'").list();
	  }
	  
	  public Accounts findByKeyTenant(String externalkey, long tenantID) throws HibernateException {
		    return  (Accounts) session.createQuery("from Accounts a where external_key='"+externalkey+"' and tenant_record_id = '"+tenantID+"'").uniqueResult();
	  }
	  
	  public Accounts findPostPaidAccount(String externalkey) throws HibernateException {
		    return  (Accounts) session.createQuery("from Accounts a where external_key='"+externalkey+"'").uniqueResult();
	  }
	 
}