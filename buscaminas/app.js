/*
 * Vista del buscaminas.
 *
 * Aquí solo se pinta y se mandan jugadas: toda la lógica (minas, cascada,
 * reglas de victoria/derrota) vive en Java y llega compilada en buscaminas.js
 * gracias a TeaVM. Cada jugada nos devuelve el estado completo en JSON y
 * repintamos el tablero con eso.
 */

// Funciones exportadas por el código Java (ver ApiWeb.java)
const api = {
  nuevaPartida: (dificultad) => JSON.parse(window.nuevaPartida(dificultad)),
  revelar: (fila, columna) => JSON.parse(window.revelar(fila, columna)),
  alternarBandera: (fila, columna) => JSON.parse(window.alternarBandera(fila, columna)),
};

// Colores de los números, como el buscaminas de siempre
const COLORES_NUMEROS = ['', '#3b82f6', '#22c55e', '#ef4444', '#a855f7',
                         '#f59e0b', '#14b8a6', '#e5e7eb', '#9ca3af'];

let partida = null;
let segundos = 0;
let reloj = null;

const tableroEl = document.getElementById('tablero');
const mensajeEl = document.getElementById('mensaje');
const minasEl = document.getElementById('minas-restantes');
const cronometroEl = document.getElementById('cronometro');
const dificultadEl = document.getElementById('dificultad');

/** Arranca una partida nueva con la dificultad del selector. */
function iniciarPartida() {
  partida = api.nuevaPartida(dificultadEl.value);
  arrancarReloj();
  pintar();
}

/** Recibe el estado que devuelve Java y lo dibuja en pantalla. */
function pintar() {
  // El tablero se reconstruye entero: con un tablero de 16x30 sigue
  // siendo instantáneo y así nunca se queda nada desactualizado
  tableroEl.innerHTML = '';
  tableroEl.style.setProperty('--columnas', partida.columnas);

  const total = partida.filas * partida.columnas;
  for (let i = 0; i < total; i++) {
    const fila = Math.floor(i / partida.columnas);
    const columna = i % partida.columnas;
    const datos = partida.casillas[i];

    const celda = document.createElement('button');
    celda.type = 'button';
    celda.dataset.fila = fila;
    celda.dataset.columna = columna;
    celda.className = 'celda';

    if (datos.estado === 'BANDERA') {
      celda.classList.add('bandera');
      celda.textContent = '🚩';
    } else if (datos.estado === 'REVELADA') {
      celda.classList.add('abierta');
      if (datos.mina) {
        celda.classList.add('mina');
        celda.textContent = '💣';
      } else if (datos.vecinas > 0) {
        celda.textContent = datos.vecinas;
        celda.style.color = COLORES_NUMEROS[datos.vecinas];
      }
    }

    tableroEl.appendChild(celda);
  }

  // Actualizamos los marcadores y el mensaje según el estado de la partida
  minasEl.textContent = `💣 ${String(partida.minasRestantes).padStart(3, '0')}`;

  const carita = document.getElementById('btn-cara');
  if (partida.estado === 'GANADA') {
    mensajeEl.textContent = '¡Has ganado! Has descubierto todas las casillas seguras 🎉';
    carita.textContent = '😎';
    pararReloj();
  } else if (partida.estado === 'PERDIDA') {
    mensajeEl.textContent = '¡Boom! Has pisado una mina. Pulsa la carita para revancha 💥';
    carita.textContent = '😵';
    pararReloj();
  } else {
    mensajeEl.textContent = 'Buscando minas...';
    carita.textContent = '🙂';
  }
}

/** Clic izquierdo: descubrir casilla (o clic derecho: bandera). */
tableroEl.addEventListener('click', (evento) => {
  const celda = evento.target.closest('.celda');
  if (!celda || partida.estado !== 'EN_CURSO') return;

  partida = api.revelar(Number(celda.dataset.fila), Number(celda.dataset.columna));
  arrancarRelojEnPrimeraJugada();
  pintar();
});

tableroEl.addEventListener('contextmenu', (evento) => {
  const celda = evento.target.closest('.celda');
  if (!celda || partida.estado !== 'EN_CURSO') return;

  evento.preventDefault();
  partida = api.alternarBandera(Number(celda.dataset.fila), Number(celda.dataset.columna));
  pintar();
});

// ---------------------------------------------------------------------
// Cronómetro: lo lleva la vista, porque es algo puramente de pantalla
// ---------------------------------------------------------------------

function arrancarReloj() {
  pararReloj();
  segundos = 0;
  pintarCronometro();
  reloj = setInterval(() => {
    if (partida && partida.estado === 'EN_CURSO') {
      segundos = Math.min(999, segundos + 1);
      pintarCronometro();
    }
  }, 1000);
}

/** El reloj no empieza a correr hasta que no haces la primera jugada. */
let yaJugo = false;
function arrancarRelojEnPrimeraJugada() {
  if (!yaJugo) {
    yaJugo = true;
    segundos = 0;
    pintarCronometro();
  }
}

function pararReloj() {
  if (reloj) {
    clearInterval(reloj);
    reloj = null;
  }
}

function pintarCronometro() {
  cronometroEl.textContent = `⏱ ${String(segundos).padStart(3, '0')}`;
}

// ---------------------------------------------------------------------
// Controles
// ---------------------------------------------------------------------

document.getElementById('btn-nueva').addEventListener('click', () => {
  yaJugo = false;
  iniciarPartida();
});

document.getElementById('btn-cara').addEventListener('click', () => {
  yaJugo = false;
  iniciarPartida();
});

dificultadEl.addEventListener('change', () => {
  yaJugo = false;
  iniciarPartida();
});

// Primera partida al abrir la página
iniciarPartida();
