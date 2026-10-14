package com.ticket.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private Long userId;
    private String username;
    private String realName;
    private List<String> roles;
    private List<String> permissions;
}