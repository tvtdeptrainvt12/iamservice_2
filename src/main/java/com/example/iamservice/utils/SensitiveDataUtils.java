package com.example.iamservice.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveDataUtils {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("\"password\"\\s*:\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE);
    public static String maskPassword(String requestBody){
        if(requestBody == null){
            return null;
        }
        Matcher matcher = PASSWORD_PATTERN.matcher(requestBody);
        return matcher.replaceAll("\"password\":\"*****\"");
    }
}
