package ai.tomorrow.instagramclone.Utils;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class StringManipulation {
    private static final String TAG = "StringManipulation";

    public static List<String> splitCommentString(String string){
        if (string.trim().equals(""))
            return null;

        String username = "";
        String comment = "";

        if (string.trim().startsWith("@")){
            if (!string.trim().contains(" ")){
                return null;
            }
            int spacdIndex = string.indexOf(" ");
            username = string.substring(1, spacdIndex);
            comment = string.substring(spacdIndex + 1);

            if (comment.equals("")){
                return null;
            }
        } else {
            comment = string;
        }
        return Arrays.asList(username, comment);
    }

    public static String expandUsername(String username){
        return username.replace(".", " ");
    }

    public static String condenseUsername(String username){
        return username.replace(" ", ".");
    }

    public static String getTags(String string){
        if (string.indexOf("#") > 0){
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundWord = false;
            for (char c: charArray){
                if (c == '#'){
                    foundWord = true;
                    sb.append(c);
                } else {
                    if (foundWord){
                        sb.append(c);
                    }
                }
                if (c == ' '){
                    foundWord = false;
                }
            }
            String s = sb.toString().replace(" ", "").replace("#", ",#");
            return s.substring(1, s.length());
        }
        return "";
    }

    public static String getLikesString(int likesCount, List<String> likesUsername){
        Log.d(TAG, "getLikesString: generating likes string, likesCount = " + likesCount);
        String likesString = "";

        try {
            if (likesCount == 1) {
                likesString = "Liked by " + likesUsername.get(0);
            } else if (likesCount == 2) {
                likesString = "Liked by " + likesUsername.get(0)
                        + " and " + likesUsername.get(1);
            } else if (likesCount == 3) {
                likesString = "Liked by " + likesUsername.get(0)
                        + ", " + likesUsername.get(1)
                        + " and " + likesUsername.get(2);

            } else if (likesCount == 4) {
                likesString = "Liked by " + likesUsername.get(0)
                        + ", " + likesUsername.get(1)
                        + ", " + likesUsername.get(2)
                        + " and " + likesUsername.get(3);
            } else if (likesCount > 4) {
                likesString = "Liked by " + likesUsername.get(0)
                        + ", " + likesUsername.get(1)
                        + ", " + likesUsername.get(2)
                        + " and " + (likesCount - 3) + " others";
            }
        }catch (IndexOutOfBoundsException e){
            Log.d(TAG, "getLikesString: IndexOutOfBoundsException: " + e.getMessage());
        }

        return likesString;
    }

}
