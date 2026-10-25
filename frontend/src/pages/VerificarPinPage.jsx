
import { useState } from "react";
import { verificarPin } from "../services/AuthService"; { }

export default function VerificarPinPage({
    desafioId,
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
            const resultado = await verificarPin(desafioId, pin);

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
        <main className="login-container">
            <section className="login-card">
                <div className="login-icon">📧</div>

                <h1>Verifica tu correo</h1>

                <p className="login-subtexto">
                    Introduce el código de seis dígitos
                    enviado a tu correo electrónico.
                </p>

                <div className="login-aviso">
                    Tu PIN tiene una vigencia de 5 minutos.
                </div>

                <form onSubmit={manejarVerificacion}>
                    <div className="campo">
                        <label htmlFor="pin">Código de verificación</label>

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
                            className="pin-input"
                            required
                            autoFocus
                        />
                    </div>

                    {error && (
                        <div className="login-error" role="alert">
                            ❌ {error}
                        </div>
                    )}

                    {intentosRestantes !== null && (
                        <p className="login-subtexto">
                            Intentos restantes: {intentosRestantes}
                        </p>
                    )}

                    <button
                        type="submit"
                        disabled={cargando || pin.length !== 6}
                    >
                        {cargando ? "Verificando..." : "Verificar PIN"}
                    </button>
                </form>

                <button
                    type="button"
                    className="boton-secundario"
                    onClick={onRegresar}
                    disabled={cargando}
                >
                    Regresar al inicio
                </button>
            </section>
        </main>
    );
}
