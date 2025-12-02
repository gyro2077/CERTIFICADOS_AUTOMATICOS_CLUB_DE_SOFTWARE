package com.example.demo;

import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ReportController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/generar-certificados")
    public ResponseEntity<byte[]> generarReporte() {
        try (Connection connection = dataSource.getConnection()) {

            // IMPORTANTE: Verifica que esta ruta sea REAL en tu Linux
            String sourceFileName = "/home/gyro/JaspersoftWorkspace/CTF_CERTIFICATE/Blank_A4_Landscape.jrxml";

            // Compilar
            JasperReport jasperReport = JasperCompileManager.compileReport(new FileInputStream(sourceFileName));

            // Llenar
            Map<String, Object> parameters = new HashMap<>();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            // Exportar
            byte[] data = JasperExportManager.exportReportToPdf(jasperPrint);

            // Responder
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=certificados_ctf.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
}