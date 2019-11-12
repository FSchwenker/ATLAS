package de.atlas.messagesystem;

import java.util.EventObject;

public class VideoZoomEvent extends EventObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private double factor;
	public VideoZoomEvent(Object source, double val)
	  { 
	    super( source ); 
	    this.factor = val;
	  } 
	public double getFactor(){
		return factor;
	}
}
