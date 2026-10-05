package server;

import java.util.*;
import java.net.*;
import java.io.*;

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
                System.out.println("[SERVIDOR] ¡Alguien se conectó desde otra PC!");

                //Escribicion al cliente
                PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true);
                salida.println("Bienvenido al servidor de Blackjack");  //Mensaje para el cliente

                socketCliente.close();
                System.out.println("[SERVIDOR] Conexión cerrada");
                
            }
        //Error en el puerto
        } catch (Exception e) {
            System.err.println("[SERVIDOR] Error crítico: No se pudo abrir el puerto. " + e.getMessage());
        }
    }
}
