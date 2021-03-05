package com.xyoye.subtitle.info;

public class Caption {
	
	public Style style;
	
	public Time start;
	public Time end;

    /**
     * Raw content, before cleaning up templates and markup.
     */
	public String rawContent="";
    /**
     * Cleaned-up subtitle content.
     */
	public String content="";

	public String endglishContent = "";

    @Override
    public String toString() {
        return "Caption{" +
                start + ".." + end +
                ", " + (style != null ? style.iD : null) + ": " + content +
                '}';
    }
}
