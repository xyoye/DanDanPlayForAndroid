package com.xyoye.subtitle.info;

public class Style {
	
	private static int styleCounter;

	/**
	 * Constructor that receives a String to use a its identifier
	 * 
	 * @param styleName = identifier of this style
	 */
	public Style(String styleName) {
		this.iD = styleName;
	}
	
	/**
	 * Constructor that receives a String with the new styleName and a style to copy
	 * 
	 * @param styleName
	 * @param style
	 */
	public Style(String styleName, Style style) {
		this.iD = styleName;
		this.font = style.font;
		this.fontSize = style.fontSize;
		this.color = style.color;
		this.backgroundColor = style.backgroundColor;
		this.textAlign = style.textAlign;
		this.italic = style.italic;
		this.underline = style.underline;
		this.bold = style.bold;
		
	}

	/* ATTRIBUTES */
	public String iD;
	public String font;
	public String fontSize;
	/**colors are stored as 8 chars long RGBA*/
	public String color;
	public String backgroundColor;
	public String textAlign = "";
	
	public boolean italic;
	public boolean bold;
	public boolean underline;
	
	/* METHODS */
	
	/**
	 * To get the string containing the hex value to put into color or background color
	 * 
	 * @param format supported: "name", "&HBBGGRR", "&HAABBGGRR", "decimalCodedBBGGRR", "decimalCodedAABBGGRR"
	 * @param value RRGGBBAA string
	 * @return
	 */
	public static String getRGBValue(String format, String value){
		String color = null;
		if (format.equalsIgnoreCase("name")){
			//standard color format from W3C
			if (value.equals("transparent"))
				color = "00000000";
			else if (value.equals("black"))
				color = "000000ff";
			else if (value.equals("silver"))
				color = "c0c0c0ff";
			else if (value.equals("gray"))
				color = "808080ff";
			else if (value.equals("white"))
				color = "ffffffff";
			else if (value.equals("maroon"))
				color = "800000ff";
			else if (value.equals("red"))
				color = "ff0000ff";
			else if (value.equals("purple"))
				color = "800080ff";
			else if (value.equals("fuchsia"))
				color = "ff00ffff";
			else if (value.equals("magenta"))
				color = "ff00ffff ";
			else if (value.equals("green"))
				color = "008000ff";
			else if (value.equals("lime"))
				color = "00ff00ff";
			else if (value.equals("olive"))
				color = "808000ff";
			else if (value.equals("yellow"))
				color = "ffff00ff";
			else if (value.equals("navy"))
				color = "000080ff";
			else if (value.equals("blue"))
				color = "0000ffff";
			else if (value.equals("teal"))
				color = "008080ff";
			else if (value.equals("aqua"))
				color = "00ffffff";
			else if (value.equals("cyan"))
				color = "00ffffff ";
		} else if (format.equalsIgnoreCase("&HBBGGRR")){
			//hex format from SSA
			if (value.length() == 3){
				color = value.substring(3) +
						value.substring(2, 3) +
						value.substring(1, 2) +
						"ff";
			} else {
				color = value.substring(6) +
						value.substring(4, 5) +
						value.substring(2, 3) +
						"ff";
			}
		} else if (format.equalsIgnoreCase("&HAABBGGRR")){
			//hex format from ASS
			StringBuilder sb = new StringBuilder();
			sb.append(value.substring(8));
			sb.append(value, 6, 7);
			sb.append(value, 4, 5);
			sb.append(value, 2, 3);
			color = sb.toString();
		} else if (format.equalsIgnoreCase("decimalCodedBBGGRR")){
			//normal format from SSA
			color = Integer.toHexString(Integer.parseInt(value));
			//any missing 0s are filled in
			while(color.length()<6)color="0"+color;
			//order is reversed
			color = color.substring(4)+color.substring(2,4)+color.substring(0,2)+"ff";
		}  else if (format.equalsIgnoreCase("decimalCodedAABBGGRR")){
			//normal format from ASS
			color = Long.toHexString(Long.parseLong(value));
			//any missing 0s are filled in
			while(color.length()<8)color="0"+color;
			//order is reversed
			color = color.substring(6)+color.substring(4,6)+color.substring(2,4)+color.substring(0,2);
		}
		 return color;
	}

	public static String defaultID() {
		return "default"+styleCounter++;
	}

    @Override
    public String toString() {
        return "Style{" +
                "id='" + iD + '\'' +
                ", font='" + font + '\'' +
                ", fontSize='" + fontSize + '\'' +
                ", color='" + color + '\'' +
                ", backgroundColor='" + backgroundColor + '\'' +
                ", textAlign='" + textAlign + '\'' +
                ", italic=" + italic +
                ", bold=" + bold +
                ", underline=" + underline +
                '}';
    }
}
