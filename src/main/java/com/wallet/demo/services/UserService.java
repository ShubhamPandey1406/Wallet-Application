package com.wallet.demo.services;

import com.wallet.demo.entities.User;
import com.wallet.demo.repositories.UserRepository;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public User createUser(User user)
    {
        log.info("Creating User: {} ", user.getEmail());
        User newUser= userRepository.save(user);
        log.info("User created with id {} in database shardwallet{}" , newUser.getId(),(newUser.getId() % 2 + 1));
        return newUser;
    }




}
