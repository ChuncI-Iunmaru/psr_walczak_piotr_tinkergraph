package enums;

public enum Relationship {
    LABEL("borrowed");

    private String key;

    Relationship(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
