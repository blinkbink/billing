package id.co.keriss.consolidate.dao;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jpos.ee.Accounts;
import org.jpos.ee.DB;
import org.jpos.util.DateUtil;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Invoice;
import id.co.keriss.consolidate.ee.InvoiceItems;
import id.co.keriss.consolidate.ee.Invoices;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Plan;
import id.co.keriss.consolidate.util.LogSystem;

public class InvoicesDao {
	Session session;
	DB db;
	Log log;

	public InvoicesDao(DB db) {
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	public List<Invoices> listInvoices(String accountid)
	{
		try {
			return (List<Invoices>) session.createQuery("from Invoices WHERE account_id='"+accountid+"' AND created_date > :starttime and created_date < :endtime order by record_id desc").setParameter("starttime", new SimpleDateFormat("yyyy-MM-dd").parse(DateUtil.getLastMonthFirstDay())).setParameter("endtime", new SimpleDateFormat("yyyy-MM-dd").parse(DateUtil.getLastMonthLastDay())).list();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	 public  Long create(Invoices Invoices) {
		 Long id=null;
	    try {
	      Transaction tx=session.beginTransaction();
	      id=(Long) session.save(Invoices);
	      tx.commit();
	    } catch (RuntimeException e) {
          LogSystem.error(getClass(), e, UUID.randomUUID().toString());

	    }
		  return id;
	  }
}
