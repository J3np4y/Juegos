package es.uva.buscaminas.web;

import org.teavm.jso.JSExport;

import es.uva.buscaminas.controlador.Buscaminas;
import es.uva.buscaminas.modelo.Casilla;
import es.uva.buscaminas.modelo.Dificultad;
import es.uva.buscaminas.modelo.Tablero;

/**
 * Puente entre la vista (HTML/JavaScript) y la lógica del juego.
 *
 * Los métodos marcados con {@code @JSExport} los exporta TeaVM como funciones
 * de JavaScript, de forma que el index.html puede llamarlos directamente.
 * Todos devuelven el estado completo de la partida en formato JSON, así la
 * vista solo tiene que pintar lo que le llega sin preguntar nada más.
 *
 * @author Oscar
 */
public final class ApiWeb {

    /** La partida vive aquí mientras la página está abierta. */
    private static final Buscaminas PARTIDA = new Buscaminas();

    private ApiWeb() {
        // Clase de utilidad: solo tiene métodos estáticos
    }

    /**
     * Empieza una partida nueva con la dificultad indicada.
     *
     * @param dificultad "FACIL", "MEDIO" o "DIFICIL" (cualquier otra cosa da la fácil)
     * @return el estado de la partida recién creada, en JSON
     */
    @JSExport
    public static String nuevaPartida(String dificultad) {
        PARTIDA.nuevaPartida(Dificultad.desdeNombre(dificultad));
        return serializar();
    }

    /**
     * El jugador pulsa una casilla para abrirla.
     *
     * @param fila    fila de la casilla (contando desde 0)
     * @param columna columna de la casilla (contando desde 0)
     * @return el estado de la partida tras la jugada, en JSON
     */
    @JSExport
    public static String revelar(int fila, int columna) {
        PARTIDA.revelar(fila, columna);
        return serializar();
    }

    /**
     * El jugador marca o desmarca una casilla con bandera (clic derecho).
     *
     * @param fila    fila de la casilla
     * @param columna columna de la casilla
     * @return el estado de la partida tras la marca, en JSON
     */
    @JSExport
    public static String alternarBandera(int fila, int columna) {
        PARTIDA.alternarBandera(fila, columna);
        return serializar();
    }

    /**
     * Escribe el estado de la partida como un texto JSON.
     *
     * Lo hacemos a mano con StringBuilder en vez de meter una librería de
     * JSON: el formato es sencillo y así el juego no arrastra dependencias.
     * Las minas solo se incluyen cuando el jugador ha perdido, así nadie
     * podría "ver" el JSON para hacer trampa mientras juega.
     */
    private static String serializar() {
        Tablero tablero = PARTIDA.getTablero();
        boolean partidaTerminada = !PARTIDA.estaEnCurso();

        StringBuilder json = new StringBuilder(4096);
        json.append("{\"filas\":").append(tablero.getFilas());
        json.append(",\"columnas\":").append(tablero.getColumnas());
        json.append(",\"estado\":\"").append(PARTIDA.getEstado()).append('"');
        json.append(",\"minasRestantes\":").append(PARTIDA.getMinasRestantes());
        json.append(",\"casillas\":[");

        for (int f = 0; f < tablero.getFilas(); f++) {
            for (int c = 0; c < tablero.getColumnas(); c++) {
                if (f > 0 || c > 0) {
                    json.append(',');
                }
                Casilla casilla = tablero.getCasilla(f, c);
                json.append("{\"estado\":\"").append(casilla.getEstado()).append('"');
                json.append(",\"vecinas\":").append(casilla.getMinasVecinas());
                if (partidaTerminada && casilla.tieneMina()) {
                    json.append(",\"mina\":true");
                }
                json.append('}');
            }
        }

        return json.append("]}").toString();
    }
}
