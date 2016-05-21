package io.pocketvote.util;

public class ToolBox {

    /**
     * Join a String[] into a single string.
     *
     * @return String
     */
    public static String implode( String[] array, String glue ) {
        String out = "";

        if(array.length == 0) {
            return out;
        }

        for(String part : array) {
            if(part == null) continue;
            out = out + part + glue;
        }
        out = out.substring(0, out.length() - glue.length());

        return out;
    }

    /**
     * Check if the provided String is a number or not.
     * @param number as a String
     * @return true if String is a number
     */
    public static boolean isNumber(String number){
        return (number.matches("-?\\d+") && !(Long.parseLong((number)) <= 0L) && (Long.parseLong((number)) < Integer.MAX_VALUE));
    }

}
