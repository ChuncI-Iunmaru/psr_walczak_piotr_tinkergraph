import enums.Book;
import enums.Reader;
import enums.Relationship;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.otherV;

public class AppMain {

    private static void addBook(GraphTraversalSource g) {
        System.out.println("Podaj tytuł książki:");
        String title = ConsoleUtils.getText(1);
        String date = ConsoleUtils.getFormattedDate();
        System.out.println("Podaj wydawnictwo:");
        String publisher = ConsoleUtils.getText(1);
        g.addV(Book.LABEL.getKey())
                .property(Book.ID.getKey(), IdGenerator.createID())
                .property(Book.TITLE.getKey(), title)
                .property(Book.DATE.getKey(), date)
                .property(Book.PUBLISHER.getKey(), publisher).next();
    }

    private static void printAllBooks(GraphTraversalSource g) {
        List<Vertex> vertices = g.V().hasLabel(Book.LABEL.getKey()).toList();
        for (Vertex v : vertices) {
            System.out.println(ConsoleUtils.printVertexAsBook(v));
        }
    }

    private static void editBook(GraphTraversalSource g) {
        System.out.println("Podaj id książki: ");
        long id = ConsoleUtils.getId();
        try {
            Vertex v = g.V().has(Book.ID.getKey(), id).next();
            if (v.label().compareTo(Book.LABEL.getKey())!=0) {
                System.out.println("Znaleziony wierzchołek nie jest książką");
                return;
            }

            System.out.println("Podaj nowy tytuł książki. Obecna wartość: " + v.value(Book.TITLE.getKey()) + ". Pozostaw puste by nie zmieniać.");
            String title = ConsoleUtils.getText(0);
            title = title.isEmpty() ? v.value(Book.TITLE.getKey()) : title;

            String date = ConsoleUtils.getFormattedDate(v.value(Book.DATE.getKey()));

            System.out.println("Podaj nowe wydawnictwo. Obecna wartość: " + v.value(Book.PUBLISHER.getKey()) + ". Pozostaw puste by nie zmieniać.");
            String publisher = ConsoleUtils.getText(0);
            publisher = publisher.isEmpty() ? v.value(Book.PUBLISHER.getKey()) : publisher;

            g.V().has(Book.ID.getKey(), id).property(Book.TITLE.getKey(), title)
                    .property(Book.PUBLISHER.getKey(), publisher)
                    .property(Book.DATE.getKey(), date).next();
        } catch (java.util.NoSuchElementException e) {
            System.out.println("Nie znaleziono wierzchołka o podanym id.");
        }
    }

    private static void deleteBook(GraphTraversalSource g){
        System.out.println("Podaj id książki do usunięcia: ");
        long id = ConsoleUtils.getId();
        try {
            g.V().hasLabel(Book.LABEL.getKey()).has(Book.ID.getKey(), id).next().remove();
            System.out.println("Usunięto książkę");
        } catch (java.util.NoSuchElementException e) {
            System.out.println("Nie znaleziono książki o podanym id.");
        }
    }

    private static void registerReader(GraphTraversalSource g){
        System.out.println("Podaj imię:");
        String name = ConsoleUtils.getText(1);
        System.out.println("Podaj nazwisko:");
        String surname = ConsoleUtils.getText(1);
        g.addV(Reader.LABEL.getKey())
                .property(Reader.ID.getKey(), IdGenerator.createID())
                .property(Reader.NAME.getKey(), name)
                .property(Reader.SURNAME.getKey(), surname)
                .next();
    }

    private static void deleteReader(GraphTraversalSource g){
        System.out.println("Podaj numer karty do wyrejestrowania: ");
        long id = ConsoleUtils.getId();
        try {
            g.V().hasLabel(Reader.LABEL.getKey()).has(Reader.ID.getKey(), id).next().remove();
            System.out.println("Usunięto kartę.");
        } catch (java.util.NoSuchElementException e) {
            System.out.println("Nie znaleziono czytelnika z danym numerem karty.");
        }
    }

    private static void getReaderById(GraphTraversalSource g){
        System.out.println("Podaj numer karty do wyszukania: ");
        long id = ConsoleUtils.getId();
        try {
            Vertex reader = g.V().hasLabel(Reader.LABEL.getKey()).has(Reader.ID.getKey(), id).next();
            System.out.println(ConsoleUtils.printVertexAsReader(reader));
            System.out.println("Wypożyczone książki:");
            for (Vertex b: g.V(reader).out().toList()) {
                System.out.println(String.format("\t[%s]: '%s'", b.value(Book.ID.getKey()), b.value(Book.TITLE.getKey()).toString()));
            }
        } catch (java.util.NoSuchElementException e) {
            System.out.println("Nie znaleziono czytelnika z danym numerem karty.");
        }
    }

    private static void printAllReaders(GraphTraversalSource g){
        List<Vertex> vertices = g.V().hasLabel(Reader.LABEL.getKey()).toList();
        for (Vertex v : vertices) {
            System.out.println(ConsoleUtils.printVertexAsReader(v));
            System.out.println("Wypożyczone książki:");
            for (Vertex b: g.V(v).out().toList()) {
                System.out.println(String.format("\t[%s]: '%s'", b.value(Book.ID.getKey()), b.value(Book.TITLE.getKey()).toString()));
            }
        }
    }

    private static void borrowBook(GraphTraversalSource g){
        try {
            System.out.println("Podaj numer karty: ");
            long id = ConsoleUtils.getId();
            Vertex reader =  g.V().hasLabel(Reader.LABEL.getKey()).has(Reader.ID.getKey(), id).next();
            System.out.println(String.format("%s %s wybiera książkę: ", reader.value(Reader.NAME.getKey()), reader.value(Reader.SURNAME.getKey())));
            System.out.println("Podaj id książki do wypożyczenia: ");
            id = ConsoleUtils.getId();
            Vertex book = g.V().hasLabel(Book.LABEL.getKey()).has(Book.ID.getKey(), id).next();
            System.out.println(String.format("%s %s wypożycza książkę pt. '%s'",
                    reader.value(Reader.NAME.getKey()),
                    reader.value(Reader.SURNAME.getKey()),
                    book.value(Book.TITLE.getKey())));
            try {
                g.V(reader).bothE().where(otherV().is(book)).next();
                System.out.println("Już wypożyczono tą książkę");
            } catch (NoSuchElementException ex) {
                g.V(reader).addE(Relationship.LABEL.getKey()).to(book).next();
                System.out.println("Wypożyczono książkę.");
            }
        } catch (java.util.NoSuchElementException e) {
            System.out.println("Nie znaleziono takiego wierzchołka.");
        }
    }

    private static void returnBook(GraphTraversalSource g) {
        try {
            System.out.println("Podaj numer karty: ");
            long id = ConsoleUtils.getId();
            Vertex reader =  g.V().hasLabel(Reader.LABEL.getKey()).has(Reader.ID.getKey(), id).next();
            System.out.println("Podaj id książki do zwrotu: ");
            id = ConsoleUtils.getId();
            Vertex book = g.V().hasLabel(Book.LABEL.getKey()).has(Book.ID.getKey(), id).next();
            System.out.println(String.format("%s %s zwraca książkę pt. '%s'",
                    reader.value(Reader.NAME.getKey()),
                    reader.value(Reader.SURNAME.getKey()),
                    book.value(Book.TITLE.getKey())));
            try {
                g.V(reader).bothE().where(otherV().is(book)).next().remove();
                System.out.println("Zwrócono książkę.");
            } catch (NoSuchElementException ex) {
                System.out.println("Nie udało się zwrócić takiej książki.");
            }
        } catch (java.util.NoSuchElementException e) {
            System.out.println("Nie znaleziono takiego wierzchołka.");
        }
    }

    private static void getReaderStats(GraphTraversalSource g){
        System.out.println("Przetwarzanie danych - ranking czytelników według wypożyczeń");
        Map<String, Integer> readersToBorrowed = new HashMap<>();
        for (Vertex v : g.V().hasLabel(Reader.LABEL.getKey()).toList()) {
            readersToBorrowed.put(
                    ConsoleUtils.printVertexAsReader(v),
                    g.V(v).out().toList().size());
        }
        HashMap<String, Integer> sortedMap = readersToBorrowed.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        System.out.println(String.format("%13s | %-100s", "Wypożyczenia", "Czytelnik"));
        System.out.println(String.format("%116s", "-").replace(" ", "-"));
        for (String s: sortedMap.keySet()) {
            System.out.println(String.format("%13s | %-100s", sortedMap.get(s), s));
        }
    }

    private static void getBooksByQuery(GraphTraversalSource g){
        System.out.println("Podaj nazwę wydawnictwa");
        String publisher = ConsoleUtils.getText(1);

        List<Vertex> allBooksFromPublisher = g.V().hasLabel(Book.LABEL.getKey()).has(Book.PUBLISHER.getKey(), publisher).toList();
        if (allBooksFromPublisher.isEmpty()) {
            System.out.println("Nie znaleziono żadnych książek tego wydawnictwa.");
            return;
        }
    }

    public static void main(String[] args) throws Exception {
        TinkerGraph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();
        g.addV(Book.LABEL.getKey())
                .property(Book.ID.getKey(), IdGenerator.createID())
                .property(Book.TITLE.getKey(), "Ogniem i Mieczem")
                .property(Book.DATE.getKey(), "11-11-2011")
                .property(Book.PUBLISHER.getKey(), "Nowe Lektury").next();

        g.addV(Reader.LABEL.getKey())
                .property(Reader.ID.getKey(), IdGenerator.createID())
                .property(Reader.NAME.getKey(), "Piotr")
                .property(Reader.SURNAME.getKey(), "Walczak")
                .next();

        System.out.println("Aplikacja na PSR lab 7 - TinkerGraph");
        System.out.println("Piotr Walczak gr. 1ID22B");
        while (true) {
            switch (ConsoleUtils.getMenuOption()) {
                case 'd':
                    addBook(g);
                    break;
                case 'e':
                    editBook(g);
                    break;
                case 'u':
                    deleteBook(g);
                    break;
                case 'k':
                    printAllBooks(g);
                    break;
                case 'w':
                    borrowBook(g);
                    break;
                case 'o':
                    returnBook(g);
                    break;
                case 'r':
                    registerReader(g);
                    break;
                case 'y':
                    deleteReader(g);
                    break;
                case 'c':
                    printAllReaders(g);
                    break;
                case 'n':
                    getReaderById(g);
                    break;
                case 'p':
                    getBooksByQuery(g);
                    break;
                case 's':
                    getReaderStats(g);
                    break;
                case 'z':
                    g.close();
                    return;
                default:
                    System.out.println("Podano nieznaną operację. Spróbuj ponownie.");
            }
        }
    }
}
