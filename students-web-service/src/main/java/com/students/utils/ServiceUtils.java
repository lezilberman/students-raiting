package com.students.utils;

public class ServiceUtils {
	
    public static boolean tryParseInt(String value) {
	   try {
	       Integer.parseInt(value);
	       return true;
	   } catch (NumberFormatException ex) {
	      return false;
	   }
	}    

}
