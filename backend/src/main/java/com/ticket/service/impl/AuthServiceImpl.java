package com.ticket.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ticket.common.BusinessException;
import com.ticket.dto.LoginDTO;
import com.ticket.entity.Permission;
import com.ticket.entity.Role;
import com.ticket.entity.User;
import com.ticket.mapper.PermissionMapper;
import com.ticket.mapper.RoleMapper;
import com.ticket.mapper.UserMapper;
import com.ticket.security.JwtTokenProvider;
import com.ticket.service.AuthService;
import com.ticket.vo.LoginVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, loginDTO.getUsername()));

        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用或不存在");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        redisTemplate.opsForValue().set("token:" + user.getUsername(), token, 24, TimeUnit.HOURS);

        List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(user.getId());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoles(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));
        vo.setPermissions(permissions.stream().map(Permission::getPermCode).collect(Collectors.toList()));

        return vo;
    }

    @Override
    public void logout(String token) {
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            redisTemplate.delete("token:" + username);
        } catch (Exception ignored) {
        }
    }
}