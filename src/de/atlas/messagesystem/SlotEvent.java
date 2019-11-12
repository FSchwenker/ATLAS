package de.atlas.messagesystem;

import java.util.EventObject;

public class SlotEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int start; 
	public SlotEvent( Object source, int val) 
	  { 
	    super( source ); 
	    this.start = val; 
	  } 
	public int getStart(){
		return start;
	}
}
