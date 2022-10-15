package id.co.keriss.consolidate.ee;

import java.math.BigDecimal;
import java.util.Date;

import org.jpos.ee.Accounts;

public class Transactions {
	
	private long id;
	private Accounts external_key;
	private Date created_date;
	private long amount;
	private long balance;
	private String phone;
	private int description;
	private String comments;
	   
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Accounts getExternal_key() {
		return external_key;
	}
	public void setExternal_key(Accounts external_key) {
		this.external_key = external_key;
	}
	public Date getCreated_date() {
		return created_date;
	}
	public void setCreated_date(Date created_date) {
		this.created_date = created_date;
	}
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	public long getBalance() {
		return balance;
	}
	public void setBalance(long balance) {
		this.balance = balance;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public int getDescription() {
		return description;
	}
	public void setDescription(int description) {
		this.description = description;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	

}


