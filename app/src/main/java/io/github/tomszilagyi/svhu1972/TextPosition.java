package io.github.tomszilagyi.svhu1972;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TextPosition {
    public int page;
    public int line;

    public TextPosition() {
        this.page = 0;
        this.line = 0;
    }

    public TextPosition(int page, int line) {
        this.page = page;
        this.line = line;
    }

    public TextPosition(DataInputStream dis) throws IOException {
        this.page = dis.readInt();
        this.line = dis.readInt();
    }

    public String toString() {
        return "tp["+this.page+":"+this.line+"]";
    }

    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(page);
        dos.writeInt(line);
    }
}
