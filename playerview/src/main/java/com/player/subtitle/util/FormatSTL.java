package com.player.subtitle.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;


/**
 * This class represents the .STL subtitle format
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
public class FormatSTL implements TimedTextFileFormat {

	public TimedTextObject parseFile(String fileName, InputStream is) throws IOException, FatalParsingException {
		return parseFile(fileName, is, Charset.defaultCharset());
	}

	public TimedTextObject parseFile(String fileName, InputStream is, Charset isCharset) throws IOException, FatalParsingException {

		TimedTextObject tto = new TimedTextObject();
		tto.fileName = fileName;

		byte [] gsiBlock = new byte [1024];
		byte [] ttiBlock = new byte [128];
		

		try {
			//we read the file
			//but first we create the possible styles
			createSTLStyles(tto);

			int bytesRead;
			//the GSI block is loaded
			bytesRead = is.read(gsiBlock);
			if (bytesRead<1024)
				//the file must contain at least a GSI block and a TTI block
				//this is a fatal parsing error.
				throw new FatalParsingException("The file must contain at least a GSI block");
			//CPC : code page number 0..2
			//DFC : disk format code 3..10
			//save the number of frames per second
			byte[] dfc = {gsiBlock[6],gsiBlock[7]};
			int fps = Integer.parseInt(new String(dfc));
			//DSC : Display Standard Code 11
			//CCT : Character Code Table number 12..13
			byte[] cct = {gsiBlock[12],gsiBlock[13]};
			int table = Integer.parseInt(new String(cct));
			//LC : Language Code 14..15
			//OPT : Original Programme Title 16..47
			byte[] opt = new byte [32];
			System.arraycopy(gsiBlock, 16, opt, 0, 32);
			String title = new String(opt);
			//OEP : Original Episode Title 48..79
			byte[] oet = new byte [32];
			System.arraycopy(gsiBlock, 48, oet, 0, 32);
			String episodeTitle = new String(oet);
			//TPT : Translated Programme Title 80..111
			//TEP : Translated Episode Title 112..143
			//TN : Translator's Name 144..175
			//TCD : Translators Contact Details 176..207
			//SLR : Subtitle List Reference code 208..223
			//CD : Creation Date 224..229
			//RD : Revision Date 230..235
			//RN : Revision Number 236..237
			//TNB : Total Number of TTI Blocks 238..242
			byte[] tnb = {gsiBlock[238],gsiBlock[239],gsiBlock[240],gsiBlock[241],gsiBlock[242]};
			int numberOfTTIBlocks = Integer.parseInt(new String(tnb).trim());
			//TNS : Total Number of Subtitles 243..247
			byte[] tns = {gsiBlock[243],gsiBlock[244],gsiBlock[245],gsiBlock[246],gsiBlock[247]};
			int numberOfSubtitles = Integer.parseInt(new String(tns).trim());
			//TNG : Total Number of Subtitle Groups 248..250
			//MNC : Max Number of characters in row 251..252
			//MNR : Max number of rows 253..254
			//TCS : Time Code: Status 255
			//TCP : Time Code: Start-of-Programme 256..263
			//TCF : Time Code: First In-Cue 264..271
			//TND : Total Number of Disks 272
			//DSN : Disk Sequence Number 273
			//CO : Country of Origin 274..276
			//PUB : Publisher 277..308
			//EN : Editor's Name 309..340
			//ECD : Editor's Contact Details 341..372
			// Spare bytes 373..447
			//UDA : User-Defined Area 448..1023

			//we add the title
			tto.title = (title.trim()+" "+episodeTitle.trim()).trim();
			//this checks the reference to the characters coding employed.
			if (table > 4 || table < 0)
				tto.warnings+="Invalid Character Code table number, corrupt data? will try to parse anyways assuming it is latin.\n\n";
			else if (table != 0)
				tto.warnings+="Only latin alphabet supported for import from STL, other languages may produce unexpected results.\n\n";

			int subtitleNumber = 0;
			boolean additionalText = false;
			Caption currentCaption = null;
			//the TTI blocks are read
			for (int i = 0; i < numberOfTTIBlocks; i++) {
				//the TTI block is loaded
				bytesRead = is.read(ttiBlock);
				if (bytesRead < 128){
					//unexpected end of file
					tto.warnings += "Unexpected end of file, "+i+" blocks read, expecting "+numberOfTTIBlocks+" blocks in total.\n\n";
					break;
				}

				//if we have additional text pending, we do not create a new caption
				if(!additionalText)
					currentCaption = new Caption();

				//SGN : Subtitle group number 0
				//SN : Subtitle Number 1..2
				int currentSubNumber = ttiBlock[1]+256*ttiBlock[2];
				if (currentSubNumber != subtitleNumber)
					//missing subtitle number?
					tto.warnings += "Unexpected subtitle number at TTI block "+i+". Parsing proceeds...\n\n";
				//EBN : Extension Block Number 3
				int ebn = ttiBlock[3];
                                if (ebn != -1 && ebn != -2){
					additionalText = true;
				} else if (ebn == -2){
					//EBN is UserData so Jump it.
					additionalText = false;
					continue;
				} else {
					additionalText = false;
				}
				//CS : Cumulative Status 4
				//TCI : Time Code In 5..8
				String startTime = ""+ttiBlock[5]+":"+ttiBlock[6]+":"+ttiBlock[7]+":"+ttiBlock[8];
				//TCO : Time Code Out 9..12
				String endTime = ""+ttiBlock[9]+":"+ttiBlock[10]+":"+ttiBlock[11]+":"+ttiBlock[12];;
				//VP : Vertical Position 13
				//JC : Justification Code 14
				int justification = ttiBlock[14];
				//0:none, 1:left, 2:centered, 3:right
				//CF : Comment Flag 15
				if (ttiBlock[15] == 0){
					//comments are ignored
					//TF : Text Field 16..112	
					byte[] textField = new byte [112];
					System.arraycopy(ttiBlock, 16, textField, 0, 112);

					if(additionalText)
						//if it is just additional text for the caption
						parseTextForSTL(currentCaption,textField,justification,tto);
					else {
						currentCaption.start = new Time("h:m:s:f/fps", startTime+"/"+fps);
						currentCaption.end = new Time("h:m:s:f/fps", endTime+"/"+fps);
						parseTextForSTL(currentCaption,textField,justification,tto);
					}
				}
				//we increase the subtitle number
				if(!additionalText)
					subtitleNumber++;

			}
			if(subtitleNumber != numberOfSubtitles)
				tto.warnings += "Number of parsed subtitles ("+subtitleNumber+") different from expected number of subtitles ("+numberOfSubtitles+").\n\n";

			//we close the reader
			is.close();


			tto.cleanUnusedStyles();

		} catch (Exception e){
			//format error
			e.printStackTrace();
			throw new FatalParsingException("Format error in the file, migth be due to corrupt data.\n"+e.getMessage());
		}

		tto.built = true;
		return tto;
	}



	public byte[] toFile(TimedTextObject tto) {
		
		//first we check if the TimedTextObject had been built, otherwise...
		if(!tto.built)
			return null;

		Caption currentC;
		
		byte [] gsiBlock = new byte [1024];
		byte [] ttiBlock = new byte [128];
		
		//we will store the whole binary file as a unique array
		byte [] file = new byte[1024+128*tto.captions.size()];
		
		//we build the GSI block
		byte [] extra = "850STL25.0110000".getBytes();
		System.arraycopy(extra, 0, gsiBlock, 0, extra.length);
		//then we add the title and fill the rest with blanks
		extra = (tto.title != null)? tto.title.getBytes(): tto.fileName.getBytes();		
		for (int i = 0; i < 224-16; i++) {
			if (i< extra.length)
				gsiBlock[i+16] = extra[i];
			else 
				gsiBlock[i+16] = 32;

		}
		//other info
		DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
		Date date = new Date();
		String aux = dateFormat.format(date);
		aux +=  aux + "00"; //revision number
		String aux2 =""+tto.captions.size();
		while (aux2.length()<5) aux2="0"+aux2;
		aux += aux2+aux2+"0013216100000000";
		//we add the time of first subtitle
		aux += tto.captions.get(tto.captions.firstKey()).start.getTime("hhmmssff/25");
		aux += "11OOO";
		extra = aux.getBytes();
		System.arraycopy(extra, 0, gsiBlock, 224, extra.length);
		//the rest is filled with blanks
		for (int i = 277; i < 1024; i++) {
				gsiBlock[i] = 32;
		}
		

		//we add the GSI block to our string representing the file
		System.arraycopy(gsiBlock,0,file,0,gsiBlock.length);
		
		//we iterate over the captions to create the TTI blocks
		Iterator<Caption> itrC = tto.captions.values().iterator();
		int subtitleNumber = 0;
		while(itrC.hasNext()){
			currentC = itrC.next();
			//SGN
			ttiBlock[0] = 0;
			//SN
			ttiBlock[1] = (byte) (subtitleNumber%256);
			ttiBlock[2] = (byte) (subtitleNumber/256);
			//EBN
			ttiBlock[3] = (byte) 0xff;
			//CS
			ttiBlock[4] = 0;
			//TCI
			String [] timeCode = currentC.start.getTime("h:m:s:f/25").split(":");
			ttiBlock[5] = Byte.parseByte(timeCode[0]);
			ttiBlock[6] = Byte.parseByte(timeCode[1]);
			ttiBlock[7] = Byte.parseByte(timeCode[2]);
			ttiBlock[8] = Byte.parseByte(timeCode[3]);
			//TCO
			timeCode = currentC.end.getTime("h:m:s:f/25").split(":");
			ttiBlock[9] = Byte.parseByte(timeCode[0]);
			ttiBlock[10] = Byte.parseByte(timeCode[1]);
			ttiBlock[11] = Byte.parseByte(timeCode[2]);
			ttiBlock[12] = Byte.parseByte(timeCode[3]);
			//VP
			ttiBlock[13] = 18;
			//JC
			if (currentC.style != null){
				if (currentC.style.textAlign.contains("left"))
					ttiBlock[14] = 1;
				else if (currentC.style.textAlign.contains("right"))
					ttiBlock[14] = 3;
			} else ttiBlock[14] = 2;
			//CF
			ttiBlock[15] = 0;
			//TF
			String[] lines = currentC.content.split("<br />");
			//we clean XML, span would be implemented here
			int pos = 16;
			for (int i = 0; i < lines.length; i++) 
				lines[i] = lines[i].replaceAll("\\<.*?\\>", "");
			//we code the style
			if (currentC.style != null){
				Style style = currentC.style;
				if(style.italic)
					ttiBlock[pos++]= (byte) 0x80;
				else ttiBlock[pos++]= (byte) 0x81;
				if(style.underline)
					ttiBlock[pos++]= (byte) 0x82;
				else ttiBlock[pos++]= (byte) 0x83;
				
				//colors
				String color = style.color.substring(0,6);
				if (color.equalsIgnoreCase("000000"))
						ttiBlock[pos++]= (byte) 0x00;
				else if (color.equalsIgnoreCase("0000ff"))
					ttiBlock[pos++]= (byte) 0x04;
				else if (color.equalsIgnoreCase("00ffff"))
					ttiBlock[pos++]= (byte) 0x06;
				else if (color.equalsIgnoreCase("00ff00"))
					ttiBlock[pos++]= (byte) 0x02;
				else if (color.equalsIgnoreCase("ff0000"))
					ttiBlock[pos++]= (byte) 0x01;
				else if (color.equalsIgnoreCase("ffff00"))
					ttiBlock[pos++]= (byte) 0x03;
				else if (color.equalsIgnoreCase("ff00ff"))
					ttiBlock[pos++]= (byte) 0x05;
				else ttiBlock[pos++]= (byte) 0x07;
	
			}
			
			//we code the text
			for (int i = 0; i < lines.length; i++) {
				
				char [] chars = lines[i].toCharArray();
				for (int j = 0; j < chars.length; j++) {
					//check the text is not too long
					if (pos >126)
						break;
					//check it is a supported char, else it is ignored
					if (chars[j]>=0x20 && chars[j]<=0x7f)
						ttiBlock[pos++]= (byte) chars[j];
				}
				
				if (i+1 < lines.length)
					ttiBlock[pos++]= (byte) 0x8A;
			}
			
			//we fill the rest with end characters
			while (pos < 128)
				ttiBlock[pos++]= (byte) 0x8F;
			
			
			//we add the TTI block to our string representing the file
			System.arraycopy(ttiBlock,0,file,1024+subtitleNumber*128,ttiBlock.length);
			ttiBlock = new byte [128];
			subtitleNumber++;
		}

		return file;
	}

	/* PRIVATEMETHODS */
	
	
	/**
	 * This method parses the text field taking into account STL control codes
	 * @param currentCaption
	 * @param textField
	 * @param justification 
	 */
	private void parseTextForSTL(Caption currentCaption, byte[] textField, int justification, TimedTextObject tto) {
		
		boolean italics = false;
		boolean underline = false;
		int diacritical_mark = 0;
		String color = "white";
		Style style;
		String text = "";
		
		//we go around the field in pair of bytes to decode them
		for (int i = 0; i < textField.length; i++) {
			
			if (textField[i] < 0){
				//first byte > 8 (4 bits)
				if (textField[i] <= -113){
					//we might be with a  control code
					if (i+1<textField.length && textField[i]==textField[i+1])
						i++; //if repeated skip one
					switch (textField[i]){
					case -128:
						italics = true;
						break;
					case -127:
						italics = false;
						break;
					case -126:
						underline = true;
						break;
					case -125:
						underline = false;
						break;
					case -124:
						//Boxing not supported
						break;
					case -123:
						//Boxing not supported
						break;
					case -118:
						//line break
						currentCaption.content+=text+"<br />"; //line could be trimmed here
						text = "";
						break;
					case -113:
						//end of caption
						currentCaption.content+=text; //line could be trimmed here
						text = "";
						//we check the style
						if(underline)
							color+="U";
						if(italics)
							color+="I";
						style = tto.styling.get(color);
						
						if (justification == 1){
							color+="L";
							if (tto.styling.get(color) == null){
								style = new Style (color,style);
								style.textAlign = "bottom-left";
								tto.styling.put(color, style);
							}else
								style = tto.styling.get(color);
						} else if (justification ==3){
							color+="R";
							if (tto.styling.get(color) == null){
								style = new Style (color,style);
								style.textAlign = "bottom-rigth";
								tto.styling.put(color, style);
							} else
								style = tto.styling.get(color);
						}
			
						//we save the style
						currentCaption.style = style;
						//and save the caption
						int key = currentCaption.start.mseconds;
						//in case the key is already there, we increase it by a millisecond, since no duplicates are allowed
						while (tto.captions.containsKey(key)) key++;
						tto.captions.put(key, currentCaption);
						//we end the loop
						i= textField.length;
						break;
					default:
						//non valid code...
					}
				} else if (textField[i] >= -64 && textField[i] <= -49){
                                    // diacritical characters
                                    diacritical_mark = textField[i];
				}
                                else {
                                    //other codes and non supported characters...
                                    //corresponds to the upper half of the character code table
                                }
			} else if(textField[i] < 32){
				//it is a teletext control code, only colors are supported
				if (i+1<textField.length && textField[i]==textField[i+1])
					i++; //if repeated skip one
				switch (textField[i]){
				case 7:
					color = "white";
					break;
				case 2:
					color = "green";
					break;
				case 4:
					color = "blue";
					break;
				case 6:
					color = "cyan";
					break;
				case 1:
					color = "red";
					break;
				case 3:
					color = "yellow";
					break;
				case 5:
					color = "magenta";
					break;
				case 0:
					color = "black";
					break;
				default:
					//non supported	
				}
				
			} else {
				//we have a supported character coded in the two bytes, range is from 0x20 to 0x7F
				byte[] x = {textField[i]};
                                String raw_string = new String(x);

                                if (diacritical_mark != 0) {
                                    if ((diacritical_mark == -62) && (textField[i] == 101)) raw_string = "é";
                                    else if ((diacritical_mark == -56) && (textField[i] == 105)) raw_string = "ï";
                                    else if ((diacritical_mark == -63) && (textField[i] == 97)) raw_string = "à";
                                    else if ((diacritical_mark == -56) && (textField[i] == 101)) raw_string = "ë";
                                    else if ((diacritical_mark == -61) && (textField[i] == 101)) raw_string = "ê";
                                    else if ((diacritical_mark == -63) && (textField[i] == 117)) raw_string = "ù";
                                    else if ((diacritical_mark == -61) && (textField[i] == 105)) raw_string = "î";
                                    else if ((diacritical_mark == -63) && (textField[i] == 101)) raw_string = "è";
                                    else if ((diacritical_mark == -61) && (textField[i] == 97)) raw_string = "â";
                                    else if ((diacritical_mark == -61) && (textField[i] == 111)) raw_string = "ô";
                                    else if ((diacritical_mark == -61) && (textField[i] == 117)) raw_string = "û";
                                    else if ((diacritical_mark == -53) && (textField[i] == 99)) raw_string = "ç";
                                    else if ((diacritical_mark == -56) && (textField[i] == 97)) raw_string = "ä";
                                    else if ((diacritical_mark == -56) && (textField[i] == 111)) raw_string = "ö";
                                    else if ((diacritical_mark == -56) && (textField[i] == 117)) raw_string = "ü";
                                    diacritical_mark = 0;
                                }
				text+=raw_string;
			}
			
		}	
		
	}
	
	private void createSTLStyles(TimedTextObject tto) {
		Style style;
		
		style = new Style("white");
		style.color = Style.getRGBValue("name", "white");
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
		
		style = new Style("black");
		style.color = Style.getRGBValue("name", "black");
		tto.styling.put(style.iD, style);
		
		style = new Style("blackU",style);
		style.underline = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("blackUI",style);
		style.italic = true;
		tto.styling.put(style.iD, style);
		
		style = new Style("blackI",style);
		style.underline = false;
		tto.styling.put(style.iD, style);
		
		
	}
	
}