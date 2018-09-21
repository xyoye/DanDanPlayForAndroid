package com.player.subtitle.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * These objects can (should) only be created through the implementations of parseFile() in the {@link subtitleFile.TimedTextFileFormat} interface
 * They are an object representation of a subtitle file and contain all the captions and associated styles.
 * <br><br>
 * Copyright (c) 2012 J. David Requejo <br>
 * j[dot]david[dot]requejo[at] Gmail
 * <br><br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * <br><br>
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 * <br><br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * 
 * @author J. David Requejo
 *
 */
public class TimedTextObject {
	
	/*
	 * Attributes
	 * 
	 */
	//meta info
	public String title = "";
	public String description = "";
	public String copyrigth = "";
	public String author = "";
	public String fileName = "";
	public String language = "";
	
	//list of styles (id, reference)
	public Hashtable<String, Style> styling;
	
	//list of layouts (id, reference)
	public Hashtable<String, Region> layout;
	
	//list of captions (begin time, reference)
	//represented by a tree map to maintain order
	public TreeMap<Integer, Caption> captions;
	
	//to store non fatal errors produced during parsing
	public String warnings;
	
	//**** OPTIONS *****
	//to know whether file should be saved as .ASS or .SSA
	public boolean useASSInsteadOfSSA = true;
	//to delay or advance the subtitles, parsed into +/- milliseconds
	public int offset = 0;
	
	//to know if a parsing method has been applied
	public boolean built = false;
	
	
	/**
	 * Protected constructor so it can't be created from outside
	 */
	protected TimedTextObject(){
		
		styling = new Hashtable<>();
		layout = new Hashtable<>();
		captions = new TreeMap<>(); 
		
		warnings = "List of non fatal errors produced during parsing:\n\n";
		
	}
	
	
	/*
	 * Writing Methods
	 * 
	 */
	/**
	 * Method to generate the .SRT file
	 * 
	 * @return an array of strings where each String represents a line
	 */
	public String[] toSRT(){
		return new FormatSRT().toFile(this);
	}

	
	/**
	 * Method to generate the .ASS file
	 * 
	 * @return an array of strings where each String represents a line
	 */
	public String[] toASS(){
		return new FormatASS().toFile(this);
	}
	
	/**
	 * Method to generate the .STL file
	 */
	public byte[] toSTL(){
		return new FormatSTL().toFile(this);
	}
	
	/**
	 * Method to generate the .SCC file
	 * @return 
	 */
	public String[] toSCC(){
		return new FormatSCC().toFile(this);
	}
	
	/**
	 * Method to generate the .XML file
	 * @return 
	 */
	public String[] toTTML(){
		return new FormatTTML().toFile(this);
	}
	
	/* 
	 * PROTECTED METHODS 
	 * 
	 */
	
	/**
	 * This method simply checks the style list and eliminate any style not referenced by any caption
	 * This might come useful when default styles get created and cover too much.
	 * It require a unique iteration through all captions.
	 * 
	 */
	protected void cleanUnusedStyles(){
		//here all used styles will be stored
		Hashtable<String, Style> usedStyles = new Hashtable<>();
		//we iterate over the captions
		Iterator<Caption> itrC = captions.values().iterator();
		while(itrC.hasNext()){
			//new caption
	    	Caption current = itrC.next();
	    	//if it has a style
	    	if(current.style != null){
	    		String iD = current.style.iD;
	    		//if we haven't saved it yet
	    		if(!usedStyles.containsKey(iD))
	    			usedStyles.put(iD, current.style);
	    	}
		}
		//we saved the used styles
		this.styling = usedStyles;
	}

}
