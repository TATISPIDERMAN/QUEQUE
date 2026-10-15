package server;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Clase que maneja la conexión y las acciones de cada jugador de forma concurrente.
 * 
 * @author Ignacio Alvarez
 * @version 1.0
 */
public class HiloCliente implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nombreJugador;
    private List<String> mano = new ArrayList<>();

    /**
     * Constructor del hilo que asocia el socket de red del jugador.
     * 
     * @param socket Cable de conexión de red del cliente
     */
    public HiloCliente(Socket socket) {
        this.socket = socket;
        try {
            /** Leer datos e imprimir */
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            /** @throws Exception error en el hilo */
            System.err.println("[HILO] Error al crear flujos: " + e.getMessage());
        }
    }

    /**
     * Obtiene el nombre registrado del jugador.
     * 
     * @return El nombre del usuario
     */
    public String getNombre() {
        return this.nombreJugador;
    }

    /**
     * Obtiene la mano actual de cartas del jugador.
     * 
     * @return Lista con las cartas del usuario
     */
    public java.util.List<String> getMano() {
        return this.mano;
    }

    /**
     * Envía un mensaje privado a la consola de este jugador.
     * 
     * @param mensaje Texto a enviar
     */
    public void enviarMensaje(String mensaje) {
        if (out != null) {
            out.println(mensaje);
        }
    }

    /**
     * Añade una carta a la mano del jugador y le notifica su estado.
     * 
     * @param carta Texto que representa la carta recibida
     */
    public void recibirCarta(String carta) {
        /** Carta del jugador */
        this.mano.add(carta); 
        
        /** le enviamos un msj para que vea que carta le salio */
        enviarMensaje("[CARTAS] Recibiste: " + carta + ".Tu mano actual: " + mano + " (Puntos: " + Servidor.calcularPuntos(mano) + ")");
    }

    /**
     * Hilo de ejecución que se encarga del procesamiento de comandos en tiempo real.
     */
    @Override
    public void run() {
        try {
            /** Bienvenida */
            enviarMensaje("Bienvenido al servidor de Blackjack \nIngresa el nombre");

            this.nombreJugador = in.readLine(); 
            
            enviarMensaje("Perfecto " + nombreJugador + ", ya estas registrado en la mesa. \nEscribí 'iniciar' para arrancar la partida.");
            System.out.println("[SERVIDOR] El jugador en este hilo se registro como: " + nombreJugador);
            
            String texto;
            while ((texto = in.readLine()) != null) {
                /** Pasamos a minúsculas y quitamos espacios */
                String comando = texto.trim().toLowerCase(); 

                if (comando.equals("iniciar") || comando.equals("i")) {
                    if (Servidor.juegoIniciado) {
                        enviarMensaje("La partida ya está en curso.");
                    } else {
                        /** Iniciarmos partida si no esta en curso */
                        Servidor.iniciarPartida(); 
                    }
                    continue;
                }

                if (!Servidor.juegoIniciado) {
                    enviarMensaje("El juego aún no comenzó. Escribí 'iniciar' para arrancar.");
                    /** Continua con el codigho */
                    continue; 
                }

                if (comando.equals("pedir") || comando.equals("p")) {
                    /** crupier le da otra carta mas */
                    String nuevaCarta = Servidor.tomarCarta();
                    recibirCarta(nuevaCarta);
                    
                    /** Avisamos a los demás */
                    Servidor.difundirMensaje("[JUEGO] " + nombreJugador + " pidio una carta.");

                    /** Calculamos los puntos al pedir otra carta */
                    int puntosActuales = Servidor.calcularPuntos(this.mano);
                    if (puntosActuales > 21) {
                        enviarMensaje("[❌] ¡Te pasaste de 21 puntos!.");
                        Servidor.difundirMensaje("[JUEGO] " + nombreJugador + " se paso de 21 con " + puntosActuales + " puntos.");
                        
                        /** Si se pasó avanzamos de turno */
                        Servidor.avanzarTurno(); 
                    } else {
                        /** Si no se pasó, le volvemos a preguntar si quiere seguir pidiendo o plantarse */
                        enviarMensaje("¿Qué querés hacer? Escribí 'pedir' o 'plantarse'.");
                    }

                } else if (comando.equals("plantarse") || comando.equals("pl")) {
                    /** avisamos que hizo y avanzamos turno */
                    Servidor.difundirMensaje("[JUEGO] " + nombreJugador + " se ha plantado con " + Servidor.calcularPuntos(this.mano) + " puntos.");
                    
                    Servidor.avanzarTurno();
                    
                } else {
                    /** Comando inválido */
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
