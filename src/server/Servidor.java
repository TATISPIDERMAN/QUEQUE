package server;

import java.util.*;
import java.net.*;

public class Servidor {
    //Definicion de puerto
    private static final int PUERTO = 6000;

    public static void main(String[] args) {
        System.out.println("[SERVIDOR] Intentando encender...");

        // Iniciar puerto
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("[SERVIDOR] ¡Encendido! Escuchando en el puerto " + PUERTO);
            System.out.println("[SERVIDOR] Esperando que se conecte un cliente...");

            // Bucle de espera
            while (true) {
                
                // acepatacion del cliente
                Socket socketCliente = serverSocket.accept(); 
                
                System.out.println("[SERVIDOR] ¡Alguien se conectó desde otra compu!");
                
            }
        //Error en el puerto
        } catch (Exception e) {
            System.err.println("[SERVIDOR] Error crítico: No se pudo abrir el puerto. " + e.getMessage());
        }
    }
}
