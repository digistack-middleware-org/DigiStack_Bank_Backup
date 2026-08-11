package com.digistack.bank.util;

import org.mindrot.jbcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        String plainPassword = "Password123!";
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        System.out.println("Hash: " + hash);
    }
}