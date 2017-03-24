package io.github.tomszilagyi.svhu1972;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Bookmark implements Serializable {

    public String label;
    public ScrollPosition position;
    public Date timestamp;

    public Bookmark(String label, ScrollPosition position) {
        this.label = label;
        this.position = position;
        this.timestamp = new Date();
    }

    public Bookmark(DataInputStream dis) throws IOException {
        this.label = dis.readUTF();
        this.position = new ScrollPosition(dis);
        this.timestamp = parse_timestamp(dis.readUTF());
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeUTF(label);
        position.write(dos);
        dos.writeUTF(format_timestamp(timestamp));
    }

    /* Format timestamp nicely depending on how far it is in the past */
    public String pretty_print_timestamp() {
        return pretty_print_timestamp(timestamp);
    }

    private static String pretty_print_timestamp(Date timestamp) {
        return pretty_print_timestamp(timestamp, new Date());
    }

    private static final String FORMAT_STORAGE = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final String FORMAT_SAME_DAY = "HH:mm";
    private static final String FORMAT_YESTERDAY = "'Tegnap' HH:mm";
    private static final String FORMAT_WITHIN_1_YEAR = "MMMM d, HH:mm";
    private static final String FORMAT_OLD = "YYYY. MMMM d, HH:mm";

    /* This is public for testing purposes only. */
    public static String pretty_print_timestamp(Date timestamp, Date now) {
        String format = choose_format(timestamp, now);
        SimpleDateFormat sdf = new SimpleDateFormat(format, new Locale("hu"));
        return sdf.format(timestamp,
                          new StringBuffer(),
                          new FieldPosition(0)).toString();
    }

    private static String choose_format(Date timestamp, Date now) {
        Calendar cal_now = Calendar.getInstance();
        Calendar cal_ts = Calendar.getInstance();
        cal_now.setTime(now);
        cal_ts.setTime(timestamp);

        if (cal_now.get(Calendar.YEAR) == cal_ts.get(Calendar.YEAR) &&
            cal_now.get(Calendar.DAY_OF_YEAR) == cal_ts.get(Calendar.DAY_OF_YEAR)) {
            return FORMAT_SAME_DAY;
        }
        if (cal_now.get(Calendar.YEAR) == cal_ts.get(Calendar.YEAR) &&
            cal_now.get(Calendar.DAY_OF_YEAR) == cal_ts.get(Calendar.DAY_OF_YEAR) + 1) {
            return FORMAT_YESTERDAY;
        }
        // Special case for viewing on 1 Jan a bookmark added on 31 Dec last year:
        if (cal_now.get(Calendar.YEAR) == cal_ts.get(Calendar.YEAR) + 1 &&
            cal_now.get(Calendar.DAY_OF_YEAR) == 1 &&
            cal_ts.get(Calendar.MONTH) == Calendar.DECEMBER &&
            cal_ts.get(Calendar.DAY_OF_MONTH) == 31) {
            return FORMAT_YESTERDAY;
        }
        if (cal_now.get(Calendar.YEAR) == cal_ts.get(Calendar.YEAR) ||
            (cal_now.get(Calendar.YEAR) == cal_ts.get(Calendar.YEAR) + 1 &&
             cal_now.get(Calendar.MONTH) < cal_ts.get(Calendar.MONTH))) {
            return FORMAT_WITHIN_1_YEAR;
        }
        return FORMAT_OLD;
    }

    private static final SimpleDateFormat sdf_store = new SimpleDateFormat(FORMAT_STORAGE);

    /* This is public for testing purposes only. */
    public static String format_timestamp(Date timestamp) {
        return sdf_store.format(timestamp,
                                new StringBuffer(),
                                new FieldPosition(0)).toString();
    }

    /* This is public for testing purposes only. */
    public static Date parse_timestamp(String str) {
        return sdf_store.parse(str, new ParsePosition(0));
    }

    public String toString() {
        return "bk[\""+label+"\" at "+position+" saved "+
            format_timestamp(timestamp)+"]";
    }
}
