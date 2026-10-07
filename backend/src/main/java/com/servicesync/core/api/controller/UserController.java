package com.servicesync.core.api.controller;

import com.servicesync.core.api.dto.UserDTO;
import com.servicesync.core.api.mapper.DtoMapper;
import com.servicesync.core.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getTechnicians() {
        return userService.getTechnicians().stream()
                .map(DtoMapper::toUserDTO)
                .toList();
    }
}
