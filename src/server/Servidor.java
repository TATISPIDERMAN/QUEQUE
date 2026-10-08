package server;

import java.util.*;
import java.net.*;
import java.io.*;

public class Servidor {
    //Definicion de puerto
    private static final int PUERTO = 6000;
    public static List<HiloCliente> jugadores = Collections.synchronizedList(new ArrayList<>());
    private static List<String> baraja = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("[SERVIDOR] Intentando encender...");

        inicializarBaraja();

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

                //Agragar jugador en la mesa
                jugadores.add(gestorCliente);
                System.out.println("[SERVIDOR] Jugador agregado a la lista. Total en mesa: " + jugadores.size());

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

    public static void inicializarBaraja() {
        baraja.clear(); // Clearamos
        
        //Valores y palos
        String[] palos = {"♥", "♦", "♣", "♠"};
        String[] valores = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "As"};
        
        // Combinamos cada palo con cada valor (4 x 13 = 52 cartas)
        for (String palo : palos) {
            for (String valor : valores) {
                baraja.add(valor + palo);
            }
        }
        
        //Mezclamos la lista
        Collections.shuffle(baraja); 
        System.out.println("[SERVIDOR] ¡Mazo de cartas creado y mezclado con éxito!");
    }

}
