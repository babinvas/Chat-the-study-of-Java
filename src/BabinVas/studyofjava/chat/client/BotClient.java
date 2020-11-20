package BabinVas.studyofjava.chat.client;

public class BotClient extends Client {
	public static void main(String[] args) {
		BotClient botClient = new BotClient();
		botClient.run();
	}

	@Override
	protected String getUserName() {
		return "date_bot_" + (int) (Math.random() * 100);
	}

	@Override
	protected boolean shouldSendTextFromConsole() {
		return false;
	}

	@Override
	protected SocketThread getSocketThread() {
		return new BotSocketThread();
	}

	public class BotSocketThread extends SocketThread {
	}
}
