package de.atlas.messagesystem;

import java.util.EventObject;

public class TimeEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long time; 
	public TimeEvent( Object source, long val) 
	  { 
	    super( source ); 
	    this.time = val; 
	  } 
	public long getTime(){
		return this.time;
	}
}
