# Juegos

Colección de juegos clásicos para jugar en el navegador. Cada juego está en su propia carpeta y se puede abrir desde el índice web:

**Juega en línea:** https://j3np4y.github.io/Juegos/

## Juegos disponibles

| Juego | Carpeta | Cómo se juega |
|---|---|---|
| 3 en Raya | [`3_en_raya/`](3_en_raya/) | Dos jugadores comparten el teclado y el ratón; gana quien alinea tres marcas. |
| Serpiente | [`serpiente/`](serpiente/) | Come fruta y evita chocar; usa las flechas o WASD. |
| Juego de la Vida | [`juego_de_vida/`](juego_de_vida/) | Elige tablero pequeño, mediano o grande; carga un bloque, parpadeador, planeador o pulsar, o dibuja tu patrón y pulsa Empezar. |
| Tetris | [`tetris/`](tetris/) | Completa líneas; ←/→ mueven, ↓ baja, ↑ gira y Espacio deja caer la pieza. |
| Pong | [`pong/`](pong/) | Dos jugadores: W/S frente a ↑/↓; gana quien llega primero a 7 puntos. |
| Buscaminas | [`buscaminas/`](buscaminas/) | Elige dificultad, descubre casillas y marca posibles minas con clic derecho. |

## Ejecutar en local

Desde la raíz del repositorio, inicia un servidor estático:

```sh
python -m http.server 8000
```

Abre <http://localhost:8000/> en el navegador y elige un juego. Los juegos de navegador no requieren instalar dependencias.

## Buscaminas: lógica Java

Buscaminas mantiene las reglas en Java y carga la salida JavaScript generada por TeaVM. Para probar o regenerar ese archivo se necesita JDK 17 o posterior y Maven:

```sh
cd buscaminas
mvn test
mvn package
```

`mvn package` genera `buscaminas.js` junto a `index.html`; el HTML y la vista JavaScript se encargan de la presentación y los controles.

## Estructura

```text
Juegos/
├── index.html                 # Índice web de los juegos
├── 3_en_raya/index.html
├── serpiente/index.html
├── juego_de_vida/index.html
├── tetris/index.html
├── pong/index.html
└── buscaminas/
    ├── index.html
    ├── app.js                 # Vista y controles
    ├── buscaminas.js          # Lógica compilada con TeaVM
    ├── pom.xml
    └── src/                   # Código Java y pruebas
```

## Ideas futuras

Ideas no implementadas ni comprometidas: exportar y compartir patrones del Juego de la Vida; niveles de velocidad seleccionables en Serpiente; y una modalidad de Pong contra la computadora. No se añadirán juegos nuevos sin aprobación.
