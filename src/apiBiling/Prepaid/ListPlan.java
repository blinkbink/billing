package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.PlanDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.Plan;
import id.co.keriss.consolidate.ee.Tenant;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.LogSystem;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;


public class ListPlan extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		
		String uuid = UUID.randomUUID().toString().replace("-", "");

		List<FileItem> fileItems=null;
		LogSystem.info(request, "DATA DEBUG :"+(i++), uuid);

		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				
				// no multipart form
				if (!isMultipart) {
					LogSystem.info(request, "Bukan multipart", uuid);
					JSONObject jo=new JSONObject();
					jo.put("result", "30");
					jo.put("notif", "Format request API salah.");
					jo.put("log", uuid);
					context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
					
					return;
				}
				
				// multipart form
				else {
			
					ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

					fileItems = upload.parseRequest(request);

					for (FileItem fileItem : fileItems) {
						if (fileItem.isFormField()) {
							if(fileItem.getFieldName().equals("jsonfield")){
								jsonString=fileItem.getString();
							}
						}
					}
				}

	        LogSystem.info(request, "PATH :"+request.getRequestURI(), uuid);
	        LogSystem.request(request, fileItems, uuid);
			if(jsonString==null) return;	         
	        JSONObject jsonRecv=new JSONObject(jsonString).getJSONObject("JSONFile");         
	         
	        JSONObject jo = null;
	        jo=ListPlan(jsonRecv, context, request, uuid);
	         
	         
			String res="";
			if(jo!=null)res= new JSONObject().put("JSONFile", jo).toString();
			else res="<b>ERROR 404</b>";
	        
			context.put("trxjson", res);
			LogSystem.response(request, jo, uuid);

		}catch (Exception e) {
            LogSystem.error(getClass(), e, uuid);

            JSONObject jo=new JSONObject();
            try {
				jo.put("result", "06");
				jo.put("notif", "Failed get list plan");
				jo.put("log", uuid);
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject ListPlan(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException{
		long startTime = System.nanoTime();
		NumberFormat formatter = new DecimalFormat("#0.00000");
		DB db = new DB();
		try {
			
			db = getDB(context);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		JSONObject jo=new JSONObject();
		JSONArray jplan = new JSONArray();

        String notif="Account already exist";


        ApiSystemDao maintanance = new ApiSystemDao(db);
        
		LogSystem.info(request, "Maintanance : " + maintanance.checkSystem(), uuid);
		
		if(maintanance.checkSystem())
		{
			notif="Billing API under maintanance";
	  	  	jo.put("result", "1122");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
		}
        
  	  	if(!jsonRecv.has("externalkey")) 
  	  	{
  	  		notif="JSON externalkey does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
  	  	}

  	  	if(!jsonRecv.has("tenant")) 
	  	{
	  		notif="JSON tenant does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
	  	}
  	
  	  	Transaction tx = db.session().getTransaction();

  	  	try {
  	  		
  	  		tx.setTimeout(300);
  	  		tx.begin();
  
  	  		PlanDao pdo = new PlanDao(db);
	 		TenantDao tdo = new TenantDao(db);
//	 		Tenant tenantData = tdo.findByID(jsonRecv.getLong("tenant"));
	 		TenantDao tdof = new TenantDao(db);
	 		Tenant tenantData = tdof.findByApiKey(jsonRecv.getString("tenant"));
  	  		List<Plan> plan= pdo.findAllPlan(jsonRecv.getString("externalkey"), tenantData.getRecord_id());
  	  		
  	  		LogSystem.info(request, plan.toString(), uuid);
  	  		
	  	  	ArrayList<String> listtenant = new ArrayList<String>();
		  
	  	  	List<Tenant> dataTenant = tdof.findAllTenantPrepaid();
	  	  	
			
			for (int l = 0 ; l < dataTenant.size() ; l++)
			{
				listtenant.add(dataTenant.get(l).getApi_key());
			}
  	  		
			if(!listtenant.contains(jsonRecv.getString("tenant")))
		  	{
		  	 	LogSystem.info(request, "Tenant not available", uuid);
			  	notif="Tenant available : " + listtenant;
			  	jo.put("result", "01");
				jo.put("notif", notif);
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
				LogSystem.info(getClass(), jo.toString());
				return jo;
			}
  	  		else
  	  		{
  	  			AccountsDao ado = new AccountsDao(db);
	  	  		if(ado.findByExternalKey(jsonRecv.getString("externalkey"), tenantData.getRecord_id()) == null)
				{
					notif="Account external key does not exist";
			  	  	jo.put("result", "05");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
				}
	  	  		else
	  	  		{
		  	  		if(jsonRecv.has("plan_id"))
	  	  			{
	  	  				PlanDao pldo = new PlanDao(db);
	  	  				if(pldo.findSinglePlan(jsonRecv.getString("externalkey"), tenantData.getRecord_id(), jsonRecv.getString("plan_id")) != null)
	  	  				{
		  	  				jo.put("result", "00");
				  	  		jo.put("plan", pldo.findSinglePlan(jsonRecv.getString("externalkey"), tenantData.getRecord_id(), jsonRecv.getString("plan_id")).getPlan_name());
				  	  		jo.put("price", pldo.findSinglePlan(jsonRecv.getString("externalkey"), tenantData.getRecord_id(), jsonRecv.getString("plan_id")).getPrice());
				  	  		jo.put("amount", pldo.findSinglePlan(jsonRecv.getString("externalkey"), tenantData.getRecord_id(), jsonRecv.getString("plan_id")).getAmount());
				
							LogSystem.info(getClass(), jo.toString());
							return jo;
	  	  				}
	  	  				else
	  	  				{
		  	  				jo.put("result", "05");
		  		            jo.put("notif", "Plan not available");
		  		            jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
		  		            LogSystem.info(request, jo.toString(), uuid);
		  		            return jo;
	  	  				}
	  	  			}
	  	  			else
	  	  			{
		  	  			if(plan.size()>0)
			  	  		{
		  	  				String type = plan.get(0).getType();
			  	  			for(Plan pl:plan) {
								JSONObject planList=new JSONObject();
								
								planList.put("plan_name", pl.getPlan_name());
								planList.put("tenant", pl.getTenant().getRecord_id());
								planList.put("price", pl.getPrice());
								planList.put("amount", pl.getAmount());
//								planList.put("type", pl.getType());
								
								jplan.put(planList);
			  	  			}	
			  	  			
				  	  		jo.put("result", "00");
				  	  		jo.put("plan", jplan);
				  	  		jo.put("type", type);
				  	  		jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
							LogSystem.info(getClass(), jo.toString());
							return jo;
			  	  		}
			  	  		else
			  	  		{
			  	  			PlanDao pldo = new PlanDao(db);
			  	  			
			  	  			List<Plan> defaultplan = pldo.findAllPlanDefault(jsonRecv.getString("externalkey"), tenantData.getRecord_id());
			  	  			
			  	  			if(defaultplan.size() > 0)
			  	  			{
			  	  				String type = defaultplan.get(0).getType();
					  	  		for(Plan pl:defaultplan) {
									JSONObject planList=new JSONObject();
									
									planList.put("plan_name", pl.getPlan_name());
//									planList.put("tenant", pl.getTenant().getRecord_id());
									planList.put("price", pl.getPrice());
									planList.put("amount", pl.getAmount());
//									planList.put("type", pl.getType());
									
									jplan.put(planList);
				  	  			}	
				  	  			
					  	  		jo.put("result", "00");
					  	  		jo.put("plan", jplan);
					  	  		jo.put("type", type);
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								LogSystem.info(getClass(), jo.toString());
								return jo;
			  	  			}
			  	  			else
			  	  			{
					  	  		jo.put("result", "05");
					            jo.put("notif", "Default list plan not available");
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					            LogSystem.info(request, jo.toString(), uuid);
					            return jo;
			  	  			}
			  	  		}
	  	  			}
	  	  		}
  	  			
	  	  		
  	  		}

  	  	}catch(RuntimeException e)
  	  	{
  	  		tx.rollback();
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Error get list plan";
	  	  	jo.put("result", "06");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			
			return jo;
  	  	}  
  	  	finally {
			db.session().close();
			db.close();
	  	}
	}
}