package id.co.keriss.consolidate.ee;

import java.util.Date;

import org.jpos.ee.Accounts;

public class PostpaidTransaction {
	
	private long id;
	private InvoiceItems invoice_items_id;
	private String external_key;
	private Tenant tenant;
	private Date date;
	private long amount;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public InvoiceItems getInvoice_items_id() {
		return invoice_items_id;
	}
	public void setInvoice_items_id(InvoiceItems invoice_items_id) {
		this.invoice_items_id = invoice_items_id;
	}
	public String getExternal_key() {
		return external_key;
	}
	public void setExternal_key(String external_key) {
		this.external_key = external_key;
	}
	public Tenant getTenant() {
		return tenant;
	}
	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	
}
