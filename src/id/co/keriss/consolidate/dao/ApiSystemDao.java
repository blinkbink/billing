package id.co.keriss.consolidate.dao;

import id.co.keriss.consolidate.ee.Account;
import id.co.keriss.consolidate.ee.ApiSystem;
import id.co.keriss.consolidate.ee.Tenant;
import id.co.keriss.consolidate.ee.Terminal;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class ApiSystemDao {
	Session session;
	DB db;
	Log log;
	
	public ApiSystemDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}

	public Boolean checkSystem () throws HibernateException 
	{
	    return (Boolean) session.createQuery("select maintanance from ApiSystem").uniqueResult();
	}
}