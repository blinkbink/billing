package api.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import id.co.keriss.consolidate.util.DSAPI;

public class SendNotifSignDoc {
	public void kirim(Date dateSign, String nama, String jk, String email, String nama_pengirim, String jk_pengirim, String nama_doc, String link, String idmitra) {
	//public void kirim(String nama, String jk, String email, String nama_pengirim, String jk_pengirim, String nama_doc, String link, String idmitra) {
			
		URL url = null;
		try {
			url = new URL(DSAPI.EMAIL_API+"sendMailNotifSign.html");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Map<String,Object> params = new LinkedHashMap<>();
     
        //Format tanggal
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(dateSign);
        
        //String oldDateString = "2018-01-17 11:34:10.489000";
        String oldDateString = dateFormat.format(dateSign);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        Date d = null;
       
        try {
			d = sdf.parse(oldDateString);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
       
        sdf.applyPattern("MMM dd, yyyy hh:mm:ss");
        System.out.println("Date Format : " + sdf.format(d) + " WIB");
        
        
        params.put("tanggal", sdf.format(d) + " WIB");
        params.put("nama_penerima", nama);
        params.put("jk_penerima", jk);
        params.put("email", email);
        params.put("nama_ttd", nama_pengirim);
        params.put("jk_ttd", jk_pengirim);
        params.put("nama_dokumen", nama_doc);
        params.put("link", link);
        params.put("id_mitra", idmitra);
        
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            try {
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }
        
        byte[] postDataBytes = null;
		try {
			postDataBytes = postData.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        HttpURLConnection conn;
		try {
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);
	        conn.setConnectTimeout(30000);
	        
	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

	        for (int c; (c = in.read()) >= 0;)
	            System.out.print((char)c);
		} catch (java.net.SocketTimeoutException t) {
			   //return false;
			t.printStackTrace();
			System.out.println("timeout ke email");
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
