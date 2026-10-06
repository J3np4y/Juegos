package es.uva.buscaminas.modelo;

/**
 * Estados posibles de una casilla del tablero.
 *
 * @author Oscar
 */
public enum EstadoCasilla {

    /** Todavía no se ha tocado, está tapada. */
    OCULTA,

    /** Ya se ha descubierto: se muestra si tiene mina o sus minas vecinas. */
    REVELADA,

    /** El jugador ha marcado bandera porque sospecha que hay una mina. */
    BANDERA
}
