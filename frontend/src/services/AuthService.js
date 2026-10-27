
async function enviarSolicitud(ruta, datos) {
    let respuesta;

    try {
        respuesta = await fetch(ruta, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(datos),
        });
    } catch {
        throw new Error("No se pudo conectar con el servidor Java.");
    }

    let resultado;

    try {
        resultado = await respuesta.json();
    } catch {
        throw new Error("El servidor devolvió una respuesta inválida.");
    }

    return {
        ...resultado,
        httpStatus: respuesta.status,
        ok: respuesta.ok,
    };
}

export function iniciarSesion(datos) {
    return enviarSolicitud("/api/auth/login", {
        usuario: datos.usuario,
        clave: datos.clave,
        correo: datos.correo,
    });
}

export function verificarPin(solicitudPinId, pin) {
    return enviarSolicitud("/api/auth/verificar-pin", {
        solicitudPinId,
        pin,
    });
}
