package com.inventario.inventario.aplication.service.impl;

import com.inventario.inventario.aplication.service.InventarioService;
import com.inventario.inventario.domain.model.Inventario;
import com.inventario.inventario.domain.repository.InventarioRepository;
import com.inventario.inventario.shared.dto.InventarioRequestJsonApiDTO;
import com.inventario.inventario.shared.dto.InventarioResponseJsonApiDTO;
import com.inventario.inventario.shared.mapper.InventarioMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {
    private final InventarioRepository inventarioRepository;
    private final InventarioMapper inventarioMapper;

    @Override
    public InventarioResponseJsonApiDTO crearInventario(InventarioRequestJsonApiDTO request) {
        Long productoId = request.getData().getAttributes().getProductoId();
        Integer cantidad = request.getData().getAttributes().getCantidadDisponible();

        log.info("[InventarioService] Creando inventario para producto ID: {}", productoId);

        if (productoId == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo.");
        }

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad disponible debe ser mayor o igual a cero.");
        }

        Inventario inventario = inventarioMapper.fromJsonApiDTO(request);
        inventario.setUltimaActualizacion(LocalDateTime.now());

        Inventario guardado = inventarioRepository.save(inventario);

        return inventarioMapper.toJsonApiDTO(guardado);
    }
}
