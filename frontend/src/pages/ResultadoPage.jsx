
export default function ResultadoPage({
    exitoso,
    mensaje,
    onRegresar,
}) {
    return (
        <main className="login-container">
            <section className="login-card">
                <div className="login-icon">
                    {exitoso ? ":D" : "x_x"}
                </div>

                <h1>
                    {exitoso
                        ? "¡Eres un picudo! "
                        : "¡Acceso denegado basura!"}
                </h1>

                <p className="login-subtexto">
                    {exitoso
                        ? "Has verificado correctamente tu identidad."
                        : mensaje || "No se pudo completar la verificación."}
                </p>

                <div className={exitoso ? "login-aviso" : "login-error"}>
                    {exitoso
                        ? "Verificación de PIN completada."
                        : "El acceso no ha sido autorizado."}
                </div>

                <button
                    type="button"
                    onClick={onRegresar}
                >
                    Volver al inicio
                </button>
            </section>
        </main>
    );
}
