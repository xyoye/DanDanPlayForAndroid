package com.player.subtitle.util;

public class Caption {
	
	public Style style;
	public Region region;
	
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

    @Override
    public String toString() {
        return "Caption{" +
                start + ".." + end +
                ", " + (style != null ? style.iD : null) + ", " + region + ": " + content +
                '}';
    }
}
