
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

    // Buscar pin pendiente de un usuario.
    List<VerificacionPin> findByUsuarioIdAndUsadoFalse(
            Long usuarioId
    );

    // Contar pines generados durante la ultima hora.
    long countByUsuarioIdAndFechaCreacionUtcAfter(
            Long usuarioId,
            LocalDateTime fecha
    );

    // Buscar y bloquear un pin durante su verificacion.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT v
        FROM VerificacionPin v
        WHERE v.id = :id
    """)
    Optional<VerificacionPin> buscarParaActualizar(
            @Param("id") UUID id
    );

    // Obtener el ultimo ín generado.
    Optional<VerificacionPin>
        findTopByUsuarioIdOrderByFechaCreacionUtcDesc(
                Long usuarioId
        );
}
