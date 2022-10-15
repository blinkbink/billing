package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.util.HashSet;
import java.util.Set;

/**
 * Terminal generated by hbm2java
 */
public class Terminal  implements java.io.Serializable {


     private Long id;
     private String tid;
     private String mcode;
     private Merchant merchant;
     private Edc edc;
     private Set batch = new HashSet(0);
     private boolean active;
     
     
    public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Terminal() {
    }

    public Terminal(String tid, Merchant merchant, Edc edc, Set batch) {
       this.tid = tid;
       this.merchant = merchant;
       this.edc = edc;
       this.batch = batch;
    }
   
    
    public String getMcode() {
		return mcode;
	}

	public void setMcode(String mcode) {
		this.mcode = mcode;
	}

	public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    public String getTid() {
        return this.tid;
    }
    
    public void setTid(String tid) {
        this.tid = tid;
    }
    public Merchant getMerchant() {
        return this.merchant;
    }
    
    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }
    public Edc getEdc() {
        return this.edc;
    }
    
    public void setEdc(Edc edc) {
        this.edc = edc;
    }
    public Set getBatch() {
        return this.batch;
    }
    
    public void setBatch(Set batch) {
        this.batch = batch;
    }




}


