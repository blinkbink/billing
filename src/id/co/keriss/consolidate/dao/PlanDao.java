package id.co.keriss.consolidate.dao;
import id.co.keriss.consolidate.action.page.Paging;
import id.co.keriss.consolidate.ee.Invoicing;
import id.co.keriss.consolidate.ee.Plan;
import id.co.keriss.consolidate.ee.Tenant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jpos.ee.DB;
import org.jpos.util.Log;

public class PlanDao {
	Session session;
	DB db;
	Log log;
	
	public PlanDao(DB db){
		super();
		session = db.session();
		this.db = db;
		log = db.getLog();
	}

	public List<Plan> findAllPlan (String external_key, long tenant) throws HibernateException {
		return  (List<Plan>) session.createQuery("from Plan p where external_key = '"+external_key+"' AND tenant ='"+tenant+"' and type='custom' order by id DESC").list();
    }
	public List<Plan> findAllDefault (long tenant) throws HibernateException {
		return  (List<Plan>) session.createQuery("from Plan p where external_key = '0' AND tenant ='"+tenant+"' and type='default'").list();
    }
	
	public List<Plan> findAllPlanDefault (String externalkey, long tenant) throws HibernateException {
		String filter = externalkey.substring(0,2);
		String defaults = "default";
		
		return  (List<Plan>) session.createQuery("from Plan p where tenant ='"+tenant+"' and type='default' and plan_name like '"+defaults+filter+"%' order by id DESC").list();
    }
	
	public List<Plan> findByPlan(String external_key, long tenant)
	{
		return (List<Plan> ) session.createQuery("from Plan p where external_key = '"+external_key+"' AND tenant ='"+tenant+"' AND type='custom' order by id DESC").list();
	}
	
	public List<Plan> findByPlanDefault(String external_key, long tenant)
	{
		return (List<Plan> ) session.createQuery("from Plan p where external_key = '0' AND tenant ='"+tenant+"' AND type='default' order by id DESC").list();
	}
	
	public Plan findSinglePlan(String external_key, long tenant, String plan_id)
	{
		return (Plan) session.createQuery("from Plan p where external_key = '"+external_key+"' AND tenant ='"+tenant+"' AND plan_name='"+plan_id+"'").uniqueResult();
	}
	
	public Plan findByPlanId(String external_key, String plan_name, long tenant)
	{
		return (Plan) session.createQuery("from Plan p where external_key = '"+external_key+"' AND  plan_name = '"+plan_name+"' AND tenant ='"+tenant+"'").uniqueResult();
	}
}
