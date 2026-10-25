
import { useState } from "react";
import { iniciarSesion } from "../services/AuthService";

export default function LoginPage({ onPinEnviado }) {
    const [formulario, setFormulario] = useState({
        usuario: "",
        clave: "",
        correo: "",
    });

    const [mostrarClave, setMostrarClave] = useState(false);
    const [cargando, setCargando] = useState(false);
    const [error, setError] = useState("");
    const [cuentaBloqueada, setCuentaBloqueada] = useState(false);

    function actualizarCampo(evento) {
        const { name, value } = evento.target;

        setFormulario((anterior) => ({
            ...anterior,
            [name]: value,
        }));

        setError("");
    }

    async function manejarLogin(evento) {
        evento.preventDefault();

        if (cargando) return;

        if (
            !formulario.usuario.trim() ||
            !formulario.clave ||
            !formulario.correo.trim()
        ) {
            setError("Debes completar los tres campos.");
            return;
        }

        setCargando(true);
        setError("");

        try {
            const resultado = await iniciarSesion(formulario);

            if (
                resultado.ok &&
                resultado.estado === "PIN_ENVIADO" &&
                resultado.desafioId
            ) {
                setCuentaBloqueada(false);

                setFormulario((anterior) => ({
                    ...anterior,
                    clave: "",
                }));

                onPinEnviado(resultado.desafioId);
                return;
            }

            if (
                resultado.estado === "CUENTA_BLOQUEADA" ||
                resultado.httpStatus === 429
            ) {
                setCuentaBloqueada(true);

                setError(
                    "Has superado el número máximo de 5 intentos fallidos. " +
                    "Tu cuenta está bloqueada temporalmente. " +
                    "Debes esperar 15 minutos antes de volver a intentarlo."
                );

                return;
            }

            setCuentaBloqueada(false);

            setError(
                resultado.mensaje ||
                "No fue posible iniciar sesión."
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
                <div className="login-icon"></div>

                <h1>Iniciar sesión</h1>

                <p className="login-subtexto">
                    Ingresa tus credenciales para continuar.
                </p>

                <form onSubmit={manejarLogin}>
                    <div className="campo">
                        <label htmlFor="usuario">Usuario</label>

                        <input
                            id="usuario"
                            name="usuario"
                            type="text"
                            autoComplete="username"
                            value={formulario.usuario}
                            onChange={actualizarCampo}
                            placeholder="Ingresa tu usuario"
                            maxLength={50}
                            required
                        />
                    </div>

                    <div className="campo">
                        <label htmlFor="clave">Contraseña</label>

                        <input
                            id="clave"
                            name="clave"
                            type={mostrarClave ? "text" : "password"}
                            autoComplete="current-password"
                            value={formulario.clave}
                            onChange={actualizarCampo}
                            placeholder="Ingresa tu contraseña"
                            required
                        />

                        <label className="mostrar-clave">
                            <input
                                type="checkbox"
                                checked={mostrarClave}
                                onChange={(e) =>
                                    setMostrarClave(e.target.checked)
                                }
                            />
                            Mostrar contraseña
                        </label>
                    </div>

                    <div className="campo">
                        <label htmlFor="correo">Correo electrónico</label>

                        <input
                            id="correo"
                            name="correo"
                            type="text"
                            value={formulario.correo}
                            onChange={actualizarCampo}
                            placeholder="ejemplo@gmail.com"
                            required
                        />
                    </div>

                    {error && (
                        <div
                            className={
                                cuentaBloqueada
                                    ? "login-bloqueado"
                                    : "login-error"
                            }
                            role="alert"
                        >
                            {cuentaBloqueada ? (
                                <>
                                    <div className="bloqueo-icono">🔒</div>

                                    <strong>Cuenta temporalmente bloqueada</strong>

                                    <p>{error}</p>

                                    <small>
                                        El acceso se habilitará cuando finalice
                                        el periodo de bloque idiota.
                                    </small>
                                </>
                            ) : (
                                <> {error}</>
                            )}
                        </div>
                    )}

                    <button type="submit" disabled={cargando}>
                        {cargando
                            ? "Validando credenciales..."
                            : "Iniciar sesión"}
                    </button>
                </form>

                <p className="login-footer">
                    LoginRegexGoogle — Proyecto Java
                </p>
            </section>
        </main>
    );
}
