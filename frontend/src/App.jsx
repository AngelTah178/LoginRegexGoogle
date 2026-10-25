
import { useState } from "react";

import LoginPage from "./pages/LoginPage";
import VerificarPinPage from "./pages/VerificarPinPage";
import ResultadoPage from "./pages/ResultadoPage";

export default function App() {
  const [pantalla, setPantalla] = useState("login");
  const [desafioId, setDesafioId] = useState(null);
  const [mensajeRechazo, setMensajeRechazo] = useState("");

  function pinEnviado(id) {
    setDesafioId(id);
    setPantalla("verificar");
  }

  function verificacionCorrecta() {
    setDesafioId(null);
    setPantalla("exito");
  }

  function verificacionRechazada(mensaje) {
    setMensajeRechazo(mensaje);
    setDesafioId(null);
    setPantalla("rechazado");
  }

  function regresarAlLogin() {
    setDesafioId(null);
    setMensajeRechazo("");
    setPantalla("login");
  }

  if (pantalla === "verificar" && desafioId) {
    return (
      <VerificarPinPage
        desafioId={desafioId}
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
