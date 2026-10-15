package server;

import java.util.*;
import java.net.*;

/**
 * Clase Servidor para coordinar el juego multijugador de Blackjack.
 * 
 * @author Ignacio Alvarez
 * @version 1.0
 */
public class Servidor {
    /** Definicion de puerto */
    private static final int PUERTO = 6000;
    public static List<HiloCliente> jugadores = Collections.synchronizedList(new ArrayList<>());
    private static List<String> baraja = new ArrayList<>();
    private static List<String> cartasCrupier = new ArrayList<>();
    public static boolean juegoIniciado = false;
    private static int turnoActual = 0;

    /**
     * Método principal para iniciar el servidor de red.
     * 
     * @param args Argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        System.out.println("[SERVIDOR] Intentando encender...");

        inicializarBaraja();

        /** Iniciar puerto */
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("[SERVIDOR] ¡Encendido! Escuchando en el puerto " + PUERTO);
            System.out.println("[SERVIDOR] Esperando que se conecte un cliente...");

            /** Bucle de espera */
            while (true) {

                /** acepatacion del cliente */
                Socket socketCliente = serverSocket.accept();

                /** Obtener la ip del jugador */
                String ipCliente = socketCliente.getInetAddress().getHostAddress();
                /** puerto de salida del jugador */
                int puertoCliente = socketCliente.getPort(); 

                System.out.println("[SERVIDOR] ¡Jugador conectado desde la IP: " + ipCliente + " (Puerto: "
                        + puertoCliente + ")!");

                /** Craciacion del hilo */
                HiloCliente gestorCliente = new HiloCliente(socketCliente);

                /** Agragar jugador en la mesa */
                jugadores.add(gestorCliente);
                System.out.println("[SERVIDOR] Jugador agregado a la lista. Total en mesa: " + jugadores.size());

                Thread hilo = new Thread(gestorCliente);

                /** Encender */
                hilo.start();

                System.out.println("[SERVIDOR] Hilo delegado con éxito. Volviendo a esperar...");

            }
        } catch (Exception e) {
            /** @throws Exception Error en el puerto */
            System.err.println("[SERVIDOR] Error crítico: No se pudo abrir el puerto. " + e.getMessage());
        }
    }

    /**
     * Inicializa y mezcla el mazo de cartas.
     */
    public static void inicializarBaraja() {
        /** Clearamos */
        baraja.clear(); 

        /** Valores y palos */
        String[] palos = { "♥", "♦", "♣", "♠" };
        String[] valores = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "As" };

        /** Combinamiento de cartas */
        for (String palo : palos) {
            for (String valor : valores) {
                baraja.add(valor + palo);
            }
        }

        /** Mezclamos la lista */
        Collections.shuffle(baraja);
        System.out.println("[SERVIDOR] ¡Mazo de cartas creado y mezclado con éxito!");
    }

    /**
     * Calcula el puntaje total de la mano de un jugador.
     * 
     * @param mano Lista de cartas obtenidas por el jugador
     * @return devuelve los puntos contados
     */
    public static int calcularPuntos(List<String> mano) {
        int puntos = 0;
        int contadorAses = 0;

        for (String carta : mano) {
            /** Le quitamos el palo(♥, ♦, ♣, ♠) */
            String valor = carta.substring(0, carta.length() - 1);

            if (valor.equals("J") || valor.equals("Q") || valor.equals("K")) {
                /** Si es j,q o k valen 10 */
                puntos += 10; 
            } else if (valor.equals("As")) {
                /** Cuantos AS tiene el jugador */
                contadorAses++; 
                /** Cuanto vale el As */
                puntos += 11; 
            } else {
                /** Sumamos los valores si no son letras */
                puntos += Integer.parseInt(valor); 
            }
        }

        /** Si tenemos AS y nos pasamos de 21: */
        while (puntos > 21 && contadorAses > 0) {
            /** Restamos 10 */
            puntos -= 10; 
            /** Restamos AS del jugador */
            contadorAses--; 
        }

        return puntos; 
    }

    /**
     * Toma la carta superior del mazo.
     * 
     * @return La carta removida de la baraja
     */
    public static synchronized String tomarCarta() {
        if (baraja.isEmpty()) {
            /** Si no hay mas cartas Mezclamos */
            inicializarBaraja();
        }
        /** Aggaramos la primera carta */
        return baraja.remove(0);
    }

    /**
     * Da inicio a la partida y reparte las cartas correspondientes.
     */
    public static synchronized void iniciarPartida() {
        if (juegoIniciado || jugadores.isEmpty())
            return;

        juegoIniciado = true;
        difundirMensaje("[JUEGO] ¡La partida ha comenzado! Siguiendo secuencia de reparto...");

        /** El crupier le da una carta al jugador */
        for (HiloCliente jugador : jugadores) {
            jugador.recibirCarta(tomarCarta());
        }

        /** Crupier se da una carta */
        cartasCrupier.add(tomarCarta());
        difundirMensaje("[CRUPIER] 1° carta: " + cartasCrupier.get(0));

        /** Le da otra carta al jugador */
        for (HiloCliente jugador : jugadores) {
            jugador.recibirCarta(tomarCarta());
        }

        /** El crupier se da otra carta pero esta no es visible */
        cartasCrupier.add(tomarCarta());
        difundirMensaje("[CRUPIER] 2° carta boca abajo.");

        /** Le preguntamos que quiere hacer */
        /** Arranca el primero de la lista */
        turnoActual = -1; 
        avanzarTurno();
    }

    /**
     * Difunde un mensaje de red a toda la mesa de juego.
     * 
     * @param mensaje Cadena de caracteres a enviar
     */
    public static void difundirMensaje(String mensaje) {
        synchronized (jugadores) {
            for (HiloCliente jugador : jugadores) {
                jugador.enviarMensaje(mensaje);
            }
        }
    }

    /**
     * Incrementa el índice de turnos y notifica al jugador correspondiente.
     */
    public static synchronized void avanzarTurno() {
        turnoActual++; 
        /** Si ya jugaron todos. Turno del crupier */
        if (turnoActual >= jugadores.size()) {
            difundirMensaje("[JUEGO] Todos los jugadores han decidido. Turno del Crupier...");
            jugarCrupier(); 
            return;
        }

        /** de quien es el turno */
        HiloCliente jugadorActual = jugadores.get(turnoActual);

        /** Avisamos de quien es el turno */
        difundirMensaje("[TURNO] Es el turno de " + jugadorActual.getNombre() + ".");

        /** msj para el jugador de quien es el turno */
        jugadorActual.enviarMensaje("Es tu turno. ¿Qué querés hacer? Escribí 'pedir' o 'plantarse'. ");
    }

    /**
     * Controla el juego automático del Crupier de la casa.
     */
    private static void jugarCrupier() {
        /** Mostramos las 2 cartas del crupier */
        difundirMensaje("[CRUPIER] Mi mano actual: " + cartasCrupier + " (Puntos: " + calcularPuntos(cartasCrupier) + ")");
        
        /** si el crupier tiene menos de 17 pide cartas */
        while (calcularPuntos(cartasCrupier) < 17) {
            String nuevaCarta = tomarCarta();
            cartasCrupier.add(nuevaCarta);
            difundirMensaje("[CRUPIER] Saco carta: " + nuevaCarta + " (Puntos: " + calcularPuntos(cartasCrupier) + ")");
        }

        /** definimos ganadores */
        evaluarGanadores();
    }

    /**
     * Evalúa las manos finales y determina quién venció a la casa.
     */
    private static void evaluarGanadores() {
        int puntosCrupier = calcularPuntos(cartasCrupier);
        
        difundirMensaje("\n================ RESULTADOS FINALES ================");
        if (puntosCrupier > 21) {
            difundirMensaje("[CRUPIER] ¡Me pasé de 21 puntos! (" + puntosCrupier + ")");
        } else {
            difundirMensaje("[CRUPIER] Puntos finales de la casa: " + puntosCrupier);
        }
        difundirMensaje("----------------------------------------------------");

        /** Quien gano */
        for (HiloCliente jugador : jugadores) {
            int puntosJugador = calcularPuntos(jugador.getMano());
            String nombre = jugador.getNombre();

            if (puntosJugador > 21) {
                difundirMensaje("❌ " + nombre + " perdió (Se pasó con " + puntosJugador + " puntos).");
            } else if (puntosCrupier > 21) {
                difundirMensaje("🏆 " + nombre + " ganó (" + puntosJugador + " puntos). ¡La casa se pasó!");
            } else if (puntosJugador > puntosCrupier) {
                difundirMensaje("🏆 " + nombre + " ganó (" + puntosJugador + " vs " + puntosCrupier + ").");
            } else if (puntosJugador < puntosCrupier) {
                difundirMensaje("❌ " + nombre + " perdió (" + puntosJugador + " vs " + puntosCrupier + ").");
            } else {
                difundirMensaje("🤝 " + nombre + " empató con la casa (" + puntosJugador + " puntos).");
            }
        }
        difundirMensaje("====================================================\n");
        difundirMensaje("[SERVIDOR] Partida finalizada.");
    }
}
