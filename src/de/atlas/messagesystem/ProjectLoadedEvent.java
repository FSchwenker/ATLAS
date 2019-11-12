package de.atlas.messagesystem;

import java.util.EventObject;

public class ProjectLoadedEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fileName;	
	public ProjectLoadedEvent( Object source, String fileName) 
	  { 
	    super( source ); 	
	    this.fileName = fileName;
	  } 
	public String getFileName(){
		return this.fileName;
	}
}
