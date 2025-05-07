package com.inventario.inventario.shared.mapper;

import com.inventario.inventario.domain.model.Inventario;
import com.inventario.inventario.shared.dto.InventarioRequestJsonApiDTO;
import com.inventario.inventario.shared.dto.InventarioResponseJsonApiDTO;
import org.springframework.stereotype.Component;

@Component
public class InventarioMapper {
    public Inventario fromJsonApiDTO(InventarioRequestJsonApiDTO request) {
        Inventario inventario = new Inventario();
        inventario.setProductoId(request.getData().getAttributes().getProductoId());
        inventario.setCantidadDisponible(request.getData().getAttributes().getCantidadDisponible());
        return inventario;
    }

    public InventarioResponseJsonApiDTO toJsonApiDTO(Inventario inventario) {
        InventarioResponseJsonApiDTO response = new InventarioResponseJsonApiDTO();

        InventarioResponseJsonApiDTO.Data data = new InventarioResponseJsonApiDTO.Data();
        data.setType("inventario");
        data.setId(String.valueOf(inventario.getProductoId()));

        InventarioResponseJsonApiDTO.Data.Attributes attributes = new InventarioResponseJsonApiDTO.Data.Attributes();
        attributes.setProductoId(inventario.getProductoId());
        attributes.setCantidadDisponible(inventario.getCantidadDisponible());
        attributes.setUltimaActualizacion(inventario.getUltimaActualizacion());

        data.setAttributes(attributes);
        response.setData(data);
        return response;
    }
}
