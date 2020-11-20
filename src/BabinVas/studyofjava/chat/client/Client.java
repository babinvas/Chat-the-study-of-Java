package BabinVas.studyofjava.chat.client;

import BabinVas.studyofjava.chat.Connection;
import BabinVas.studyofjava.chat.ConsoleHelper;
import BabinVas.studyofjava.chat.Message;
import BabinVas.studyofjava.chat.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 Клиент, в начале своей работы, должен запросить у пользователя адрес
 и порт сервера, подсоединиться к указанному адресу,получить запрос имени от сервера,
 спросить имя у пользователя, отправить имя пользователя серверу, дождаться принятия имени сервером.

 После этого клиент может обмениваться текстовыми сообщениями с сервером.
 Обмен сообщениями будет происходить в двух параллельно работающих потоках.
 Один будет заниматься чтением из консоли и отправкой прочитанного серверу,
 а второй поток будет получать данные от сервера и выводить их в консоль.
 */

public class Client {
	protected Connection connection;

	// Поле-флаг: true, если клиент подсоединен к серверу или в false в противном случае.
	private volatile boolean clientConnected = false;

	public static void main(String[] args) {
		Client client = new Client();
		client.run();
	}

	// Метод создаёт вспомогательный поток SocketThread, ожидает пока тот установит соединение с сервером,
	// а после этого в цикле считывать сообщения с консоли и отправлять их серверу.
	// Условием выхода из цикла будет отключение клиента или ввод пользователем команды 'exit'.
	// Для информирования главного потока, что соединение установлено во вспомогательном потоке,
	// используй методы wait() и notify() объекта класса Client.
	public void run() {
		SocketThread socketThread = getSocketThread();
		socketThread.setDaemon(true);
		socketThread.start();

		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				ConsoleHelper.writeMessage("Возникло исключение! Выход из программы.");
			}
		}

		if (clientConnected) {
			ConsoleHelper.writeMessage("Соединение установлено.\n Для выхода наберите команду 'exit'.");
		} else {
			ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
		}

		while (clientConnected) {
			String incomingMessage = ConsoleHelper.readString();

			if (incomingMessage.equals("exit")) {
				break;
			}

			if (shouldSendTextFromConsole()) {
				sendTextMessage(incomingMessage);
			}
		}
	}

	// Метод заправшивает ввод адреса сервера у пользователя.
	protected String getServerAddress() {
		return ConsoleHelper.readString();
	}

	// Метод заправшивает ввод порта сервера у пользователя.
	protected int getServerPort() {
		return ConsoleHelper.readInt();
	}

	// Метод заправшивает ввод имя пользователя у пользователя.
	protected String getUserName() {
		return ConsoleHelper.readString();
	}

	// Метод, в данной реализации клиента, всегда должен возвращать true
	// (мы всегда отправляем текст введенный в консоль).
	// Этот метод может быть переопределен, если мы будем писать какой-нибудь другой клиент,
	// унаследованный от нашего, который не должен отправлять введенный в консоль текст.
	protected boolean shouldSendTextFromConsole() {
		return true;
	}

	// Метод создаёт и возвращать новый объект класса SocketThread.
	protected SocketThread getSocketThread() {
		return new SocketThread();
	}

	// Метод создает новое текстовое сообщение, используя переданный текст
	// и отправляет его серверу через соединение connection.
	protected void sendTextMessage(String text) {
		Message message;
		try {
			message = new Message(MessageType.TEXT, text);
			connection.send(message);
		}
		catch (IOException e) {
			ConsoleHelper.writeMessage("Во время отправки сообщения произошла ошибка.");
			clientConnected = false;
		}
	}

	/**
	 * Клас SocketThread отвечате за поток, устанавливающий сокетное соединение и читающий сообщения сервера.
	 */
	public class SocketThread extends Thread {
		public void run() {
			String serverAddress = getServerAddress();
			int serverPort = getServerPort();
			Socket socket = null;
			try {
				socket = new Socket(serverAddress, serverPort);
				connection = new Connection(socket);
				clientHandshake();
				clientMainLoop();
			} catch (IOException | ClassNotFoundException e) {
				notifyConnectionStatusChanged(false);
			}
		}

		// Метод выводить текст message в консоль.
		protected void processIncomingMessage(String message) {
			ConsoleHelper.writeMessage(message);
		}

		// Метод выводить в консоль информацию о том,
		// что участник с именем userName присоединился к чату.
		protected void informAboutAddingNewUser(String userName) {
			ConsoleHelper.writeMessage("Участник с именем " + userName + " присоединился к чату.");
		}

		// Метод выводить в консоль, что участник с именем userName покинул чат.
		protected void informAboutDeletingNewUser(String userName) {
			ConsoleHelper.writeMessage("Участник с именем " + userName + " покинул чат.");
		}

		// Метод:
		// а) Устанавливает значение поля clientConnected внешнего объекта Client в соответствии с переданным параметром.
		// б) Оповещает (пробуждать ожидающий) основной поток класса Client.

		protected void notifyConnectionStatusChanged(boolean clientConnected) {
			synchronized (Client.this) {
				Client.this.clientConnected = clientConnected;
				Client.this.notify();
			}
		}

		// Представляет клиента серверу.
		protected void clientHandshake() throws IOException, ClassNotFoundException {
			Message message = null;

			while (true) {
				message = connection.receive();

				// Сервер запросил имя.
				if (message.getType() == MessageType.NAME_REQUEST) {
					String userName = getUserName();
					message = new Message(MessageType.USER_NAME, userName);
					connection.send(message);
				} else if (message.getType() == MessageType.NAME_ACCEPTED) {
					notifyConnectionStatusChanged(true);
					return;
				} else {
					throw new IOException("Unexpected MessageType");
				}
			}
		}

		// Реализовывает главный цикл обработки сообщений сервера.
		protected void clientMainLoop() throws IOException, ClassNotFoundException {
			Message message = null;

			while (true) {
				message = connection.receive();

				if (message.getType() == MessageType.TEXT) {
					processIncomingMessage(message.getData());
				} else if (message.getType() == MessageType.USER_ADDED) {
					informAboutAddingNewUser(message.getData());
				} else if (message.getType() == MessageType.USER_REMOVED) {
					informAboutDeletingNewUser(message.getData());
				} else {
					throw new IOException("Unexpected MessageType");
				}
			}
		}
	}
}