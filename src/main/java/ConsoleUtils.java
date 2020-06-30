import enums.Reader;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import enums.Book;

public class ConsoleUtils {
    private static Scanner scanner = new Scanner(System.in);
    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final String readerLabel = "reader";
    public static final String relLabel = "borrowed";

    static char getMenuOption() {
        System.out.println();
        List<String> menuOptions = new ArrayList<>(Arrays.asList(
                "[d]odaj książkę",
                "[e]dytuj książkę",
                "[u]suń książkę",
                "pokaz [k]sięgozbiór",
                "za[r]ejstruj kartę",
                "w[y]rejestruj kartę",
                "wszyscy [c]zytelnicy",
                "czytelnik po [n]umerze karty(id)",
                "[w]ypożycz książkę",
                "[o]ddaj książkę",
                "[p]obierz zapytaniem",
                "[s]tatystyki czytelników",
                "[z]akoncz"));
        int i = 1;
        for (String s : menuOptions) {
            System.out.println(i + ". " + s);
            i++;
        }
        while (true) {
            try {
                System.out.print("Podaj operację: ");
                return scanner.nextLine().toLowerCase().charAt(0);
            } catch (StringIndexOutOfBoundsException e) {
                scanner.nextLine();
                System.out.println("Podano nieprawidłową operację.");
            }
        }
    }

    static String getFormattedDate(String setValue) {
        System.out.println("Podaj date wydania w formacie DD-MM-YYYY");
        if (!setValue.isEmpty())
            System.out.println("Obecna wartość: " + setValue + ". Pozostaw puste by nie zmieniać.");
        while (true) {
            try {
                String line = scanner.nextLine();
                if (!setValue.isEmpty() && line.isEmpty()) return setValue;
                LocalDate date = LocalDate.parse(line, format);
                return format.format(date);
            } catch (DateTimeParseException e) {
                System.out.println("Podaj prawidłową datę!");
            }
        }
    }

    static String getFormattedDate() {
        return getFormattedDate("");
    }

    static String getText(int minLength) {
        String tmp = "";
        do {
            tmp = scanner.nextLine();
            if (tmp.length() < minLength) System.out.println("Podaj minimum " + minLength + " znakow!");
        } while (tmp.length() < minLength);
        return tmp;
    }

    static long getId() {
        long num = -1;
        while (num < 0) {
            try {
                num = scanner.nextLong();
                scanner.nextLine();
                if (num < 0) System.out.println("Podaj prawidłową wartość => 0");
            } catch (InputMismatchException e) {
                scanner.next();
                System.out.println("Podaj prawidłową wartość!");
            }
        }
        return num;
    }

    public static String printVertexAsBook(Vertex v){
        if (v.label().compareTo(Book.LABEL.getKey()) != 0) {
            System.out.println("Błąd wypisywania - ten wierzchołek może nie opisywać książki.");
            return "";
        } else return String.format("[%s] '%s', wyd. %s, %s", v.value(Book.ID.getKey()),
                v.value(Book.TITLE.getKey()),
                v.value(Book.PUBLISHER.getKey()),
                v.value(Book.DATE.getKey()));
    }

    public static String printVertexAsReader(Vertex v){
        if (v.label().compareTo(Reader.LABEL.getKey()) != 0) {
            System.out.println("Błąd wypisywania - ten wierzchołek może nie opisywać czytelnika.");
            return "";
        } else return String.format("Nr. karty: %d - %s %s",
                v.value(Reader.ID.getKey()),
                v.value(Reader.NAME.getKey()),
                v.value(Reader.SURNAME.getKey()));
    }
}
