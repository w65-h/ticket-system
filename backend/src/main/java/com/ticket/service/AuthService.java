package com.ticket.service;

import com.ticket.dto.LoginDTO;
import com.ticket.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO loginDTO);

    void logout(String token);
}