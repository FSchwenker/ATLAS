package de.atlas.messagesystem;

import java.util.EventObject;

import de.atlas.data.TimeType;

public class TimeTypeEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private TimeType time; 
	public TimeTypeEvent( Object source, TimeType e) 
	  { 
	    super( source ); 
	    this.time = e; 
	  } 
	public TimeType getTimeType(){
		return this.time;
	}
}
