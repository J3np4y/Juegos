package es.uva.buscaminas.modelo;

/**
 * Estado de la partida en curso. Se comprueba al final de cada jugada
 * para saber si seguimos jugando o ya hay un ganador o un perdedor.
 *
 * @author Oscar
 */
public enum EstadoPartida {

    /** La partida sigue viva, se pueden hacer más jugadas. */
    EN_CURSO,

    /** Se han descubierto todas las casillas sin mina. */
    GANADA,

    /** Se ha pulsado una casilla con mina. */
    PERDIDA
}
