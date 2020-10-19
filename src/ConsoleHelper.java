import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 JavaRush. Java Multithreading. Уровень 27. Лекция 15 (3008)

 * Чат (Chat)

 * Класс (вспомогательный), для чтения или записи в консоль.

   Вся работа с консолью происходит через этот класс.
 */

public class ConsoleHelper {
    public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {

    }

    public static String readString() {
        return "";
    }

    public static int readInt() {
        return 0;
    }
}