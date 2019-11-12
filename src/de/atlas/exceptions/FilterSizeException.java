package de.atlas.exceptions;

public class FilterSizeException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FilterSizeException(){
		super("invalid filtersize");
	}

	public FilterSizeException(String string) {
		super(string);
	}
}
