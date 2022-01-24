/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.fingerprint.utils;


import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.gen.utils.Misc;

/**
 *
 * @author IPSSI
 */
public class SampleUtils {
    
	public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;
    public static final int SECONDS_PER_DAY = (HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);
    public static final long   DAY_MILLISECONDS = SECONDS_PER_DAY * 1000L;
    private static final Pattern TIME_SEPARATOR_PATTERN = Pattern.compile(":");
    private static final Pattern date_ptrn1 = Pattern.compile("^\\[\\$\\-.*?\\]");
    private static final Pattern date_ptrn2 = Pattern.compile("^\\[[a-zA-Z]+\\]");
    private static final Pattern date_ptrn3a = Pattern.compile("[yYmMdDhHsS]");
    private static final Pattern date_ptrn3b = Pattern.compile("^[\\[\\]yYmMdDhHsS\\-T/,. :\"\\\\]+0*[ampAMP/]*$");
    private static final Pattern date_ptrn4 = Pattern.compile("^\\[([hH]+|[mM]+|[sS]+)\\]");
    private static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");
    private static int lastFormatIndex = -1;
    private static String lastFormatString = null;
    private static boolean cached = false;
	private static ArrayList<String> dateFormatStrListYYYY =  new ArrayList<String>(Arrays.asList(
			"yyyy-MM-dd HH:mm",
			"yyyy.MM.dd HH:mm",
			"yyyy/MM/dd HH:mm",
			"yyyy.MM.dd",
			"yyyy-MM-dd",
			"yyyy/MM/dd",
			"yyyy-MMM-dd HH:mm",
			"yyyy-MMM-dd"
			));
	private static ArrayList<String> dateFormatStrListDD =  new ArrayList<String>(Arrays.asList(
			"dd/MM/yyyy HH:mm",
			"dd-MM-yyyy HH:mm",
			"dd.MM.yyyy HH:mm",
			"dd/MM/yyyy",
			"dd-MM-yyyy",
			"dd.MM.yyyy",
			"dd/MM/yy HH:mm",
			"dd-MM-yy HH:mm",
			"dd.MM.yy HH:mm",
			"dd/MM/yy",
			"dd-MM-yy",
			"dd.MM.yy",
			"dd-MMM-yyyy HH:mm",
			"dd-MMM-yyyy",
			"dd-MMM-yy"
			
			));
	
	private static ArrayList<String> dateFormatStrListYYYYNew =  new ArrayList<String>(Arrays.asList(
			"yyyy-MM-dd HH:mm:ss",
			"yyyy.MM.dd HH:mm",
			"yyyy/MM/dd HH:mm",
			"yyyy.MM.dd",
			"yyyy-MM-dd",
			"yyyy/MM/dd",
			"yyyy-MMM-dd HH:mm",
			"yyyy-MMM-dd"
			));
	private static ArrayList<String> dateFormatStrListDDNew =  new ArrayList<String>(Arrays.asList(
			"dd/MM/yyyy HH:mm:ss",
			"dd-MM-yyyy HH:mm",
			"dd.MM.yyyy HH:mm",
			"dd/MM/yyyy",
			"dd-MM-yyyy",
			"dd.MM.yyyy",
			"dd/MM/yy HH:mm",
			"dd-MM-yy HH:mm",
			"dd.MM.yy HH:mm",
			"dd/MM/yy",
			"dd-MM-yy",
			"dd.MM.yy",
			"dd-MMM-yyyy HH:mm",
			"dd-MMM-yyyy",
			"dd-MMM-yy"
			
			));
	
	static	public int getVehicleId(Connection conn,String vehName){
		int vehId = Misc.UNDEF_VALUE;
		if(vehName == null || vehName.length() < 4)
			return vehId;
		try{
			vehName = vehName.split("\\(")[0];
			if(vehName.length() > 0){
				String vehStdName = CacheTrack.standardizeName(vehName);
				vehId = CacheTrack.VehicleSetup.getSetupByStdName(vehStdName, conn);
				if(Misc.isUndef(vehId))
					vehId = CacheTrack.VehicleSetup.getSetupByStdName(vehStdName+"IP", conn);
				if(Misc.isUndef(vehId))
					vehId = CacheTrack.VehicleSetup.getSetupByStdName(vehStdName+"IPSSI", conn);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return vehId;
	}

	public static boolean isLikeDateAndTimePattern(String str){
		boolean isValid = false;
		if (str != null && str.length() > 0) {
			String expression = "^([0-9]{4})(-(?:1[0-2]|0?[1-9])(-(?:3[01]|[12][0-9]|0[1-9])?)?)(\\s+((?:2[0-3]|[01][0-9])(:(?:[0-5][0-9])?)?)$)?";  
			CharSequence inputStr = str;  
			Pattern pattern = Pattern.compile(expression);  
			Matcher matcher = pattern.matcher(inputStr);  
			if(matcher.matches()){  
				isValid = true;  
			} 
		}
		return isValid;
	}
	public static boolean haveSpecialChar(String str){
		boolean isValid = false;

		if (str != null && str.length() > 0) { 
			Pattern p = Pattern.compile("[^a-zA-Z0-9]");
			return p.matcher(str).find();
		}
		return isValid;
	}
	public static Date getUtilDateForYYYYMMDDHHmmss(String dateTime){
		return new Date(getLongDateForYYYYMMDDHHmmss( dateTime));
	}
	
	public static long getLongDateForYYYYMMDDHHmmss(String dateTime){
		//20151227090646
		Calendar cal = Calendar.getInstance().getInstance();

		int year = (Misc.getParamAsInt(dateTime.substring(0,4)));
		int month = (Misc.getParamAsInt(dateTime.substring(4,6)));
		int dat = Misc.getParamAsInt(dateTime.substring(6,8));
		int hour = Misc.getParamAsInt(dateTime.substring(8,10));
		int min = Misc.getParamAsInt(dateTime.substring(10,12));
		int sec = Misc.getParamAsInt(dateTime.substring(12));
		if (Misc.isUndef(year) || Misc.isUndef(month) || Misc.isUndef(dat) || Misc.isUndef(hour) || Misc.isUndef(min) || Misc.isUndef(sec))
			return Misc.getUndefInt();
		//		       year += 2000;
		//long date = new Date(year,month,dat,hour,min,sec).getTime();
		cal.set(year, month-1, dat, hour, min, sec);
		// Date datedd = cal.getTime();
		return (cal.getTime().getTime());
	}
	public static boolean isNumeric(String number){  
		boolean isValid = false;  
		String expression = "[-+]?[0-9]*\\.?[0-9]+$";  
		CharSequence inputStr = number;  
		Pattern pattern = Pattern.compile(expression);  
		Matcher matcher = pattern.matcher(inputStr);  
		if(matcher.matches()){  
			isValid = true;  
		}  
		return isValid;  
	}  
	public static Date getDateFromStr(String dateStr){
		return getDateFromStr(dateStr,null);
	}
	public static Date getDateFromStrDo(String dateStr){
		return getDateFromStrDo(dateStr,null);
	}
	public static Date getDateFromStr(String dateStr,Date undef){
		//check if begins with 4 digits
		if (dateStr == null || dateStr.length() <= 0)
			return undef;
		if(isNumeric(dateStr) && Misc.getParamAsDouble(dateStr) <= 55001.0)
			return getDateFromNumericVal(dateStr, undef);
		int cnt = 0;
		for (int i=0,is=dateStr.length();i<is;i++) {
			if (Character.isDigit(dateStr.charAt(i))) {
				cnt++;
			}
			else
				break;
		}
		boolean beginsWithYear = cnt == 4;
		SimpleDateFormat formater = null;
		ArrayList<String> dateFormatList = beginsWithYear ? dateFormatStrListYYYY : dateFormatStrListDD;
		for(String formatStr : dateFormatList){
			try{
				formater = new SimpleDateFormat(formatStr);
				return formater.parse(dateStr);
			}catch(Exception e1){
			}
		
		}
		return undef;
	}
	
	public static Date getDateFromStrDo(String dateStr,Date undef){
		//check if begins with 4 digits
		if (dateStr == null || dateStr.length() <= 0)
			return undef;
		if(isNumeric(dateStr) && Misc.getParamAsDouble(dateStr) <= 55001.0)
			return getDateFromNumericVal(dateStr, undef);
		int cnt = 0;
		for (int i=0,is=dateStr.length();i<is;i++) {
			if (Character.isDigit(dateStr.charAt(i))) {
				cnt++;
			}
			else
				break;
		}
		boolean beginsWithYear = cnt == 4;
		SimpleDateFormat formater = null;
		ArrayList<String> dateFormatList = beginsWithYear ? dateFormatStrListYYYYNew : dateFormatStrListDDNew;
		for(String formatStr : dateFormatList){
			try{
				formater = new SimpleDateFormat(formatStr);
				return formater.parse(dateStr);
			}catch(Exception e1){
			}
		
		}
		return undef;
	}
	private static Date getDateFromNumericVal(String dateStr,Date undef){
		Date retval = undef;
		double val = Misc.getParamAsDouble(dateStr,0.0);
		if(val >= 0.0){
			//val -= 25569.0; // days from 1970 - java base date; note: excel base date-1900
			retval = getJavaDate(val);
		}
		return retval;
	}
	private static long getMillisecondsFromDoubleVal(double day){
		return (long) (day * 24 * 60 * 60 * 1000);
	}
	private static long getTimeFromNumericVal(String timeStr,long undef){
		long retval = undef;
		double val = Misc.getParamAsDouble(timeStr,0.0);
		if(val >= 0.0){
			val = val - Math.floor(val);
			retval = getMillisecondsFromDoubleVal(val);
			//retval = Math.round(val*60*60*1000);
		}
		return retval;
	}
	public static long getTimeFromStr(String timeStr,long undef){
		boolean valueFind = false;
		if (timeStr == null || timeStr.length() < 4)
			return undef;
		if(timeStr.length()%2 == 1)
			timeStr = "0" + timeStr;
		if(timeStr.contains(":") || timeStr.contains("-"))
		{
			String[] tempArray = timeStr.contains(":") ? timeStr.split(":") : timeStr.split("-");
			int size = tempArray.length;
			for(int i=0;i<size;i++){
				undef = undef + Math.round(Misc.getParamAsInt(tempArray[i])*(Math.pow(60.0, (size-1)-i))*1000);
			}
			valueFind = true;
		}
		else if(timeStr.indexOf(".") == -1){ 
			timeStr = addPadding(timeStr, 6);
			int size = timeStr.length();
			for(int i=0;i<size;i+=2){
				int st = i;
				int en = i+2;
				if(en >= size)
					en = size;
				String eleStr = timeStr.substring(st, en);
				undef = undef + Math.round(Misc.getParamAsInt(eleStr)*(Math.pow(60.0, ((size-1)-i)/2))*1000);
			}
			valueFind = true;
		}
		if(!valueFind && isNumeric(timeStr))
			undef = getTimeFromNumericVal(timeStr, undef);
		return undef;
	}
	public static void main(String[] args) {
		System.out.println(getDateFromStr("30.06.2015"));
	}
    
    public static Date getJavaDate(double date, TimeZone tz) {
       return getJavaDate(date, false, tz);
    }
    public static Date getJavaDate(double date) {
        return getJavaDate(date, (TimeZone)null);
    }
    public static Date getJavaDate(double date, boolean use1904windowing, TimeZone tz) {
        return getJavaCalendar(date, use1904windowing, tz, false).getTime();
    }
    public static Date getJavaDate(double date, boolean use1904windowing, TimeZone tz, boolean roundSeconds) {
        return getJavaCalendar(date, use1904windowing, tz, roundSeconds).getTime();
    }
    public static Date getJavaDate(double date, boolean use1904windowing) {
        return getJavaCalendar(date, use1904windowing, null, false).getTime();
    }
    public static void setCalendar(Calendar calendar, int wholeDays,
            int millisecondsInDay, boolean use1904windowing, boolean roundSeconds) {
        int startYear = 1900;
        int dayAdjust = -1; // Excel thinks 2/29/1900 is a valid date, which it isn't
        if (use1904windowing) {
            startYear = 1904;
            dayAdjust = 1; // 1904 date windowing uses 1/2/1904 as the first day
        }
        else if (wholeDays < 61) {
            // Date is prior to 3/1/1900, so adjust because Excel thinks 2/29/1900 exists
            // If Excel date == 2/29/1900, will become 3/1/1900 in Java representation
            dayAdjust = 0;
        }
        calendar.set(startYear,0, wholeDays + dayAdjust, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, millisecondsInDay);
        if (roundSeconds) {
            calendar.add(Calendar.MILLISECOND, 500);
            calendar.clear(Calendar.MILLISECOND);
        }
    }
    public static Calendar getJavaCalendar(double date) {
        return getJavaCalendar(date, false, (TimeZone)null, false);
    }
    public static Calendar getJavaCalendar(double date, boolean use1904windowing) {
        return getJavaCalendar(date, use1904windowing, (TimeZone)null, false);
    }
    public static Calendar getJavaCalendarUTC(double date, boolean use1904windowing) {
    	return getJavaCalendar(date, use1904windowing, TIMEZONE_UTC, false);
    }
    public static Calendar getJavaCalendar(double date, boolean use1904windowing, TimeZone timeZone) {
        return getJavaCalendar(date, use1904windowing, timeZone, false);
    }
    public static Calendar getJavaCalendar(double date, boolean use1904windowing, TimeZone timeZone, boolean roundSeconds) {
        if (!isValidExcelDate(date)) {
            return null;
        }
        int wholeDays = (int)Math.floor(date);
        int millisecondsInDay = (int)((date - wholeDays) * DAY_MILLISECONDS + 0.5);
        Calendar calendar;
        if (timeZone != null) {
            calendar = new GregorianCalendar(timeZone);
        } else {
            calendar = new GregorianCalendar();     // using default time-zone
        }
        setCalendar(calendar, wholeDays, millisecondsInDay, use1904windowing, roundSeconds);
        return calendar;
    }

    public static synchronized boolean isADateFormat(int formatIndex, String formatString) {
       
         if (formatString != null && formatIndex == lastFormatIndex && formatString.equals(lastFormatString)) {
           		return cached;
         }
        // First up, is this an internal date format?
        if(isInternalDateFormat(formatIndex)) {
            lastFormatIndex = formatIndex;
            lastFormatString = formatString;
            cached = true;
            return true;
        }

        // If we didn't get a real string, it can't be
        if(formatString == null || formatString.length() == 0) {
            lastFormatIndex = formatIndex;
            lastFormatString = formatString;
            cached = false;
            return false;
        }
        String fs = formatString;
        StringBuilder sb = new StringBuilder(fs.length());
        for (int i = 0; i < fs.length(); i++) {
            char c = fs.charAt(i);
            if (i < fs.length() - 1) {
                char nc = fs.charAt(i + 1);
                if (c == '\\') {
                    switch (nc) {
                        case '-':
                        case ',':
                        case '.':
                        case ' ':
                        case '\\':
                            // skip current '\' and continue to the next char
                            continue;
                    }
                } else if (c == ';' && nc == '@') {
                    i++;
                    // skip ";@" duplets
                    continue;
                }
            }
            sb.append(c);
        }
        fs = sb.toString();
        if(date_ptrn4.matcher(fs).matches()){
            lastFormatIndex = formatIndex;
            lastFormatString = formatString;
            cached = true;
            return true;
        }

        // If it starts with [$-...], then could be a date, but
        //  who knows what that starting bit is all about
        fs = date_ptrn1.matcher(fs).replaceAll("");
        // If it starts with something like [Black] or [Yellow],
        //  then it could be a date
        fs = date_ptrn2.matcher(fs).replaceAll("");
        // You're allowed something like dd/mm/yy;[red]dd/mm/yy
        //  which would place dates before 1900/1904 in red
        // For now, only consider the first one
        if(fs.indexOf(';') > 0 && fs.indexOf(';') < fs.length()-1) {
           fs = fs.substring(0, fs.indexOf(';'));
        }

        // Ensure it has some date letters in it
        // (Avoids false positives on the rest of pattern 3)
        if (! date_ptrn3a.matcher(fs).find()) {
           return false;
        }
        
        // If we get here, check it's only made up, in any case, of:
        //  y m d h s - \ / , . : [ ] T
        // optionally followed by AM/PM

        boolean result = date_ptrn3b.matcher(fs).matches();
        lastFormatIndex = formatIndex;
        lastFormatString = formatString;
        cached = result;
        return result;
    }
    public static boolean isInternalDateFormat(int format) {
            switch(format) {
                // Internal Date Formats as described on page 427 in
                // Microsoft Excel Dev's Kit...
                case 0x0e:
                case 0x0f:
                case 0x10:
                case 0x11:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x2d:
                case 0x2e:
                case 0x2f:
                    return true;
            }
       return false;
    }
    public static boolean isCellDateFormatted(Cell cell) {
        if (cell == null) return false;
        boolean bDate = false;

        double d = cell.getNumericCellValue();
        if ( isValidExcelDate(d) ) {
            CellStyle style = cell.getCellStyle();
            if(style==null) return false;
            int i = style.getDataFormat();
            String f = style.getDataFormatString();
            bDate = isADateFormat(i, f);
        }
        return bDate;
    }
    public static boolean isCellInternalDateFormatted(Cell cell) {
        if (cell == null) return false;
        boolean bDate = false;
        double d = cell.getNumericCellValue();
        if ( isValidExcelDate(d) ) {
            CellStyle style = cell.getCellStyle();
            int i = style.getDataFormat();
            bDate = isInternalDateFormat(i);
        }
        return bDate;
    }

    public static boolean isValidExcelDate(double value)
    {
        return (value > -Double.MIN_VALUE);
    }
    protected static int absoluteDay(Calendar cal, boolean use1904windowing)
    {
        return cal.get(Calendar.DAY_OF_YEAR)
               + daysInPriorYears(cal.get(Calendar.YEAR), use1904windowing);
    }

    private static int daysInPriorYears(int yr, boolean use1904windowing)
    {
        if ((!use1904windowing && yr < 1900) || (use1904windowing && yr < 1900)) {
            throw new IllegalArgumentException("'year' must be 1900 or greater");
        }
        int yr1  = yr - 1;
        int leapDays =   yr1 / 4   // plus julian leap days in prior years
                       - yr1 / 100 // minus prior century years
                       + yr1 / 400 // plus years divisible by 400
                       - 460;      // leap days in previous 1900 years

        return 365 * (yr - (use1904windowing ? 1904 : 1900)) + leapDays;
    }

    @SuppressWarnings("serial")
    private static final class FormatException extends Exception {
        public FormatException(String msg) {
            super(msg);
        }
    }
    public static double convertTime(String timeStr) {
        try {
            return convertTimeInternal(timeStr);
        } catch (FormatException e) {
            String msg = "Bad time format '" + timeStr
                + "' expected 'HH:MM' or 'HH:MM:SS' - " + e.getMessage();
            throw new IllegalArgumentException(msg);
        }
    }
    private static double convertTimeInternal(String timeStr) throws FormatException {
        int len = timeStr.length();
        if (len < 4 || len > 8) {
            throw new FormatException("Bad length");
        }
        String[] parts = TIME_SEPARATOR_PATTERN.split(timeStr);

        String secStr;
        switch (parts.length) {
            case 2: secStr = "00"; break;
            case 3: secStr = parts[2]; break;
            default:
                throw new FormatException("Expected 2 or 3 fields but got (" + parts.length + ")");
        }
        String hourStr = parts[0];
        String minStr = parts[1];
        int hours = parseInt(hourStr, "hour", HOURS_PER_DAY);
        int minutes = parseInt(minStr, "minute", MINUTES_PER_HOUR);
        int seconds = parseInt(secStr, "second", SECONDS_PER_MINUTE);
        double totalSeconds = seconds + (minutes + (hours) * 60) * 60;
        return totalSeconds / (SECONDS_PER_DAY);
    }
    public static Date parseYYYYMMDDDate(String dateStr) {
        try {
            return parseYYYYMMDDDateInternal(dateStr);
        } catch (FormatException e) {
            String msg = "Bad time format " + dateStr
                + " expected 'YYYY/MM/DD' - " + e.getMessage();
            throw new IllegalArgumentException(msg);
        }
    }
    private static Date parseYYYYMMDDDateInternal(String timeStr) throws FormatException {
        if(timeStr.length() != 10) {
            throw new FormatException("Bad length");
        }

        String yearStr = timeStr.substring(0, 4);
        String monthStr = timeStr.substring(5, 7);
        String dayStr = timeStr.substring(8, 10);
        int year = parseInt(yearStr, "year", Short.MIN_VALUE, Short.MAX_VALUE);
        int month = parseInt(monthStr, "month", 1, 12);
        int day = parseInt(dayStr, "day", 1, 31);

        Calendar cal = new GregorianCalendar(year, month-1, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    private static int parseInt(String strVal, String fieldName, int rangeMax) throws FormatException {
        return parseInt(strVal, fieldName, 0, rangeMax-1);
    }

    private static int parseInt(String strVal, String fieldName, int lowerLimit, int upperLimit) throws FormatException {
        int result;
        try {
            result = Integer.parseInt(strVal);
        } catch (NumberFormatException e) {
            throw new FormatException("Bad int format '" + strVal + "' for " + fieldName + " field");
        }
        if (result < lowerLimit || result > upperLimit) {
            throw new FormatException(fieldName + " value (" + result
                    + ") is outside the allowable range(0.." + upperLimit + ")");
        }
        return result;
    }
    public static String addPadding(String str, int length)
    {
    	int count = 0;
    	if(str == null)
    		str = "";
    	count = length - str.length();
    	if (count > 0)
    	{
    		for (int i = 0; i < count; i++)
    			str = "0" + str;
    	}
    	return str;
    }

    
}
