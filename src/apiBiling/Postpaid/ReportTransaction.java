package apiBiling.Postpaid;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.ee.InvoiceItems;
import id.co.keriss.consolidate.ee.Mitra;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.LogSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jpos.ee.DB;
import org.jpos.ee.User;
import org.jpublish.JPublishContext;
import com.anthonyeden.lib.config.Configuration;


public class ReportTransaction extends ActionSupport {

	static String basepath="/opt/data-DS/UploadFile/";
	static String basepathPreReg="/opt/data-DS/PreReg/";
	final static Logger log=LogManager.getLogger("digisignlogger");

	@Override
	public void execute(JPublishContext context, Configuration cfg) {
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		List<FileItem> fileItems=null;
		
		LogSystem.info(request, "DATA DEBUG :"+(i++), uuid);

		try{
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);

			String process=request.getRequestURI().split("/")[2];
	        LogSystem.info(request, "PATH :"+request.getRequestURI(), uuid);
	        LogSystem.request(request, fileItems, uuid);
	        
	        JSONArray jo = null;
	        String accoundid = request.getParameter("account");
	        jo = InvoiceAccount(context, request, accoundid, uuid);

			context.put("trxjson", jo);

		}catch (Exception e) {
            LogSystem.error(getClass(), e, uuid);

            JSONObject jo=new JSONObject();
            try {
				jo.put("result", "06");
				jo.put("notif", "Failed get accounts");
				LogSystem.info(request, jo.toString(), uuid);
				context.put("trxjson", new JSONObject().put("JSONFile", jo).toString());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	JSONArray InvoiceAccount(JPublishContext context,  HttpServletRequest  request, String accountid, String uuid) throws JSONException{

		DB db = new DB();
		try {
			
			db = getDB(context);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		JSONArray jo = new JSONArray();

        ApiSystemDao maintanance = new ApiSystemDao(db);
        
		LogSystem.info(request, "Maintanance : " + maintanance.checkSystem(), uuid);
		
		if(maintanance.checkSystem())
		{
			jo.put("1122");
			
			LogSystem.info(request, jo.toString(), uuid);
			return jo;
		}
  		
  	  	Transaction tx = null;
  	  	if(db.session().isOpen())
  	  	{
  	  		tx = db.session().getTransaction();
  	  	}
  	  	else
  	  	{
  	  		tx = db.session().beginTransaction();
  	  	}
  	  	
  	  	try {
  	  		tx.setTimeout(15);
  	  		tx.begin();
  	  		
  	  		InvoiceItemsDao invdao = new InvoiceItemsDao(db);
  	  		List<InvoiceItems> transaction = invdao.listTransaction(accountid);
  	  		
  	  		if(transaction.size() < 1)
  	  		{
  	  			return jo;
  	  		}
  	  		else
  	  		{
  	  			int i;
  	  			for(i=0 ; i < transaction.size(); i++)
  	  			{
  	  				String type = null;
  	  				if(transaction.get(i).getType().equals("USAGE"))
  	  				{
  	  					type = "Transaction";
  	  				}
  	  				if(transaction.get(i).getType().equals("ITEM_ADJ"))
	  				{
	  					type = "Reversal";
	  				}
  	  				if(transaction.get(i).getType().equals("CREDIT_ADJ"))
	  				{
	  					type = "Topup";
	  				}
  	  				if(transaction.get(i).getType().equals("EXTERNAL_CHARGE"))
	  				{
	  					type = "External Charge";
	  				}

  	  				JSONObject data = new JSONObject();
  	  				data.put("jumlah_transaksi", transaction.get(i).getId());
  	  				data.put("tenant", type);
  	  				data.put("type", type);
  	  				
  	  				jo.put(data);
  	  				
  	  			}
  	  			
  	  			return jo;
  	  		}

  	  	}catch(RuntimeException e)
  	  	{
  	  		tx.rollback();
  	  		LogSystem.error(getClass(), e, uuid);
	  	  	jo.put("06");
			return jo;
  	  	}  	
	}
}