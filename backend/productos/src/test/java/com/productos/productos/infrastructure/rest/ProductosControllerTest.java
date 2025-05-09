package com.productos.productos.infrastructure.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.productos.productos.aplication.service.ProductosService;
import com.productos.productos.shared.dto.ProductoRequestJsonApiDTO;
import com.productos.productos.shared.dto.ProductoResponseJsonApiDTO;
import com.productos.productos.shared.dto.filters.ProductoFiltroDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductosControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ProductosService productosService;

    @InjectMocks
    private ProductosController productosController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productosController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listarProductosConFiltro() throws Exception {
        ProductoFiltroDTO filtro = new ProductoFiltroDTO();
        ProductoResponseJsonApiDTO mockResponse = new ProductoResponseJsonApiDTO();

        when(productosService.getTodosLosProductos(any(ProductoFiltroDTO.class), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/productos/filtro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filtro)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(productosService).getTodosLosProductos(any(ProductoFiltroDTO.class), urlCaptor.capture());
    }

    @Test
    void obtenerProductoPorId() throws Exception {
        Long id = 1L;
        ProductoResponseJsonApiDTO mockResponse = new ProductoResponseJsonApiDTO();

        when(productosService.getProductoPorId(id)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

        verify(productosService).getProductoPorId(id);
    }

    @Test
    void crearProducto() throws Exception {
        ProductoRequestJsonApiDTO request = new ProductoRequestJsonApiDTO();
        ProductoResponseJsonApiDTO mockResponse = new ProductoResponseJsonApiDTO();

        when(productosService.crearProducto(any(ProductoRequestJsonApiDTO.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

        verify(productosService).crearProducto(any(ProductoRequestJsonApiDTO.class));
    }

    @Test
    void actualizarProducto() throws Exception {
        Long id = 1L;
        ProductoRequestJsonApiDTO request = new ProductoRequestJsonApiDTO();
        ProductoResponseJsonApiDTO mockResponse = new ProductoResponseJsonApiDTO();

        when(productosService.actualizarProducto(eq(id), any(ProductoRequestJsonApiDTO.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(put("/api/v1/productos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

        verify(productosService).actualizarProducto(eq(id), any(ProductoRequestJsonApiDTO.class));
    }

    @Test
    void eliminarProducto() throws Exception {
        Long id = 1L;

        doNothing().when(productosService).eliminarProductoPorId(id);

        mockMvc.perform(delete("/api/v1/productos/{id}", id))
                .andExpect(status().isNoContent());

        verify(productosService).eliminarProductoPorId(id);
    }
}