package ru.hse.avk_spingbot.Service;

import ru.hse.avk_spingbot.config.BotConfig;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    /**
     * @return config.getBotName()
     */
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    /**
     * @return config.getBotToken()
     */
    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    /**
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":

                    startCommandRecieved(chatId, update.getMessage().getChat().getFirstName());
                    break;

                default:

                    sendMessage(chatId, "Sorry, command did not recognized!");

            }
        }
    }


    private void startCommandRecieved(long chatId, String name) {

        String answer = "Hi, " + name + "! Welcome to my bot!";

        sendMessage(chatId, answer);

    }

    private void sendMessage(long chatId, String messageToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }


    }
}
