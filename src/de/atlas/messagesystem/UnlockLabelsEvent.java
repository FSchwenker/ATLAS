package de.atlas.messagesystem;

import java.util.EventObject;

public class UnlockLabelsEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean unlock; 
	public UnlockLabelsEvent( Object source, boolean unlock) 
	  { 
	    super( source ); 
	    this.unlock = unlock; 
	  } 
	public boolean isUnlocked(){
		return unlock;
	}
}
