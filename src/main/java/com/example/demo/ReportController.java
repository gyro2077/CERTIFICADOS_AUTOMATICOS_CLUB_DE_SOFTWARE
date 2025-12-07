package com.example.demo;

import net.sf.jasperreports.engine.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

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

            // 3. Llenar y Exportar (sin parámetro de cédula = todos los certificados)
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), connection);
            byte[] data = JasperExportManager.exportReportToPdf(jasperPrint);

            // 4. Mostrar en navegador
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=certificados.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/certificado/{cedula}")
    public ResponseEntity<byte[]> generarCertificadoIndividual(@PathVariable String cedula) {
        try (Connection connection = dataSource.getConnection()) {

            // 1. Cargar el XML desde resources
            InputStream reportStream = getClass().getResourceAsStream("/Blank_A4_Landscape.jrxml");

            // 2. Compilar
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 3. Parámetros con la cédula
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("cedula", cedula);

            // 4. Llenar y Exportar
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            // 5. Verificar si se generó algún certificado
            if (jasperPrint.getPages().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            byte[] data = JasperExportManager.exportReportToPdf(jasperPrint);

            // 6. Mostrar en navegador
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=certificado-" + cedula + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}