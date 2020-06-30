package enums;

public enum Reader {
    LABEL("reader"),
    ID("cardNumber"),
    NAME("name"),
    SURNAME("surname");

    private String key;

    Reader(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
