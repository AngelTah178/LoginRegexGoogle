package com.proyecto.login.repository;

import com.proyecto.login.entity.Usuario;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);

    boolean existsByCorreo(String correo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT u FROM Usuario u
        WHERE u.usuario = :usuario
    """)
    Optional<Usuario> buscarParaActualizar(
        @Param("usuario") String usuario
    );
}