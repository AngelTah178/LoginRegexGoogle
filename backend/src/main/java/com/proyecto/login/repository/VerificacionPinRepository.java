
package com.proyecto.login.repository;

import com.proyecto.login.entity.VerificacionPin;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificacionPinRepository
        extends JpaRepository<VerificacionPin, UUID> {

    // Buscar desafios pendientes de un usuario.
    List<VerificacionPin> findByUsuarioIdAndUsadoFalse(
            Long usuarioId
    );

    // Contar desafios generados durante la ultima hora.
    long countByUsuarioIdAndFechaCreacionUtcAfter(
            Long usuarioId,
            LocalDateTime fecha
    );

    // Buscar y bloquear un desafio durante su verificacion.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT v
        FROM VerificacionPin v
        WHERE v.id = :id
    """)
    Optional<VerificacionPin> buscarParaActualizar(
            @Param("id") UUID id
    );

    // Obtener el ultimo desafio generado.
    Optional<VerificacionPin>
        findTopByUsuarioIdOrderByFechaCreacionUtcDesc(
                Long usuarioId
        );
}
