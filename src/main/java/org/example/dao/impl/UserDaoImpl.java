package org.example.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.UserDao;
import org.example.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (ResultSet rs, int rowNum) -> {
        User user = new User();
        try {
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setEmail(rs.getString("email"));
        } catch (SQLException e) {
            log.error("用户数据映射失败: {}", e.getMessage());
        }
        return user;
    };

    @Override
    public Optional<User> findByUsername(String username) {
        log.info("根据用户名查询用户: {}", username);
        try {
            String sql = "SELECT id, username, password, email FROM users WHERE username = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            log.info("用户查询成功: username={}, id={}", username, user.getId());
            return Optional.of(user);
        } catch (Exception e) {
            log.warn("用户不存在: {}", username);
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        log.debug("检查用户名是否存在: {}", username);
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
            boolean exists = count != null && count > 0;
            log.debug("用户名 {} 存在: {}", username, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查用户名存在性失败: {}, error={}", username, e.getMessage());
            return false;
        }
    }

    @Override
    public User save(User user) {
        log.info("保存用户: username={}", user.getUsername());
        try {
            String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                var ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                return ps;
            }, keyHolder);
            
            Long id = keyHolder.getKey().longValue();
            user.setId(id);
            log.info("用户保存成功: username={}, id={}", user.getUsername(), id);
            return user;
        } catch (Exception e) {
            log.error("用户保存失败: username={}, error={}", user.getUsername(), e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        log.info("根据ID查询用户: {}", id);
        try {
            String sql = "SELECT id, username, password, email FROM users WHERE id = ?";
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            log.info("用户查询成功: id={}, username={}", id, user.getUsername());
            return Optional.of(user);
        } catch (Exception e) {
            log.warn("用户不存在: id={}", id);
            return Optional.empty();
        }
    }
}
