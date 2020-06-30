package enums;

public enum Book
{
    ID("index"),
    LABEL("book"),
    TITLE("title"),
    PUBLISHER("publisher"),
    DATE("releaseDate");

    private String key;

    Book(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}