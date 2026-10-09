package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class HiloCliente implements  Runnable{
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nombreJugador;
    private List<String> mano = new ArrayList<>();

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

    public void recibirCarta(String carta) {
        this.mano.add(carta); // Carta del jugador
        
        //le enviamos un msj para que vea que carta le salio
        enviarMensaje("[CARTAS] Recibiste: " + carta + ".\nTu mano actual: " + mano + " (Puntos: " + Servidor.calcularPuntos(mano) + ")");
    }

    @Override
    public void run() {
        try {
            // Bienvenida
            enviarMensaje("Bienvenido al servidor de Blackjack \nIngresa el nombre");

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
