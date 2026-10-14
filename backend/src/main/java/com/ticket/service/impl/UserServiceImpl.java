package com.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticket.entity.User;
import com.ticket.mapper.UserMapper;
import com.ticket.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public List<User> listAll() {
        return list(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1)
                .select(User::getId, User::getUsername, User::getRealName, User::getDeptId));
    }
}
