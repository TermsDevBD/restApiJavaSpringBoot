package com.example.restapi.service;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.Key;
import java.util.Properties;
import ch.qos.logback.core.joran.sanity.Pair;
import com.example.restapi.persistance.UserInfo;
import com.example.restapi.persistance.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Clock;

import jakarta.persistence.Tuple;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.security.SecureRandom;
@Service
public class UserService {

    UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public List<UserInfo> getAllUsers() {
        return this.userRepository.findAll();
    }
    public UserInfo getUserById(Long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public String addUser(UserInfo user) {

        if(this.userRepository.existsByUsername(user.getUsername())){
            return "User already exist";
        }else {

            String verifyToken = createVerifyToken();
            UserInfo userInfo = prepareUserInfo(user,verifyToken);

            EmailSendingClass.sendMyEmail(verifyToken,user.getEmail());
            this.userRepository.save(userInfo);

            return "User registration successfully done! Please check your email to verify your email.";
        }
    }
    public class EmailSendingClass {
        public static void sendMyEmail(String verifyToken,String toEmail) {
            try {
                String fromEmail = "farhadhossain379@gmail.com";
                String fromPassword = "msyfvkgppmysixmg";
                String subject = "Please verify your email address";
                String body=  "Please click here to verify your email <a href=http://localhost:8080/users/verify?token=" + verifyToken + "></a>";
                //http://localhost:8080/users/verify?token=4e43184e-e801-4a59-92b4-c05631a0a936

                EmailSender.sendEmail(fromEmail, fromPassword, toEmail, subject,body);

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }
    public class EmailSender {
        public static void sendEmail(String fromEmail, String fromPassword, String toEmail, String subject, String body) throws MessagingException {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com"); // set the hostname for Gmail SMTP server
            props.put("mail.smtp.port", "587"); // set the port for Gmail SMTP server
            props.put("mail.smtp.starttls.enable", "true"); // enable TLS encryption
            props.put("mail.smtp.auth", "true"); // enable authentication

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, fromPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        }
    }
    public  UserInfo prepareUserInfo(UserInfo user, String verifyToken){



        Dictionary dict = createPasswordHash(user.getPassword());

        String passwordHash = (String) dict.get("passwordHash");
        String passwordSalt = (String) dict.get("passwordSalt");

        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setPassword_hash(passwordHash);
        userInfo.setPassword_salt(passwordSalt);
        userInfo.setVerifytoken(verifyToken);

        return userInfo;
    }
    public Dictionary createPasswordHash(String password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(password);

        String passwordHash = Base64.getEncoder().encodeToString(hashedPassword.getBytes());
        String passwordSalt = Base64.getEncoder().encodeToString(salt);

        Dictionary<String, String> dict = new Hashtable<>();

        // Add key-value pairs to the Dictionary
        dict.put("passwordHash", passwordHash);
        dict.put("passwordSalt", passwordSalt);


        return dict;
    }
    public String createVerifyToken(){

        String randomUUID = UUID.randomUUID().toString();
        return randomUUID;
    }
    public void deleteUser(Long id) {
        this.userRepository.deleteById(id);
    }

    public UserInfo verifyUserByToken(String token){

        var user =  this.userRepository.findByVerifytoken(token);
        if(user !=null){
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expDate = now.plus(1, ChronoUnit.DAYS);

            user.setVerify_token_date(now);
            user.setVerify_token_exp_date(expDate);

            this.userRepository.save(user);
        }
        return user;

    }

    public String userLogin(UserInfo userInfo) {

        UserInfo user = this.userRepository.findByUsername(userInfo.getUsername());
        if(user == null){
            return "User does not exist!";
        }

        if(user.getVerify_token_date() == null){
            return "User has not been verified yet!";
        }

        if(user == null) {
            if(!verifyPasswordHash(user)){
                return "Invalid password";
            }
        }

        String JWT_Login_Token= createToken(user);
        if(JWT_Login_Token !=null){
            return "Login successful! And your login token is:\n" + JWT_Login_Token;
        }else {
            return "Please check your username and password";
        }

    }
//    public static String createToken(UserInfo user) {
//
//        long housekeepingInterval = 3600L; // 1 hour in seconds
//        JwtParser parser = Jwts.parserBuilder()
//                .setClock(new io.jsonwebtoken.Clock() {
//                    @Override
//                    public Date now() {
//                        return new Date();
//                    }
//                })
//                .setAllowedClockSkewSeconds(housekeepingInterval)
//                .build();
//        // Create a map of claims
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("username", user.getUsername());
//        claims.put("role", "Manager");
//
//        // Generate a signing key using a secret key string
//        // later i will put it in environment file
//        String secretKeyString = "5yPJYt9FWowR/V5pDdCIOdfkwzzi7rL+RuChzF7Pv2Q";
//        Key key = Keys.hmacShaKeyFor(secretKeyString.getBytes());
//
//        // Build the JWT token
//        JwtBuilder builder = Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day expiration
//                .signWith(key, SignatureAlgorithm.HS512);
//
//        // Get the token string
//        String token = builder.compact();
//
//        return token;
//    }
    public static String createToken(UserInfo user) {

            long housekeepingInterval = 3600L; // 1 hour in seconds
            JwtParser parser = Jwts.parserBuilder()
                    .setClock(new io.jsonwebtoken.Clock() {
                        @Override
                        public Date now() {
                            return new Date();
                        }
                    })
                    .setAllowedClockSkewSeconds(housekeepingInterval)
                    .build();
        // Create a map of claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", "Manager");

        // Generate a signing key using a secret key string
        // later i will put it in environment file

//        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
//        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());


        String encodedKey = "6NxyNLxN42xwv9lceRuAsOhPPBCmGbMftVyp9OHaTwSstZYug3riEpjjq6gYiOO9wirZGUCdkRbNs1Aq7JdPww==";

        // Build the JWT token
        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day expiration
                .signWith(SignatureAlgorithm.HS512, encodedKey);

        // Get the token string
        String token = builder.compact();

        return token;
    }
    public boolean verifyPasswordHash(UserInfo  user){
        // Retrieve the user's salt and hashed password from the database
        String savedPasswordHash = user.getPassword_hash();
        String savedPasswordSalt = user.getPassword_salt();

        // Decode the salt and hashed password from Base64 encoding
        byte[] decodedSalt = Base64.getDecoder().decode(savedPasswordSalt);
        byte[] decodedHash = Base64.getDecoder().decode(savedPasswordHash);

        // Hash the user's input password using the same salt that was used to hash the original password
        String userPassword = user.password;
        String hashedPassword = new BCryptPasswordEncoder().encode(userPassword + new String(decodedSalt));

        // Compare the hashed password obtained  with the original hashed password retrieved from the database
        if (Arrays.equals(decodedHash, Base64.getDecoder().decode(hashedPassword))) {
            // Passwords match, login successful
            return true;
        } else {
            // Passwords do not match, login failed
            return false;
        }
    }

    public  String userAuthenticationByJwtToken(String token){

        String jwtToken = token.substring(7); // remove "Bearer " prefix

        try {
            String encodedKey = "6NxyNLxN42xwv9lceRuAsOhPPBCmGbMftVyp9OHaTwSstZYug3riEpjjq6gYiOO9wirZGUCdkRbNs1Aq7JdPww==";
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            Key key = Keys.hmacShaKeyFor(keyBytes);

            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtToken);
            return "Secure resource accessed successfully!";
        } catch (Exception e) {
            // handle exception or log error
            return "User is not authorized";
        }

    }

}
