package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.math.BigDecimal;
import java.util.Date;


/**
 * Transaction generated by hbm2java
 */
public class TransactionDetail  implements java.io.Serializable {


	 private Long id;
     private Transaction transaction;
     private String bit48;
     private String bit56;
     private String bit62;
     private String bit63;
     


    public TransactionDetail() {
    }
  	public String getBit48() {
		return bit48;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setBit48(String bit48) {
		this.bit48 = bit48;
	}

	public String getBit56() {
		return bit56;
	}

	public void setBit56(String bit56) {
		this.bit56 = bit56;
	}

	public String getBit62() {
		return bit62;
	}

	public void setBit62(String bit62) {
		this.bit62 = bit62;
	}

	public String getBit63() {
		return bit63;
	}

	public void setBit63(String bit63) {
		this.bit63 = bit63;
	}




	public Transaction getTransaction() {
		return transaction;
	}


	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	


}


