package io.github.tomszilagyi.svhu1972;

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
}
