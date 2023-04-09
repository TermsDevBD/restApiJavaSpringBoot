package com.example.restapi.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user_info")
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;

    private String email;

    @Transient
    public String password;
    private String password_hash;
    private String password_salt;
    private String verifytoken;
    @Column(name = "verify_token_date", columnDefinition = "datetime2(7)")
    private LocalDateTime verify_token_date;
    @Column(name = "verify_token_exp_date", columnDefinition = "datetime2(7)")
    private LocalDateTime verify_token_exp_date;


}



//private String password_hash;




//
//    @Column(name = "password_salt")
//    private String passwordSalt;
//
//    @Column(name = "verify_token")
//    private String verifyToken;
//
//    @Column(name = "verify_token_date", columnDefinition = "datetime2(7)")
//    private LocalDateTime verifyTokenDate;
//
//    @Column(name = "verify_token_exp_date", columnDefinition = "datetime2(7)")
//    private LocalDateTime verifyTokenExpDate;
