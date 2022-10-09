package ru.hse.avk_spingbot.controller;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
import ru.hse.avk_spingbot.entity.Announcement;
import ru.hse.avk_spingbot.repository.AnnouncementRepository;
import ru.hse.avk_spingbot.entity.User;
import ru.hse.avk_spingbot.repository.BashQuoteRepository;
import ru.hse.avk_spingbot.repository.JokeRepository;
import ru.hse.avk_spingbot.repository.UserRepository;
import ru.hse.avk_spingbot.service.WeatherNow;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

    static final String YES_BUTTON = "ДА";
    static final String NO_BUTTON = "НЕТ";
    public static final String ERROR_TEXT = "Error occurred!: ";
    public static final String NOT_RECOGNIZED = "Извините, команда не опознана!";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private JokeRepository jokeRepository;

    @Autowired
    private BashQuoteRepository bashQuoteRepository;
    final BotConfig config;

    final static String HELP_TEXT = "Этот БОТ сделан для изучения/проверки возможностей Spring и PostgreSQL\n\n" +
            "Команды можно ввоодить через главное меню слева или вводя текст:\n\n" +
            "Введите /start для приветственного сообщения\n\n" +
            "Введите /mydata чтобы увидеть всё что мы знаем о вас\n\n" +
            "Введите /deletedata чтобы удалить информация о себе\n\n" +
            "Введите /register чтобы зарегистрироваться\n\n" +
            "Введите /weather {название_города} чтобы получить информацию о погоде\n\n" +
            "Введите /joke чтобы получить случайный анекдот\n\n" +
            "Введите /bash чтобы получить случайную цитату с BASH.ORG\n\n" +
            "Введите /help чтобы полчить это сообщение снова";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "получить приветствие"));
        listofCommands.add(new BotCommand("/mydata", "получить информацию о себе"));
        listofCommands.add(new BotCommand("/deletedata", "удалить свои данные"));
        listofCommands.add(new BotCommand("/joke", "шутка"));
        listofCommands.add(new BotCommand("/bash", "цитата с БАШа"));
        listofCommands.add(new BotCommand("/register", "зарегистрироваться"));
        listofCommands.add(new BotCommand("/weather", "погода"));
        listofCommands.add(new BotCommand("/help", "справочная информация по боту"));

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

            if (messageText.contains("/sendall") && config.getOwnerId() == chatId){
                    String text = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                    sendMessageToAllUsers(text);
            } else if(messageText.contains("/weather")){
                    String city = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")+1));
                    prepareAndSendMessage(chatId, WeatherNow.getWeatherNow(city, config.getWeatherToken(), config.getWeatherURL()));
            }
            else{
                switch (messageText) {
                    case "/start":
                        registerUser(update.getMessage());
                        startCommandRecieved(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    case "/help":
                        prepareAndSendMessage(chatId, HELP_TEXT);
                        break;
                    case "/joke":
                        String joke = jokeRepository.findRandomOne();
                        prepareAndSendMessage(chatId, joke);
                        break;
                    case "/bash":
                        String bashQ = bashQuoteRepository.findRandomOne();
                        prepareAndSendMessage(chatId, bashQ);
                        break;
                    case "/mydata":
                        Optional<User> user = userRepository.findById(chatId);
                        if (user.isPresent()){
                            prepareAndSendMessage(chatId, user.toString());
                        } else {
                            prepareAndSendMessage(chatId, "Данные о пользователе не найдены!");
                        }
                        break;
                    case "/deletedata":
                        userRepository.deleteById(chatId);
                        prepareAndSendMessage(chatId, "Ваши данные были удалены!");
                        break;
                    case "/register":
                        register(update.getMessage().getChatId());
                        break;
                    default:
                        prepareAndSendMessage(chatId, NOT_RECOGNIZED);

                }
            }

        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals(YES_BUTTON)) {
                String text = "You pressed YES button";
                executeEditMessageText((int) messageId, chatId, text);
            }else if (callbackData.equals(NO_BUTTON)) {
                String text = "You pressed NO button";
                executeEditMessageText((int) messageId, chatId, text);
            }
        }
    }

    private void sendMessageToAllUsers(String text) {
        Iterable<User> allUsers = userRepository.findAll();
        for (User user: allUsers) {
            prepareAndSendMessage(user.getChatId(), text);
        }
        log.info("Sent message to everyone : " + text);
    }

    private void executeEditMessageText(int messageId, long chatId, String text) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId(messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e + " " + e.getMessage());
        }
    }

    private void register(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вы хотите зарегистрироваться?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton yesButton = new InlineKeyboardButton("Yes");
        yesButton.setCallbackData(YES_BUTTON);

        InlineKeyboardButton noButton = new InlineKeyboardButton("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInline.add(yesButton);
        rowInline.add(noButton);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInline);

        executeMessage(message);
        log.info("register called by: " + chatId);
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

        String answer = EmojiParser.parseToUnicode("Hi, " + name + "!\n Welcome to my bot!" + " :blush:");
        sendMessage(chatId, answer);
        log.info("Replied to user " + name + " chatId: " + chatId);

    }

    private void sendMessage(long chatId, String messageToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        // первый ряд кнопок
//        row.add("/weather");
        row.add("/help");
        row.add("/joke");
        row.add("/bash");

        keyboardRows.add(row);

        // второй ряд кнопок
        row = new KeyboardRow();

        row.add("/register");
        row.add("/mydata");
        row.add("/deletedata");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);


    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
            log.info("Send message to chatId: " + message.getChatId());
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e + " " + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    /**
     * Рассылка сообщения все пользователям по крону
     */
    @Scheduled(cron = "${cron.scheduler}")
    private void sendAds() {
        Iterable<Announcement> allAds = announcementRepository.findAllByIsActive(true);
        for (Announcement ad: allAds) {
            sendMessageToAllUsers(ad.getAd_text());
            ad.setIsActive(false);
            announcementRepository.save(ad);
            log.info("Ad sent to everyone");
        }
    }

    @Scheduled(cron = "${cron.morning}")
    private void morningJoke() {
        log.info("scheduler morning started");
        String joke_text = jokeRepository.findRandomOne();
        sendMessageToAllUsers("Доброе утро!\nЛовите утренний рандомный анекдот! :)");
        sendMessageToAllUsers(joke_text);
        log.info("scheduler morning stopped");
    }
}
