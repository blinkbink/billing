package id.co.keriss.consolidate.ee;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.jpos.ee.Accounts;

public class PaymentBill {
	
	private long id;
	private InvoiceItems invoice_items_id;
	private Date payment_date;
	private String status;
	private String total_transfer;
	private String from_bank;
	private String description;
	private String account_number;
	private String payment_proof;
	private String price;
	private Tenant tenant;
	private String total_product;
	
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
	public Date getPayment_date() {
		return payment_date;
	}
	public void setPayment_date(Date payment_date) {
		this.payment_date = payment_date;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTotal_transfer() {
		return total_transfer;
	}
	public void setTotal_transfer(String total_transfer) {
		this.total_transfer = total_transfer;
	}
	public String getFrom_bank() {
		return from_bank;
	}
	public void setFrom_bank(String from_bank) {
		this.from_bank = from_bank;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAccount_number() {
		return account_number;
	}
	public void setAccount_number(String account_number) {
		this.account_number = account_number;
	}
	public String getPayment_proof() {
		return payment_proof;
	}
	public void setPayment_proof(String payment_proof) {
		this.payment_proof = payment_proof;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public Tenant getTenant() {
		return tenant;
	}
	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	public String getTotal_product() {
		return total_product;
	}
	public void setTotal_product(String total_product) {
		this.total_product = total_product;
	}
	
}
