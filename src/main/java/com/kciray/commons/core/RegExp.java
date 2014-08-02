package com.kciray.commons.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExp {
    public static String findOne(String regExp, String str){
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()) {
            return matcher.group();
        }else{
            return null;
        }
    }
}
