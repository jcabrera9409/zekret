package com.zekret.service;

import com.zekret.model.User;

public interface IUserService extends ICRUD<User, Long> {
    User register(User user);
}