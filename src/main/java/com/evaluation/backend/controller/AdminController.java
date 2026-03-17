package com.evaluation.backend.controller;

import com.evaluation.backend.dto.Admin.AdminUserDTO;
import com.evaluation.backend.entity.Authentification;
import com.evaluation.backend.repository.AuthentificationRepository;
import com.evaluation.backend.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/enseignants")
    public List<AdminUserDTO> getEnseignants() {
        return adminService.getEnseignants();
    }

    @PutMapping("/toggle/{id}")
    public void toggleUser(@PathVariable Long id) {
        adminService.toggleUser(id);
    }

    @GetMapping("/etudiants")
    public List<AdminUserDTO> getEtudiants() {
        return adminService.getEtudiants();
    }
}
