package org.example.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.UserDao;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserDaoImpl implements UserDao {

    private final UserMapper userMapper;

    @Override
    public Optional<User> findByUsername(String username) {
        log.info("根据用户名查询用户: {}", username);
        Optional<User> user = userMapper.findByUsername(username);
        if (user.isPresent()) {
            log.info("用户查询成功: username={}, id={}", username, user.get().getId());
        } else {
            log.warn("用户不存在: {}", username);
        }
        return user;
    }

    @Override
    public boolean existsByUsername(String username) {
        log.debug("检查用户名是否存在: {}", username);
        boolean exists = userMapper.existsByUsername(username);
        log.debug("用户名 {} 存在: {}", username, exists);
        return exists;
    }

    @Override
    public User save(User user) {
        log.info("保存用户: username={}", user.getUsername());
        userMapper.save(user);
        log.info("用户保存成功: username={}, id={}", user.getUsername(), user.getId());
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        log.info("根据ID查询用户: {}", id);
        Optional<User> user = userMapper.findById(id);
        if (user.isPresent()) {
            log.info("用户查询成功: id={}, username={}", id, user.get().getUsername());
        } else {
            log.warn("用户不存在: id={}", id);
        }
        return user;
    }
}
