package com.nts.awspremium.controller;

import com.nts.awspremium.GoogleApi;
import com.nts.awspremium.model.*;
import com.nts.awspremium.model_system.MySQLCheck;
import com.nts.awspremium.model_system.OrderThreadCheck;
import com.nts.awspremium.repositories.*;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "/google_key")
public class GoogleKeyController {
    @Autowired
    private GoogleKeyRepository googleKeyRepository;
    String get_key(){
        try{
            GoogleKey key = googleKeyRepository.get_Google_Key();
            key.setGet_count(key.getGet_count() + 1);
            googleKeyRepository.save(key);
            return key.getKey_id();

        }catch (Exception e){
           return null;
        }
    }

}
