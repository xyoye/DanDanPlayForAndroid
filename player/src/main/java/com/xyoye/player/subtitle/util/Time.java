package com.xyoye.player.subtitle.util;

public class Time {
	
	/**
	 * Constructor to create a time object.
	 * 
	 * @param format supported formats: "hh:mm:ss,ms", "h:mm:ss.cs" and "h:m:s:f/fps"
	 * @param value  string in the correct format
	 */
	public Time(String format, String value){
		if (format.equalsIgnoreCase("hh:mm:ss,ms")){
			// this type of format:  01:02:22,501 (used in .SRT)
			int h, m, s, ms;
			h = Integer.parseInt(value.substring(0, 2));
			m = Integer.parseInt(value.substring(3, 5));
			s = Integer.parseInt(value.substring(6, 8));
			ms = Integer.parseInt(value.substring(9, 12));
			
			mseconds = ms + s*1000 + m*60000 + h*3600000;
			
		} else if (format.equalsIgnoreCase("h:mm:ss.cs")){
			// this type of format:  1:02:22.51 (used in .ASS/.SSA) 
			int h =0, m = 0, s = 0, cs = 0;
			String[] hms = value.split(":");
			try {
				h = Integer.parseInt(hms[0]);
				m = Integer.parseInt(hms[1]);
				s = Integer.parseInt(hms[2].substring(0, 2));
				cs = Integer.parseInt(hms[2].substring(3, 5));
			}catch (NumberFormatException e){
				try {
					String[] values = value.split(".");
					if (values.length > 0)
						cs = Integer.parseInt(value.split(".")[1]);
					else
						cs = Integer.parseInt(value);
				}catch (Exception ignore){
				}
			}

			mseconds = cs*10 + s*1000 + m*60000 + h*3600000;
		} else if (format.equalsIgnoreCase("h:m:s:f/fps")){
			int h, m, s, f;
			float fps;
			String[] args = value.split("/");
			fps = Float.parseFloat(args[1]);
			args = args[0].split(":");
			h = Integer.parseInt(args[0]);
			m = Integer.parseInt(args[1]);
			s = Integer.parseInt(args[2]);
			f = Integer.parseInt(args[3]);

			mseconds = (int)(f*1000/fps) + s*1000 + m*60000 + h*3600000;
		}
	}

	// in an integer we can store 24 days worth of milliseconds, no need for a long
	protected int mseconds;

    public int getMseconds() {
        return mseconds;
    }

    public void setMseconds(int mseconds) {
        this.mseconds = mseconds;
    }

    /* METHODS */
	
	/**
	 * Method to return a formatted value of the time stored
	 * 
	 * @return formatted time in a string
	 */
	protected String getTime(String format) {
		//we use string builder for efficiency
		StringBuilder time = new StringBuilder();
		String aux;
		if(format.equalsIgnoreCase("hh:mm:ss,ms")){
			// this type of format:  01:02:22,501 (used in .SRT)
			int h, m, s, ms;
			h =  mseconds/3600000;
			aux = String.valueOf(h);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(':');
			m = (mseconds/60000)%60;
			aux = String.valueOf(m);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(':');
			s = (mseconds/1000)%60;
			aux = String.valueOf(s);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(',');
			ms = mseconds%1000;
			aux = String.valueOf(ms);
			if (aux.length()==1) time.append("00");
			else if (aux.length()==2) time.append('0');
			time.append(aux);
			
		} else if(format.equalsIgnoreCase("h:mm:ss.cs")){
			// this type of format:  1:02:22.51 (used in .ASS/.SSA)
			int h, m, s, cs;
			h =  mseconds/3600000;
			aux = String.valueOf(h);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(':');
			m = (mseconds/60000)%60;
			aux = String.valueOf(m);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(':');
			s = (mseconds/1000)%60;
			aux = String.valueOf(s);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append('.');
			cs = (mseconds/10)%100;
			aux = String.valueOf(cs);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			
		} else if (format.startsWith("hhmmssff/")){
			//this format is used in EBU's STL
			int h, m, s, f;
			float fps;
			String[] args = format.split("/");
			fps = Float.parseFloat(args[1]);
			//now we concatenate time
			h =  mseconds/3600000;
			aux = String.valueOf(h);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			m = (mseconds/60000)%60;
			aux = String.valueOf(m);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			s = (mseconds/1000)%60;
			aux = String.valueOf(s);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			f = (mseconds%1000)*(int)fps/1000;
			aux = String.valueOf(f);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			
		} else if (format.startsWith("h:m:s:f/")){
			//this format is used in EBU's STL
			int h, m, s, f;
			float fps;
			String[] args = format.split("/");
			fps = Float.parseFloat(args[1]);
			//now we concatenate time
			h =  mseconds/3600000;
			aux = String.valueOf(h);
			//if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(':');
			m = (mseconds/60000)%60;
			aux = String.valueOf(m);
			//if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(':');
			s = (mseconds/1000)%60;
			aux = String.valueOf(s);
			//if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(':');
			f = (mseconds%1000)*(int)fps/1000;
			aux = String.valueOf(f);
			//if (aux.length()==1) time.append('0');
			time.append(aux);
		} else if (format.startsWith("hh:mm:ss:ff/")){
			//this format is used in SCC
			int h, m, s, f;
			float fps;
			String[] args = format.split("/");
			fps = Float.parseFloat(args[1]);
			//now we concatenate time
			h =  mseconds/3600000;
			aux = String.valueOf(h);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(':');
			m = (mseconds/60000)%60;
			aux = String.valueOf(m);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(':');
			s = (mseconds/1000)%60;
			aux = String.valueOf(s);
			if (aux.length()==1) time.append('0');
			time.append(aux);
			time.append(':');
			f = (mseconds%1000)*(int)fps/1000;
			aux = String.valueOf(f);
			if (aux.length()==1) time.append('0');
			time.append(aux);
		}

		return time.toString();
	}

    /**
     * ASS/SSA time format.
     * @return ASS/SSA time format.
     */
    @Override
    public String toString() {
        return getTime("h:mm:ss.cs");
    }

}
