package client;

import java.net.*;

public class Cliente {
    private static final String IP_SERVIDOR = "130.10.2.11"; // Ip de la PC
    private static final int PUERTO_SERVIDOR = 6000;      //puerto servidor

    public static void main(String[] args) {
        //Iniciando conexion
        System.out.println("[CLIENTE] Intentando conectar al servidor...");

        try (Socket socket = new Socket(IP_SERVIDOR, PUERTO_SERVIDOR)) {
            //Conexion exitosa
            System.out.println("[CLIENTE] ¡Conectado exitosamente!");
            System.out.println("[CLIENTE] El juego terminará de inmediato porque es la versión básica.");

        } catch (Exception e) {
            //Error en la conexion
            System.err.println("[CLIENTE] Error: No se pudo conectar. ¿El servidor está apagado? " + e.getMessage());
        }
    }
}