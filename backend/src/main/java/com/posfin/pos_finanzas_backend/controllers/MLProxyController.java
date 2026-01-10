package com.posfin.pos_finanzas_backend.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador proxy para el servicio ML.
 * Actúa como intermediario entre el frontend y el servicio de predicciones ML,
 * resolviendo problemas de conectividad desde el navegador.
 */
@RestController
@RequestMapping("/api/ml")
@CrossOrigin(origins = {"https://pos.tonatiuham.dev", "http://localhost:5173"})
public class MLProxyController {

    @Value("${ml.service.url:http://ml-prediction:8000}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate;

    public MLProxyController() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Endpoint de salud del servicio ML
     * GET /api/ml/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> getHealthStatus() {
        try {
            String url = mlServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Servicio ML no disponible");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("mlServiceUrl", mlServiceUrl);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    /**
     * Información del servicio ML
     * GET /api/ml/info
     */
    @GetMapping("/info")
    public ResponseEntity<?> getServiceInfo() {
        try {
            String url = mlServiceUrl + "/info";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No se pudo obtener información del servicio ML");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("mlServiceUrl", mlServiceUrl);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    /**
     * Endpoint de predicciones ML
     * POST /api/ml/predict
     */
    @PostMapping("/predict")
    public ResponseEntity<?> makePrediction(@RequestBody Map<String, Object> requestData) {
        try {
            String url = mlServiceUrl + "/predict";
            
            // Configurar headers para la petición al servicio ML
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Crear la petición HTTP
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestData, headers);
            
            // Hacer la petición al servicio ML
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            return ResponseEntity.ok(response.getBody());
            
        } catch (RestClientException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al procesar predicción ML");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("mlServiceUrl", mlServiceUrl);
            errorResponse.put("requestData", requestData);
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    /**
     * Endpoint para probar la conectividad con el servicio ML
     * GET /api/ml/test-connection
     */
    @GetMapping("/test-connection")
    public ResponseEntity<?> testConnection() {
        Map<String, Object> testResult = new HashMap<>();
        testResult.put("mlServiceUrl", mlServiceUrl);
        
        try {
            // Probar conectividad con /health
            String healthUrl = mlServiceUrl + "/health";
            ResponseEntity<String> healthResponse = restTemplate.getForEntity(healthUrl, String.class);
            
            testResult.put("status", "success");
            testResult.put("healthCheck", healthResponse.getStatusCode());
            testResult.put("healthResponse", healthResponse.getBody());
            testResult.put("message", "Conexión al servicio ML exitosa");
            
            return ResponseEntity.ok(testResult);
            
        } catch (RestClientException e) {
            testResult.put("status", "error");
            testResult.put("error", e.getMessage());
            testResult.put("message", "No se pudo conectar al servicio ML");
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(testResult);
        }
    }
}