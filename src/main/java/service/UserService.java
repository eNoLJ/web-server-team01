package service;

import model.User;

import java.util.Map;

import static db.DataBase.addUser;

public class UserService {

    public static void save(Map<String, String> userInfo) {
        saveUser(User.of(userInfo));
    }

    private static void saveUser(User user) {
        if (user != null) {
            addUser(user);
        }
    }
}
