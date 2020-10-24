import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 JavaRush. Java Multithreading. Уровень 27. Лекция 15 (3008)

 * Чат (Chat)

 * Основной класс сервера.

 - Сервер создает серверное сокетное соединение.
 - В цикле ожидает, когда какой-то клиент подключится к сокету.
 - Создает новый поток обработчик Handler, в котором будет происходить обмен сообщениями с клиентом.
 - Ожидает следующее соединение.
 */

public class Server {
    // Ключ: имя клиента, значение: соединение с ним.
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Запрашивать порт сервера
        int port = ConsoleHelper.readInt();
        ServerSocket server = null;

        // Создавать серверный сокет
        try {
            server = new ServerSocket(port);
            System.out.println("Сервер запущен.");

            while (true) {
                //Слушает и принимает входящие сокетные соединения
                //и создаваёт и запускает поток Handler, передавая в конструктор сокет
                Socket socket = server.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (Exception e) {
            try {
                server.close();
            } catch (IOException f) {
            }

            System.out.println(e);
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> member : connectionMap.entrySet()) {
            Connection value = member.getValue();

            try {
                value.send(message);
            } catch (IOException e) {
                System.out.println("Не смогли отправить сообщение.");
            }
        }
    }

    private static class Handler extends Thread {
        // Класс Handler реализовывает протокол общения с клиентом (происходит обмен сообщениями с клиентом).
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            SocketAddress socketAddress = socket.getRemoteSocketAddress();
            ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом: " + socketAddress);

            Connection connection = null;;
            String userName = null;
            Message message = null;

            try {
                connection = new Connection(socket);

                // Знакомства сервера с клиентом с возратом имени нового клиента
                userName = serverHandshake(connection);

                //Рассылка всем участникам чата информацию об имени присоединившегося участника (MessageType.USER_ADDED).
                message = new Message(MessageType.USER_ADDED, userName);
                sendBroadcastMessage(message);
                notifyUsers(connection, userName);

                // Запуск главный цикл обработки сообщений сервером.
                serverMainLoop(connection, userName);
            }
            catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом.");

                // При возникновении исключения соединение закрывается.
                try {
                    if (connection != null) {
                        connection.close();
                    }
                }
                catch (Exception f) {
                    ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом.");
                }
            }

            // Если было получено имя клиена, мы должны удалить запись для этого имени из connectionMap
            // и разослать всем остальным участникам сообщение с типом USER_REMOVED и сохраненным именем.
            if (userName != null) {
                connectionMap.remove(userName);
                message = new Message(MessageType.USER_REMOVED, userName);
                sendBroadcastMessage(message);
            }

            ConsoleHelper.writeMessage("Соединение с удаленным адресом закрыто.");
        }

        // Метод знакомства сервера с клиентом с возратом имени нового клиента
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                // Формирование и отправка команды запроса имени пользователя
                Message message = new Message(MessageType.NAME_REQUEST);
                connection.send(message);

                // Получение ответ клиента
                message = connection.receive();

                // Проверка, что получена команда с именем пользователя (MessageType.USER_NAME)
                if (message.getType() == MessageType.USER_NAME) {
                    /* Достать из ответа имя, проверить,
                    что оно не пустое и пользователь с таким именем еще не подключен (connectionMap)
                    Если какая-то проверка не прошла, заново запросить имя клиента/
                    */

                    String name = message.getData();

                    if (name != "" && !connectionMap.containsKey(name)) {
                        connectionMap.put(name, connection);
                        message = new Message(MessageType.NAME_ACCEPTED);
                        connection.send(message);
                        return name;
                    }
                }
            }
        }

        // Метод отправки клиенту (новому участнику) информации об остальных клиентах (участниках) чата.
        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> member : connectionMap.entrySet()) {
                String name = member.getKey();

                if (!name.equals(userName)) {
                    Message message = new Message(MessageType.USER_ADDED, name);
                    connection.send(message);
                }
            }
        }

        // Метод - главный цикл обработки сообщений сервером
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                // Принимать сообщение клиента
                Message message = connection.receive();

                // Если принятое сообщение - это текст (тип TEXT),
                // то формируется новое текстовое сообщение по форме и отправляется всем клиентам методом sendBroadcastMessage.
                // Если принятое сообщение не является текстом, выводится сообщение об ошибке.
                if (message.getType() == MessageType.TEXT) {
                    String messageText = userName + ": " + message.getData();
                    message = new Message(MessageType.TEXT, messageText);
                    sendBroadcastMessage(message);
                }
                else {
                    ConsoleHelper.writeMessage("Ошибка: принятое сообщение не является текстом.");
                }
            }
        }
    }
}