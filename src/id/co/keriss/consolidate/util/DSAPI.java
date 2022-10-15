package id.co.keriss.consolidate.util;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public interface DSAPI {
//		static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/DigitalSignatureAPI2.0/";
//		static final String KMS_HOST="http://ejbca:7090/DSKeyCore";
//		static final String DOMAIN="app.digisign.id";
//		static final String DOMAINAPI="api.digisign.id";
//        static final String BILLING_HOST="http://localhost:8080";
//	    static final String SMS_API="http://localhost:7090";
//	    static final String FACE_API="http://localhost/liveness/";
//		static final String FACE_API2="http://localhost/liveness/";
	
	
//	//production
/*
	static final String BILLING_HOST="http://localhost:8080";
	static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/DigitalSignatureAPI2.1/";
	static final String KMS_HOST="http://ejbca:7090/DSKeyCore";
	static final String DOMAIN="app.digisign.id";
	static final String DOMAINAPI="api.digisign.id";
	static final String DOMAINAPIWV="wvapi.digisign.id";
	static final String SMS_API="https://corp.digisign.id";
	static final String WEBDOMAIN="digisign.id";
	//static final String SMS_API="https://localhost:7272";
	static final String FACE_API="https://192.168.250.10/";
	static final String FACE_API2="https://192.168.250.10/";
	static final String EMAIL_API="http://localhost:8080/mail/api/"; 
	static final String DUKCAPIL_API="https://localhost:7070/DSSv1-0/";
	static final String LOGIN="https://app.digisign.id/login.html";
	static final String LINK="app.digisign.id/forgotpassword.html";
*/
	
	 	//test
		/*
	    static final String BILLING_HOST="http://192.168.182.7:8080";
		static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/DigitalSignatureAPI2.0/";
		static final String KMS_HOST="http://ejbca:7090/DSKeyCore";
		static final String LINK="app.tandatanganku.com/forgotpassword.html";
		static final String DOMAIN="app.tandatanganku.com";
		static final String DOMAINAPI="api.tandatanganku.com";
		static final String DOMAINAPIWV="wvapi.tandatanganku.com";
		static final String SMS_API="https://corp.tandatanganku.com";
		static final String FACE_API="https://192.168.250.10/";
		static final String FACE_API2="https://192.168.250.10/";
		static final String EMAIL_API="http://192.168.182.7:8080/mail/api/";
		static final String DUKCAPIL_API="https://192.168.182.7:7070/DSSv1-0/";
		static final String LOGIN="https://app.tandatanganku.com/login.html";
		*/
		
		//UAT
		static final String BILLING_VERSION="v1.6";
	    static final String BILLING_HOST="http://192.168.182.7:8080";
		static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/DigitalSignatureAPI2.0/";
		static final String KMS_HOST="http://ejbca:7090/DSKeyCore";
		static final String LINK="appuat.tandatanganku.com/forgotpassword.html";
		static final String DOMAIN="appuat.tandatanganku.com";
		static final String DOMAINAPI="192.168.182.7:1070/BillingAPI";
		static final String DOMAINAPIWV="wvuat.tandatanganku.com";
		static final String SMS_API="https://corp.tandatanganku.com";
		static final String FACE_API="https://192.168.250.10/";
		static final String FACE_API2="https://192.168.250.10/";
//		static final String EMAIL_API="https://emailservice:7272/mail/api/";
		static final String EMAIL_API = System.getenv("EMAIL_API");
//		static final String EMAIL_API="https://emailservice:7272/mail/";
//		static final String EMAIL_HEALTH_CHECK = System.getenv("EMAIL_HEALTH_CHECK");
		static final String DUKCAPIL_API="https://192.168.182.7:7070/DSSv1-0/";
		static final String LOGIN="https://appuat.tandatanganku.com/login.html";

	/*
	//Development
    //static final String BILLING_HOST="http://192.168.182.7:8080";
    //static final String BILLING_HOST="http://192.168.182.7:8080";
    //static final String BILLING_HOST="http://192.168.182.7:8080";
	static final String BILLING_HOST="http://192.168.78.16:8080";
	static final String ROOTDIR=System.getProperty("catalina.home")+"/webapps/DigitalSignatureAPI2.0/";
	static final String KMS_HOST="http://ejbca:7090/DSKeyCore";
	static final String DOMAIN="devapp.tandatanganku.com";
	static final String DOMAINAPI="devapi.tandatanganku.com";
	static final String DOMAINAPIWV="devapi.tandatanganku.com";
	//static final String DOMAIN="devapp.tandatanganku.com";
	//static final String DOMAINAPI="devapi.tandatanganku.com";
	static final String SMS_API="http://192.168.78.16:7090";
	//static final String FACE_API="http://192.168.78.16/liveness/";
	static final String FACE_API="https://35.184.1.246/";
	static final String FACE_API2="https://35.184.1.246/";
	static final String EMAIL_API="http://192.168.78.16:8080/mail/api/";
	//static final String EMAIL_API="http://192.168.78.16:8080/mail/api/";
	//static final String EMAIL_API="http://localhost:8080/mail/api/";
	static final String DUKCAPIL_API="https://192.168.182.7:7070/DSSv1-0/";
	static final String LOGIN="https://tandatanganku.com/login.html";
*/
}
