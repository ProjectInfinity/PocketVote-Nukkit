package io.pocketvote.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.pocketvote.PocketVote;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Creates a JWT and returns it as a String for use in
     * POST requests.
     * @return JWT String
     */
    public static String createJWT(HashMap<String, Object> fields) throws UnsupportedEncodingException {
        return Jwts.builder()
                .addClaims(fields)
                .signWith(SignatureAlgorithm.HS256, PocketVote.getPlugin().secret.getBytes("UTF-8"))
                .compact();
    }

    /**
     * Takes a hashmap and converts it into a key=value
     * POST data format then returns it as a String.
     * @param fields HashMap containing the keys and values
     * @return key=value String
     */
    public static String mapToPostString(HashMap<String, Object> fields) {
        StringBuilder postData = new StringBuilder();
        for(Map.Entry<String, Object> entry : fields.entrySet()) {
            postData.append(entry.getKey()).append("=").append(entry.getValue().toString()).append("&");
        }
        return postData.toString().substring(0, postData.length() - 1);
    }
}
