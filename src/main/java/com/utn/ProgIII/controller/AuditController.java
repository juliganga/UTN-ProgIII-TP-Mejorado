package com.utn.ProgIII.controller;

import com.querydsl.core.BooleanBuilder;
import com.utn.ProgIII.dto.AuditLogDTO;
import com.utn.ProgIII.model.Audit.AuditLog;
import com.utn.ProgIII.model.Audit.QAuditLog;
import com.utn.ProgIII.repository.AuditLogRepository;
import com.utn.ProgIII.service.interfaces.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit")
@Tag(name = "Auditorias", description = "Operaciones relacionadas con las tablas de auditoria")
public class AuditController {

    final private AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(summary = "Obtener todos los registros de la tabla de auditoria", description = "Obtiene todos los registros de la tabla de auditoria")
    @ApiResponse(responseCode = "200",description = "Hay registros disponibles", content = @Content(
            schema = @Schema(implementation = AuditLog.class)
    ))
    @ApiResponse(responseCode = "403", description = "Acceso prohibido/dirección no encontrada", content = @Content())
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogs(@RequestParam(required = false) String category,
            @RequestParam(required = false) Integer type, Pageable pageable) {

        Page<AuditLogDTO> logsPage = auditService.getAuditLogs(category, type, pageable);
        return ResponseEntity.ok(logsPage);
    }
}
