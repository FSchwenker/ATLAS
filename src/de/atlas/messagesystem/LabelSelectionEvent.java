package de.atlas.messagesystem;

import java.util.EventObject;

import de.atlas.data.LabelObject;

public class LabelSelectionEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private LabelObject prev; 
	private LabelObject label;
	private LabelObject post; 
	
	public LabelSelectionEvent( Object source, LabelObject prev ,LabelObject label, LabelObject post) 
	  { 
	    super( source ); 
	    this.prev = prev;
	    this.label = label;
	    this.post = post;
	  } 
	public LabelObject getLabelObject(){
		return label;
	}
	public LabelObject getPrevObject(){
		return prev;
	}
	public LabelObject getPostObject(){
		return post;
	}
	
}
