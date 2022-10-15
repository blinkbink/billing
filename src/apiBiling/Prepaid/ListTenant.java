package apiBiling.Prepaid;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.Tenant;
import id.co.keriss.consolidate.util.LogSystem;


public class ListTenant extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String jsonString=null;

		List<FileItem> fileItems=null;
		
		String uuid = UUID.randomUUID().toString().replace("-", "");
		
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
        
  	  	if(!jsonRecv.has("data")) 
  	  	{
  	  		notif="JSON data does not exist";
	  	  	jo.put("result", "28");
			jo.put("notif", notif);
			jo.put("log", uuid);
			jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
  	  	}
  	
  	  	try {
  	  		int i;
  	  		
	 		TenantDao tdo = new TenantDao(db);
	 		JSONArray arraytenant = new JSONArray();
	 		
	 		List<Tenant> listtenant = tdo.findAllTenant();
  	  		if(jsonRecv.getString("data").equals("info"))
  	  		{
	  	  		if(listtenant.size() < 1)
		 		{
		 			jo.put("result", "00");
		  	  		jo.put("tenant", "null");
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
		  	  		LogSystem.info(request, jo.toString(), uuid);
		  	  		return jo;
		 		}
		 		else
		 		{
		 			for(i = 0 ; i < listtenant.size() ; i++)
		  	  		{
		  	  			JSONObject objtenant = new JSONObject();
		  	  			objtenant.put("record_id", listtenant.get(i).getRecord_id());
		  	  			objtenant.put("id", listtenant.get(i).getId());
		  	  			objtenant.put("type", listtenant.get(i).getApi_key());
		  	  			
		  	  			arraytenant.put(objtenant);
		  	  		}
		  	  		
		  	  		jo.put("result", "00");
		  	  		jo.put("tenant", arraytenant);
					jo.put("log", uuid);
					jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
		  	  		LogSystem.info(request, jo.toString(), uuid);
		  	  		return jo;
		 		}
  	  		}
  	  		else
  	  		{
	  	  		jo.put("result", "05");
	  	  		jo.put("info", "Wrong request");
				jo.put("log", uuid);
				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
	  	  		LogSystem.info(request, jo.toString(), uuid);
	  	  		return jo;
  	  		}
	 		
	 		 	
  	  	}catch(RuntimeException e)
  	  	{
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