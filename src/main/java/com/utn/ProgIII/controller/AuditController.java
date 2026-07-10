package com.utn.ProgIII.controller;

import com.utn.ProgIII.dto.AuditLogDTO;
import com.utn.ProgIII.model.Audit.AuditLog;
import com.utn.ProgIII.service.interfaces.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
                                                          @RequestParam(required = false) Integer type,
                                                          @ParameterObject @PageableDefault(size = 10) @SortDefault(sort = "revisionDate", direction = Sort.Direction.DESC) Pageable pageable){

        Page<AuditLogDTO> logsPage = auditService.getAuditLogs(category, type, pageable);
        return ResponseEntity.ok(logsPage);
    }

    @Operation(summary = "Obtener los datos de un movimiento puntual de la tabla de auditoría general", description = "" +
            "Obtiene todos los datos de un movimiento, tomados de la tabla de auditoría correspondiente a ese tipo de movimiento" +
            " y usando como referencia el ID del movimiento obtenido de la tabla general y la categoría del mismo" +
            "Ya que una acción puede producir movimientos de dos categorías al mismo tiempo y con el mismo ID")
    @GetMapping("/detail/{category}/{rev}")
    public ResponseEntity<Map<String, Object>> getGenericAuditDetail(@PathVariable String category, @PathVariable Long rev) {
        return ResponseEntity.ok(auditService.getRawAuditRow(category, rev));
    }
}
