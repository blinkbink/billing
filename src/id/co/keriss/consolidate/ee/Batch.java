package id.co.keriss.consolidate.ee;

import java.math.BigInteger;
import java.util.Date;

import org.jpos.ee.Accounts;


public class Batch  implements java.io.Serializable {


     private BigInteger id;
     private Date open_date;
     private Date closing_date;
     private Accounts account_record_id;
     private Integer price;
     private Integer quota;
     private Integer usage;
     private Integer remaining_balance;
     private String name_batch;
     private Boolean settled;

    public Batch() {
    }

	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
		this.id = id;
	}

	public Date getOpen_date() {
		return open_date;
	}

	public void setOpen_date(Date open_date) {
		this.open_date = open_date;
	}

	public Date getClosing_date() {
		return closing_date;
	}

	public void setClosing_date(Date closing_date) {
		this.closing_date = closing_date;
	}

	public Accounts getAccount_record_id() {
		return account_record_id;
	}

	public void setAccount_record_id(Accounts account_record_id) {
		this.account_record_id = account_record_id;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public Integer getQuota() {
		return quota;
	}

	public void setQuota(Integer quota) {
		this.quota = quota;
	}

	public Integer getUsage() {
		return usage;
	}

	public void setUsage(Integer usage) {
		this.usage = usage;
	}

	public Integer getRemaining_balance() {
		return remaining_balance;
	}

	public void setRemaining_balance(Integer remaining_balance) {
		this.remaining_balance = remaining_balance;
	}

	public String getName_batch() {
		return name_batch;
	}

	public void setName_batch(String name_batch) {
		this.name_batch = name_batch;
	}

	public Boolean getSettled() {
		return settled;
	}

	public void setSettled(Boolean settled) {
		this.settled = settled;
	}

}


