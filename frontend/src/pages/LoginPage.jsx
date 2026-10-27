
import { useState } from "react";
import { iniciarSesion } from "../services/AuthService";
import "bootstrap/dist/css/bootstrap.min.css";

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
                resultado.solicitudPinId
            ) {
                setCuentaBloqueada(false);

                setFormulario((anterior) => ({
                    ...anterior,
                    clave: "",
                }));

                onPinEnviado(resultado.solicitudPinId);
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
        <main className="container min-vh-100 d-flex align-items-center justify-content-center">

            <section className="card shadow p-4 w-100" style={{ maxWidth: "420px" }}>

                <h1 className="text-center mb-3">Iniciar sesión</h1>

                <p className="text-muted text-center mb-4">
                    Ingresa tus credenciales para continuar.
                </p>

                <form onSubmit={manejarLogin}>

                    <div className="mb-3">
                        <label htmlFor="usuario" className="form-label">
                            Usuario
                        </label>

                        <input
                            id="usuario"
                            name="usuario"
                            type="text"
                            className="form-control"
                            value={formulario.usuario}
                            onChange={actualizarCampo}
                            placeholder="Ingresa tu usuario"
                            required
                        />
                    </div>

                    <div className="mb-3">
                        <label htmlFor="clave" className="form-label">
                            Contraseña
                        </label>

                        <input
                            id="clave"
                            name="clave"
                            type={mostrarClave ? "text" : "password"}
                            className="form-control"
                            value={formulario.clave}
                            onChange={actualizarCampo}
                            placeholder="Ingresa tu contraseña"
                            required
                        />

                        <div className="form-check mt-2">
                            <input
                                id="mostrarClave"
                                type="checkbox"
                                className="form-check-input"
                                checked={mostrarClave}
                                onChange={(e) =>
                                    setMostrarClave(e.target.checked)
                                }
                            />

                            <label htmlFor="mostrarClave" className="form-check-label">
                                Mostrar contraseña
                            </label>
                        </div>
                    </div>

                    <div className="mb-3">
                        <label htmlFor="correo" className="form-label">
                            Correo electrónico
                        </label>

                        <input
                            id="correo"
                            name="correo"
                            type="text"
                            className="form-control"
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
                                    ? "alert alert-danger"
                                    : "alert alert-warning"
                            }
                            role="alert"
                        >
                            {cuentaBloqueada && (
                                <h5> Cuenta temporalmente bloqueada</h5>
                            )}

                            <p className="mb-0">{error}</p>
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={cargando}
                        className="btn btn-primary w-100"
                    >
                        {cargando
                            ? "Validando credenciales..."
                            : "Iniciar sesión"}
                    </button>
                </form>

                <p className="text-center text-muted mt-4 mb-0">
                    LoginRegexGoogle — Proyecto Java
                </p>
            </section>
        </main>
    );

}
