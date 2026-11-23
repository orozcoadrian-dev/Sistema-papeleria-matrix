package com.papeleriamatrix.controller;

import com.papeleriamatrix.dto.ReporteDelNegocioRequest;
import com.papeleriamatrix.dto.ReporteDelNegocioResponse;
import com.papeleriamatrix.service.ReporteDelNegocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes del negocio", description = "Reportes enlazados con sus históricos de ingresos y estadísticas")
@SecurityRequirement(name = "claveApi")
public class ReporteDelNegocioController {

    private final ReporteDelNegocioService reporteService;

    public ReporteDelNegocioController(ReporteDelNegocioService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping
    @Operation(summary = "Crear un reporte del negocio")
    public ResponseEntity<ReporteDelNegocioResponse> crear(@Valid @RequestBody ReporteDelNegocioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ReporteDelNegocioResponse.desde(reporteService.crear(request)));
    }

    @GetMapping
    @Operation(summary = "Lista los reportes generados")
    public ResponseEntity<List<ReporteDelNegocioResponse>> listar() {
        List<ReporteDelNegocioResponse> reportes = reporteService.listar().stream()
            .map(ReporteDelNegocioResponse::desde)
            .toList();
        return ResponseEntity.ok(reportes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un reporte")
    public ResponseEntity<ReporteDelNegocioResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(ReporteDelNegocioResponse.desde(reporteService.obtener(id)));
    }
}