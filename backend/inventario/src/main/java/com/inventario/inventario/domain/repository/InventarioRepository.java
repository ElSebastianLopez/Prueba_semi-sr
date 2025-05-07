package com.inventario.inventario.domain.repository;

import com.inventario.inventario.domain.model.Inventario;

public interface InventarioRepository {
    Inventario save(Inventario inventario);
}
