package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA


import java.util.HashSet;
import java.util.Set;

/**
 * Terminal generated by hbm2java
 */
public class Wl_Provinces  implements java.io.Serializable {

    private String id;
    private String name;

    public Wl_Provinces(){
    	
    }
    
    public Wl_Provinces(String id, String name) {
       this.id = id;
       this.name = name;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
}


