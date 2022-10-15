package id.co.keriss.consolidate.ee;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;
import org.jpos.ee.Accounts;

@Entity
@Table(name = "master_balance")
@OptimisticLocking(type = OptimisticLockType.ALL)

public class MasterBalance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BigInteger id;
	private String externalkey;
	private Tenant tenant_record_id;
	private BigDecimal balance;
//	private BigDecimal free_balance;
	
	public BigInteger getId() {
		return id;
	}
	public void setId(BigInteger id) {
		this.id = id;
	}
	public String getExternalkey() {
		return externalkey;
	}
	public void setExternalkey(String externalkey) {
		this.externalkey = externalkey;
	}
	public Tenant getTenant_record_id() {
		return tenant_record_id;
	}
	public void setTenant_record_id(Tenant tenant_record_id) {
		this.tenant_record_id = tenant_record_id;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
//	public BigDecimal getFree_balance() {
//		return free_balance;
//	}
//	public void setFree_balance(BigDecimal free_balance) {
//		this.free_balance = free_balance;
//	}
	
	
}
