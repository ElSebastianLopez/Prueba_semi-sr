package com.inventario.inventario.infrastructure.persistence;

import com.inventario.inventario.domain.model.Inventario;
import com.inventario.inventario.domain.repository.InventarioRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InventarioRepositoryJpa implements InventarioRepository {
    public final  SpringDataJpaRepository springDataJpaRepository;
    public InventarioRepositoryJpa(SpringDataJpaRepository springDataJpaRepository){
        this.springDataJpaRepository = springDataJpaRepository;
    }
    @Override
    public Inventario save(Inventario inventario){
        return springDataJpaRepository.save(inventario);
    }
}
