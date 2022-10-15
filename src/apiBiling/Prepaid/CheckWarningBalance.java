package apiBiling.Prepaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.TenantDao;
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
import org.jpos.ee.Accounts;
import org.jpos.ee.DB;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;


public class CheckWarningBalance extends ActionSupport {

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
		//System.out.println("DATA DEBUG :"+(i++));
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
	        jo=Balance(jsonRecv, context, request, uuid);
	         
	         
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
				jo.put("notif", "Failed check notif minimum balance");
				jo.put("log", uuid);
				LogSystem.info(request, jo.toString(), uuid);
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	JSONObject Balance(JSONObject jsonRecv, JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException{
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
//	  	if(!jsonRecv.has("tenant")) 
//	  	{
//		  		notif="JSON tenant does not exist";
//		  	  	jo.put("result", "28");
//				jo.put("notif", notif);
//				jo.put("log", uuid);
//				jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
//				LogSystem.info(request, jo.toString(), uuid);
//				return jo;
//		}
//  	
  	  	Transaction tx = db.session().getTransaction();
  	  	
  	  	try {
	  	  		tx.setTimeout(300);
	  	  		tx.begin();
	  	  		
	  	  		AccountsDao ado = new AccountsDao(db);
  	  			TenantDao tdao = new TenantDao(db);
  	  			
	  	  		if(jsonRecv.has("tenant"))
	  	  		{
		  	  		Tenant tenantApiKey = tdao.findByApiKey(jsonRecv.getString("tenant"));
		  	  		ArrayList<String> listtenant = new ArrayList<String>();
		  	  		List<Tenant> dataTenant = tdao.findAllTenantPrepaid();
				
					LogSystem.info(request, "Arraylist tenant : " + listtenant, uuid);
					
					Accounts account = ado.findByExternalKey(jsonRecv.getString("externalkey"), tenantApiKey.getRecord_id());
					
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
					
					if(account == null)
					{
						notif="Account externalkey with that tenant does not exist";
				  	  	jo.put("result", "04");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(request, jo.toString(), uuid);
						return jo;
					}
					else
					{
						jo.put("tenant", account.getTenant_record_id().getApi_key());
						jo.put("active", account.getSend_notif_balance());
			  	  		jo.put("minimum_balance", account.getNotif_balance());
			  	  		jo.put("email", account.getEmail());
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			  	  		LogSystem.info(request, jo.toString(), uuid);
			  	  		return jo;
					}
	  	  		}
	  	  		else
	  	  		{
	  	  			List<Accounts> account = ado.findSingleAccount(jsonRecv.getString("externalkey"));
	  	  			
		  	  		if(account == null || account.size() < 1)
					{
						notif="Account externalkey with that tenant does not exist";
				  	  	jo.put("result", "04");
						jo.put("notif", notif);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
						LogSystem.info(request, jo.toString(), uuid);
						return jo;
					}
					else
					{
						JSONArray jos = new JSONArray();
						for(int i=0 ; i < account.size() ; i++)
						{
							JSONObject emailaccount = new JSONObject();
							
							emailaccount.put("tenant", account.get(i).getTenant_record_id().getApi_key());
							emailaccount.put("active", account.get(i).getSend_notif_balance());
							emailaccount.put("minimum_balance", account.get(i).getNotif_balance());
							emailaccount.put("email", account.get(i).getEmail());
							
							jos.put(emailaccount);
						}
						
						jo.put("accounts", jos);
						jo.put("log", uuid);
						jo.put("ptime", formatter.format((System.nanoTime() - startTime)/1000000000d));
			  	  		LogSystem.info(request, jo.toString(), uuid);
			  	  		return jo;
					}
	  	  		}

  	  	}catch(RuntimeException e)
  	  	{
  	  		tx.rollback();
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	notif="Failed check account data";
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