package server;

import java.io.*;
import java.net.*;

public class HiloCliente implements  Runnable{
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
      private String nombreJugador;

    public HiloCliente(Socket socket) {
        this.socket = socket;
        try {
            //Leer datos e imprimir
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            //error en el hilo
            System.err.println("[HILO] Error al crear flujos: " + e.getMessage());
        }
    }

    public void enviarMensaje(String mensaje) {
        if (out != null) {
            out.println(mensaje);
        }
    }

    @Override
    public void run() {
        try {
            // Bienvenida
            enviarMensaje("Bienvenido al servidor de Blackjack");

            this.nombreJugador = in.readLine(); 
            
            enviarMensaje("Perfecto " + nombreJugador + ", ya estas registrado en la mesa. Esperando que arranque la partida...");
            System.out.println("[SERVIDOR] El jugador en este hilo se registro como: " + nombreJugador);
            
            while (true) {
                String texto = in.readLine(); 
                if (texto == null) break; // se desconecta, rompemos el bucle
            }

        } catch (Exception e) {
            System.err.println("[HILO] Conexión perdida con un jugador.");
        } finally {
            try {
                socket.close();
                System.out.println("[SERVIDOR] Un hilo cerró la conexión de un jugador.");
            } catch (Exception e) { /**/ }
        }
    }
}
