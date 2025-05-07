package com.productos.productos.infrastructure.client;


import com.productos.productos.infrastructure.rest.exception.InventarioException;
import com.productos.productos.shared.dto.InventarioRequestJsonApiDTO;
import com.productos.productos.shared.dto.InventarioResponseJsonApiDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

@RequiredArgsConstructor
@Slf4j
@Component
public class InventarioClient {
    private final RestTemplate restTemplate;

    @Value("${inventario.api.url}")
    private String apiUrlInventario;

    @Value("${api.key}")
    private String apiKey;

    public InventarioResponseJsonApiDTO crearInventarioConReintentos(Long productoId, Integer cantidad) {
        String url = apiUrlInventario+"inventarios";
        InventarioRequestJsonApiDTO request = new InventarioRequestJsonApiDTO(productoId, cantidad);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventarioRequestJsonApiDTO> entity = new HttpEntity<>(request, headers);

        int reintentos = 0;
        Exception ultimaExcepcion = null;

        while (reintentos < 3) {
            try {
                ResponseEntity<InventarioResponseJsonApiDTO> response =
                        restTemplate.postForEntity(url, entity, InventarioResponseJsonApiDTO.class);

                // Verificar explícitamente el código de estado
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("[InventarioClient] Inventario creado correctamente para producto ID {}", productoId);
                    return response.getBody();
                } else {
                    // Manejar explícitamente otros códigos de estado
                    throw new InventarioException("Error al crear inventario. Código de estado: " + response.getStatusCodeValue());
                }
            } catch (Exception e) {
                reintentos++;
                ultimaExcepcion = e;
                log.warn("[InventarioClient] Reintento {}/3 fallido al crear inventario: {}", reintentos, e.getMessage());

                // Añadir un pequeño retraso entre reintentos
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.error("[InventarioClient] Fallo tras 3 intentos al crear inventario para producto ID {}", productoId);
        throw new InventarioException("No se pudo crear el inventario para el producto. Último error: " +
                (ultimaExcepcion != null ? ultimaExcepcion.getMessage() : "Desconocido"));
    }
}
