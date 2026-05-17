package com.parking.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.system.entity.User;

public interface UserService extends IService<User> {

    User login(String username, String password);

    boolean register(User user);

    User getByUsername(String username);

}