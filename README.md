# Juegos

Repositorio de juegos clásicos escritos a mano, sin frameworks ni librerías externas. Cada juego vive en su propia carpeta y se puede ejecutar directamente en el navegador.

**Jugándolos aquí:** https://j3np4y.github.io/Juegos/

## Juegos

| Juego | Carpeta | Estado |
|-------|---------|--------|
| 3 en Raya | [`3_en_raya/`](3_en_raya/) | ✅ Disponible |
| Serpiente | [`serpiente/`](serpiente/) | 🔨 Próximamente |
| Juego de la Vida | [`juego_de_vida/`](juego_de_vida/) | 🔨 Próximamente |
| Tetris | [`tetris/`](tetris/) | 🔨 Próximamente |
| Pong | [`pong/`](pong/) | 🔨 Próximamente |
| Buscaminas | [`buscaminas/`](buscaminas/) | ✅ Disponible |

## Estructura

```
Juegos/
├── index.html        ← índice visual (se sirve en GitHub Pages)
├── README.md
├── 3_en_raya/
│   └── index.html
├── serpiente/
│   └── index.html
├── juego_de_vida/
│   └── index.html
├── tetris/
│   └── index.html
├── pong/
│   └── index.html
└── buscaminas/
    └── index.html
```

## Cómo ejecutar un juego

1. **En línea:** entra en la página de Pages y pulsa la tarjeta del juego.
2. **En local:** abre el `index.html` del juego con doble clic en el navegador.

## Cómo añadir un juego nuevo

1. Crea una carpeta nueva con un `index.html` dentro (el nombre de la carpeta es la URL del juego).
2. Añade una tarjeta en el `index.html` de la raíz y una fila en la tabla de arriba.
3. Haz commit y push: GitHub Pages publica los cambios automáticamente.
