package client;

import java.net.*;

public class Cliente {
    // 1. ATRIBUTOS: ¿A dónde nos vamos a conectar?
    private static final String IP_SERVIDOR = "127.0.0.1"; // Tu propia compu
    private static final int PUERTO_SERVIDOR = 12345;      // El puerto del servidor

    // 2. MÉTODO PRINCIPAL: El arranque del jugador
    public static void main(String[] args) {
        System.out.println("[CLIENTE] Intentando conectar al servidor...");

        // 3. INTENTAR TIRAR EL CABLE DE RED
        try (Socket socket = new Socket(IP_SERVIDOR, PUERTO_SERVIDOR)) {
            
            System.out.println("[CLIENTE] ¡Conectado exitosamente!");
            System.out.println("[CLIENTE] El juego terminará de inmediato porque es la versión básica.");

        } catch (Exception e) {
            System.err.println("[CLIENTE] Error: No se pudo conectar. ¿El servidor está apagado? " + e.getMessage());
        }
}
