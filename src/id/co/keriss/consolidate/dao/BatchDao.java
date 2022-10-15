package id.co.keriss.consolidate.dao;
import java.math.BigInteger;
import java.util.List;

import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import id.co.keriss.consolidate.ee.Batch;
import id.co.keriss.consolidate.ee.InvoiceItems;

public class BatchDao {
	Session session;
	DB db;
	Log log;
	
	public BatchDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	public Batch findPostpaidByAccountRecord(Long account_record)
	{
		return (Batch) session.createQuery("from Batch WHERE account_record_id = '"+account_record+"' and settled is false").uniqueResult(); 
	}
	
	public Batch findPostpaidByBatchId(BigInteger id)
	{
		return (Batch) session.createQuery("from Batch WHERE id = '"+id+"'").uniqueResult(); 
	}
}
