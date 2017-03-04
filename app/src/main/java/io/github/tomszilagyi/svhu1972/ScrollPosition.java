package io.github.tomszilagyi.svhu1972;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class ScrollPosition implements Serializable {
    public int page;
    public int offset;

    public ScrollPosition() {
        this.page = 0;
        this.offset = 0;
    }

    public ScrollPosition(int page, int offset) {
        this.page = page;
        this.offset = offset;
    }

    public void update(int page, int offset) {
        this.page = page;
        this.offset = offset;
    }

    public ScrollPosition(DataInputStream dis) throws IOException {
        this.page = dis.readInt();
        this.offset = dis.readInt();
    }

    public String toString() {
        return "sp["+this.page+":"+this.offset+"]";
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(page);
        dos.writeInt(offset);
    }
}
