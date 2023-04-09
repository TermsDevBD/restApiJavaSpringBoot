package com.example.restapi.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository  extends JpaRepository<UserInfo,Long> {
    boolean  existsByUsername(String username);
    //boolean existsByToken(String token);
    //UserInfo existsByVerifytoken(String verifyToken);
    UserInfo findByVerifytoken(String verifyToken);

    UserInfo findByUsername(String username);
}
