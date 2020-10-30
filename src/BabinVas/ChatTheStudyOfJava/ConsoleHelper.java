package BabinVas.ChatTheStudyOfJava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 JavaRush. Java Multithreading. Уровень 27. Лекция 15 (3008)

 * Чат (Chat)

 * Класс (вспомогательный), для чтения или записи в консоль.

   Вся работа с консолью происходит через этот класс.
 */

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    // Метод выводить сообщение message в консоль.
    public static void writeMessage(String message) {
        System.out.println(message);
    }

    // Метод считывает строку с консоли.
    // Если во время чтения произошло исключение, выводит пользователю сообщение
    // и повторно считывает строку с консоли.
    public static String readString() {
        while (true) {
            try {
                return reader.readLine();
            } catch (IOException e) {
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }
    }

    // Метод возвращает введенное число (использует метод readString()).
    // Если произошло исключение, выводит пользователю сообщение
    // и повторно считывает число с консоли.
    public static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e) {
                System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }
    }
}