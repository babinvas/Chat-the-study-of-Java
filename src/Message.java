import java.io.Serializable;

/**
 JavaRush. Java Multithreading. Уровень 27. Лекция 15 (3008)

 * Чат (Chat)

 * Класс, отвечающий за пересылаемые сообщения.
 */

public class Message implements Serializable {
    private final MessageType type;
    private final String data;

    public Message(MessageType type){
        this.type = type;
        data = null;
    }

    public Message(MessageType type, String data){
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

}