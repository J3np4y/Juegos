package es.uva.buscaminas.modelo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Tablero del buscaminas.
 *
 * Se encarga de repartir las minas, contar cuántas minas tiene cada casilla
 * alrededor y abrir el campo en cascada cuando el jugador pilla una zona vacía.
 * No sabe nada de la vista ni de la partida: eso lo llevan otras clases.
 *
 * @author Oscar
 */
public class Tablero {

    /**
     * Desplazamientos para recorrer las 8 casillas que rodean a una.
     * Formato: {desplazamientoDeFila, desplazamientoDeColumna}.
     */
    private static final int[][] DIRECCIONES = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1},           {0, 1},
        {1, -1},  {1, 0},  {1, 1}
    };

    private final Casilla[][] casillas;
    private final int filas;
    private final int columnas;
    private int totalMinas;
    private boolean minasColocadas;

    public Tablero(int filas, int columnas) {
        if (filas <= 0 || columnas <= 0) {
            throw new IllegalArgumentException("El tablero necesita al menos una fila y una columna");
        }
        this.filas = filas;
        this.columnas = columnas;
        this.casillas = new Casilla[filas][columnas];
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                casillas[f][c] = new Casilla();
            }
        }
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }

    public int getTotalMinas() {
        return totalMinas;
    }

    /** @return true si la posición está dentro del tablero */
    public boolean dentroLimites(int fila, int columna) {
        return fila >= 0 && fila < filas && columna >= 0 && columna < columnas;
    }

    /**
     * Devuelve la casilla de la posición indicada.
     *
     * @throws IndexOutOfBoundsException si la posición está fuera del tablero
     */
    public Casilla getCasilla(int fila, int columna) {
        if (!dentroLimites(fila, columna)) {
            throw new IndexOutOfBoundsException(
                    "Posición (" + fila + ", " + columna + ") fuera del tablero");
        }
        return casillas[fila][columna];
    }

    /**
     * Reparte las minas por el tablero.
     *
     * Se llama con la primera jugada del jugador en mano, y por eso recibe la
     * posición de la casilla pulsada: su entorno (las 9 casillas de alrededor)
     * queda libre de minas. Así nadie pierde en el primer clic, que sería muy
     * injusto.
     *
     * El parámetro {@code azar} se puede fijar en los tests para que los
     * resultados sean repetibles.
     *
     * @param totalMinas   cuántas minas queremos repartir
     * @param filaSegura   fila de la primera casilla pulsada
     * @param columnaSegura columna de la primera casilla pulsada
     * @param azar         fuente de aleatoriedad a usar
     */
    public void colocarMinas(int totalMinas, int filaSegura, int columnaSegura, Random azar) {
        if (minasColocadas) {
            return;
        }

        // Todas las posiciones válidas que no son zona segura
        List<Integer> candidatas = new ArrayList<>();
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                if (!esZonaSegura(f, c, filaSegura, columnaSegura)) {
                    candidatas.add(f * columnas + c);
                }
            }
        }

        // Mezclamos y cogemos las primeras: reparto parecido y sin sesgos raros
        Collections.shuffle(candidatas, azar);

        int minasAColocar = Math.min(totalMinas, candidatas.size());
        for (int i = 0; i < minasAColocar; i++) {
            int posicion = candidatas.get(i);
            casillas[posicion / columnas][posicion % columnas].colocarMina();
        }

        this.totalMinas = minasAColocar;
        this.minasColocadas = true;
        calcularMinasVecinas();
    }

    /** La casilla pulsada y sus 8 vecinas no pueden tener mina. */
    private boolean esZonaSegura(int fila, int columna, int filaSegura, int columnaSegura) {
        return Math.abs(fila - filaSegura) <= 1 && Math.abs(columna - columnaSegura) <= 1;
    }

    /**
     * Recorre todo el tablero y deja en cada casilla el número de minas
     * que tiene a su alrededor. Se ejecuta una sola vez, justo después de
     * repartir las minas.
     */
    private void calcularMinasVecinas() {
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                if (casillas[f][c].tieneMina()) {
                    continue;
                }
                for (int[] direccion : DIRECCIONES) {
                    int vecinaFila = f + direccion[0];
                    int vecinaColumna = c + direccion[1];
                    if (dentroLimites(vecinaFila, vecinaColumna)
                            && casillas[vecinaFila][vecinaColumna].tieneMina()) {
                        casillas[f][c].sumarMinaVecina();
                    }
                }
            }
        }
    }

    /**
     * Abre la casilla indicada. Si no tiene minas vecinas, seguimos abriendo
     * las de al lado hasta llegar a casillas con número (recorrido en anchura
     * con una pila). Así el jugador solo tiene que clicar las zonas vacías.
     *
     * Las casillas con bandera se respetan y no se abren.
     *
     * @return cuántas casillas se han abierto con esta jugada (0 si ninguna)
     */
    public int revelar(int fila, int columna) {
        if (!dentroLimites(fila, columna)) {
            return 0;
        }

        int reveladas = 0;
        Deque<int[]> pendientes = new ArrayDeque<>();
        pendientes.push(new int[]{fila, columna});

        while (!pendientes.isEmpty()) {
            int[] actual = pendientes.pop();
            Casilla casilla = getCasilla(actual[0], actual[1]);

            // Saltamos las que ya están abiertas o marcadas con bandera
            if (!casilla.estaOculta()) {
                continue;
            }

            casilla.revelar();
            reveladas++;

            // Solo seguimos si la casilla está vacía (0 minas vecinas)
            if (casilla.getMinasVecinas() == 0) {
                for (int[] direccion : DIRECCIONES) {
                    int vecinaFila = actual[0] + direccion[0];
                    int vecinaColumna = actual[1] + direccion[1];
                    if (dentroLimites(vecinaFila, vecinaColumna)) {
                        pendientes.push(new int[]{vecinaFila, vecinaColumna});
                    }
                }
            }
        }

        return reveladas;
    }

    /**
     * Pone o quita la bandera de la casilla (siempre que esté tapada).
     *
     * @return true si la casilla ha cambiado de estado
     */
    public boolean alternarBandera(int fila, int columna) {
        if (!dentroLimites(fila, columna)) {
            return false;
        }
        return casillas[fila][columna].alternarBandera();
    }

    /** Abre todas las casillas con mina, para enseñarle al jugador lo que había. */
    public void revelarMinas() {
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                if (casillas[f][c].tieneMina()) {
                    casillas[f][c].revelar();
                }
            }
        }
    }

    /** @return cuántas casillas siguen tapadas (contando las de mina) */
    public int contarOcultas() {
        int contador = 0;
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                if (casillas[f][c].estaOculta()) {
                    contador++;
                }
            }
        }
        return contador;
    }

    /** @return cuántas banderas hay puestas por el jugador */
    public int contarBanderas() {
        int contador = 0;
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                if (casillas[f][c].tieneBandera()) {
                    contador++;
                }
            }
        }
        return contador;
    }

    /**
     * Cuenta las casillas que están abiertas sin mina.
     * Si ese número es igual a las casillas totales menos las minas,
     * el jugador ha ganado.
     */
    public int contarReveladasSinMina() {
        int contador = 0;
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                Casilla casilla = casillas[f][c];
                if (casilla.estaRevelada() && !casilla.tieneMina()) {
                    contador++;
                }
            }
        }
        return contador;
    }
}
