package service;

import model.User;

import java.util.Map;

import static db.DataBase.addUser;
import static db.DataBase.matchUserByIdAndPassword;

public class UserService {

    public void save(Map<String, String> userInfo) {
        saveUser(User.of(userInfo));
    }

    private void saveUser(User user) {
        if (user != null) {
            addUser(user);
        }
    }
}
