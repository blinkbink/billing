package org.jpos.ee;

import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.Merchant;
import id.co.keriss.consolidate.ee.Mitra;
import id.co.keriss.consolidate.ee.Tenant;
import id.co.keriss.consolidate.ee.Userdata;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class Accounts extends Cloneable implements Serializable {
	private long record_id;
    private String id;
	private String external_key;
    private String email;
    private String name;
    private String address1;
    private String company_name;
    private Date created_date;
    private Date updated_date;
    private String phone;
    private Tenant tenant_record_id;
    private BigDecimal notif_balance;
    private Boolean notif_email;
    private Boolean send_notif_balance;
//    private String subscription;
//    private Integer bill_period;
//    
//	public Integer getBill_period() {
//		return bill_period;
//	}
//	public void setBill_period(Integer bill_period) {
//		this.bill_period = bill_period;
//	}
//	public String getSubscription() {
//		return subscription;
//	}
//	public void setSubscription(String subscription) {
//		this.subscription = subscription;
//	}
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCompany_name() {
		return company_name;
	}
	public void setCompany_name(String company_name) {
		this.company_name = company_name;
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
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	public Tenant getTenant_record_id() {
		return tenant_record_id;
	}
	public void setTenant_record_id(Tenant tenant_record_id) {
		this.tenant_record_id = tenant_record_id;
	}
	public BigDecimal getNotif_balance() {
		return notif_balance;
	}
	public void setNotif_balance(BigDecimal notif_balance) {
		this.notif_balance = notif_balance;
	}
	public Boolean getNotif_email() {
		return notif_email;
	}
	public void setNotif_email(Boolean notif_email) {
		this.notif_email = notif_email;
	}
	public Boolean getSend_notif_balance() {
		return send_notif_balance;
	}
	public void setSend_notif_balance(Boolean send_notif_balance) {
		this.send_notif_balance = send_notif_balance;
	}

}

