package es.uva.buscaminas.modelo;

/**
 * Tamaños de tablero y número de minas para cada nivel de dificultad.
 * Los números son los clásicos del buscaminas de Windows.
 *
 * @author Oscar
 */
public enum Dificultad {

    FACIL(9, 9, 10),
    MEDIO(16, 16, 40),
    DIFICIL(16, 30, 99);

    private final int filas;
    private final int columnas;
    private final int minas;

    Dificultad(int filas, int columnas, int minas) {
        this.filas = filas;
        this.columnas = columnas;
        this.minas = minas;
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }

    public int getMinas() {
        return minas;
    }

    /**
     * Convierte el nombre que manda el navegador en una dificultad.
     * Si llega algo raro (una URL manipulada, por ejemplo) devolvemos
     * la fácil para que la partida nunca falle al arrancar.
     *
     * @param nombre texto recibido desde la vista, p. ej. "MEDIO"
     * @return la dificultad pedida o {@link #FACIL} si el nombre no es válido
     */
    public static Dificultad desdeNombre(String nombre) {
        if (nombre == null) {
            return FACIL;
        }
        try {
            return valueOf(nombre.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return FACIL;
        }
    }
}
