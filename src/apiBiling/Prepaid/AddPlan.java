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
import java.util.Date;
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

public class AddPlan extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
//		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;
		
		String uuid = UUID.randomUUID().toString().replace("-", "");

		List<FileItem> fileItems=null;
//		LogSystem.info(request, "DATA DEBUG :"+(i++), uuid);

		try{
				boolean isMultipart = ServletFileUpload.isMultipartContent(request);
				
				// no multipart form
				if (!isMultipart) {
					LogSystem.info(request, "Bukan multipart", uuid);
					JSONObject jo=new JSONObject();
					jo.put("result", "30");
					jo.put("notif", "Format request API salah.");
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
	        jo=AddPlan(jsonRecv, context, request, uuid);
	         
	         
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
				jo.put("notif", "Failed add plan");
				jo.put("log", uuid);
				LogSystem.info(request, jo.toString(), uuid);
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject AddPlan(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException{
		NumberFormat formatter = new DecimalFormat("#0.00000");
		long startTime = System.nanoTime();
		DB db = new DB();
		try {
			
			db = getDB(context);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		JSONObject jo=new JSONObject();

        String notif="Account already exist";

//        ApiSystemDao maintanance = new ApiSystemDao(db);
//        
//		LogSystem.info(request, "Maintanance : " + maintanance.checkSystem(), uuid);
//		
//		if(maintanance.checkSystem())
//		{
//			notif="Billing API under maintanance";
//	  	  	jo.put("result", "1122");
//			jo.put("notif", notif);
//			jo.put("log", uuid);
//			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//			LogSystem.info(request, jo.toString(), uuid);
//			return jo;
//		}
        
        if(!jsonRecv.has("plan")) 
  	  	{
  	  		notif="JSON plan does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
  	  	}
  	  
  	  	Transaction tx = db.session().getTransaction();
 
  	  	try {
  	  		tx.setTimeout(20);
  	  		tx.begin();
  	  		
	  	  	if(jsonRecv.get("plan")!=null) 
	  	  	{
	  	  		JSONArray sendList = null;
	  	  		try
	  	  		{
	  	  			sendList=(JSONArray) jsonRecv.getJSONArray("plan");
	  	  		}catch(Exception e)
	  	  		{
		  	  		notif="Plan not JSONArray";
			  	  	jo.put("result", "28");
					jo.put("notif", notif);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
					LogSystem.info(request, jo.toString(), uuid);
					return jo;
	  	  		}
	  	  		
		  	  	
	  	  		
				for(int i=0; i<sendList.length(); i++) 
				{
					JSONObject obj=(JSONObject) sendList.get(i);
					
			  		if(!obj.has("externalkey")) 
			  	  	{
			  	  		notif="JSON external key does not exist at array" + i;
				  	  	jo.put("result", "28");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(request, jo.toString(), uuid);
						return jo;
			  	  	}
			  	  	if(!obj.has("price")) 
				  	{
				  		notif="JSON price does not exist at array" + i;
				  	  	jo.put("result", "28");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(request, jo.toString(), uuid);
						return jo;
				  	}
			  	  	if(!obj.has("amount")) 
				  	{
				  		notif="JSON amount does not exist at array" + i;
				  	  	jo.put("result", "28");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(request, jo.toString(), uuid);
						return jo;
				  	}
			  	  	if(!obj.has("tenant")) 
				  	{
				  		notif="JSON tenant does not exist at array" + i;
				  	  	jo.put("result", "28");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(request, jo.toString(), uuid);
						return jo;
				  	}
			  	  	if(!obj.has("type")) 
				  	{
				  		notif="JSON type does not exist at array" + i;
				  	  	jo.put("result", "28");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(request, jo.toString(), uuid);
						return jo;
				  	}
					
					Plan plan = new Plan();
					Tenant tenant = new Tenant();
					TenantDao tdo = new TenantDao(db);
					PlanDao pldo = new PlanDao(db);
					tenant = tdo.findByApiKey(obj.getString("tenant"));
					AccountsDao ado = new AccountsDao(db);
					
					ArrayList<String> listtenant = new ArrayList<String>();
					
					TenantDao tdof = new TenantDao(db);
					List<Tenant> dataTenant = tdof.findAllTenantPrepaid();
					
					LogSystem.info(request, "Arraylist tenant : " + listtenant, uuid);
					
					for (int l = 0 ; l < dataTenant.size() ; l++)
					{
						listtenant.add(dataTenant.get(l).getApi_key());
					}
					
					if(!listtenant.contains(obj.getString("tenant")))
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
						if(!obj.getString("type").equals("default"))
						{
							if(ado.findByExternalKey(obj.getString("externalkey"), tenant.getRecord_id()) == null)
							{
								notif="Account external key with that tenant does not exist";
						  	  	jo.put("result", "05");
								jo.put("notif", notif);
								jo.put("log", uuid);
								jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
								LogSystem.info(request, jo.toString(), uuid);
								return jo;
							}
							else
							{
								if(pldo.findAllPlan(obj.getString("externalkey"), tenant.getRecord_id()).size() < 1)
								{
									plan.setPlan_name(obj.getString("externalkey") + "_" +"a");
									
									plan.setExternal_key(obj.getString("externalkey"));
									
									if(isNumeric(obj.getString("price")))
									{
										plan.setPrice(obj.getLong("price"));
									}
									else
									{
										notif="price contain non numeric value";
								  	  	jo.put("result", "28");
										jo.put("notif", notif);
										jo.put("log", uuid);
										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
										LogSystem.info(request, jo.toString(), uuid);
										return jo;
									}
									
									if(isNumeric(obj.getString("amount")))
									{
										plan.setAmount(obj.getLong("amount"));
									}
									else
									{
										notif="amount contain non numeric value";
								  	  	jo.put("result", "28");
										jo.put("notif", notif);
										jo.put("log", uuid);
										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
										LogSystem.info(request, jo.toString(), uuid);
										return jo;
									}
									
									plan.setType(obj.getString("type"));
									plan.setTenant(tenant);		
									plan.setCreated_date(new Date());
									plan.setUpdated_date(new Date());
									
									db.session().save(plan);
								}
								else
								{
									int charValue = pldo.findByPlan(obj.getString("externalkey"), tenant.getRecord_id()).get(0).getPlan_name().toString().split("_")[1].charAt(0);
								    String next = String.valueOf( (char) (charValue + 1));
								    
								    plan.setPlan_name(obj.getString("externalkey") + "_" +next);
									
									plan.setExternal_key(obj.getString("externalkey"));
									
									if(isNumeric(obj.getString("price")))
									{
										plan.setPrice(obj.getLong("price"));
									}
									else
									{
										notif="price contain non numeric value";
								  	  	jo.put("result", "28");
										jo.put("notif", notif);
										jo.put("log", uuid);
										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
										LogSystem.info(request, jo.toString(), uuid);
										return jo;
									}
									
									if(isNumeric(obj.getString("amount")))
									{
										plan.setAmount(obj.getLong("amount"));
									}
									else
									{
										notif="amount contain non numeric value";
								  	  	jo.put("result", "28");
										jo.put("notif", notif);
										jo.put("log", uuid);
										jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
										LogSystem.info(request, jo.toString(), uuid);
										return jo;
									}
									
									plan.setType(obj.getString("type"));
									plan.setTenant(tenant);		
									plan.setCreated_date(new Date());
									plan.setUpdated_date(new Date());
									
									db.session().save(plan);
									
								}
							}
						}
						else
						{
							LogSystem.info(request, "EXTERNAL KEY : " + obj.getString("externalkey"), uuid);
							LogSystem.info(request, "TENANT RECORD ID : " + tenant.getRecord_id(), uuid);
							LogSystem.info(request, "DEFAULT EXIST SIZE : " + pldo.findAllDefault(tenant.getRecord_id()).size(), uuid);
							
							if(pldo.findAllDefault(tenant.getRecord_id()).size() < 1)
							{
								plan.setPlan_name(obj.getString("type") + obj.getString("externalkey").substring(0, 2) + "_" +"a");
								
								plan.setExternal_key("0");
								
								if(isNumeric(obj.getString("price")))
								{
									plan.setPrice(obj.getLong("price"));
								}
								else
								{
									notif="price contain non numeric value";
							  	  	jo.put("result", "28");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									LogSystem.info(request, jo.toString(), uuid);
									return jo;
								}
								
								if(isNumeric(obj.getString("amount")))
								{
									plan.setAmount(obj.getLong("amount"));
								}
								else
								{
									notif="amount contain non numeric value";
							  	  	jo.put("result", "28");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									LogSystem.info(request, jo.toString(), uuid);
									return jo;
								}
								
								plan.setType(obj.getString("type"));
								plan.setTenant(tenant);		
								plan.setCreated_date(new Date());
								plan.setUpdated_date(new Date());
								
								db.session().save(plan);
							}
							else
							{

								int charValue = pldo.findByPlanDefault(obj.getString("externalkey"), tenant.getRecord_id()).get(0).getPlan_name().toString().split("_")[1].charAt(0);
						        String next = String.valueOf( (char) (charValue + 1));
						        
						        plan.setPlan_name(obj.getString("type") + obj.getString("externalkey").substring(0, 2) + "_" +next);
								
								plan.setExternal_key("0");
								
								if(isNumeric(obj.getString("price")))
								{
									plan.setPrice(obj.getLong("price"));
								}
								else
								{
									notif="price contain non numeric value";
							  	  	jo.put("result", "28");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									LogSystem.info(request, jo.toString(), uuid);
									return jo;
								}
								
								if(isNumeric(obj.getString("amount")))
								{
									plan.setAmount(obj.getLong("amount"));
								}
								else
								{
									notif="amount contain non numeric value";
							  	  	jo.put("result", "28");
									jo.put("notif", notif);
									jo.put("log", uuid);
									jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
									LogSystem.info(request, jo.toString(), uuid);
									return jo;
								}
								
								plan.setType(obj.getString("type"));
								plan.setTenant(tenant);		
								plan.setCreated_date(new Date());
								plan.setUpdated_date(new Date());
								
								db.session().save(plan);
								
							}
						}	
					}
				}
			}

	  	  	tx.commit();
			
			notif="Success create new plan";
	  	  	jo.put("result", "00");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
  			return jo;
	  	  	
  	  	}catch(RuntimeException e)
  	  	{
  	  		tx.rollback();
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Failed add plan";
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
	
	public static boolean isNumeric(String strNum) {
	    try {
	        double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException | NullPointerException nfe) {
	        return false;
	    }
	    return true;
	}
}