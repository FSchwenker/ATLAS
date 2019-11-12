package de.atlas.messagesystem;

import java.util.EventObject;

public class ZoomEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private double zoom; 
	public ZoomEvent( Object source, double val) 
	  { 
	    super( source ); 
	    this.zoom = val; 
	  } 
	public double getZoom(){
		return zoom;
	}
}
