package com.player.subtitle.util;

import org.mozilla.universalchardet.ReaderFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class that represents the .ASS and .SSA subtitle file format
 *
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
 * @author J. David REQUEJO
 *
 */
public class FormatASS implements TimedTextFileFormat {

	public TimedTextObject parseFile(File file) throws IOException {
		return parseFile(file, null);
	}

	public TimedTextObject parseFile(File file, Charset isCharset) throws IOException {

		TimedTextObject tto = new TimedTextObject();
		tto.fileName = file.getName();

		Caption caption = new Caption();
		Style style;

		//for the clock timer
		float timer = 100;

		//if the file is .SSA or .ASS
		boolean isASS = false;

		//variables to store the formats
		String [] styleFormat;
		String [] dialogueFormat;

		//first lets load the file
		//creating a reader with correct encoding
		InputStreamReader in= (InputStreamReader) ReaderFactory.createReaderFromFile(file);
		BufferedReader br = new BufferedReader(in);
		String line;
		int lineCounter = 0;
		try {
			//we scour the file
			line=br.readLine();
			lineCounter++;
			while (line!=null){
				line = line.trim();
				//we skip any line until we find a section [section name]
				if(line.startsWith("[")){
					//now we must identify the section
					if(line.equalsIgnoreCase("[Script info]")){
						//its the script info section section
						lineCounter++;
						line=br.readLine().trim();
						//Each line is scanned for useful info until a new section is detected
						while (!line.startsWith("[")){
							if(line.startsWith("Title:")) {
								String[] title = line.split(":");
								if (title.length > 1)
									tto.title = title[1].trim();
								else
									tto.title = "";
							}else if (line.startsWith("Original Script:")) {
								String[] author = line.split(":");
								if (author.length > 1)
									tto.author = author[1].trim();
								else
									tto.author = "";
							}else if (line.startsWith("Script Type:")){
								String[] version = line.split(":");
								if (version.length > 1){
									if(version[1].trim().equalsIgnoreCase("v4.00+"))
										isASS = true;
									else if(!version[1].trim().equalsIgnoreCase("v4.00"))
										tto.warnings+="Script version is older than 4.00, it may produce parsing errors.";
								}

							} else if (line.startsWith("Timer:")) {
								String[] time = line.split(":");
								if (time.length > 1)
									timer = Float.parseFloat(time[1].trim().replace(',', '.'));
							}
							//we go to the next line
							lineCounter++;
							line=br.readLine().trim();
						}

					} else if (line.equalsIgnoreCase("[v4 Styles]")
							|| line.equalsIgnoreCase("[v4 Styles+]")
							|| line.equalsIgnoreCase("[v4+ Styles]")){
						//its the Styles description section
						if(line.contains("+")&&isASS==false){
							//its ASS and it had not been noted
							isASS=true;
							tto.warnings+="ScriptType should be set to v4:00+ in the [Script Info] section.\n\n";
						}
						lineCounter++;
						line=br.readLine().trim();
						//the first line should define the format
						if(!line.startsWith("Format:")){
							//if not, we scan for the format.
							tto.warnings+="Format: (format definition) expected at line "+line+" for the styles section\n\n";
							while (!line.startsWith("Format:")){
								lineCounter++;
								line=br.readLine().trim();
							}
						}
						// we recover the format's fields
						String[] format = line.split(":");
						if (format.length > 1)
							styleFormat = format[1].trim().split(",");
						else
							styleFormat = new String[]{};
						lineCounter++;
						line=br.readLine().trim();
						// we parse each style until we reach a new section
						while (!line.startsWith("[")){
							//we check it is a style
							if (line.startsWith("Style:")){
								//we parse the style
								String[] styles = line.split(":");
								if (styles.length > 1){
									style = parseStyleForASS(line.split(":")[1].trim().split(","),styleFormat,lineCounter,isASS,tto.warnings);
									//and save the style
									tto.styling.put(style.iD, style);
								}
							}
							//next line
							lineCounter++;
							line=br.readLine().trim();
						}

					} else if (line.trim().equalsIgnoreCase("[Events]")){
						//its the events specification section
						lineCounter++;
						line=br.readLine().trim();
						tto.warnings+="Only dialogue events are considered, all other events are ignored.\n\n";
						//the first line should define the format of the dialogues
						if(!line.startsWith("Format:")){
							//if not, we scan for the format.
							tto.warnings+="Format: (format definition) expected at line "+line+" for the events section\n\n";
							while (!line.startsWith("Format:")){
								lineCounter++;
								line=br.readLine().trim();
							}
						}
						// we recover the format's fields
						String[] formats = line.split(":");
						if (formats.length > 1)
							dialogueFormat = line.split(":")[1].trim().split(",");
						else
							dialogueFormat = new String[]{};
						//next line
						lineCounter++;
						line=br.readLine().trim();
						// we parse each style until we reach a new section
						while (!line.startsWith("[")){
							//we check it is a dialogue
							//WARNING: all other events are ignored.
							if (line.startsWith("Dialogue:")){
								//we parse the dialogue
								String[] dialogs = line.split(":",2);
								if (dialogs.length > 1){
									caption = parseDialogueForASS(dialogs[1].trim().split(",",dialogueFormat.length),dialogueFormat,timer, tto);
									//and save the caption
									int key = caption.start.mseconds;
									//in case the key is already there, we increase it by a millisecond, since no duplicates are allowed
									while (tto.captions.containsKey(key)) key++;
									tto.captions.put(key, caption);
								}
							}
							//next line
							lineCounter++;
							line=br.readLine().trim();
						}

					} else if (line.trim().equalsIgnoreCase("[Fonts]") || line.trim().equalsIgnoreCase("[Graphics]")){
						//its the custom fonts or embedded graphics section
						//these are not supported
						tto.warnings+= "The section "+line.trim()+" is not supported for conversion, all information there will be lost.\n\n";
						line=br.readLine().trim();
					} else {
						tto.warnings+= "Unrecognized section: "+line.trim()+" all information there is ignored.";
						line=br.readLine().trim();
					}
				} else {
					line = br.readLine();
					lineCounter++;
				}
			}
			// parsed styles that are not used should be eliminated
			tto.cleanUnusedStyles();

		}  catch (NullPointerException e){
			tto.warnings+= "unexpected end of file, maybe last caption is not complete.\n\n";
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
		//the minimum size of the file is the number of captions and styles + lines for sections and formats and the script info, so we'll take some extra space.
		ArrayList<String> file = new ArrayList<>(30 + tto.styling.size() + tto.captions.size());

		//header is placed
		file.add(index++,"[Script Info]");
		//title next
		String title = "Title: ";
		if (tto.title == null || tto.title.isEmpty())
			title += tto.fileName;
		else title += tto.title;
		file.add(index++,title);
		//author next
		String author = "Original Script: ";
		if (tto.author == null || tto.author.isEmpty())
			author += "Unknown";
		else author += tto.author;
		file.add(index++,author);
		//additional info
		if (tto.copyrigth != null && !tto.copyrigth.isEmpty())
			file.add(index++,"; "+tto.copyrigth);
		if (tto.description != null && !tto.description.isEmpty())
			file.add(index++,"; "+tto.description);
		file.add(index++,"; Converted by the Online Subtitle Converter developed by J. David Requejo");
		//mandatory info
		if (tto.useASSInsteadOfSSA)
			file.add(index++,"Script Type: V4.00+");
		else file.add(index++,"Script Type: V4.00");
		file.add(index++,"Collisions: Normal");
		file.add(index++,"Timer: 100,0000");
		if (tto.useASSInsteadOfSSA)
			file.add(index++,"WrapStyle: 1");
		//an empty line is added
		file.add(index++,"");

		//Styles section
		if (tto.useASSInsteadOfSSA)
			file.add(index++,"[V4+ Styles]");
		else file.add(index++,"[V4 Styles]");
		//define the format
		if (tto.useASSInsteadOfSSA)
			file.add(index++,"Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding");
		else file.add(index++,"Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, TertiaryColour, BackColour, Bold, Italic, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, AlphaLevel, Encoding");
		//Next we iterate over the styles
		Iterator<Style> itrS = tto.styling.values().iterator();
		while(itrS.hasNext()){
			String styleLine = "Style: ";
			//new style
			Style current = itrS.next();
			//name
			styleLine+= current.iD+",";
			styleLine+= current.font+",";
			styleLine+= current.fontSize+",";
			styleLine+= getColorsForASS(tto.useASSInsteadOfSSA, current);
			styleLine+= getOptionsForASS(tto.useASSInsteadOfSSA, current);
			//BorderStyle, Outline, Shadow
			styleLine+= "1,2,2,";
			styleLine+= getAlignForASS(tto.useASSInsteadOfSSA, current.textAlign);
			//MarginL, MarginR, MarginV
			styleLine+= ",0,0,0,";
			//AlphaLevel
			if(!tto.useASSInsteadOfSSA)styleLine+= "0,";
			//Encoding
			styleLine+= "0";

			//and we add the style definition line
			file.add(index++,styleLine);
		}
		//an empty line is added
		file.add(index++,"");

		//Events section
		file.add(index++,"[Events]");
		//define the format
		if (tto.useASSInsteadOfSSA)
			file.add(index++,"Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text");
		else file.add(index++,"Format: Marked, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text");
		//Next we iterate over the captions
		Iterator<Caption> itrC = tto.captions.values().iterator();
		while(itrC.hasNext()){
			//for each caption
			String line = "Dialogue: 0,";
			//new caption
			Caption current = itrC.next();
			//offset is applied
			if(tto.offset != 0){
				current.start.mseconds += tto.offset;
				current.end.mseconds += tto.offset;
			}
			//start time
			line+= current.start.getTime("h:mm:ss.cs")+",";
			//end time
			line+= current.end.getTime("h:mm:ss.cs")+",";
			//offset is undone
			if(tto.offset != 0){
				current.start.mseconds -= tto.offset;
				current.end.mseconds -= tto.offset;
			}
			//style
			if (current.style != null)
				line+= current.style.iD;
			else
				line+="Default";
			//default margins are used, no name or effect is recognized
			line+=",,0000,0000,0000,,";

			//we add the caption text with \N as line breaks  and clean of XML
			line +=  current.content.replaceAll("<br />","\\\\N").replaceAll("<.*?>", "");
			//and we add the caption line
			file.add(index++,line);
		}
		//an empty line is added
		file.add(index++,"");

		//we return the expected file as an array of String
		String[] toReturn = new String [file.size()];
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = file.get(i);
		}
		return toReturn;
	}

	/* PRIVATEMETHODS */

	/**
	 * This methods transforms a format line from ASS according to a format definition into an Style object.
	 *
	 * @param line the format line without its declaration
	 * @param styleFormat the list of attributes in this format line
	 * @return a new Style object.
	 */
	private Style parseStyleForASS(String[] line, String[] styleFormat, int index, boolean isASS, String warnings) {

		Style newStyle = new Style(Style.defaultID());
		if (line.length != styleFormat.length){
			//both should have the same size
			warnings+="incorrectly formated line at "+index+"\n\n";
		} else {
			for (int i = 0; i < styleFormat.length; i++) {
				String trimmedStyleFormat = styleFormat[i].trim();
				//we go through every format parameter and save the interesting values
				if (trimmedStyleFormat.equalsIgnoreCase("Name")){
					//we save the name
					newStyle.iD=line[i].trim();
				} else if (trimmedStyleFormat.equalsIgnoreCase("Fontname")){
					//we save the font
					newStyle.font=line[i].trim();
				} else if (trimmedStyleFormat.equalsIgnoreCase("Fontsize")){
					//we save the size
					newStyle.fontSize=line[i].trim();
				}else if (trimmedStyleFormat.equalsIgnoreCase("PrimaryColour")){
					//we save the color
					String color =line[i].trim();
					if(isASS){
						if(color.startsWith("&H")) newStyle.color= Style.getRGBValue("&HAABBGGRR", color);
						else  newStyle.color= Style.getRGBValue("decimalCodedAABBGGRR", color);
					} else {
						if(color.startsWith("&H")) newStyle.color= Style.getRGBValue("&HBBGGRR", color);
						else  newStyle.color= Style.getRGBValue("decimalCodedBBGGRR", color);
					}
				}else if (trimmedStyleFormat.equalsIgnoreCase("BackColour")){
					//we save the background color
					String color =line[i].trim();
					if(isASS){
						if(color.startsWith("&H")) newStyle.backgroundColor= Style.getRGBValue("&HAABBGGRR", color);
						else  newStyle.backgroundColor= Style.getRGBValue("decimalCodedAABBGGRR", color);
					} else {
						if(color.startsWith("&H")) newStyle.backgroundColor= Style.getRGBValue("&HBBGGRR", color);
						else  newStyle.backgroundColor= Style.getRGBValue("decimalCodedBBGGRR", color);
					}
				}else if (trimmedStyleFormat.equalsIgnoreCase("Bold")){
					//we save if bold
					newStyle.bold=Boolean.parseBoolean(line[i].trim());
				}else if (trimmedStyleFormat.equalsIgnoreCase("Italic")){
					//we save if italic
					newStyle.italic=Boolean.parseBoolean(line[i].trim());
				}else if (trimmedStyleFormat.equalsIgnoreCase("Underline")){
					//we save if underlined
					newStyle.underline=Boolean.parseBoolean(line[i].trim());
				}else if (trimmedStyleFormat.equalsIgnoreCase("Alignment")){
					//we save the alignment
					int placement =Integer.parseInt(line[i].trim());
					if (isASS){
						switch(placement){
							case 1:
								newStyle.textAlign="bottom-left";
								break;
							case 2:
								newStyle.textAlign="bottom-center";
								break;
							case 3:
								newStyle.textAlign="bottom-right";
								break;
							case 4:
								newStyle.textAlign="mid-left";
								break;
							case 5:
								newStyle.textAlign="mid-center";
								break;
							case 6:
								newStyle.textAlign="mid-right";
								break;
							case 7:
								newStyle.textAlign="top-left";
								break;
							case 8:
								newStyle.textAlign="top-center";
								break;
							case 9:
								newStyle.textAlign="top-right";
								break;
							default:
								warnings+="undefined alignment for style at line "+index+"\n\n";
						}
					} else {
						switch(placement){
							case 9:
								newStyle.textAlign="bottom-left";
								break;
							case 10:
								newStyle.textAlign="bottom-center";
								break;
							case 11:
								newStyle.textAlign="bottom-right";
								break;
							case 1:
								newStyle.textAlign="mid-left";
								break;
							case 2:
								newStyle.textAlign="mid-center";
								break;
							case 3:
								newStyle.textAlign="mid-right";
								break;
							case 5:
								newStyle.textAlign="top-left";
								break;
							case 6:
								newStyle.textAlign="top-center";
								break;
							case 7:
								newStyle.textAlign="top-right";
								break;
							default:
								warnings+="undefined alignment for style at line "+index+"\n\n";
						}
					}
				}

			}
		}

		return newStyle;
	}

	/**
	 * This methods transforms a dialogue line from ASS according to a format definition into an Caption object.
	 *
	 * @param line the dialogue line without its declaration
	 * @param dialogueFormat the list of attributes in this dialogue line
	 * @param timer % to speed or slow the clock, above 100% span of the subtitles is reduced.
	 * @return a new Caption object
	 */
	private Caption parseDialogueForASS(String[] line, String[] dialogueFormat, float timer, TimedTextObject tto) {

		Caption newCaption = new Caption();

		for (int i = 0; i < dialogueFormat.length; i++) {
			String trimmedDialogueFormat = dialogueFormat[i].trim();
			//we go through every format parameter and save the interesting values
			if (trimmedDialogueFormat.equalsIgnoreCase("Style")){
				//we save the style
				Style s =  tto.styling.get(line[i].trim());
				if (s!=null)
					newCaption.style= s;
				else
					tto.warnings+="undefined style: "+line[i].trim()+"\n\n";
			} else if (trimmedDialogueFormat.equalsIgnoreCase("Start")){
				//we save the starting time
				newCaption.start=new Time("h:mm:ss.cs",line[i].trim());
			} else if (trimmedDialogueFormat.equalsIgnoreCase("End")){
				//we save the starting time
				newCaption.end=new Time("h:mm:ss.cs",line[i].trim());
			} else if (trimmedDialogueFormat.equalsIgnoreCase("Text")) {
				//we save the text
				String captionText = line[i];
				newCaption.rawContent = captionText;
				//text is cleaned before being inserted into the caption
				newCaption.content = captionText.replaceAll("\\{.*?}", "").replace("\n", "<br />").replace("\\N", "<br />");
			}
		}

		//timer is applied
		if (timer != 100){
			newCaption.start.mseconds /= (timer/100);
			newCaption.end.mseconds /= (timer/100);
		}
		return newCaption;
	}

	/**
	 * returns a string with the correctly formated colors
	 * @param useASSInsteadOfSSA true if formated for ASS
	 * @return the colors in the decimal format
	 */
	private String getColorsForASS(boolean useASSInsteadOfSSA, Style style) {
		String colors;
		if(useASSInsteadOfSSA)
			//primary color(BBGGRR) with Alpha level (00) in front + 00FFFFFF + 00000000 + background color(BBGGRR) with Alpha level (80) in front
			colors=Integer.parseInt("00"+ style.color.substring(4,6)+style.color.substring(2, 4)+style.color.substring(0, 2), 16)+",16777215,0,"+Long.parseLong("80"+ style.backgroundColor.substring(4,6)+style.backgroundColor.substring(2, 4)+style.backgroundColor.substring(0, 2), 16)+",";
		else {
			//primary color(BBGGRR) + FFFFFF + 000000 + background color(BBGGRR)
			String color = style.color.substring(4,6)+style.color.substring(2, 4)+style.color.substring(0, 2);
			String bgcolor = style.backgroundColor.substring(4,6)+style.backgroundColor.substring(2, 4)+style.backgroundColor.substring(0, 2);
			colors=Long.parseLong(color, 16)+",16777215,0,"+Long.parseLong(bgcolor, 16)+",";
		}
		return colors;
	}

	/**
	 * returns a string with the correctly formated options
	 * @param useASSInsteadOfSSA
	 * @return
	 */
	private String getOptionsForASS(boolean useASSInsteadOfSSA, Style style) {
		String options;
		if (style.bold)
			options="-1,";
		else
			options="0,";
		if (style.italic)
			options+="-1,";
		else
			options+="0,";
		if(useASSInsteadOfSSA){
			if (style.underline)
				options+="-1,";
			else
				options+="0,";
			options+="0,100,100,0,0,";
		}
		return options;
	}

	/**
	 * converts the string explaining the alignment into the ASS equivalent integer offering bottom-center as default value
	 * @param useASSInsteadOfSSA
	 * @param align
	 * @return
	 */
	private int getAlignForASS(boolean useASSInsteadOfSSA, String align) {
		if (useASSInsteadOfSSA){
			int placement = 2;
			if ("bottom-left".equals(align))
				placement = 1;
			else if ("bottom-center".equals(align))
				placement = 2;
			else if ("bottom-right".equals(align))
				placement = 3;
			else if ("mid-left".equals(align))
				placement = 4;
			else if ("mid-center".equals(align))
				placement = 5;
			else if ("mid-right".equals(align))
				placement = 6;
			else if ("top-left".equals(align))
				placement = 7;
			else if ("top-center".equals(align))
				placement = 8;
			else if ("top-right".equals(align))
				placement = 9;

			return placement;
		} else {

			int placement = 10;
			if ("bottom-left".equals(align))
				placement = 9;
			else if ("bottom-center".equals(align))
				placement = 10;
			else if ("bottom-right".equals(align))
				placement = 11;
			else if ("mid-left".equals(align))
				placement = 1;
			else if ("mid-center".equals(align))
				placement = 2;
			else if ("mid-right".equals(align))
				placement = 3;
			else if ("top-left".equals(align))
				placement = 5;
			else if ("top-center".equals(align))
				placement = 6;
			else if ("top-right".equals(align))
				placement = 7;

			return placement;
		}
	}

}
