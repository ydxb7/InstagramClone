package ai.tomorrow.instagramclone.Utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class StringManipulation {
    private static final String TAG = "StringManipulation";

    /**
     * return a string presenting the number of days ago the post was made.
     *
     * @return
     */
    public static String getTimeStampDifference(String oldTimeStamp) {
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        try {
            timestamp = sdf.parse(oldTimeStamp);
            int diff_year = Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 / 365));
            int diff_month = Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 / 30));
            int diff_week = Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 / 7));
            int diff_day = Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24));
            int diff_hour = Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60));
            int diff_minute = Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60));
            int diff_second = Math.round(((today.getTime() - timestamp.getTime()) / 1000));

            if (diff_year != 0) {
                difference = diff_year + "year";
            } else if (diff_month != 0) {
                difference = diff_month + "month";
            } else if (diff_week != 0) {
                difference = diff_week + "w";
            } else if (diff_day != 0) {
                difference = diff_day + "d";
            } else if (diff_hour != 0) {
                difference = diff_hour + "h";
            } else if (diff_minute != 0) {
                difference = diff_minute + "m";
            } else if (diff_second != 0) {
                difference = diff_second + "s";
            } else if (diff_second == 0) {
                difference = "just now";
            }

        } catch (ParseException e) {
            Log.d(TAG, "getTimeStampDifference: ParseException: " + e.getMessage());
        }
        return difference;
    }

    public static String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        return sdf.format(new Date());
    }

    public static List<String> splitCommentString(String string) {
        if (string.trim().equals(""))
            return null;

        String username = "";
        String comment = "";

        if (string.trim().startsWith("@")) {
            if (!string.trim().contains(" ")) {
                return null;
            }
            int spacdIndex = string.indexOf(" ");
            username = string.substring(1, spacdIndex);
            comment = string.substring(spacdIndex + 1);

            if (comment.equals("")) {
                return null;
            }
        } else {
            comment = string;
        }
        return Arrays.asList(username, comment);
    }

    public static String expandUsername(String username) {
        return username.replace(".", " ");
    }

    public static String condenseUsername(String username) {
        return username.replace(" ", ".");
    }

    public static String getTags(String string) {
        if (string.indexOf("#") > 0) {
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundWord = false;
            for (char c : charArray) {
                if (c == '#') {
                    foundWord = true;
                    sb.append(c);
                } else {
                    if (foundWord) {
                        sb.append(c);
                    }
                }
                if (c == ' ') {
                    foundWord = false;
                }
            }
            String s = sb.toString().replace(" ", "").replace("#", ",#");
            return s.substring(1);
        }
        return "";
    }

    public static String getLikesString(int likesCount, List<String> likesUsername) {
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
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "getLikesString: IndexOutOfBoundsException: " + e.getMessage());
        }
        return likesString;
    }
}
