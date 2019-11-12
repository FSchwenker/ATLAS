package de.atlas.messagesystem;

import java.util.EventObject;

import de.atlas.collections.ObjectLine;

public class MinMaxChangedEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ObjectLine ol; 
	public MinMaxChangedEvent( Object source, ObjectLine ol) 
	  { 
	    super( source ); 
	    this.ol = ol; 
	  } 
	public ObjectLine getObjectLine(){
		return ol;
	}
}
