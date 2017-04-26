package wang.laic.kanban.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by duduba on 2017/4/11.
 */

public class KukuUtil {
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private static DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");
//    private static DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static DateTime parse(String date) {
        return DateTime.parse(date, format);
    }

    public static String getFormatDate(Date date) {
        return formatter.format(date);
    }


    public static String md5Digest(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(message.getBytes());
            byte[] digest = md.digest();
            BigInteger number = new BigInteger(1, digest);
            String hashText = number.toString(16);
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }
            return hashText.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
