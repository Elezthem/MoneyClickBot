package org.example;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.*;
import java.util.Random;

public class Seach {

    public static String TopInBalance(String DB_URL, String DB_USERNAME, String DB_PASSWORD, String localTime) {
        StringBuilder topList = new StringBuilder();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT id, balance FROM users ORDER BY balance DESC LIMIT 10";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    int number = 1;
                    while (resultSet.next()) {
                        long id = resultSet.getInt("id");
                        double balance = resultSet.getDouble("balance");
                        topList.append("[").append(number).append("] ID: ").append(id).append(", Баланс: ").append(balance).append("\n");
                        number++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Топ пользователей на " + localTime + ":\n" + topList.toString();
    }
    public static String generateRandomLetters(int count) {
        char[] letters = "abcdefghijklmnopqrstuvwxyz".toCharArray();

        Random random = new Random();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++) {

            int randomIndex = random.nextInt(letters.length);

            sb.append(letters[randomIndex]);
        }

        return sb.toString();
    }
    public static String generateInviteLink(String DB_URL, String DB_USERNAME, String DB_PASSWORD, String id) {
        String inviteLink = generateRandomLetters(10);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String updateSql = "UPDATE users SET referalLink = ? WHERE id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            updateStatement.setString(1, inviteLink);
            updateStatement.setString(2, id);

            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Invite link generated and updated successfully!");
            } else {
                System.out.println("Failed to update invite link.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return inviteLink;
    }
    public static int findReferralLinkById(String DB_URL, String DB_USERNAME, String DB_PASSWORD, String id) {
        int peoples = 0;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String selectSql = "SELECT peoples FROM users WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                peoples = resultSet.getInt("peoples");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return peoples;
    }
    public static double findBalanceById(String DB_URL, String DB_USERNAME, String DB_PASSWORD, String id) {
        double balance = 0;

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String selectSql = "SELECT balance FROM users WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                balance = resultSet.getInt("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }




    public static boolean searchUser(String DB_URL, String DB_USERNAME, String DB_PASSWORD, String name, String chat_id) {
        boolean found = false;
        String sqlQuery = "SELECT id FROM users WHERE id = ? AND name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = conn.prepareStatement(sqlQuery)) {
            statement.setString(1, chat_id);
            statement.setString(2, name);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                found = true;
                int idtelegram = resultSet.getInt("chat_id");
                System.out.println("chat_id: " + idtelegram);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching user: " + e.getMessage(), e);
        }

        return found;
    }
    public static int getIDBalance(String DB_URL, String DB_USERNAME, String DB_PASSWORD, String recipientId, String chat_id) {
        int balance = 0;
        String query = "SELECT balance FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, Long.parseLong(chat_id));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    balance = resultSet.getInt("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }
    public static int getBalance(String DB_URL, String DB_USERNAME, String DB_PASSWORD, String chat_id) {
        int balance = 0;
        String query = "SELECT balance FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, Long.parseLong(chat_id));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    balance = resultSet.getInt("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }
    public static int getBalanceByChatId(String DB_URL, String DB_USERNAME, String DB_PASSWORD, String chat_id) {
        String query = "SELECT balance FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, chat_id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void updateBalance(String DB_URL, String DB_USERNAME, String DB_PASSWORD, String recipientId, String chat_id, int amount) {
        // Получение текущего баланса отправителя
        int senderBalance = getBalanceByChatId(DB_URL, DB_USERNAME, DB_PASSWORD, chat_id);

        // Получение текущего баланса получателя
        int recipientBalance = getBalance(DB_URL, DB_USERNAME, DB_PASSWORD, recipientId);

        // Вычисление новых балансов
        int senderNewBalance = senderBalance - amount;
        int recipientNewBalance = recipientBalance + amount;

        String senderQuery = "UPDATE users SET balance = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement senderStatement = connection.prepareStatement(senderQuery)) {
            senderStatement.setInt(1, senderNewBalance);
            senderStatement.setString(2, chat_id);
            senderStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String recipientQuery = "UPDATE users SET balance = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement recipientStatement = connection.prepareStatement(recipientQuery)) {
            recipientStatement.setInt(1, recipientNewBalance);
            recipientStatement.setString(2, recipientId);
            recipientStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkUserExistence(String DB_URL, String DB_USERNAME, String DB_PASSWORD, String id) {
        String query = "SELECT COUNT(*) as count FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
