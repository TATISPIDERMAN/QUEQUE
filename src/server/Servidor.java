package server;

import java.util.*;
import java.net.*;

public class Servidor {
    // Definicion de puerto
    private static final int PUERTO = 6000;
    public static List<HiloCliente> jugadores = Collections.synchronizedList(new ArrayList<>());
    private static List<String> baraja = new ArrayList<>();
    private static List<String> cartasCrupier = new ArrayList<>();
    private static boolean juegoIniciado = false;
    private static int turnoActual = 0;

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

                // Obtener la ip del jugador
                String ipCliente = socketCliente.getInetAddress().getHostAddress();
                int puertoCliente = socketCliente.getPort(); // puerto de salida del jugador

                System.out.println("[SERVIDOR] ¡Jugador conectado desde la IP: " + ipCliente + " (Puerto: "
                        + puertoCliente + ")!");

                // Craciacion del hilo
                HiloCliente gestorCliente = new HiloCliente(socketCliente);

                // Agragar jugador en la mesa
                jugadores.add(gestorCliente);
                System.out.println("[SERVIDOR] Jugador agregado a la lista. Total en mesa: " + jugadores.size());

                Thread hilo = new Thread(gestorCliente);

                // Encender
                hilo.start();

                System.out.println("[SERVIDOR] Hilo delegado con éxito. Volviendo a esperar...");

            }
            // Error en el puerto
        } catch (Exception e) {
            System.err.println("[SERVIDOR] Error crítico: No se pudo abrir el puerto. " + e.getMessage());
        }
    }

    public static void inicializarBaraja() {
        baraja.clear(); // Clearamos

        // Valores y palos
        String[] palos = { "♥", "♦", "♣", "♠" };
        String[] valores = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "As" };

        // Combinamiento de cartas
        for (String palo : palos) {
            for (String valor : valores) {
                baraja.add(valor + palo);
            }
        }

        // Mezclamos la lista
        Collections.shuffle(baraja);
        System.out.println("[SERVIDOR] ¡Mazo de cartas creado y mezclado con éxito!");
    }

    public static int calcularPuntos(List<String> mano) {
        int puntos = 0;
        int contadorAses = 0;

        for (String carta : mano) {
            // Le quitamos el palo(♥, ♦, ♣, ♠)
            String valor = carta.substring(0, carta.length() - 1);

            if (valor.equals("J") || valor.equals("Q") || valor.equals("K")) {
                puntos += 10; // Si es j,q o k valen 10
            } else if (valor.equals("As")) {
                contadorAses++; // Cuantos AS tiene el jugador
                puntos += 11; // Cuanto vale el As
            } else {
                puntos += Integer.parseInt(valor); // Sumamos los valores si no son letras
            }
        }

        // Si tenemos AS y nos pasamos de 21:
        while (puntos > 21 && contadorAses > 0) {
            puntos -= 10; // Restamos 10
            contadorAses--; // Restamos AS del jugador
        }

        return puntos; // devuelve los puntos contados
    }

    public static synchronized String tomarCarta() {
        if (baraja.isEmpty()) {
            // Si no hay mas cartas Mezclamos
            inicializarBaraja();
        }
        // Aggaramos la primera carta
        return baraja.remove(0);
    }

    public static synchronized void iniciarPartida() {
        if (juegoIniciado || jugadores.isEmpty())
            return;

        juegoIniciado = true;
        difundirMensaje("[JUEGO] ¡La partida ha comenzado! Siguiendo secuencia de reparto...");

        // El crupier le da una carta al jugador
        for (HiloCliente jugador : jugadores) {
            jugador.recibirCarta(tomarCarta());
        }

        // Crupier se da una carta
        cartasCrupier.add(tomarCarta());
        difundirMensaje("[CRUPIER] 1° carta: " + cartasCrupier.get(0));

        // Le da otra carta al jugador
        for (HiloCliente jugador : jugadores) {
            jugador.recibirCarta(tomarCarta());
        }

        // El crupier se da otra carta pero esta no es visible
        cartasCrupier.add(tomarCarta());
        difundirMensaje("[CRUPIER] 2° carta boca abajo.");

        // Le preguntamos que quiere hacer
        difundirMensaje("[JUEGO] Reparto inicial completo. Comienza la ronda de decisiones.");
        turnoActual = 0; // Arranca el primero de la lista
        avanzarTurno();
    }

    public static void difundirMensaje(String mensaje) {
        synchronized (jugadores) {
            for (HiloCliente jugador : jugadores) {
                jugador.enviarMensaje(mensaje);
            }
        }
    }

    public static synchronized void avanzarTurno() {
        // Si ya jugaron todos. Turno del crupier
        if (turnoActual >= jugadores.size()) {
            difundirMensaje("[JUEGO] Todos los jugadores han decidido. Turno del Crupier...");
            return;
        }

        // de quien es el turno
        HiloCliente jugadorActual = jugadores.get(turnoActual);

        // Avisamos de quien es el turno
        difundirMensaje("[TURNO] Es el turno de " + jugadorActual.getNombre() + ".");

        // msj para el jugador de quien es el turno
        jugadorActual.enviarMensaje("Es tu turno. ¿Qué querés hacer? Escribí 'pedir' o 'plantarse'.");
    }

}
