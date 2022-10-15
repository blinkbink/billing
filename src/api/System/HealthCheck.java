package api.System;

import id.co.keriss.consolidate.action.ActionSupport;
import id.co.keriss.consolidate.dao.AccountsDao;
import id.co.keriss.consolidate.dao.ApiSystemDao;
import id.co.keriss.consolidate.dao.InvoiceItemsDao;
import id.co.keriss.consolidate.dao.MasterBalanceDao;
import id.co.keriss.consolidate.dao.TenantDao;
import id.co.keriss.consolidate.ee.InvoiceItems;
import id.co.keriss.consolidate.ee.Invoices;
import id.co.keriss.consolidate.ee.MasterBalance;
import id.co.keriss.consolidate.ee.Tenant;

import org.hibernate.HibernateException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

import api.email.SendMailApi;
import api.email.SendNotifPeringatanSaldo;

public class HealthCheck extends ActionSupport {

	final static Logger log=LogManager.getLogger("digisignlogger");

	@SuppressWarnings("unchecked")
	@Override
	public void execute(JPublishContext context, Configuration cfg)  {
		
		int i=0;
		HttpServletRequest  request  = context.getRequest();

		String uuid = UUID.randomUUID().toString().replace("-", "");

		LogSystem.info(request, "DATA DEBUG :"+(i++), uuid);

		try{
	
	        JSONObject jo = null;
	        jo=HealthCheck(context, request, uuid);
	        
			context.put("trxjson", jo.toString());
			LogSystem.response(request, jo, uuid);

		}catch (Exception e) {
      
		}
	}
	
	
	JSONObject HealthCheck(JPublishContext context,  HttpServletRequest  request, String uuid) throws JSONException, StaleObjectStateException, HibernateException, IOException{


		JSONObject jo=new JSONObject();
		ArrayList<String> alist=new ArrayList<String>();
		DB db = null;
		try {
			db = new DB();
			try {
				db = getDB(context);
			}catch(Exception e)
			{
				e.printStackTrace();
			}

			LogSystem.info(request, "DB Connection status : " + db.session().isConnected(), uuid);
			
			//Check connection to DB
			LogSystem.info(request, "Check Database connection start", uuid);
			try {
				ApiSystemDao maintanance = new ApiSystemDao(db);
				LogSystem.info(request, "Maintanance : " + maintanance.checkSystem(), uuid);
			}catch (Exception e)
			{
				LogSystem.error(getClass(), e, uuid);
				alist.add("DB");
			}
			LogSystem.info(request, "Check Database connection finish", uuid);
			
			//Check connection to email
			LogSystem.info(request, "Check API email connection start", uuid);
			
			try {
				Integer cekmail = new SendMailApi().pingMail(request, uuid);
				LogSystem.info(request, "Response cek email : " + cekmail, uuid);
	            if(cekmail != 200) 
	            {
	            	alist.add("Email");
	            }
			}catch (Exception e)
			{
				LogSystem.error(getClass(), e, uuid);
				alist.add("Email");
			}
			LogSystem.info(request, "Check API email connection finish", uuid);
			
			if(alist.size() > 0)
			{
				jo.put("result", "05");
				jo.put("error", alist);
				context.getResponse().setStatus(503);
				return jo;
			}
			else
			{
				jo.put("result", "00");
				return jo;
			}

  	  	}catch(RuntimeException e)
  	  	{
  	  		alist.add("");
	  	  	jo.put("result", "05");
			jo.put("error", alist);
  	  		return jo;
  	  	} finally {
  	  		LogSystem.info(request, "Close session connection to db", uuid);
			db.session().close();
			LogSystem.info(request, "Close connection db", uuid);
			db.close();
  	  	}
	}
}