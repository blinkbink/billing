package api;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;



public class ApplicationLogServlet extends HttpServlet {
	 
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init()
	 {
	     String prefix =  getServletContext().getRealPath("/");
	     String file = getInitParameter("log4j-init-file");
	  
	     // if the log4j-init-file context parameter is not set, then no point in trying
	     if(file != null){
//	    	 PropertiesConfiguration.preConfigure(null);
//	    	 PropertiesConfiguration.configure("log4j.properties");
	      System.out.println("Log4J Logging started: " + prefix+file);
	     }
	     else{
	      System.out.println("Log4J Is not configured for your Application: " + prefix + file);
	     }     
	 }
}
