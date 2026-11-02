import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Translator {
    /** ключ — англійське слово, значення —український переклад*/
    private final HashMap<String, String> dictionary = new HashMap<>();

    /*розділяє слово на: розділові знаки на початку, саме слово, розділові знаки в кінці */
    private static final Pattern TOKEN =
            Pattern.compile("^([\\p{Punct}«»“”…—–]*)(.*?)([\\p{Punct}«»“”…—–]*)$");

    /* Додає пару слів у словник, якщо слово вже є то переклад оновлюється, якщо слова раніше не було повертає null або поп. переклад*/
    public String addWord(String english, String ukrainian) {
        if (english == null || english.isBlank() || ukrainian == null || ukrainian.isBlank()) {
            throw new IllegalArgumentException("слово та переклад не можуть бути порожніми");
        }
        return dictionary.put(english.trim().toLowerCase(), ukrainian.trim());
    }

    /*невідомі слова залишаються англ. у квадратних дужках*/
    public String translate(String phrase) {
        if (phrase == null || phrase.isBlank()) {
            throw new IllegalArgumentException("фраза не може бути порожньою");
        }

        StringBuilder result = new StringBuilder();
        for (String token : phrase.trim().split("\\s+")) {
            Matcher m = TOKEN.matcher(token);
            m.matches();
            String prefix = m.group(1);
            String word = m.group(2);
            String suffix = m.group(3);

            if (result.length() > 0) {
                result.append(' ');
            }
            if (word.isEmpty()) {              // токен складається лише зі знаків пунктуації
                result.append(token);
                continue;
            }
            String translation = dictionary.get(word.toLowerCase());
            result.append(prefix)
                  .append(translation != null ? translation : "[" + word + "]")
                  .append(suffix);
        }
        return result.toString();
    }

    /**кількість слів у словнику. */
    public int size() {
        return dictionary.size();
    }

    /** копія словника, відсортована за алфавітом */
    public Map<String, String> getSortedDictionary() {
        return new TreeMap<>(dictionary);
    }
}

public class Main {

    public static void main(String[] args) {
        // Коректне відображення кирилиці в консолі
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

        Translator translator = new Translator();
        fillDefaultDictionary(translator);

        System.out.println("=== Англо-український перекладач ===");
        System.out.println("У словнику слів: " + translator.size());

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Ваш вибір: ");
            if (!in.hasNextLine()) {
                break;
            }
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1" -> addOnePair(in, translator);
                case "2" -> addManyPairs(in, translator);
                case "3" -> showDictionary(translator);
                case "4" -> translatePhrase(in, translator);
                case "0" -> {
                    running = false;
                    System.out.println("Роботу завершено.");
                }
                default -> System.out.println("Невірний пункт меню. Введіть число від 0 до 4.");
            }
        }
        in.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1 - Додати пару слів");
        System.out.println("2 - Ввести кілька пар слів (порожній рядок - завершення)");
        System.out.println("3 - Показати словник");
        System.out.println("4 - Перекласти фразу");
        System.out.println("0 - Вихід");
    }

    /** Початкове наповнення словника. */
    private static void fillDefaultDictionary(Translator t) {
        t.addWord("hello", "привіт");
        t.addWord("world", "світ");
        t.addWord("i", "я");
        t.addWord("love", "люблю");
        t.addWord("my", "мій");
        t.addWord("name", "ім'я");
        t.addWord("is", "є");
        t.addWord("good", "добрий");
        t.addWord("morning", "ранок");
        t.addWord("cat", "кіт");
        t.addWord("dog", "собака");
        t.addWord("book", "книга");
        t.addWord("read", "читати");
    }

    /** Додавання однієї пари з клавіатури. */
    private static void addOnePair(Scanner in, Translator t) {
        System.out.print("Англійське слово: ");
        String en = in.nextLine();
        System.out.print("Український переклад: ");
        String ua = in.nextLine();
        try {
            String old = t.addWord(en, ua);
            if (old == null) {
                System.out.println("Пару додано: " + en.trim().toLowerCase() + " - " + ua.trim());
            } else {
                System.out.println("Переклад слова '" + en.trim().toLowerCase()
                        + "' оновлено: " + old + " -> " + ua.trim());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }
    }

    /** Додавання кількох пар з клавіатури до порожнього рядка. */
    private static void addManyPairs(Scanner in, Translator t) {
        int count = 0;
        while (true) {
            System.out.print("Англійське слово (порожній рядок - завершити): ");
            String en = in.nextLine();
            if (en.isBlank()) {
                break;
            }
            System.out.print("Український переклад: ");
            String ua = in.nextLine();
            try {
                t.addWord(en, ua);
                count++;
            } catch (IllegalArgumentException e) {
                System.out.println("Помилка: " + e.getMessage());
            }
        }
        System.out.println("Додано/оновлено пар: " + count);
    }

    private static void showDictionary(Translator t) {
        System.out.println("Словник (" + t.size() + " слів):");
        for (Map.Entry<String, String> e : t.getSortedDictionary().entrySet()) {
            System.out.println("  " + e.getKey() + " - " + e.getValue());
        }
    }

    /** Введення фрази англійською та виведення перекладу. */
    private static void translatePhrase(Scanner in, Translator t) {
        System.out.print("Введіть фразу англійською: ");
        String phrase = in.nextLine();
        try {
            System.out.println("Переклад: " + t.translate(phrase));
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }
    }
}