package io.github.tomszilagyi.svhu1972;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);

    /* This is public for testing purposes only. */
    public static String format_timestamp(Date timestamp) {
        return sdf.format(timestamp,
                          new StringBuffer(),
                          new FieldPosition(0)).toString();
    }

    /* This is public for testing purposes only. */
    public static Date parse_timestamp(String str) {
        return sdf.parse(str, new ParsePosition(0));
    }

    public String toString() {
        return "bk[\""+label+"\" at "+position+" saved "+
            format_timestamp(timestamp)+"]";
    }
}
