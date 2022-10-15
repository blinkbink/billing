package id.co.keriss.consolidate.ee;

import java.math.BigInteger;
import java.util.Date;

public class Tenant {
	
	private long record_id;
	private String id;
	private String external_key;
	private Date created_date;
	private Date updated_date;
	private String api_key;
	

	public String getApi_key() {
		return api_key;
	}
	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}
	public long getRecord_id() {
		return record_id;
	}
	public void setRecord_id(long record_id) {
		this.record_id = record_id;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getExternal_key() {
		return external_key;
	}
	public void setExternal_key(String external_key) {
		this.external_key = external_key;
	}
	public Date getCreated_date() {
		return created_date;
	}
	public void setCreated_date(Date created_date) {
		this.created_date = created_date;
	}
	public Date getUpdated_date() {
		return updated_date;
	}
	public void setUpdated_date(Date updated_date) {
		this.updated_date = updated_date;
	}
	
}
