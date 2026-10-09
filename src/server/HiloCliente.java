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

    public String getNombre() {
        return this.nombreJugador;
    }

    public java.util.List<String> getMano() {
        return this.mano;
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
            
            String texto;
            while ((texto = in.readLine()) != null) {
                // Pasamos a minúsculas y quitamos espacios
                String comando = texto.trim().toLowerCase(); 

                if (comando.equals("pedir")) {
                    // crupier le da otra carta mas
                    String nuevaCarta = Servidor.tomarCarta();
                    recibirCarta(nuevaCarta);
                    
                    // Avisamos a los demás
                    Servidor.difundirMensaje("[JUEGO] " + nombreJugador + " pidio una carta.");

                    // Calculamos los puntos al pedir otra carta
                    int puntosActuales = Servidor.calcularPuntos(this.mano);
                    if (puntosActuales > 21) {
                        enviarMensaje("[❌] ¡Te pasaste de 21 puntos!.");
                        Servidor.difundirMensaje("[JUEGO] " + nombreJugador + " se paso de 21 con " + puntosActuales + " puntos.");
                        
                        // Si se pasó avanzamos de turno
                        Servidor.avanzarTurno(); 
                    } else {
                        // Si no se pasó, le volvemos a preguntar si quiere seguir pidiendo o plantarse
                        enviarMensaje("¿Qué querés hacer? Escribí 'pedir' o 'plantarse'.");
                    }

                } else if (comando.equals("plantarse")) {
                    // avisamos que hizo y avanzamos turno
                    Servidor.difundirMensaje("[JUEGO] " + nombreJugador + " se ha plantado con " + Servidor.calcularPuntos(this.mano) + " puntos.");
                    
                    Servidor.avanzarTurno();
                    
                } else {
                    // Comando inválido
                    enviarMensaje("Comando no reconocido. Por favor, escribí 'pedir' o 'plantarse'.");
                }
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
