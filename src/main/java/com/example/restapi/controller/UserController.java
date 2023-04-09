package com.example.restapi.controller;

import com.example.restapi.persistance.UserInfo;
import com.example.restapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping(value = "/users")
    public List<UserInfo> getAllUser(){

        return this.userService.getAllUsers();
    }
    @GetMapping(value ="/users/{id}")
    public UserInfo getUserById(@PathVariable Long id){
        return this.userService.getUserById(id);
    }
    @PostMapping(value = "/users")
    public String addUser(@RequestBody UserInfo user){
        return this.userService.addUser(user);
    }
    @DeleteMapping(value = "/users/{id}")
    public void deleteUser(@PathVariable long id){
        this.userService.deleteUser(id);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {

        UserInfo user =  this.userService.verifyUserByToken(token);

        if(user != null){
            return ResponseEntity.ok("Email verification has successfully done!");

        }else return ResponseEntity.ok("Sorry!. User does not exists!");

    }

    @PostMapping(value = "/login")
    public String userLogin(@RequestBody UserInfo user){
        return this.userService.userLogin(user);
    }


    @GetMapping("/user_authentication_jwt")
    public ResponseEntity<String> userAuthenticationByJwt(@RequestHeader("authentication") String jwtToken) {

        String str = this.userService.userAuthenticationByJwtToken(jwtToken);
        return ResponseEntity.ok(str);

    }



}
