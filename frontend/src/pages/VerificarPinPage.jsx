
import { useState } from "react";
import { verificarPin } from "../services/AuthService"; { }

export default function VerificarPinPage({
    solicitudPinId,
    onVerificado,
    onRechazado,
    onRegresar,
}) {
    const [pin, setPin] = useState("");
    const [cargando, setCargando] = useState(false);
    const [error, setError] = useState("");
    const [intentosRestantes, setIntentosRestantes] = useState(null);

    function cambiarPin(evento) {
        const valor = evento.target.value.replace(/\D/g, "");
        setPin(valor.slice(0, 6));
        setError("");
    }

    async function manejarVerificacion(evento) {
        evento.preventDefault();

        if (cargando) return;

        if (!/^\d{6}$/.test(pin)) {
            setError("Introduce un PIN de exactamente 6 dígitos.");
            return;
        }

        setCargando(true);
        setError("");

        try {
            const resultado = await verificarPin(solicitudPinId, pin);

            // Solo Java puede confirmar la verificación.
            if (resultado.ok && resultado.estado === "VERIFICADO") {
                setPin("");
                onVerificado();
                return;
            }

            if (
                resultado.estado === "PIN_BLOQUEADO" ||
                resultado.estado === "PIN_EXPIRADO" ||
                resultado.estado === "PIN_UTILIZADO"
            ) {
                setPin("");
                onRechazado(resultado.mensaje || "Verificación rechazada.");
                return;
            }

            if (resultado.estado === "PIN_INCORRECTO") {
                setPin("");

                if (typeof resultado.intentosRestantes === "number") {
                    setIntentosRestantes(resultado.intentosRestantes);
                }

                setError(
                    resultado.mensaje ||
                    "El PIN es incorrecto."
                );
                return;
            }

            if (resultado.httpStatus === 404) {
                setError(
                    "El servicio de verificación todavía no está disponible."
                );
                return;
            }

            setError(
                resultado.mensaje ||
                "No fue posible verificar el PIN."
            );

        } catch (err) {
            setError(err.message);
        } finally {
            setCargando(false);
        }
    }

    return (
        <main className="container min-vh-100 d-flex align-items-center justify-content-center">
            <section className="card shadow p-4 w-100" style={{ maxWidth: "420px" }}>

                <h1 className="text-center mb-3">Verifica tu correo</h1>

                <p className="text-muted text-center mb-4">
                    Introduce el código de seis dígitos
                    enviado a tu correo electrónico.
                </p>

                <div className="login-subtexto mb-3 text-center">
                    Tu PIN tiene una vigencia de 5 minutos.
                </div>

                <form onSubmit={manejarVerificacion}>
                    <div className="campo">
                        <label htmlFor="pin" className="form-label">Código de verificación</label>

                        <input
                            id="pin"
                            name="pin"
                            type="text"

                            inputMode="numeric"
                            autoComplete="one-time-code"
                            maxLength={6}
                            pattern="[0-9]{6}"
                            placeholder="000000"
                            value={pin}
                            onChange={cambiarPin}
                            className="form-control"
                            required
                            autoFocus
                        />
                    </div>

                    {error && (
                        <div className="alert alert-danger" role="alert">
                            {error}
                        </div>
                    )}

                    {intentosRestantes !== null && (
                        <p className="login-subtexto">
                            Intentos restantes: {intentosRestantes}
                        </p>
                    )}
                    <br></br>
                    <button
                        type="submit" className="btn btn-primary display-block w-100"
                        disabled={cargando || pin.length !== 6}
                    >
                        {cargando ? "Verificando..." : "Verificar PIN"}
                    </button>
                </form>
                <br></br>
                <button
                    type="button"
                    className="btn btn-primary"
                    onClick={onRegresar}
                    disabled={cargando}
                >
                    Regresar al inicio
                </button>
            </section>
        </main>
    );
}
