package id.co.keriss.consolidate.ee;

import java.util.Date;

import org.jpos.ee.Accounts;

public class Invoices {

	private long record_id;
	private String id;
	private Accounts account_record_id;
	private String account_id;
	private Tenant tenant_record_id;
	private Date invoice_date;
	private Date created_date;
	private Date target_date;
	private String status;

	public Date getTarget_date() {
		return target_date;
	}
	public void setTarget_date(Date target_date) {
		this.target_date = target_date;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAccount_id() {
		return account_id;
	}
	public void setAccount_id(String account_id) {
		this.account_id = account_id;
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
	public Accounts getAccount_record_id() {
		return account_record_id;
	}
	public void setAccount_record_id(Accounts account_record_id) {
		this.account_record_id = account_record_id;
	}
	public Tenant getTenant_record_id() {
		return tenant_record_id;
	}
	public void setTenant_record_id(Tenant tenant_record_id) {
		this.tenant_record_id = tenant_record_id;
	}
	public Date getInvoice_date() {
		return invoice_date;
	}
	public void setInvoice_date(Date invoice_date) {
		this.invoice_date = invoice_date;
	}
	public Date getCreated_date() {
		return created_date;
	}
	public void setCreated_date(Date created_date) {
		this.created_date = created_date;
	}
	
}
