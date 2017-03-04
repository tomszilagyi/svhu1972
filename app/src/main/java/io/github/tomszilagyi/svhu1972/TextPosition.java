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

    public String toString() {
        return "tp["+this.page+":"+this.line+"]";
    }
}
