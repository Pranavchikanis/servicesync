package com.servicesync.core.service;

import com.servicesync.core.domain.Role;
import com.servicesync.core.domain.User;
import com.servicesync.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getTechnicians() {
        return userRepository.findByRole(Role.TECH);
    }
}
