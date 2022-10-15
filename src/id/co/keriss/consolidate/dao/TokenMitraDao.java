package id.co.keriss.consolidate.dao;

import java.util.List;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jpos.ee.DB;

import id.co.keriss.consolidate.ee.Alamat;
import id.co.keriss.consolidate.ee.Login;
import id.co.keriss.consolidate.ee.RefRegistrasi;
import id.co.keriss.consolidate.ee.TokenMitra;
import id.co.keriss.consolidate.ee.Userdata;
import id.co.keriss.consolidate.util.LogSystem;

public class TokenMitraDao {
    private Session session;

    public TokenMitraDao (DB db) {
        super ();
        this.session = db.session();
    }
    public TokenMitraDao (Session session) {
        super ();
        this.session = session;
    }
    
    public TokenMitra findById(Long id) throws HibernateException {
	    return (TokenMitra)session.load(TokenMitra.class, id);
    }
    
    public TokenMitra findByToken(String token){
	    try{
	    	String sql = "from TokenMitra where lower(token) = :token and status_aktif='true'";
	    	Query query= session.createQuery(sql);
	    	query.setString("token", token);
	    	return (TokenMitra) query.setMaxResults(1).uniqueResult();
	    }catch (Exception e) {
//	    	e.printStackTrace();
            LogSystem.error(getClass(), e, UUID.randomUUID().toString());

	    	return null;
	    	
	    }
  }
    
  public void update(TokenMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.update(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e, UUID.randomUUID().toString());

      }
    
    
  }
  
  public void create(TokenMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.save(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e, UUID.randomUUID().toString());

      }
    
    
  }
  
  public void delete(TokenMitra rr) {
      try {
        Transaction tx=session.beginTransaction();
        session.delete(rr);
        tx.commit();
      } catch (RuntimeException e) {
            LogSystem.error(getClass(), e, UUID.randomUUID().toString());

      }
    
    
  }
    
}
