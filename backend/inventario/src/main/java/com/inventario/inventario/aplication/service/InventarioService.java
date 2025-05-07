package com.inventario.inventario.aplication.service;

import com.inventario.inventario.shared.dto.InventarioRequestJsonApiDTO;
import com.inventario.inventario.shared.dto.InventarioResponseJsonApiDTO;

public interface InventarioService {
    InventarioResponseJsonApiDTO crearInventario(InventarioRequestJsonApiDTO request);
}
