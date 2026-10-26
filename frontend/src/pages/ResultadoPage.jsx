import "bootstrap/dist/css/bootstrap.min.css";

export default function ResultadoPage({
    exitoso,
    mensaje,
    onRegresar
}) {
    return (
        <div className="container min-vh-100 d-flex align-items-center justify-content-center">
            <div className="card shadow p-4 w-100" style={{ maxWidth: "420px" }}>

                <h1 className="text-center mb-3">
                    {exitoso ? "¡Eres un picudo! :D" : "¡Buen intento, genio!"}
                </h1>

                <p className="text-muted text-center mb-4">
                    {exitoso
                        ? "Has verificado correctamente tu identidad."
                        : mensaje || "No se pudo completar la verificación."}
                </p>

                <p className="text-muted text-center mb-4">
                    {exitoso
                        ? "Verificación de PIN completada."
                        : "El acceso no ha sido autorizado."}
                </p>

                <button onClick={onRegresar} className="btn btn-danger">
                    Volver al inicio
                </button>

            </div>
        </div >
    );
}
