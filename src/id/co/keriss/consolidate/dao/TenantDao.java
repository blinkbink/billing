package id.co.keriss.consolidate.dao;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.DocumentSummary;
import id.co.keriss.consolidate.ee.Documents;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.Tenant;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.LogSystem;

public class TenantDao {
	Session session;
	DB db;
	Log log;
	
	public TenantDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	public Tenant findByID(long recordid) throws HibernateException {
	    return  (Tenant) session.createQuery("from Tenant t where record_id='"+recordid+"'").uniqueResult();
	}
	
	public Tenant findByApiKey(String type) throws HibernateException {
	    return  (Tenant) session.createQuery("from Tenant t where api_key='"+type+"'").uniqueResult();
	}
	
	public Tenant findAll() throws HibernateException {
		 return  (Tenant) session.createQuery("from Tenant").list();
	}
	
	public Tenant findByType(String type) throws HibernateException {
		 return  (Tenant) session.createQuery("from Tenant WHERE api_key = '"+type+"'").uniqueResult();
	}
	
	public List<Tenant> findAllTenant() throws HibernateException {
		 return  (List<Tenant>) session.createQuery("from Tenant t").list();
	}
	
	public List<Tenant> findAllTenantPrepaid() throws HibernateException {
		 return  (List<Tenant>) session.createQuery("from Tenant t WHERE api_key != 'postpaid'").list();
	}
	
	public List<Tenant> findTenantPostpaid() throws HibernateException {
		 return  (List<Tenant>) session.createQuery("from Tenant t WHERE api_key = 'postpaid'").list();
	}
	  
	  
}
