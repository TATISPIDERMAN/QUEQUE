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

                //Obtener la ip del jugador
                String ipCliente = socketCliente.getInetAddress().getHostAddress();
                int puertoCliente = socketCliente.getPort(); //puerto de salida del jugador

                System.out.println("[SERVIDOR] ¡Jugador conectado desde la IP: " + ipCliente + " (Puerto: " + puertoCliente + ")!");

                //Craciacion del hilo
                HiloCliente gestorCliente = new HiloCliente(socketCliente);
                Thread hilo = new Thread(gestorCliente);
                
                // Encender
                hilo.start(); 
                
                System.out.println("[SERVIDOR] Hilo delegado con éxito. Volviendo a esperar...");
                
            }
        //Error en el puerto
        } catch (Exception e) {
            System.err.println("[SERVIDOR] Error crítico: No se pudo abrir el puerto. " + e.getMessage());
        }
    }
}
