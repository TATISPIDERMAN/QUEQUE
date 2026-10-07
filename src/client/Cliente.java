package client;

import java.io.*;
import java.net.*;

public class Cliente {
    private static final String IP_SERVIDOR = "130.10.2.12"; // Ip de la PC
    private static final int PUERTO_SERVIDOR = 6000;      //puerto servidor

    public static void main(String[] args) {
        //Iniciando conexion
        System.out.println("[CLIENTE] Intentando conectar al servidor...");

        try (Socket socket = new Socket(IP_SERVIDOR, PUERTO_SERVIDOR)) {
            //Conexion exitosa
            System.out.println("[CLIENTE] ¡Conectado exitosamente!");

            //Leer msj del server
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));     
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in)); //Para que el jugador typee

            String mensajeServidor;

            while ((mensajeServidor = entrada.readLine()) != null) {
                System.out.println("[SERVIDOR]: " + mensajeServidor); //imprimir msj para el jugador

                if (mensajeServidor.contains("¿Qué querés hacer?") || mensajeServidor.contains("Ingresa")) {
                    System.out.print("> ");
                    String opcion = teclado.readLine(); // Esperando respuesta del jugador
                    salida.println(opcion); // Lo manda al servidor por el cable
                }
            }

            System.out.println("[CLIENTE] Conexión finalizada por el servidor.");


        } catch (Exception e) {
            //Error en la conexion
            System.err.println("[CLIENTE] Error: No se pudo conectar. ¿El servidor está apagado? " + e.getMessage());
        }
    }
}