package ru.hse.avk_spingbot.Service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.hse.avk_spingbot.config.BotConfig;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.hse.avk_spingbot.model.User;
import ru.hse.avk_spingbot.model.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    final BotConfig config;

    final static String HELP_TEXT = "This bot created to learn Spring capabilities. You can execute commends from main menu on the left or by typing commands.";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get welcome message"));
        listofCommands.add(new BotCommand("/mydata", "get your data stored"));
        listofCommands.add(new BotCommand("/deletedata", "deletee your data stored"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand("/settings", "set your preferences"));
        listofCommands.add(new BotCommand("/register", "register"));

        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
            throw new RuntimeException(e);
        }
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

            if (messageText.contains("/send") && config.getOwnerId() == chatId){
                    String text = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                    Iterable<User> allUsers = userRepository.findAll();
                    for (User user: allUsers) {
                        sendMessage(user.getChatId(), text);
                    }
            } else{
                switch (messageText) {
                    case "/start":
                        registerUser(update.getMessage());
                        startCommandRecieved(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    case "/help":
                        sendMessage(chatId, HELP_TEXT);
                        break;
                    case "/register":
                        register(update.getMessage().getChatId());
                        break;

                    default:

                        sendMessage(chatId, "Sorry, command did not recognized!");

                }
            }

        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("YES_BUTTON")) {
                String text = "You pressed YES button";
                EditMessageText message = new EditMessageText();
                message.setChatId(chatId);
                message.setText(text);
                message.setMessageId((int)messageId);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error("Error occured!: " + e + " " + e.getMessage());
                }
            }else if (callbackData.equals("NO_BUTTON")) {
                String text = "You pressed NO button";
                EditMessageText message = new EditMessageText();
                message.setChatId(chatId);
                message.setText(text);
                message.setMessageId((int)messageId);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error("Error occured!: " + e + " " + e.getMessage());
                }
            }
        }
    }

    private void register(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton yesButton = new InlineKeyboardButton("Yes");
        yesButton.setCallbackData("YES_BUTTON");

        InlineKeyboardButton noButton = new InlineKeyboardButton("No");
        noButton.setCallbackData("NO_BUTTON");

        rowInline.add(yesButton);
        rowInline.add(noButton);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occured!: " + e + " " + e.getMessage());
        }
    }

    private void registerUser(Message msg) {
        if(!userRepository.findById(msg.getChatId()).isPresent()){
            Long chatId = msg.getChatId();
            Chat chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());

            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            log.info("new user saved: " + user);
        }
    }


    private void startCommandRecieved(long chatId, String name) {

//        String answer = "Hi, " + name + "! Welcome to my bot!";
        String answer = EmojiParser.parseToUnicode("Hi, " + name + "! Welcome to my bot!" + " :blush:");
        log.info("Replied to user " + name + " chatId: " + chatId);
        sendMessage(chatId, answer);

    }

    private void sendMessage(long chatId, String messageToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        // первый ряд кнопок
        row.add("weather");
        row.add("get random joke");

        keyboardRows.add(row);

        // второй ряд кнопок
        row = new KeyboardRow();

        row.add("register");
        row.add("check my data");
        row.add("delete my data");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occured!: " + e + " " + e.getMessage());
        }


    }
}
