package com.example.ssilvermandistl1.Models.Email;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Random;

public class SMTPAuthenticator extends Authenticator {
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(SendMail.senderEmail, SendMail.senderPassword);
    }

//    @GetMapping(path="/forgotPassword")
//    public void sendTemporaryPassword(@RequestParam String username, @RequestParam String email) {
//        //Remove Old User From InMemoryUserDetailsManager
//        String tempNums = "1234567890";
//        String tempLowLetters = "qwertyuiopasdfghjklmnbvcxz";
//        String tempUpLetters = "QWERTYUIOPASDFGHJKLZXCVBNM";
//        String tempChars = "!@#$%^&*()[]{};:'<>,.\\/?";
//
//        StringBuilder tempPassword = new StringBuilder();
//        Random rand = new Random();
//        while(tempPassword.length() < 16) {
//            tempPassword.append(tempNums.charAt(rand.nextInt(tempNums.length())));
//            tempPassword.append(tempLowLetters.charAt(rand.nextInt(tempLowLetters.length())));
//            tempPassword.append(tempUpLetters.charAt(rand.nextInt(tempUpLetters.length())));
//            tempPassword.append(tempChars.charAt(rand.nextInt(tempChars.length())));
//        }
//
//        AppUser currentUser = userJpa.findByNameAndEmail(username, email);
//        currentUser.setPassword(tempPassword.toString());
//        userJpa.save(currentUser);
//        new SendMail(email, "Recovery Password", "Here is your temporary password: " + currentUser.getPassword());
//    }
}
