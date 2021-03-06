package com.xyoye.subtitle.format;

import com.xyoye.subtitle.info.Caption;
import com.xyoye.subtitle.exception.FatalParsingException;
import com.xyoye.subtitle.info.Style;
import com.xyoye.subtitle.info.Time;
import com.xyoye.subtitle.info.TimedTextObject;

import org.mozilla.universalchardet.ReaderFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents the .SCC subtitle format
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
public class FormatSCC implements TimedTextFileFormat {

	public TimedTextObject parseFile(File file) throws IOException, FatalParsingException {
		return parseFile(file, null);
	}

	public TimedTextObject parseFile(File file, Charset isCharset) throws IOException, FatalParsingException {

		TimedTextObject tto = new TimedTextObject();
		Caption newCaption = null;
		
		//variables to represent a decoder
		String textBuffer = "";
		boolean isChannel1 = false;
		boolean isBuffered = true;
		
		//to store current style
		boolean underlined = false;
		boolean italics = false;
		String color = null;

		//first lets load the file
		//creating a reader with correct encoding
		Charset defaultCharset = Charset.forName("GBK");
		InputStreamReader in= (InputStreamReader) ReaderFactory.createReaderFromFile(file, defaultCharset);
		BufferedReader br = new BufferedReader(in);

		//the file name is saved
		tto.fileName = file.getName();
		tto.title = file.getName();

		String line;
		int lineCounter = 0;
		try {
			
			lineCounter++;
			//the file must start with the type declaration
			if (!br.readLine().trim().equalsIgnoreCase("Scenarist_SCC V1.0")){
				//this is a fatal parsing error.
				throw new FatalParsingException("The fist line should define the file type: \"Scenarist_SCC V1.0\"");
			} else {
				
				createSCCStyles(tto);
				
				tto.warnings+="Only data from CC channel 1 will be extracted.\n\n";
				line = br.readLine();

				while(line!=null){
					line = line.trim();
					lineCounter++;
					//if its not an empty line
					if(!line.isEmpty()){
						//we separate the time code from the VANC data
						String[] data = line.split("\t");
						Time currentTime = new Time("h:m:s:f/fps", data[0]+"/29.97");
						//we separate the words
						data = data[1].split(" ");
						for (int j = 0; j < data.length; j++) {
							//we get its hex value stored in a short
							int word = Integer.parseInt(data[j],16);

							// odd parity could be checked here

							//we eliminate the parity bits before decoding
							word &= 0x7f7f;

							// if it is a char:
							if ((word & 0x6000) != 0){
								//if we are in the right channel (1)
								if(isChannel1){
									//we extract the two chars
									byte c1 = (byte) ((word & 0xff00)>>>8);
									byte c2 = (byte) (word & 0x00ff);
									
									if (isBuffered){
										//we decode the byte and add it to the text buffer
										textBuffer += decodeChar(c1);
										//we decode the second char and add it, this one can be empty.
										textBuffer += decodeChar(c2);
									} else {
										//we decode the byte and add it to the text screen
										newCaption.content += decodeChar(c1);
										//we decode the second char and add it, this one can be empty.
										newCaption.content += decodeChar(c2);
									}
								}

							} else if (word==0x0000)
								// word 8080 is filler to add frames
								currentTime.mseconds+=1000/29.97;	
							else {
								//it is a control code
								if (j+1 < data.length && data[j].equals(data[j+1]))
									//if code is repeated, skip one.
									j++;

								// we check the channel
								if ((word&0x0800)==0){
									//we are on channel 1 or 3

									//we parse the code
									if ((word&0x1670)==0x1420){
										//it is a command code
										//we check the channel
										if ((word&0x0100)==0){
											//it is channel 1
											isChannel1 = true;
											//the command is decoded
											word&=0x000f;
											switch (word){
											case 0:
												//Resume Caption Loading: start pop on captions
												isBuffered = true;
												textBuffer = "";
												break;
											case 5:
											case 6:
											case 7:
												//roll-up caption by number of rows, effect not supported
												//clear text buffer
												textBuffer="";
												//clear screen text
												if (newCaption != null){
													newCaption.end = currentTime;
													String style = "";
													style += color;
													if (underlined) style +="U";
													if (italics) style += "I";
													newCaption.style = tto.styling.get(style);
													tto.captions.put(newCaption.start.mseconds, newCaption);
												}
												//new caption starts with roll up style
												newCaption = new Caption();
												newCaption.start = currentTime;
												//all characters and codes will be applied directly to the screen
												isBuffered = false;
												break;
											case 9:
												//Resume Direct Captioning: start paint-on captions
												isBuffered = false;
												newCaption = new Caption();
												newCaption.start = currentTime;
												break;
											case 12:
												//Erase Displayed Memory: clear screen text
												if (newCaption != null){
													newCaption.end = currentTime;
													if (newCaption.start != null){
														//we save the caption
														long key = newCaption.start.mseconds;
														//in case the key is already there, we increase it by a millisecond, since no duplicates are allowed
														while (tto.captions.containsKey(key)) key++;
														//we save the caption
														tto.captions.put(newCaption.start.mseconds, newCaption);
														//and reset the caption builder
														newCaption = new Caption();	
													}
												}
												break;
											case 14:
												//Erase Non-Displayed Memory: clear the text buffer
												textBuffer = "";
												break;
											case 15:
												//End of caption: Swap off-screen buffer with caption screen.
												newCaption = new Caption();
												newCaption.start = currentTime;
												newCaption.content += textBuffer;
												break;
											default:
												//unsupported or unrecognized command code
											}
											
										} else {
											isChannel1 = false;
										}

									} else if (isChannel1){
										if ((word&0x1040)==0x1040){
											//it is a preamble code, format is removed
											color = "white";
											underlined = false;
											italics = false;
											//it is a new line
											if (isBuffered && !textBuffer.isEmpty())
												textBuffer+="<br />";
											if (!isBuffered && !newCaption.content.isEmpty())
												newCaption.content+="<br />";
											if((word&0x0001)==1)
												//it is underlined
												underlined = true;
											//positioning is not supported, rows and columns are ignored
											if((word&0x0010)!=0x0010){
												//setting style for following text
												word&=0x000e;
												word = (short)(word>>1);
												switch (word){
												case 0:
													color = "white";
													break;
												case 1:
													color = "green";
													break;
												case 2:
													color = "blue";
													break;
												case 3:
													color = "cyan";
													break;
												case 4:
													color = "red";
													break;
												case 5:
													color = "yellow";
													break;
												case 6:
													color = "magenta";
													break;
												case 7:
													italics = true;
													break;
												default:
													//error!	
												}
											} else {
												color = "white";
											}
											

										} else if ((word&0x1770)==0x1120){
											//it is a midrow style code
											//it is underlined
											underlined = (word & 0x001) == 1;
											//setting style for text
											word&=0x000e;
											word = (short)(word>>1);
											switch (word){
											case 0:
												color = "white";
												italics = false;
												break;
											case 1:
												color = "green";
												italics = false;
												break;
											case 2:
												color = "blue";
												italics = false;
												break;
											case 3:
												color = "cyan";
												italics = false;
												break;
											case 4:
												color = "red";
												italics = false;
												break;
											case 5:
												color = "yellow";
												italics = false;
												break;
											case 6:
												color = "magenta";
												italics = false;
												break;
											case 7:
												italics = true;
												break;
											default:
												//error!	
											}
										} else if ((word&0x177c)==0x1720){
											//it is a tab code
											//positioning is not supported

										} else if ((word&0x1770)==0x1130){
											//it is a special character code
											word&=0x000f;
											//coded value is extracted
											if (isBuffered)
												//we decode the special char and add it to the text buffer
												textBuffer += decodeSpecialChar(word);
											 else 
												//we decode the special char and add it to the text
												 newCaption.content += decodeSpecialChar(word);
										} else if ((word&0x1660)==0x1220){
											//it is an extended character code
											word&=0x011f;
											//coded value is extracted
											if (isBuffered)
												//we decode the extended char and add it to the text buffer
												decodeXtChar(textBuffer, word);
											 else 
												//we decode the extended char and add it to the text
												decodeXtChar(newCaption.content, word);

										} else {
											//non recognized code
										}
									}
								} else {
									//we are on channel 2 or 4
									isChannel1 = false;
								}

							}
						}


					}
					// end of while
					line = br.readLine();

				}
				
				if(newCaption != null) {
					//we save any last shown caption
					newCaption.end = new Time("h:m:s:f/fps", "99:59:59:29/29.97");
				}
				if (newCaption.start != null){
					//we save the caption
					long key = newCaption.start.mseconds;
					//in case the key is already there, we increase it by a millisecond, since no duplicates are allowed
					while (tto.captions.containsKey(key)) key++;
					//we save the caption
					tto.captions.put(newCaption.start.mseconds, newCaption);
				}
				tto.cleanUnusedStyles();
			}

		}  catch (NullPointerException e){
			tto.warnings+= "unexpected end of file at line "+lineCounter+", maybe last caption is not complete.\n\n";
		} finally{
			//we close the reader
			in.close();
		}

		tto.built = true;
		return tto;
	}


	public String[] toFile(TimedTextObject tto) {
		
		//first we check if the TimedTextObject had been built, otherwise...
		if(!tto.built)
			return null;
		
		//we will write the lines in an ArrayList 
		int index = 0;
		//the minimum size of the file is double the number of captions since lines are double spaced.
		ArrayList<String> file = new ArrayList<>(20 + 2 * tto.captions.size());

		//first we add the header
		file.add(index++,"Scenarist_SCC V1.0\n");

		//line is to store the information to add to the file
		String line;
		//to store information about the captions
		Caption oldC;
		Caption newC = new Caption();
		newC.content = "";
		newC.end =  new Time("h:mm:ss.cs", "0:00:00.00");

		//Next we iterate over the captions
		Iterator<Caption> itrC = tto.captions.values().iterator();
		while(itrC.hasNext()){
			line = "";
			oldC = newC;
			newC = itrC.next();
			//if old caption ends after new caption starts
			if (oldC.end.mseconds > newC.start.mseconds ){
				//captions overlap
				newC.content+="<br />"+oldC.content;
				//we add the time to the new line, and clear old caption so both can now appear
				newC.start.mseconds-=1000/29.97;
				//we correct the frame delay (8080 8080)
				line+=newC.start.getTime("hh:mm:ss:ff/29.97")+"\t942c 942c ";
				newC.start.mseconds+=1000/29.97;
				//we clear the buffer and start new pop-on caption
				line+= "94ae 94ae 9420 9420 ";
				
			} else if (oldC.end.mseconds < newC.start.mseconds ){
				//we clear the screen for new caption
				line+=oldC.end.getTime("hh:mm:ss:ff/29.97")+"\t942c 942c\n\n";
				//we add the time to the new line, we clear buffer and start new caption
				newC.start.mseconds-=1000/29.97;
				//we correct the frame delay (8080 8080)
				line+=newC.start.getTime("hh:mm:ss:ff/29.97")+"\t94ae 94ae 9420 9420 ";
				newC.start.mseconds+=1000/29.97;
			} else {
				//we add the time to the new line, we clear screen and buffer and start new caption
				newC.start.mseconds-=1000/29.97;
				//we correct the frame delay (8080 8080)
				line+=newC.start.getTime("hh:mm:ss:ff/29.97")+"\t942c 942c 94ae 94ae 9420 9420 ";
				newC.start.mseconds+=1000/29.97;
			}
			
			//we add the coded caption text along with any styles to the off-screen buffer
			line += codeText(newC);
			//lastly we display the caption
			line += "8080 8080 942f 942f\n";
			
			//we add it to the "file"
			file.add(index++,line);

		}

		//an empty line is added
		file.add(index++,"");

		//we return the expected file as an array of String
		String[] toReturn = new String[file.size()];
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = file.get(i);
		}
		return toReturn;
	}
	
	/* PRIVATEMETHODS */
	
	/**
	 * INCOMPLETE METHOD: does not tab to correct position or applies styles
	 */
	private String codeText(Caption newC) {
		String toReturn = "";
		
		String[]lines = newC.content.split("<br />");
		
		int i = 0;
		int tab;
		//max 32 chars
		if(lines[i].length() > 32)
			lines[i] = lines[i].substring(0, 32);
		// we calculate tabs to center the text
		tab = (32-lines[i].length())/2;
		
		//we position the cursor with a preamble code
		//the row should be chosen according to how many lines left...
		toReturn+="1340 1340 ";
		//we tab over to the correct spot
		if(tab%4 != 0)
			//tab code should go here
			;
		
		//we add the caption style using midrow codes
		
		//we code the caption text
		toReturn+= codeChar(lines[i].toCharArray());

		if (lines.length > 1){
			//and next line
			i++;

			//max 32 chars
			if(lines[i].length() > 32)
				lines[i] = lines[i].substring(0, 32);
			// we calculate tabs to center the text
			tab = (32-lines[i].length())/2;

			//we position the cursor with a preamble code
			//the row should be chosen according to how many lines left...
			toReturn+="13e0 13e0 ";
			//we tab over to the correct spot
			if(tab%4 != 0)
				//tab code should go here
				;

			//we add the caption style using midrow codes

			//we code the caption text
			toReturn+= codeChar(lines[i].toCharArray());

			if (lines.length > 2){
				//and next line
				i++;

				//max 32 chars
				if(lines[i].length() > 32)
					lines[i] = lines[i].substring(0, 32);
				// we calculate tabs to center the text
				tab = (32-lines[i].length())/2;

				//we position the cursor with a preamble code
				toReturn+="9440 9440 ";
				//we tab over to the correct spot
				if(tab%4 != 0)
					//tab code should go here
					;
				//we add the caption style using midrow codes

				//we code the caption text
				toReturn+= codeChar(lines[i].toCharArray());

				if (lines.length > 3){
					//and next line
					i++;

					//max 32 chars
					if(lines[i].length() > 32)
						lines[i] = lines[i].substring(0, 32);
					// we calculate tabs to center the text
					tab = (32-lines[i].length())/2;

					//we position the cursor with a preamble code
					toReturn+="94e0 94e0 ";
					//we tab over to the correct spot
					if(tab%4 != 0)
						//tab code should go here
						;
					//we add the caption style using midrow codes

					//we code the caption text
					toReturn+= codeChar(lines[i].toCharArray());

				}
			}
		}

		return toReturn;
	}

	/**
	 * INCOMPLETE METHOD, does not consider special or extended chars
	 */
	private String codeChar(char[] chars) {
		StringBuilder toReturn = new StringBuilder();
		
		int i;
		for (i = 0; i < chars.length; i++) {
			switch (chars[i]){
			case ' ':
				toReturn.append("20");
				break;
			case '!':
				toReturn.append("a1");
				break;
			case '"':
				toReturn.append("a2");
				break;
			case '#':
				toReturn.append("23");
				break;
			case '$':
				toReturn.append("a4");
				break;
			case '%':
				toReturn.append("25");
				break;
			case '&':
				toReturn.append("26");
				break;
			case '\'':
				toReturn.append("a7");
				break;
			case '(':
				toReturn.append("a8");
				break;
			case ')':
				toReturn.append("29");
				break;
			case 'á':
				toReturn.append("2a");
				break;
			case '+':
				toReturn.append("ab");
				break;
			case ',':
				toReturn.append("2c");
				break;
			case '-':
				toReturn.append("ad");
				break;
			case '.':
				toReturn.append("ae");
				break;
			case '/':
				toReturn.append("2f");
				break;
			case '0':
				toReturn.append("b0");
				break;
			case '1':
				toReturn.append("31");
				break;
			case '2':
				toReturn.append("32");
				break;
			case '3':
				toReturn.append("b3");
				break;
			case '4':
				toReturn.append("34");
				break;
			case '5':
				toReturn.append("b5");
				break;
			case '6':
				toReturn.append("b6");
				break;
			case '7':
				toReturn.append("37");
				break;
			case '8':
				toReturn.append("38");
				break;
			case '9':
				toReturn.append("b9");
				break;
			case ':':
				toReturn.append("ba");
				break;
			case ';':
				toReturn.append("3b");
				break;
			case '<':
				toReturn.append("bc");
				break;
			case '=':
				toReturn.append("3d");
				break;
			case '>':
				toReturn.append("3e");
				break;
			case '?':
				toReturn.append("bf");
				break;
			case '@':
				toReturn.append("40");
				break;
			case 'A':
				toReturn.append("c1");
				break;
			case 'B':
				toReturn.append("c2");
				break;
			case 'C':
				toReturn.append("43");
				break;
			case 'D':
				toReturn.append("c4");
				break;
			case 'E':
				toReturn.append("45");
				break;
			case 'F':
				toReturn.append("46");
				break;
			case 'G':
				toReturn.append("c7");
				break;
			case 'H':
				toReturn.append("c8");
				break;
			case 'I':
				toReturn.append("49");
				break;
			case 'J':
				toReturn.append("4a");
				break;
			case 'K':
				toReturn.append("cb");
				break;
			case 'L':
				toReturn.append("4c");
				break;
			case 'M':
				toReturn.append("cd");
				break;
			case 'N':
				toReturn.append("ce");
				break;
			case 'O':
				toReturn.append("4f");
				break;
			case 'P':
				toReturn.append("d0");
				break;
			case 'Q':
				toReturn.append("51");
				break;
			case 'R':
				toReturn.append("52");
				break;
			case 'S':
				toReturn.append("d3");
				break;
			case 'T':
				toReturn.append("54");
				break;
			case 'U':
				toReturn.append("d5");
				break;
			case 'V':
				toReturn.append("d6");
				break;
			case 'W':
				toReturn.append("57");
				break;
			case 'X':
				toReturn.append("58");
				break;
			case 'Y':
				toReturn.append("d9");
				break;
			case 'Z':
				toReturn.append("da");
				break;
			case '[':
				toReturn.append("5b");
				break;
			case 'é':
				toReturn.append("dc");
				break;
			case ']':
				toReturn.append("5d");
				break;
			case 'í':
				toReturn.append("5e");
				break;
			case 'ó':
				toReturn.append("df");
				break;
			case 'ú':
				toReturn.append("e0");
				break;
			case 'a':
				toReturn.append("61");
				break;
			case 'b':
				toReturn.append("62");
				break;
			case 'c':
				toReturn.append("e3");
				break;
			case 'd':
				toReturn.append("64");
				break;
			case 'e':
				toReturn.append("e5");
				break;
			case 'f':
				toReturn.append("e6");
				break;
			case 'g':
				toReturn.append("67");
				break;
			case 'h':
				toReturn.append("68");
				break;
			case 'i':
				toReturn.append("e9");
				break;
			case 'j':
				toReturn.append("ea");
				break;
			case 'k':
				toReturn.append("6b");
				break;
			case 'l':
				toReturn.append("ec");
				break;
			case 'm':
				toReturn.append("6d");
				break;
			case 'n':
				toReturn.append("6e");
				break;
			case 'o':
				toReturn.append("ef");
				break;
			case 'p':
				toReturn.append("70");
				break;
			case 'q':
				toReturn.append("f1");
				break;
			case 'r':
				toReturn.append("f2");
				break;
			case 's':
				toReturn.append("73");
				break;
			case 't':
				toReturn.append("f4");
				break;
			case 'u':
				toReturn.append("75");
				break;
			case 'v':
				toReturn.append("76");
				break;
			case 'w':
				toReturn.append("f7");
				break;
			case 'x':
				toReturn.append("f8");
				break;
			case 'y':
				toReturn.append("79");
				break;
			case 'z':
				toReturn.append("7a");
				break;
			case 'ç':
				toReturn.append("fb");
				break;
			case '÷':
				toReturn.append("7c");
				break;
			case 'Ñ':
				toReturn.append("fd");
				break;
			case 'ñ':
				toReturn.append("fe");
				break;
			case '|':
				toReturn.append("7f");
				break;
				
			default:
				//error
				//it happens for strange chars, since it is not complete, they are replaced by spaces
				toReturn.append("7f");
				break;
			}
			if(i%2==1) toReturn.append(" ");
		}
		if(i%2==1) toReturn.append("80 ");
		
		return toReturn.toString();
	}


	private String decodeChar(byte c) {
		switch (c){
		case 42:
			return "�";
		case 92:
			return "é";
		case 94:
			return "í";
		case 95:
			return "ó";
		case 96:
			return "ú";
		case 123:
			return "ç";
		case 124:
			return "�";
		case 125:
			return "Ñ";
		case 126:
			return "ñ";
		case 127:
			return "|";
		case 0:
			//filler code
			return "";
		default:
			return Character.toString((char)c);
		}
	}
	
	
	private String decodeSpecialChar(int word) {
		switch(word){
		case 15:
			return"�";
		case 14:
			return "�";
		case 13:
			return "�";
		case 12:
			return "�";
		case 11:
			return "�";
		case 10:
			return "�";
		case 9:
			return "\u00A0";
		case 8:
			return "�";
		case 7:
			return "\u266A";
		case 6:
			return "�";
		case 5:
			return "�";
		case 4:
			return "�";
		case 3:
			return "�";
		case 2:
			return "�";
		case 1:
			return "�";
		case 0:
			return "�";
		default:
			//unrecoginzed code
			return "";
		}
	}
	
	private void decodeXtChar(String textBuffer, int word) {
		switch(word){
		
		}
	}
	
	private void createSCCStyles(TimedTextObject tto) {
		Style style;
		
		style = new Style("white");
		tto.styling.put(style.iD, style);
		
		style = new Style("whiteU",style);
		style.underline = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("whiteUI",style);
		style.italic = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("whiteI",style);
		style.underline = false;
		tto.styling.put(style.iD, style);
		
		style = new Style("green");
		style.color = Style.getRGBValue("name", "green");
		tto.styling.put(style.iD, style);
		
		style = new Style("greenU",style);
		style.underline = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("greenUI",style);
		style.italic = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("greenI",style);
		style.underline = false;
		tto.styling.put(style.iD, style);
		
		style = new Style("blue");
		style.color = Style.getRGBValue("name", "blue");
		tto.styling.put(style.iD, style);
		
		style = new Style("blueU",style);
		style.underline = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("blueUI",style);
		style.italic = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("blueI",style);
		style.underline = false;
		tto.styling.put(style.iD, style);
		
		style = new Style("cyan");
		style.color = Style.getRGBValue("name", "cyan");
		tto.styling.put(style.iD, style);
		
		style = new Style("cyanU",style);
		style.underline = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("cyanUI",style);
		style.italic = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("cyanI",style);
		style.underline = false;
		tto.styling.put(style.iD, style);
		
		style = new Style("red");
		style.color = Style.getRGBValue("name", "red");
		tto.styling.put(style.iD, style);
		
		style = new Style("redU",style);
		style.underline = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("redUI",style);
		style.italic = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("redI",style);
		style.underline = false;
		tto.styling.put(style.iD, style);
		
		style = new Style("yellow");
		style.color = Style.getRGBValue("name", "yellow");
		tto.styling.put(style.iD, style);
		
		style = new Style("yellowU",style);
		style.underline = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("yellowUI",style);
		style.italic = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("yellowI",style);
		style.underline = false;
		tto.styling.put(style.iD, style);
		
		style = new Style("magenta");
		style.color = Style.getRGBValue("name", "magenta");
		tto.styling.put(style.iD, style);
		
		style = new Style("magentaU",style);
		style.underline = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("magentaUI",style);
		style.italic = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("magentaI",style);
		style.underline = false;
		tto.styling.put(style.iD, style);
		
	}

}
