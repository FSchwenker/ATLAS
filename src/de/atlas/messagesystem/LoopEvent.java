package de.atlas.messagesystem;

import java.util.EventObject;

import de.atlas.data.LabelObject;

public class LoopEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private LabelObject activeLabel;
	
	public LoopEvent( Object source, LabelObject al) 
	  { 
	    super( source ); 
	    this.activeLabel=al;
	  } 
	public LabelObject getActiveLabel(){
		return this.activeLabel;
	}	
}
