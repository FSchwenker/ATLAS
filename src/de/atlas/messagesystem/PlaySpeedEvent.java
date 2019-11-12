package de.atlas.messagesystem;

import java.util.EventObject;

public class PlaySpeedEvent extends EventObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private double speed;
	public PlaySpeedEvent(Object source, double speed)
	  { 
	    super( source ); 
	    this.speed = speed;
	  } 
	public double getSpeedFactor(){
		return this.speed;
	}
}
