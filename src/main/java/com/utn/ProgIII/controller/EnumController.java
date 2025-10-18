package com.utn.ProgIII.controller;


import com.utn.ProgIII.dto.EnumDTO;
import com.utn.ProgIII.service.implementations.EnumServiceImpl;
import com.utn.ProgIII.service.interfaces.EnumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/enums")
public class EnumController {

    @Autowired
    private EnumService enumService;

    @GetMapping("/state")
    public ResponseEntity<List<EnumDTO>> viewProductStates()
    {
        return ResponseEntity.ok().body(enumService.getAllStates());
    }

    @GetMapping("/role")
    public ResponseEntity<List<EnumDTO>> viewEmployeeRoles()
    {
        return ResponseEntity.ok().body(enumService.getAllRoles());
    }

    @GetMapping("/user-state")
    public ResponseEntity<List<EnumDTO>> viewUserStates()
    {
        return ResponseEntity.ok().body(enumService.getAllUserStates());
    }
}
