package id.co.keriss.consolidate.ee;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.NamedStoredProcedureQuery;
import javax.persistence.StoredProcedureParameter;
import javax.persistence.ParameterMode;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;
import org.jpos.ee.Accounts;

@Entity
@Table(name = "invoice_items")
@OptimisticLocking(type = OptimisticLockType.ALL)

@NamedStoredProcedureQuery(
	    name = "balance", 
	    procedureName = "balance", 
	    resultClasses = InvoiceItems.class
	)

@NamedStoredProcedureQuery(
	    name = "balanceplus", 
	    procedureName = "balanceplus", 
	    resultClasses = InvoiceItems.class
	)

@NamedStoredProcedureQuery(
	    name = "balancemines", 
	    procedureName = "balanceplusmines", 
	    resultClasses = InvoiceItems.class
	)

public class InvoiceItems implements Serializable {

	private BigInteger record_id;
	private String id;
	private String type;
	private String invoice_id;
	private Accounts account_record_id;
	private Tenant tenant_record_id;
	private String description;
	private BigDecimal amount;
	private Date created_date;
	private String usage_name;
	private String plan_name;
	private String account_id;
	private String phase_name;
	private Date start_date;
	private Date end_date;
	private BigDecimal current_balance;
	private String external_key;
	private Integer trx;
	private BigInteger items;
//	private Batch batch;
//	
//	
//	public Batch getBatch() {
//		return batch;
//	}
//	public void setBatch(Batch batch) {
//		this.batch = batch;
//	}
	
	@Version
    private Long version;

	public Integer getTrx() {
		return trx;
	}
	public void setTrx(Integer trx) {
		this.trx = trx;
	}
	public String getPhase_name() {
		return phase_name;
	}
	public void setPhase_name(String phase_name) {
		this.phase_name = phase_name;
	}
	public String getAccount_id() {
		return account_id;
	}
	public void setAccount_id(String account_id) {
		this.account_id = account_id;
	}
	public synchronized BigInteger getRecord_id() {
		return record_id;
	}
	public synchronized void setRecord_id(BigInteger record_id) {
		this.record_id = record_id;
	}
	public  String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getInvoice_id() {
		return invoice_id;
	}
	public void setInvoice_id(String invoice_id) {
		this.invoice_id = invoice_id;
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public Date getCreated_date() {
		return created_date;
	}
	public void setCreated_date(Date created_date) {
		this.created_date = created_date;
	}
	public String getUsage_name() {
		return usage_name;
	}
	public void setUsage_name(String usage_name) {
		this.usage_name = usage_name;
	}
	public String getPlan_name() {
		return plan_name;
	}
	public void setPlan_name(String plan_name) {
		this.plan_name = plan_name;
	}
	public Date getStart_date() {
		return start_date;
	}
	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}
	public Date getEnd_date() {
		return end_date;
	}
	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}
	public synchronized BigDecimal getCurrent_balance() {
		return current_balance;
	}
	public synchronized void setCurrent_balance(BigDecimal current_balance) {
		this.current_balance = current_balance;
	}
	public String getExternal_key() {
		return external_key;
	}
	public void setExternal_key(String external_key) {
		this.external_key = external_key;
	}

	public BigInteger getItems() {
		return items;
	}
	public void setItems(BigInteger items) {
		this.items = items;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
//	public Boolean getFree() {
//		return free;
//	}
//	public void setFree(Boolean free) {
//		this.free = free;
//	}
	
}
