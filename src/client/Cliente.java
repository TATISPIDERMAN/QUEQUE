package client;

import java.io.*;
import java.net.*;

/**
 * Clase Cliente para el juego de Blackjack.
 * 
 * @author TATISPIDERMAN
 * @version 1.0
 * @see server.Servidor
 */
public class Cliente {
    /** @see IP_SERVIDOR Ip de la PC */
    private static final String IP_SERVIDOR = "130.10.1.11"; 
    /** @see PUERTO_SERVIDOR puerto servidor */
    private static final int PUERTO_SERVIDOR = 6000;      

    /**
     * Método principal para iniciar el programa del jugador.
     * 
     * @param args Argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        /** Iniciando conexion */
        System.out.println("[CLIENTE] Intentando conectar al servidor...");

        try (Socket socket = new Socket(IP_SERVIDOR, PUERTO_SERVIDOR)) {
            /** Conexion exitosa */
            System.out.println("[CLIENTE] ¡Conectado exitosamente!");

            /** @param entrada Leer msj del server */
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));     
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            /** @param teclado Para que el jugador typee */
            BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in)); 

            String mensajeServidor;

            while ((mensajeServidor = entrada.readLine()) != null) {
                /** @return mensajeServidor imprimir msj para el jugador */
                System.out.println("[SERVIDOR]: " + mensajeServidor); 

                if (mensajeServidor.contains("¿Qué querés hacer?") || 
                    mensajeServidor.contains("Ingresa") || 
                    mensajeServidor.contains("Escribí") || 
                    mensajeServidor.contains("Comando no reconocido")) {
                    
                    System.out.print("> ");
                    /** @return opcion Esperando respuesta del jugador */
                    String opcion = teclado.readLine(); 
                    /** Lo manda al servidor por el cable */
                    salida.println(opcion); 
                }
            }

            System.out.println("[CLIENTE] Conexión finalizada por el servidor.");

        } catch (Exception e) {
            /** @throws Exception Si ocurre un error en la conexion */
            System.err.println("[CLIENTE] Error: No se pudo conectar. ¿El servidor está apagado? " + e.getMessage());
        }
    }
}
