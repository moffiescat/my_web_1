package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.User;

import java.util.Optional;

@Mapper
public interface UserMapper {

    User save(User user);

    Optional<User> findById(@Param("id") Long id);

    Optional<User> findByUsername(@Param("username") String username);

    boolean existsByUsername(@Param("username") String username);
}
