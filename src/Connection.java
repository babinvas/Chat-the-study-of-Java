import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

/**
 JavaRush. Java Multithreading. Уровень 27. Лекция 15 (3008)

 * Чат (Chat)

 * Класс соединения между клиентом и серверомю
 */

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    // Метод записывает (сериализовывает) сообщение message в ObjectOutputStream.
    public void send(Message message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
        }
    }

    // Метод читает (десериализовает) данные из ObjectInputStream.
    public Message receive() throws IOException, ClassNotFoundException {
        synchronized (in) {
            return (Message) in.readObject();
        }
    }

    // Метод возвращает удаленный адрес сокетного соединения.
    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    // Метод закрывает все ресурсы класса.
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}