package id.co.keriss.consolidate.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.apache.commons.logging.impl.*;

public class LogSystem implements DSAPI{
	
	public static Logger log = LogManager.getLogger("ver 1.0");
	
	public static void request(HttpServletRequest request) {
		String[] url = request.getRequestURI().split("/");
		String logData=request.getRemoteAddr() + " " + url[1]+"/"+ BILLING_VERSION +"/"+url[2];
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
		logData+= formattedDate + " [RECEIVE] ";

		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if(!isMultipart) {
		Enumeration<String> params = request.getParameterNames(); 
		while(params.hasMoreElements()){
        String paramName = params.nextElement();
			 	if(paramName.equals("fprt-1")||paramName.equals("fprt-2")||paramName.indexOf("foto")>=0||paramName.equals("pwd")) {
			 		logData+="["+paramName+"] : {WIPED};";

			 	}else
			 		logData+="["+paramName+"] :\""+request.getParameter(paramName)+"\";";
			}		
		}
		else {
			
			ServletFileUpload upload = new ServletFileUpload(
					new DiskFileItemFactory());

			// parse requests
			List<FileItem> fileItems=null;
			try {
				fileItems = upload.parseRequest(request);
			} catch (FileUploadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			// Process the uploaded items
			for (FileItem fileItem : fileItems) {
				// a regular form field

				if (fileItem.isFormField()) {
					String paramName=fileItem.getFieldName();
					if(paramName.equals("fprt-1")||paramName.equals("fprt-2")||paramName.indexOf("foto")>=0||paramName.equals("pwd")) {
				 		logData+="["+paramName+"] : {WIPED};";

				 	}else
				 		logData+="["+paramName+"] :\""+fileItem.getString()+"\";";	
				}
				else {
					
				 	logData+="["+fileItem.getFieldName()+"] :{"+fileItem.getName()+","+fileItem.getSize()+"};";
				}
			}

		}
		log.info(logData);
	}
	
	public static void request(HttpServletRequest request, List<FileItem> fileItems, String uuid) {
		
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		String[] url = request.getRequestURI().split("/");
		String logData=request.getRemoteAddr() + " " + url[1]+"/"+ BILLING_VERSION +"/"+url[2];
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
		// no multipart form
		if (!isMultipart) {
			request(request);	
			
		}
		// multipart form
		else {
			
			logData+= formattedDate + " [RECEIVE]["+uuid+"]";
			
			// Process the uploaded items
			for (FileItem fileItem : fileItems) {
				// a regular form field

				if (fileItem.isFormField()) {
					String paramName=fileItem.getFieldName();
					if(paramName.equals("fprt-1")||paramName.equals("fprt-2")||paramName.equals("foto")||paramName.indexOf("foto")>=0||paramName.equals("pwd")) {
				 		logData+="["+paramName+"] : {WIPED};";

				 	}else
				 		logData+="["+paramName+"] :\""+fileItem.getString()+"\";";

					
				}
				else {
					
				 	logData+="["+fileItem.getFieldName()+"] :{"+fileItem.getName()+","+fileItem.getSize()+"};";

					// System.out.println(fileItem.getFieldName()+" : "+fileItem.getName()+","+fileItem.getContentType());

				}
			}
			log.info(logData);

		}
		
	}
	
	public static void response(HttpServletRequest request, JSONObject respData, String uuid) {
		String[] url = request.getRequestURI().split("/");
		String logData=request.getRemoteAddr() + " " + url[1]+"/"+ BILLING_VERSION +"/"+url[2];
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
		logData+= formattedDate + " [SEND]["+uuid+"]  : { \"JSONFile\" : { ";
		Iterator<String> it=respData.keys();
		int i=0;
		while(it.hasNext()) {
			  if(i>0) {
				  logData+=", ";
			  }
		      String ky = it.next();
		      String val="[WIPED]";
		      switch (ky) {
				case "signature-pic":
				case "fotoktp":
				case "fotodiri":
				case "file":
					
					break;
	
				default:
					try {
						val=respData.getString(ky);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
		      logData+="\""+ky+"\":\""+val+"\"";
		      i++;
		      
		}

		logData+=" } }";
		log.info(logData);
	}
	
	public static void info(HttpServletRequest request, String respData, String uuid) {
		String[] url = request.getRequestURI().split("/");
		String logData=request.getRemoteAddr() + " " + url[1]+"/"+ BILLING_VERSION +"/"+url[2];
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
		logData= formattedDate + " [CHECK]" + "["+uuid+"]" + " : "+respData;
		try {
		log.info(logData);
		}catch(Exception e)
		{
			e.printStackTrace();
			System.out.print(e);
		}
	}
	
	public static void error(HttpServletRequest request, String respData, String uuid) {
		String[] url = request.getRequestURI().split("/");
		String logData=request.getRemoteAddr() + " " + url[1]+"/"+ BILLING_VERSION +"/"+url[2];
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
		logData= formattedDate + " [CHECK]" + "["+uuid+"]" + " : "+respData;
		
		log.error(logData);
	}
	
	public static void error(Class className, Exception obj, String uuid) {
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
		
		LogManager.getLogger(className).error(formattedDate + " [CHECK]" + "["+uuid+"]" + " : ",obj);
		
	}
	public static void info(Class className, String obj) {
		Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String formattedDate="["+dateFormat.format(date)+"]";
        
        LogManager.getLogger(className).info(formattedDate + " [INFO] " + obj);
		
	}
}
