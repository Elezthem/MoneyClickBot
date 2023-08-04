package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends TelegramLongPollingBot {


    private ReplyKeyboardMarkup previousKeyboardMarkup;
    private Map<Long, String> userMessages = new HashMap<>();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moneyclick";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "8001";
    private static final String NR = "Новую регистрацию !";
    private HashMap<Long, Boolean> awaitingPhoto = new HashMap<>();

    private static final String NoAccess = "Увы! Но у вас нет должного доступа для выполнение команды!";
    private HashMap<Long, Integer> awaitingIntegers = new HashMap<>();
    int WithdrawalAmount = 0;

    private static int KeyBoard = 0;
    private String currentChatId;
    public static final String owner = "";
    public int rubs;

    public void onUpdateReceived(Update update) {
        SendMessage card = null;
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();

            if (callbackData.equals("Card")) {
                card = new SendMessage();
                card.setChatId(currentChatId);
                int link = Seach.findReferralLinkById(DB_URL, DB_USERNAME, DB_PASSWORD, currentChatId);
                double balance = Seach.findBalanceById(DB_URL, DB_USERNAME, DB_PASSWORD, currentChatId);
                System.out.println(link);
                if (link >= 15) {
                    if (balance >= 100) {
                        card.setText("Введите номер карты: ");

                        // Создание клавиатуры для ввода номера карты
                        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                        keyboardMarkup.setResizeKeyboard(true);
                        keyboardMarkup.setOneTimeKeyboard(true);

                        // Создание списка кнопок
                        List<KeyboardRow> keyboard = new ArrayList<>();
                        KeyboardRow row = new KeyboardRow();
                        row.add("Назад"); // Кнопка "Отмена" для отмены ввода
                        keyboard.add(row);

                        // Назначение клавиатуры сообщению
                        card.setReplyMarkup(keyboardMarkup);
                        keyboardMarkup.setKeyboard(keyboard);
                    } else {
                        card.setText("На вашем балансе менее 100 рублей, невозможно осуществить вывод");
                    }
                } else {
                    card.setText("Выполните условие!");
                }
            } else if (callbackData.equals("Qiwi")) {
                card = new SendMessage();
                card.setChatId(currentChatId);
                int link = Seach.findReferralLinkById(DB_URL, DB_USERNAME, DB_PASSWORD, currentChatId);
                double balance = Seach.findBalanceById(DB_URL, DB_USERNAME, DB_PASSWORD, currentChatId);
                System.out.println(link);
                if (link >= 15) {
                    if (balance >= 100) {
                        card.setText("Способ вывода на киви сейчас не работает!");
                    }else {
                        card.setText("На балансе недостаточно средств!");
                    }
                } else {
                    card.setText("Выполните условие!");
                }
            }
            try {
                execute(card);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String user = update.getMessage().getFrom().getFirstName();
            long chatId = update.getMessage().getChatId();
            String chatIdStr = String.valueOf(chatId);
            currentChatId = String.valueOf(chatId);
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy|HH/mm");
            String localTime = now.format(formatter);
            String date = " | " + user + " | ID: " + chatId + " | Время запроса: " + localTime;
            SendMessage message = new SendMessage();
            String messageText = update.getMessage().getText();
            long userId = update.getMessage().getFrom().getId();
            SendMessage refmess = new SendMessage();
            String insertSql = "INSERT INTO users (id, name, balance, peoples, titul, referalLink, verification, changeRL) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            if (messageText.startsWith("/start")) {
                String[] args = messageText.split(" ");
                String ref = String.join(" ", args);
                ReplyKeyboardMarkup keyboardMarkup = createDefaultKeyboardMarkup();
                message = new SendMessage();
                message.setChatId(chatIdStr);
                message.setReplyMarkup(keyboardMarkup);
                if (isUserMember((int) userId)) {
                if (!Seach.checkUserExistence(DB_URL, DB_USERNAME, DB_PASSWORD, chatIdStr)) {
                    message = new SendMessage();
                    message.setChatId(chatIdStr);

                        String ReferalLink = "https://t.me/Sh0cksBot?start=" + chatIdStr;

                        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                            PreparedStatement preparedStatement = connection.prepareStatement(insertSql);
                            preparedStatement.setString(1, chatIdStr);
                            preparedStatement.setString(2, user);
                            preparedStatement.setDouble(3, 0);
                            preparedStatement.setInt(4, 0);
                            preparedStatement.setString(5, "user");
                            preparedStatement.setString(6, ReferalLink);
                            preparedStatement.setInt(7, 0);
                            preparedStatement.setInt(8, 0);
                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                            String referralId = args.length > 1 ? args[1] : "";

                            if (referralId.isEmpty()) {
                                System.out.println("хуесос!");
                            } else {
                                message = new SendMessage();
                                message.setChatId(chatIdStr);
                                String updateSql = "UPDATE users SET peoples = peoples + 1 WHERE id = ?";
                                PreparedStatement updateStatement = connection.prepareStatement(updateSql);
                                updateStatement.setString(1, referralId);
                                updateStatement.executeUpdate();
                                updateStatement.close();

                                // Получение значения поля "peoples" из базы данных
                                String selectSql = "SELECT peoples, referalLink  FROM users WHERE id = ?";
                                PreparedStatement selectStatement = connection.prepareStatement(selectSql);
                                selectStatement.setString(1, referralId);
                                ResultSet resultSet = selectStatement.executeQuery();
                                if (resultSet.next()) {
                                    int peoples = resultSet.getInt("peoples");
                                    String reflink = resultSet.getString("referalLink");
                                    refmess.setChatId(referralId);
                                    refmess.setText("По твоей ссылке пресоединились в бота!" + "\n" + "Вы пригласили " + peoples + " из 15" + "\n" + "Твоя ссылка для приглашения друзей: " + "\n" + reflink);
                                }
                                resultSet.close();
                                selectStatement.close();

                                message.setText("Кликай и получай монетки!");
                                message.setReplyMarkup(keyboardMarkup);
                                System.out.println("Подключение базы данных, результат: " + NR);
                                execute(refmess);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("Ошибка при подключении к базе данных!");
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                } else {
                        message = new SendMessage();
                        message.setChatId(chatIdStr);
                        message.setText("Привет, " + user + "! Добро пожаловать в наш бот.");
                        message.setReplyMarkup(keyboardMarkup);
                    }
                }else {
                    message = new SendMessage();
                    message.setChatId(chatIdStr);
                    message.setText("Вступите в группу: https://t.me/money_clickers" + " чтобы начать!");
                }
            } else if (messageText.equalsIgnoreCase("профиль")) {
                message = new SendMessage();
                message.setChatId(chatIdStr);
                String selectSql = "SELECT * FROM users WHERE id = ?";
                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                    PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
                    preparedStatement.setString(1, chatIdStr);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        String userName = resultSet.getString("name");
                        int balance = resultSet.getInt("balance");
                        int PI = resultSet.getInt("peoples");
                        int Verify = resultSet.getInt("verification");

                        message.setText(
                                "Имя пользователя: " + userName + "\n" +
                                        "ID: " + chatIdStr + "\n" +
                                        "Баланс: " + balance + "руб." + "\n" +
                                        "Приглашено людей: " + PI + "чел." + "\n" +
                                        "Верификация: " + Verify + "\n"
                        );

                        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                        List<InlineKeyboardButton> row = new ArrayList<>();

                        keyboard.add(row);

                        row = new ArrayList<>();
                        if (PI < 15) {
                            InlineKeyboardButton InviteButton = new InlineKeyboardButton();
                            InviteButton.setText("Пригласить людей");
                            InviteButton.setCallbackData("Invite");
                            row.add(InviteButton);
                        }

                        keyboard.add(row);

                        keyboardMarkup.setKeyboard(keyboard);

                        message.setReplyMarkup(keyboardMarkup);

                    }

                    resultSet.close();
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Ошибка при подключении к базе данных!");
                }
            } else if (messageText.equalsIgnoreCase("топ")) {
                message = new SendMessage();
                message.setChatId(chatIdStr);
                message.setText("Топа нету!");
                //message.setText(Seach.TopInBalance(DB_URL, DB_USERNAME, DB_PASSWORD, localTime));
            } else if (messageText.equalsIgnoreCase("Зарабатывать")) {
                message = new SendMessage();
                message.setChatId(chatIdStr);
                ReplyKeyboardMarkup keyboardMarkup = createEarnKeyboardMarkup();
                message.setText("Погнали!");
                message.setReplyMarkup(keyboardMarkup);
            }else if (messageText.equalsIgnoreCase("Клик")) {
                message = new SendMessage();
                message.setChatId(chatIdStr);
                message.setText("+0.02 руб.");
                String selectSql = "SELECT * FROM users WHERE id = ?";
                    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                        PreparedStatement selectStatement = connection.prepareStatement(selectSql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        selectStatement.setString(1, chatIdStr);
                        ResultSet resultSet = selectStatement.executeQuery();

                        if (resultSet.next()) {
                            double balance = resultSet.getDouble("balance");

                            double newBalance = balance + 0.02;

                            resultSet.updateDouble("balance", newBalance);
                            resultSet.updateRow();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
            } else if (messageText.equalsIgnoreCase("Реферальная система")) {
                message = new SendMessage();
                message.setChatId(chatIdStr);

                try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                    String selectSql = "SELECT referalLink FROM users WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
                    preparedStatement.setString(1, chatIdStr);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        String referralLink = resultSet.getString("referalLink");
                        message.setText("Ваша реферальная ссылка: " + referralLink);
                    } else {
                        message.setText("Реферальная ссылка не найдена.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    message.setText("Ошибка при получении реферальной ссылки.");
                }
            }else if (messageText.startsWith("Вывести")) {
                String[] args = messageText.split(" ");
                if (args.length > 1 && !args[1].isEmpty()) {
                    int WithdrawalAmountt = Integer.parseInt(args[1]);
                    WithdrawalAmount = WithdrawalAmountt;
                    message = new SendMessage();
                    message.setChatId(chatIdStr);
                    InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

                    List<InlineKeyboardButton> row = new ArrayList<>();

                    keyboard.add(row);

                    row = new ArrayList<>();
                    message.setText("Выберите на что пополнять:");
                    InlineKeyboardButton cardButton = new InlineKeyboardButton();
                    cardButton.setText("Карта");
                    cardButton.setCallbackData("Card");
                    row.add(cardButton);
                    InlineKeyboardButton qiviButton = new InlineKeyboardButton();
                    qiviButton.setText("Qiwi (ник)");
                    qiviButton.setCallbackData("Qiwi");
                    row.add(qiviButton);

                    keyboard.add(row);
                    keyboardMarkup.setKeyboard(keyboard);
                    message.setReplyMarkup(keyboardMarkup);
                } else {
                    message = new SendMessage();
                    message.setChatId(chatIdStr);
                    message.setText("После 'Вывести' введите сумму!");
                }
            } else if (messageText.equalsIgnoreCase("Тех. поддержка")) {
                message = new SendMessage();
                message.setChatId(chatIdStr);
                message.setText("Если у вас возникли вопросы, обращайтесь к https://t.me/rea_ly_go");
            } else if (messageText.equalsIgnoreCase("назад")) {
                message = new SendMessage();
                message.setChatId(chatIdStr);
                message.setText("Назад!");

                KeyBoard = (KeyBoard == 1) ? 2 : (KeyBoard == 2) ? 0 : 0;
                ReplyKeyboardMarkup keyboardMarkup = (KeyBoard == 1 || KeyBoard == 2) ? createEarnKeyboardMarkup() : createDefaultKeyboardMarkup();

                message.setReplyMarkup(keyboardMarkup);
            }else if (messageText.matches("\\d{16}")){
                message = new SendMessage();
                message.setChatId(chatIdStr);
                message.setText("Ожидайте вывод в размере " + WithdrawalAmount + " на карту " + messageText);

                SendMessage ownmess = new SendMessage();
                ownmess.setChatId(chatIdStr);
                ownmess.setText("Выполнен запрос на пополнение баланса. Карта:  " + messageText + " Сумма: " + WithdrawalAmount);
                try {
                    execute(ownmess);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private ReplyKeyboardMarkup createDefaultKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton EarnButton = new KeyboardButton("Зарабатывать");
        EarnButton.setText("Зарабатывать");
        row.add(EarnButton);
        KeyboardButton RSButton = new KeyboardButton("Реферальная система");
        RSButton.setText("Реферальная система");
        row.add(RSButton);
        keyboardRows.add(row);

        row = new KeyboardRow();
        KeyboardButton profileButton = new KeyboardButton("Профиль");
        profileButton.setText("Профиль");
        row.add(profileButton);
        KeyboardButton WithdrawButton = new KeyboardButton("Вывести");
        WithdrawButton.setText("Вывести");
        row.add(WithdrawButton);
        keyboardRows.add(row);

        row = new KeyboardRow();
        KeyboardButton tsButton = new KeyboardButton("Тех. поддержка");
        tsButton.setText("Тех. поддержка");
        row.add(tsButton);
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }


    private ReplyKeyboardMarkup createEarnKeyboardMarkup() {

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton ClickButton = new KeyboardButton("Клик");
        ClickButton.setText("Клик");
        row.add(ClickButton);
        KeyboardButton BackButton = new KeyboardButton("Назад");
        BackButton.setText("Назад");
        row.add(BackButton);

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }
    public boolean isUserMember( int userId) {
        String chatIdGroup = "-1001954270628";
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(chatIdGroup);
        getChatMember.setUserId((long) userId);

        try {
            ChatMember chatMember = execute(getChatMember);

            if (chatMember instanceof ChatMemberMember) {
                return true;
            } else if (chatMember instanceof ChatMemberLeft ||
                    chatMember instanceof ChatMemberBanned) {
                return false;
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return false;
    }


    public String getBotUsername() {
        return "Sh0cksBot";
    }

    public String getBotToken() {
        return "5719729995:AAHf3yr18-BYwe-KvPzeokDtO-368MaOkbg";
    }

    public static void main(String[] args) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new App());
            System.out.println("Bot started successfully!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
