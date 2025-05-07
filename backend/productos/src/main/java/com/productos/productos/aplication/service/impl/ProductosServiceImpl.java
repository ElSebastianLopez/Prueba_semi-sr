package com.productos.productos.aplication.service.impl;

import com.productos.productos.aplication.service.ProductoTransactionalService;
import com.productos.productos.aplication.service.ProductosService;
import com.productos.productos.domain.model.Producto;
import com.productos.productos.domain.repository.ProductosRepository;
import com.productos.productos.infrastructure.client.InventarioClient;
import com.productos.productos.infrastructure.persistence.spec.ProductoSpecification;
import com.productos.productos.infrastructure.rest.exception.InventarioException;
import com.productos.productos.shared.dto.ProductoRequestJsonApiDTO;
import com.productos.productos.shared.dto.ProductoResponseJsonApiDTO;
import com.productos.productos.shared.dto.filters.ProductoFiltroDTO;
import com.productos.productos.shared.dto.pageable.PageableRequest;
import com.productos.productos.shared.dto.pageable.PaginacionMeta;
import com.productos.productos.shared.mapper.JsonApiResponseBuilder;
import com.productos.productos.shared.mapper.ProductoMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductosServiceImpl implements ProductosService {
    private final ProductosRepository productosRepository;
    private final ProductoMapper productoMapper;
    private final InventarioClient inventarioClient;
    private final ProductoTransactionalService productoTransactionalService;


    @Override
    public ProductoResponseJsonApiDTO getTodosLosProductos(ProductoFiltroDTO filtro,String baseUrl) {
        PageableRequest pageableRequest = filtro.getPageable();
        log.info("[ProductosService] Iniciando listado de productos con filtros: nombre='{}', categoria='{}', precioMin='{}', precioMax='{}'",
                filtro.getNombre(), filtro.getCategoria(), filtro.getPrecioMin(), filtro.getPrecioMax());

        log.info("[ProductosService] Paginacion solicitada - page: {}, size: {}, order: {}",
                pageableRequest.getPage(), pageableRequest.getSize(), pageableRequest.getOrder());


            Sort sort = "desc".equalsIgnoreCase(pageableRequest.getOrder())
                    ? Sort.by("id").descending()
                    : Sort.by("id").ascending();

            Pageable pageable = PageRequest.of(pageableRequest.getPage(), pageableRequest.getSize(), sort);

            Specification<Producto> spec = ProductoSpecification.construir(filtro);

            Page<Producto> productosPage = productosRepository.findAll(spec, pageable);
        List<ProductoResponseJsonApiDTO.Data> dataList = productosPage.getContent().stream()
                .map(productoMapper::toJsonApiDTOData)
                .toList();

        log.info("[ProductosService] Se encontraron {} productos en total (pagina actual: {}).",
                    productosPage.getTotalElements(), productosPage.getNumber());

        return JsonApiResponseBuilder.build(productosPage, dataList, baseUrl, pageableRequest.getOrder());
    }

    @Override
    public ProductoResponseJsonApiDTO getProductoPorId(Long id) {
        log.info("[ProductosService] Buscando producto con ID: {}", id);

        Producto producto = productosRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        ProductoResponseJsonApiDTO.Data data = productoMapper.toJsonApiDTOData(producto);

        ProductoResponseJsonApiDTO response = new ProductoResponseJsonApiDTO();
        response.setData(List.of(data));

        return response;
    }


    @Transactional
    @Override
    public ProductoResponseJsonApiDTO crearProducto(ProductoRequestJsonApiDTO request) {
        log.info("[ProductosService] Creando nuevo producto: {}", request);
        try {
            Producto producto = productoMapper.fromCreateDTO(request);
            Producto guardado = productoTransactionalService.crearProductoYInventario(
                    producto,
                    request.getData().getAttributes().getCantidad()
            );

            ProductoResponseJsonApiDTO.Data data = productoMapper.toJsonApiDTOData(guardado);

            ProductoResponseJsonApiDTO response = new ProductoResponseJsonApiDTO();
            response.setData(List.of(data));
            return response;

        } catch (DataIntegrityViolationException e) {
            log.error("[ProductosService] Violacion de integridad al crear producto: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error de integridad al guardar el producto: " + e.getMessage());
        }
        catch (Exception e) {
            log.error("[ProductosService] Error al crear producto + inventario: {}", e.getMessage(), e);
            throw new InventarioException("Fallo al crear inventario. Se revertirá la transacción.", e);
        }
    }

    @Override
    public ProductoResponseJsonApiDTO actualizarProducto(Long id, ProductoRequestJsonApiDTO request) {
        log.info("[ProductosService] Actualizando producto con ID: {} - Nuevos datos: {}", id, request);
        try {
            Producto existente = productosRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

            existente.setNombre(request.getData().getAttributes().getNombre());
            existente.setDescripcion(request.getData().getAttributes().getDescripcion());
            existente.setPrecio(request.getData().getAttributes().getPrecio());
            existente.setCategoria(request.getData().getAttributes().getCategoria());

            Producto actualizado = productosRepository.save(existente);

            ProductoResponseJsonApiDTO.Data data = productoMapper.toJsonApiDTOData(actualizado);

            ProductoResponseJsonApiDTO response = new ProductoResponseJsonApiDTO();
            response.setData(List.of(data));
            return response;

        } catch (DataIntegrityViolationException e) {
            log.error("[ProductosService] Violacion de integridad al actualizar producto ID {}: {}", id, e.getMessage(), e);
            throw new IllegalArgumentException("Error de integridad al actualizar el producto: " + e.getMessage());
        }
    }

    @Override
    public void eliminarProductoPorId(Long id) {
        log.info("[ProductosService] Eliminando producto con ID: {}", id);

        Producto producto = productosRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        productosRepository.delete(producto);

        log.info("[ProductosService] Producto con ID {} eliminado exitosamente.", id);
    }



}
