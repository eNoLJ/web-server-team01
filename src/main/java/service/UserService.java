package service;

import model.User;

import java.util.Map;

import static db.DataBase.addUser;
import static db.DataBase.matchUserByIdAndPassword;

public class UserService {

    public void save(Map<String, String> userInfo) {
        addUser(User.of(userInfo));
    }

    public boolean login(Map<String, String> userInfo) {
        return matchUserByIdAndPassword(userInfo.get("userId"), userInfo.get("password"));
    }
}
