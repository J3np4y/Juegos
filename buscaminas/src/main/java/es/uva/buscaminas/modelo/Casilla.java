package es.uva.buscaminas.modelo;

/**
 * Casilla individual del tablero.
 *
 * Guarda si hay mina debajo, cuántas minas hay a su alrededor y en qué
 * estado está (oculta, revelada o con bandera). Es una clase bastante
 * modesta: la lógica de verdad vive en {@link Tablero}.
 *
 * @author Oscar
 */
public class Casilla {

    /** Solo la casilla conoce si la mina está debajo: la vista nunca la consulta. */
    private boolean mina;

    /** Minas en las 8 casillas que rodean esta. Si es 0 se puede abrir en cascada. */
    private int minasVecinas;

    private EstadoCasilla estado;

    public Casilla() {
        this.estado = EstadoCasilla.OCULTA;
    }

    public boolean tieneMina() {
        return mina;
    }

    /** Lo usa el tablero al repartir minas, no el jugador. */
    public void colocarMina() {
        this.mina = true;
    }

    public int getMinasVecinas() {
        return minasVecinas;
    }

    /** Incrementa el contador de minas vecinas mientras se analiza el tablero. */
    public void sumarMinaVecina() {
        this.minasVecinas++;
    }

    public EstadoCasilla getEstado() {
        return estado;
    }

    public void revelar() {
        // Solo tiene sentido abrir casillas tapadas; si ya está con bandera
        // o abierta no tocamos nada para no dejar estados raros.
        if (estado == EstadoCasilla.OCULTA) {
            estado = EstadoCasilla.REVELADA;
        }
    }

    /** @return true si la casilla estaba oculta y ahora tiene bandera (o al revés) */
    public boolean alternarBandera() {
        if (estado == EstadoCasilla.OCULTA) {
            estado = EstadoCasilla.BANDERA;
            return true;
        }
        if (estado == EstadoCasilla.BANDERA) {
            estado = EstadoCasilla.OCULTA;
            return true;
        }
        // Una casilla ya revelada no admite bandera
        return false;
    }

    public boolean estaOculta() {
        return estado == EstadoCasilla.OCULTA;
    }

    public boolean estaRevelada() {
        return estado == EstadoCasilla.REVELADA;
    }

    public boolean tieneBandera() {
        return estado == EstadoCasilla.BANDERA;
    }
}
