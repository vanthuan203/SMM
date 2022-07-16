package com.nts.awspremium;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtils {
    public static long getLongTimeFromString(String dateTimeString,String format){
        SimpleDateFormat f = new SimpleDateFormat(format);
        try {
            Date d = f.parse(dateTimeString);
            long milliseconds = d.getTime();
            return milliseconds;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static String convertMMMtoMM(String mmm){
        try {
            if(mmm.contains("Jul")){
                return "07";
            }else if (mmm.contains("Feb")) {
                return "02";
            }else if(mmm.contains("Mar")){
                return "03";
            }else if(mmm.contains("Apr")){
                return "04";
            }else if(mmm.contains("May")){
                return "05";
            }else if(mmm.contains("Jun")){
                return "06";
            }else if(mmm.contains("Aug")){
                return "08";
            }else if(mmm.contains("Sep")){
                return "09";
            }else if(mmm.contains("Oct")){
                return "10";
            }else if(mmm.contains("Nov")){
                return "11";
            }else if(mmm.contains("Dec")){
                return "12";
            }

        } catch (Exception e) {
        }
        return "01";
    }
    public static String getProxyhost(String proxy){
        int indexport=proxy.indexOf(":");
        return proxy.substring(0,indexport)+"%";
    }
}
