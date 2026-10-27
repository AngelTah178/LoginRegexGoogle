
import { useState } from "react";

import LoginPage from "./pages/LoginPage";
import VerificarPinPage from "./pages/VerificarPinPage";
import ResultadoPage from "./pages/ResultadoPage";

export default function App() {
  const [pantalla, setPantalla] = useState("login");
  const [solicitudPinId, setsolicitudPinId] = useState(null);
  const [mensajeRechazo, setMensajeRechazo] = useState("");

  function pinEnviado(id) {
    setsolicitudPinId(id);
    setPantalla("verificar");
  }

  function verificacionCorrecta() {
    setsolicitudPinId(null);
    setPantalla("exito");
  }

  function verificacionRechazada(mensaje) {
    setMensajeRechazo(mensaje);
    setsolicitudPinId(null);
    setPantalla("rechazado");
  }

  function regresarAlLogin() {
    setsolicitudPinId(null);
    setMensajeRechazo("");
    setPantalla("login");
  }

  if (pantalla === "verificar" && solicitudPinId) {
    return (
      <VerificarPinPage
        solicitudPinId={solicitudPinId}
        onVerificado={verificacionCorrecta}
        onRechazado={verificacionRechazada}
        onRegresar={regresarAlLogin}
      />
    );
  }

  if (pantalla === "exito") {
    return (
      <ResultadoPage
        exitoso={true}
        onRegresar={regresarAlLogin}
      />
    );
  }

  if (pantalla === "rechazado") {
    return (
      <ResultadoPage
        exitoso={false}
        mensaje={mensajeRechazo}
        onRegresar={regresarAlLogin}
      />
    );
  }

  return <LoginPage onPinEnviado={pinEnviado} />;
}
