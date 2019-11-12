package de.atlas.messagesystem;

import java.util.EventObject;

public class ClassChangedEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ClassChangedEvent( Object source) 
	  { 
	    super( source ); 
	    
	  } 	
}
