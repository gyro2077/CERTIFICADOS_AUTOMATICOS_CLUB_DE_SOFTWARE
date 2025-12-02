package com.example.demo;

import net.sf.jasperreports.engine.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;

@RestController
public class ReportController {

    private final DataSource dataSource;

    public ReportController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/generar-certificados")
    public ResponseEntity<byte[]> generarReporte() {
        try (Connection connection = dataSource.getConnection()) {
            
            // 1. Cargar el XML desde resources
            InputStream reportStream = getClass().getResourceAsStream("/Blank_A4_Landscape.jrxml");
            
            // 2. Compilar
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 3. Llenar y Exportar
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), connection);
            byte[] data = JasperExportManager.exportReportToPdf(jasperPrint);

            // 4. Descargar
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=certificados.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}