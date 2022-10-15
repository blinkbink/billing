package api.email;

import java.io.DataOutputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.ThreadContext;

import id.co.keriss.consolidate.util.DSAPI;
import id.co.keriss.consolidate.util.LogSystem;

public class SendMailApi {
	
	Integer responseCode = 200;
	String rst = "Success";
	

	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
        throws java.security.cert.CertificateException {
      // TODO Auto-generated method stub

    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
        throws java.security.cert.CertificateException {
      // TODO Auto-generated method stub

    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
      // TODO Auto-generated method stub
      return null;
    }

  } };
	
	public Integer pingMail(HttpServletRequest request, String refTrx) {
	    String mail_index = DSAPI.EMAIL_API.replace("mail/api/", "mail/");
	    LogSystem.info(request, "Sending 'POST' request to URL : " + mail_index, refTrx);
	    String res = sending(mail_index, request, refTrx);
	    return responseCode;
	  }
	

	private String sending(String url, HttpServletRequest request, String refTrx) {
	    try {
	      
	      SSLContext sc = SSLContext.getInstance("SSL");
	      sc.init(null, trustAllCerts, new java.security.SecureRandom());
	      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	      // Create all-trusting host name verifier
	      HostnameVerifier allHostsValid = new HostnameVerifier() {
	        @Override
	        public boolean verify(String hostname, SSLSession session) {
	          return true;
	        }
	      };
	      URL obj = new URL(url);
	      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	      HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
	      con.setHostnameVerifier(allHostsValid);
	      con.setSSLSocketFactory(sc.getSocketFactory());
	      // Setting basic post request
	      con.setRequestMethod("POST");
	      con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	      con.setRequestProperty("Content-Type", "application/json");
	      // String postJsonData = "{'text':'c54s123','dest' : '081212611881'}";
	      con.setConnectTimeout(10000);// set timeout to 5 seconds extend to 30 second
	      // Send post request
	      con.setDoOutput(true);
	      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	      // wr.writeBytes(postJsonData);
	      wr.flush();
	      wr.close();
	      ThreadContext.put("tp", "SND");
	      
	      responseCode = con.getResponseCode();
	      ThreadContext.put("tp", "RCV");
	      LogSystem.info(request, "Response Code : " + responseCode, refTrx);
	      con.disconnect();
	    } catch (Exception e) {
	      e.printStackTrace();
	      responseCode = 700;
	    }
	    return rst;
	  }
}
