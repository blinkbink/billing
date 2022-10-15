package id.co.keriss.consolidate.dao;

import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;
import id.co.keriss.consolidate.ee.MasterBalance;

public class MasterBalanceDao {
	private static final String DATE_FORMAT = "yyyy-mm-dd";
	Session session;
	DB db;
	Log log;
	private EntityManager manager;
	
	public MasterBalanceDao(DB db) {
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}
	
	public MasterBalance Balance(String externalkey, long tenant)
	{
		return (MasterBalance) session.createQuery("from MasterBalance where externalkey = '"+externalkey+"' and tenant_record_id = '"+tenant+"'").uniqueResult();
	}
		
}
