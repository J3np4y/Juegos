package es.uva.buscaminas.controlador;

import java.util.Random;

import es.uva.buscaminas.modelo.Casilla;
import es.uva.buscaminas.modelo.Dificultad;
import es.uva.buscaminas.modelo.EstadoCasilla;
import es.uva.buscaminas.modelo.EstadoPartida;
import es.uva.buscaminas.modelo.Tablero;

/**
 * Controlador de la partida.
 *
 * Es la fachada que usa la vista: recibe las jugadas del jugador, aplica las
 * reglas (primera jugada sin minas, perder al tocar una mina, ganar al abrir
 * todo lo seguro) y deja el tablero listo para que la vista lo pinte.
 *
 * @author Oscar
 */
public class Buscaminas {

    private final Random azar;

    private Dificultad dificultad;
    private Tablero tablero;
    private EstadoPartida estado;

    /** Las minas no se colocan hasta la primera jugada, para no perder en el clic 1. */
    private boolean primeraJugada;

    /** Partida normal, con aleatoriedad de verdad. */
    public Buscaminas() {
        this(new Random());
    }

    /**
     * Variante para tests: con la semilla fija el reparto de minas
     * sale siempre igual y los tests se pueden repetir sin sustos.
     */
    public Buscaminas(Random azar) {
        this.azar = azar;
        nuevaPartida(Dificultad.FACIL);
    }

    /** Deja el tablero como recién estrenado y pone la partida en marcha. */
    public void nuevaPartida(Dificultad dificultadNueva) {
        this.dificultad = dificultadNueva;
        this.tablero = new Tablero(dificultadNueva.getFilas(), dificultadNueva.getColumnas());
        this.estado = EstadoPartida.EN_CURSO;
        this.primeraJugada = true;
    }

    /**
     * El jugador pulsa una casilla para abrirla.
     *
     * Si es la primera jugada de la partida, aquí colocamos las minas
     * alrededor (dejando libre la casilla pulsada). Después vemos si hay
     * mina (perdimos), abrimos en cascada y comprobamos si ya ganamos.
     *
     * Las jugadas que llegan con la partida terminada o en una posición
     * rara se ignoran, así la vista nunca puede dejar el juego en un
     * estado inconsistente.
     */
    public void revelar(int fila, int columna) {
        if (estado != EstadoPartida.EN_CURSO) {
            return;
        }
        if (!tablero.dentroLimites(fila, columna)) {
            return;
        }

        Casilla casilla = tablero.getCasilla(fila, columna);

        // Una casilla con bandera no se abre: primero hay que quitarle la marca
        if (casilla.tieneBandera() || casilla.estaRevelada()) {
            return;
        }

        if (primeraJugada) {
            tablero.colocarMinas(dificultad.getMinas(), fila, columna, azar);
            primeraJugada = false;
        }

        if (casilla.tieneMina()) {
            // Boom: enseñamos todas las minas y terminamos
            estado = EstadoPartida.PERDIDA;
            tablero.revelarMinas();
            return;
        }

        tablero.revelar(fila, columna);

        if (hemosGanado()) {
            estado = EstadoPartida.GANADA;
        }
    }

    /**
     * El jugador marca/desmarca una casilla como sospechosa de tener mina.
     *
     * @return true si la marca ha cambiado (para que la vista actualice el contador)
     */
    public boolean alternarBandera(int fila, int columna) {
        if (estado != EstadoPartida.EN_CURSO) {
            return false;
        }
        return tablero.alternarBandera(fila, columna);
    }

    /**
     * Ganamos cuando solo quedan tapadas las casillas con mina,
     * es decir, cuando hemos abierto todas las seguras.
     */
    private boolean hemosGanado() {
        int casillasSeguras = tablero.getFilas() * tablero.getColumnas() - tablero.getTotalMinas();
        return tablero.contarReveladasSinMina() == casillasSeguras;
    }

    /**
     * Minas que faltan por marcar: las del total menos las banderas puestas.
     * Si el jugador abusa de las banderas puede bajar de 0, así que lo
     * cortamos en cero para no enseñar números negativos.
     */
    public int getMinasRestantes() {
        return Math.max(0, dificultad.getMinas() - tablero.contarBanderas());
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public boolean estaEnCurso() {
        return estado == EstadoPartida.EN_CURSO;
    }

    /**
     * Consulta rápida para tests y para la vista: qué hay en una casilla.
     * Si el juego ya ha terminado en derrota devuelve la casilla tal cual;
     * si sigue en curso, una casilla con mina tapada se comporta como tapada.
     */
    public EstadoCasilla getEstadoCasilla(int fila, int columna) {
        return tablero.getCasilla(fila, columna).getEstado();
    }
}
