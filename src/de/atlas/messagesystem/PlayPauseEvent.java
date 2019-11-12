package de.atlas.messagesystem;

import java.util.EventObject;

public class PlayPauseEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean looping; 
	public PlayPauseEvent( Object source, boolean looping) 
	  { 
	    super( source ); 
	    this.looping = looping; 
	  } 
	public boolean isLooping(){
		return this.looping;
	}
}
