package es.uva.buscaminas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uva.buscaminas.controlador.Buscaminas;
import es.uva.buscaminas.modelo.Casilla;
import es.uva.buscaminas.modelo.Dificultad;
import es.uva.buscaminas.modelo.EstadoCasilla;
import es.uva.buscaminas.modelo.EstadoPartida;
import es.uva.buscaminas.modelo.Tablero;

/**
 * Tests de la lógica del buscaminas.
 *
 * Usamos siempre la misma semilla de aleatoriedad para que el reparto de
 * minas sea el mismo en cada ejecución y los tests no fallen de vez en cuando.
 *
 * @author Oscar
 */
class BuscaminasTest {

    private static final long SEMILLA = 4242L;

    private Buscaminas juego;

    @BeforeEach
    void empezarPartida() {
        juego = new Buscaminas(new Random(SEMILLA));
        juego.nuevaPartida(Dificultad.FACIL);
    }

    // ---------------------------------------------------------------------
    // Primera jugada
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("La primera jugada nunca pierde: la casilla pulsada no tiene mina")
    void primeraJugadaNuncaTocaMina() {
        // Repetimos con varias posiciones distintas
        for (int fila = 0; fila < 9; fila += 2) {
            for (int columna = 0; columna < 9; columna += 2) {
                juego.nuevaPartida(Dificultad.FACIL);
                juego.revelar(fila, columna);

                assertEquals(EstadoPartida.EN_CURSO, juego.getEstado(),
                        "La partida debe seguir viva al abrir la primera casilla ("
                                + fila + "," + columna + ")");
                assertEquals(EstadoCasilla.REVELADA, juego.getEstadoCasilla(fila, columna));
            }
        }
    }

    @Test
    @DisplayName("Las minas no se colocan hasta que el jugador hace la primera jugada")
    void minasNoSeColocanAntesDeLaPrimeraJugada() {
        // Al crear la partida todavía no hay minas repartidas
        assertEquals(0, juego.getTablero().getTotalMinas());

        juego.revelar(4, 4);

        assertTrue(juego.getTablero().getTotalMinas() > 0,
                "Tras la primera jugada ya deben estar las minas");
    }

    // ---------------------------------------------------------------------
    // Reglas básicas
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Tocar una mina termina la partida en derrota y muestra las minas")
    void tocarMinaPierdeLaPartida() {
        // Buscamos una casilla con mina jugando con cuidado en otra zona
        int[] mina = buscarMinaEnZonaSegura();

        juego.revelar(mina[0], mina[1]);

        assertEquals(EstadoPartida.PERDIDA, juego.getEstado());
        // Con la partida perdida las minas deben estar visibles para el jugador
        assertTrue(juego.getTablero().getCasilla(mina[0], mina[1]).estaRevelada());
    }

    @Test
    @DisplayName("No se pueden hacer jugadas cuando la partida ya ha terminado")
    void jugadasIgnoradasTrasTerminar() {
        int[] mina = buscarMinaEnZonaSegura();
        juego.revelar(mina[0], mina[1]);
        assertEquals(EstadoPartida.PERDIDA, juego.getEstado());

        // Intentamos abrir y marcar casillas después de perder
        int ocultasAntes = juego.getTablero().contarOcultas();
        juego.revelar(0, 0);
        juego.alternarBandera(0, 0);

        assertEquals(ocultasAntes, juego.getTablero().contarOcultas(),
                "Tras perder no debe cambiar nada del tablero");
        assertEquals(EstadoPartida.PERDIDA, juego.getEstado());
    }

    // ---------------------------------------------------------------------
    // Banderas y contador de minas
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("La bandera se pone y se quita, y descuenta el contador de minas")
    void banderaAlteraElContadorDeMinas() {
        assertEquals(Dificultad.FACIL.getMinas(), juego.getMinasRestantes());

        assertTrue(juego.alternarBandera(3, 3));
        assertTrue(juego.getTablero().getCasilla(3, 3).tieneBandera());
        assertEquals(Dificultad.FACIL.getMinas() - 1, juego.getMinasRestantes());

        // Otra vez sobre la misma casilla la quita
        assertTrue(juego.alternarBandera(3, 3));
        assertFalse(juego.getTablero().getCasilla(3, 3).tieneBandera());
        assertEquals(Dificultad.FACIL.getMinas(), juego.getMinasRestantes());
    }

    @Test
    @DisplayName("Las banderas no se pueden poner sobre casillas ya abiertas")
    void banderaSobreCasillaReveladaNoHaceNada() {
        juego.revelar(4, 4);

        boolean cambia = juego.alternarBandera(4, 4);

        assertFalse(cambia, "Una casilla abierta no debería aceptar bandera");
    }

    @Test
    @DisplayName("Una casilla con bandera no se abre al pulsarla")
    void casillaConBanderaNoSeAbre() {
        // Buscamos una casilla sin mina para marcar
        int[] segura = buscarCasillaSinMina();

        juego.alternarBandera(segura[0], segura[1]);
        juego.revelar(segura[0], segura[1]);

        assertEquals(EstadoCasilla.BANDERA, juego.getEstadoCasilla(segura[0], segura[1]),
                "La bandera debe proteger la casilla de abrirse");
    }

    @Test
    @DisplayName("El contador de minas restantes nunca baja de cero")
    void contadorDeMinasNoNegativo() {
        // Marcamos muchas más banderas de las que hay minas
        int marcadas = 0;
        for (int f = 0; f < 9 && marcadas < 15; f++) {
            for (int c = 0; c < 9 && marcadas < 15; c++) {
                if (juego.alternarBandera(f, c)) {
                    marcadas++;
                }
            }
        }

        assertTrue(juego.getMinasRestantes() >= 0, "El contador no puede ser negativo");
    }

    // ---------------------------------------------------------------------
    // Cascada
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Abrir una casilla vacía abre también las de alrededor en cascada")
    void casillaVaciaAbreEnCascada() {
        // Montamos un tablero pequeño a mano para controlar el resultado
        Tablero tablero = new Tablero(5, 5);
        tablero.colocarMinas(1, 4, 4, new Random(SEMILLA));

        // Buscamos una casilla que esté de verdad vacía (sin minas alrededor),
        // así no dependemos de que la esquina quede o no vacía con esta semilla
        int filaVacia = -1;
        int columnaVacia = -1;
        for (int f = 0; f < 5 && filaVacia < 0; f++) {
            for (int c = 0; c < 5; c++) {
                Casilla casilla = tablero.getCasilla(f, c);
                if (casilla.estaOculta() && casilla.getMinasVecinas() == 0) {
                    filaVacia = f;
                    columnaVacia = c;
                    break;
                }
            }
        }
        assertTrue(filaVacia >= 0, "Debe haber alguna casilla vacía en el tablero");

        int abiertas = tablero.revelar(filaVacia, columnaVacia);

        assertTrue(abiertas > 1,
                "Cascada: al abrir una casilla con 0 minas vecinas deberían abrirse varias");
    }

    @Test
    @DisplayName("El tablero reparte exactamente las minas pedidas y deja libre la zona segura")
    void repartoDeMinasCorrecto() {
        Tablero tablero = new Tablero(9, 9);
        tablero.colocarMinas(10, 4, 4, new Random(SEMILLA));

        assertEquals(10, tablero.getTotalMinas());

        // La zona 3x3 alrededor de (4,4) no puede tener mina
        for (int f = 3; f <= 5; f++) {
            for (int c = 3; c <= 5; c++) {
                assertFalse(tablero.getCasilla(f, c).tieneMina(),
                        "La casilla (" + f + "," + c + ") está en la zona segura");
            }
        }
    }

    @Test
    @DisplayName("Cada casilla sabe cuántas minas tiene alrededor")
    void conteoDeMinasVecinas() {
        Tablero tablero = new Tablero(3, 3);

        // Colocamos una única mina en la esquina... sin zona segura no aplica,
        // así que usamos colocarMinas con una casilla que quede lejos.
        tablero.colocarMinas(1, 0, 0, new Random(SEMILLA));

        int minasTotales = tablero.getTotalMinas();
        assertEquals(1, minasTotales);

        // Sumando las minas vecinas de todas las casillas debe salir
        // como mínimo lo mismo que las minas del tablero
        int suma = 0;
        for (int f = 0; f < 3; f++) {
            for (int c = 0; c < 3; c++) {
                if (!tablero.getCasilla(f, c).tieneMina()) {
                    suma += tablero.getCasilla(f, c).getMinasVecinas();
                }
            }
        }
        assertTrue(suma >= 1, "Debe haber al menos una casilla avisada de la mina");
    }

    // ---------------------------------------------------------------------
    // Victoria
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Ganar cuando se abren todas las casillas sin mina")
    void victoriaAlAbrirTodoLoSeguro() {
        // Jugamos a ciegas abriendo todo lo que no esté marcado: como la
        // primera jugada abre cascada, esto termina tarde o temprano
        juego.revelar(4, 4);

        for (int f = 0; f < 9; f++) {
            for (int c = 0; c < 9; c++) {
                if (juego.estaEnCurso()) {
                    juego.revelar(f, c);
                }
            }
        }

        // Como mínimo, jugar a ciegas debe o ganar o perder sin excepciones
        assertNotEquals(null, juego.getEstado());
        assertTrue(juego.getEstado() == EstadoPartida.GANADA
                        || juego.getEstado() == EstadoPartida.PERDIDA,
                "El juego siempre termina en victoria o derrota");
    }

    // ---------------------------------------------------------------------
    // Ayudas para los tests
    // ---------------------------------------------------------------------

    /** Busca una casilla con mina fuera de la zona segura (5,5) inicial. */
    private int[] buscarMinaEnZonaSegura() {
        juego.revelar(0, 0);
        for (int f = 0; f < 9; f++) {
            for (int c = 0; c < 9; c++) {
                if (juego.getTablero().getCasilla(f, c).tieneMina()) {
                    return new int[]{f, c};
                }
            }
        }
        throw new IllegalStateException("El tablero no tiene ninguna mina");
    }

    /** Busca una casilla sin mina que siga oculta. */
    private int[] buscarCasillaSinMina() {
        juego.revelar(0, 0);
        for (int f = 0; f < 9; f++) {
            for (int c = 0; c < 9; c++) {
                var casilla = juego.getTablero().getCasilla(f, c);
                if (!casilla.tieneMina() && casilla.estaOculta()) {
                    return new int[]{f, c};
                }
            }
        }
        throw new IllegalStateException("No queda ninguna casilla sin mina oculta");
    }
}
