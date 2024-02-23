package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

public class MortadeloAdventure extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Texture img;

	//Elementos para la musica
	private Music musicaJuego;
	private float stateTime;

	//Elementos para los sonidos
	private Sound tesoroEncontrado;
	private Sound pillado;
	private Sound fracaso;
	private Sound exito;

	private int anchoJugador;
	private int altoJugador;
	//Objeto que permite dibujar en el método render() imágenes 2D
	private SpriteBatch sb;
	private static final int FRAME_COLS = 3;
	private static final int FRAME_ROWS = 4;

	// Atributo en el que se cargará la imagen del personaje principal.
	private Texture imagenPrincipal;

	//Animacion que se muestra en el metodo render()
	private Animation<TextureRegion> jugador;
	//Animaciones para cada una de las direcciones de mvto. del jugador
	private Animation<TextureRegion> jugadorArriba;
	private Animation<TextureRegion> jugadorDerecha;
	private Animation<TextureRegion> jugadorAbajo;
	private Animation<TextureRegion> jugadorIzquierda;

	//variable que controla el avance de tiempo para los npc
	private float stateTimeNPC;

	//Jugadores no principales
	private Texture[] imgNPC;

	//Array de animaciones activas de los npc
	private Animation[] npc;

	//Array de animaciones de los NPC para cada dirección
	private Animation[] npcArriba;
	private Animation[] npcDerecha;
	private Animation[] npcAbajo;
	private Animation[] npcIzquierda;

	//Numero de NPC que hay en el juego
	private static final int numeroNPC = 5;
	//Posiciones de los NPC
	private Vector2[] posicionNPC;
	//Posiciones iniciales
	private Vector2[] origen;
	//Posiciones finales
	private Vector2[] destino;
	//Velocidad de desplazamiento de los NPC
	private float velocidadNPC;

	private static boolean izquierda, derecha, arriba, abajo;

	//Objeto que recoge el mapa de baldosas
	private TiledMap mapa;

	//Capa del mapa donde se encuentran los tesoros
	private TiledMapTileLayer capaTesoros;

	//Ancho y alto del mapa en tiles
	private int anchoTiles, altoTiles;

	private int cuentaTesoros;
	//Celda en la que el mapa finaliza
	private Vector2 celdaFinal = new Vector2(2, 4);

	//Arrays bidimensionales de booleanos que contienen los obstáculos y los tesoros del mapa
	private boolean[][] obstaculo, tesoro;

	//Objeto con el que se pinta el mapa de baldosas
	private TiledMapRenderer mapaRenderer;

	//Variables de ancho y alto
	int anchoMapa, altoMapa, anchoCelda, altoCelda;

	//Variable para contabilizar el número de tesoros
	int totalTesoros;

	// Cámara que nos da la vista del juego
	private OrthographicCamera camara;

	//Variables para las dimensiones de la pantalla
	private float anchuraPantalla, alturaPantalla;
	private Vector2 posicionJugador;

	//Velocidad de desplazamiento del jugador para cada iteración del bucle de renderizado
	private float velocidadJugador;

	//Celdas inicial y final del recorrido del personaje principal
	private Vector2 celdaInicial = new Vector2(2, 4) ;

	@Override
	public void create () {



		batch = new SpriteBatch();


		//Cargamos el mapa de baldosas desde la carpeta de assets
		mapa = new TmxMapLoader().load("map/mapaProgreso3.tmx");
		mapaRenderer = new OrthogonalTiledMapRenderer(mapa);

//Determinamos el alto y ancho del mapa de baldosas. Para ello necesitamos extraer la capa
//base del mapa y, a partir de ella, determinamos el número de celdas a lo ancho y alto,
//así como el tamaño de la celda, que multiplicando por el número de celdas a lo alto y
//ancho, da como resultado el alto y ancho en pixeles del mapa.
		TiledMapTileLayer capa = (TiledMapTileLayer) mapa.getLayers().get(0);

//Determinamos el ancho y alto de cada celda
		anchoCelda = (int) capa.getTileWidth();
		altoCelda = (int) capa.getTileHeight();

//Determinamos el ancho y alto del mapa completo
		anchoMapa = capa.getWidth() * anchoCelda;
		altoMapa = capa.getHeight() * altoCelda;

//Cargamos las capas de los obstáculos y las de los pasos en el TiledMap.
		TiledMapTileLayer capaSuelo = (TiledMapTileLayer) mapa.getLayers().get(0);
		TiledMapTileLayer capaObstaculos = (TiledMapTileLayer) mapa.getLayers().get(1);
		TiledMapTileLayer capaPasos = (TiledMapTileLayer) mapa.getLayers().get(2);
		capaTesoros = (TiledMapTileLayer) mapa.getLayers().get(3);
		TiledMapTileLayer capaProfundidad = (TiledMapTileLayer) mapa.getLayers().get(4);

//El numero de tiles es igual en todas las capas. Lo tomamos de la capa Suelo
		anchoTiles = capaSuelo.getWidth();
		altoTiles = capaSuelo.getHeight();

//Creamos un array bidimensional de booleanos para obstáculos y tesoros
		obstaculo = new boolean[anchoTiles][altoTiles];
		tesoro = new boolean[anchoTiles][altoTiles];

//Rellenamos los valores recorriendo el mapa
		for (int x = 0; x < anchoTiles; x++) {
			for (int y = 0; y < altoTiles; y++) {
				//rellenamos el array bidimensional de los obstaculos
				obstaculo[x][y] = ((capaObstaculos.getCell(x, y) != null) //obstaculos de la capa Obstaculos
						&& (capaPasos.getCell(x, y) == null)); //que no sean pasos permitidos de la capa Pasos
				//rellenamos el array bidimensional de los tesoros
				tesoro[x][y] = (capaTesoros.getCell(x, y) != null);
				//contabilizamos cuántos tesoros se han incluido en el mapa
				if (tesoro[x][y]) totalTesoros++;
			}
		}

//Posiciones inicial y final del recorrido
		Vector2 celdaInicial = new Vector2(2, 4);
		celdaFinal = new Vector2(24, 1);

		//Inicializamos la cámara del juego
		anchuraPantalla = Gdx.graphics.getWidth();
		alturaPantalla = Gdx.graphics.getHeight();

//Creamos una cámara que mostrará una zona del mapa (igual en todas las plataformas)
		int anchoCamara = 400, altoCamara = 240;
		camara = new OrthographicCamera(anchoCamara, altoCamara);

		posicionJugador = new Vector2(posicionaMapa(celdaInicial));

//Actualizamos la posición de la cámara
		camara.update();





		img = new Texture("badlogic.jpg");
		stateTime = 0f;
		imagenPrincipal = new Texture(Gdx.files.internal("mortadelo/personaje2.png"));
		TextureRegion[][] tmp = TextureRegion.split(imagenPrincipal,  imagenPrincipal.getWidth() / FRAME_COLS, imagenPrincipal.getHeight() / FRAME_ROWS);
//Creamos el objeto SpriteBatch que nos permitirá crear animaciones dentro del método render()
		sb = new SpriteBatch();
		//Tile Inicial y Final
		celdaInicial = new Vector2(0, 0);
		celdaFinal = new Vector2(24, 1); //el tile final en el mapa de ejemplo

		//Creamos las distintas animaciones en bucle, teniendo en cuenta que el timepo entre frames será 150 milisegundos

		float frameJugador = 0.15f;

		jugadorAbajo = new Animation<>(frameJugador, tmp[0]); //Fila 0, dirección abajo
		jugadorAbajo.setPlayMode(Animation.PlayMode.LOOP);
		jugadorIzquierda = new Animation<>(frameJugador, tmp[1]); //Fila 1, dirección izquierda
		jugadorIzquierda.setPlayMode(Animation.PlayMode.LOOP);
		jugadorDerecha = new Animation<>(frameJugador, tmp[2]); //Fila 2, dirección derecha
		jugadorDerecha.setPlayMode(Animation.PlayMode.LOOP);
		jugadorArriba = new Animation<>(frameJugador, tmp[3]); //Fila 3, dirección arriba
		jugadorArriba.setPlayMode(Animation.PlayMode.LOOP);

//En principio se utiliza la animación en la dirección abajo
		jugador = jugadorAbajo;
		//Variable para contar los tesoros recogidos
		cuentaTesoros = 0;
		velocidadJugador = 0.75f;

//Dimensiones del jugador
		anchoJugador = tmp[0][0].getRegionWidth();
		altoJugador = tmp[0][0].getRegionHeight();


		//Ponemos a cero el atributo stateTimeNPC, que marca el tiempo de ejecución de los npc
		stateTimeNPC = 0f;

//Velocidad de los NPC
		velocidadNPC = 0.75f; //Vale cualquier múltiplo de 0.25f
//Creamos arrays de animaciones para los NPC
//Las animaciones activas
		npc = new Animation[numeroNPC];

//Las animaciones direccionales
		npcAbajo = new Animation[numeroNPC];
		npcIzquierda = new Animation[numeroNPC];
		npcDerecha = new Animation[numeroNPC];
		npcArriba = new Animation[numeroNPC];

//Posiciones actuales, origen y destino de los npc
		posicionNPC = new Vector2[numeroNPC];
		origen = new Vector2[numeroNPC];
		destino = new Vector2[numeroNPC];

//Creamos los arrays (filas) de imágenes para cada npc extrayéndolos
//de las imágenes png de los distintos sprites

//Array de imágenes para cada npc
		imgNPC = new Texture[numeroNPC];

//Imágenes de cada npc
		imgNPC[0] = new Texture(Gdx.files.internal("mortadelo/npc1.png"));
		imgNPC[1] = new Texture(Gdx.files.internal("mortadelo/npc2.png"));
		imgNPC[2] = new Texture(Gdx.files.internal("mortadelo/npc3.png"));
		imgNPC[3] = new Texture(Gdx.files.internal("mortadelo/npc4.png"));
		imgNPC[4] = new Texture(Gdx.files.internal("mortadelo/npc5.png"));

//Extraemos los frames de cada imagen en tmp[][]
		for (int i = 0; i < numeroNPC; i++) {
			//Sacamos los frames de img en un array de TextureRegion
			tmp = TextureRegion.split(imgNPC[i], imgNPC[i].getWidth() / FRAME_COLS, imgNPC[i].getHeight() / FRAME_ROWS);

			//Creamos las distintas animaciones, teniendo en cuenta el tiempo entre frames
			float frameNPC = 0.15f;

			npcAbajo[i] = new Animation<>(frameNPC, tmp[0]);
			npcAbajo[i].setPlayMode(Animation.PlayMode.LOOP);
			npcIzquierda[i] = new Animation<>(frameNPC, tmp[1]);
			npcIzquierda[i].setPlayMode(Animation.PlayMode.LOOP);
			npcDerecha[i] = new Animation<>(frameNPC, tmp[2]);
			npcDerecha[i].setPlayMode(Animation.PlayMode.LOOP);
			npcArriba[i] = new Animation<>(frameNPC, tmp[3]);
			npcArriba[i].setPlayMode(Animation.PlayMode.LOOP);

			//Las animaciones activas iniciales de todos los npc las seteamos en dirección abajo
			npc[i] = npcAbajo[i];
		}

//RECORRIDO DE LOS NPC. Indicamos las baldosas de inicio y fin de su recorrido y  usamos
//la funcion posicionaMapa para traducirlo a puntos del mapa.
		origen[0] = posicionaMapa(new Vector2(3, 2));
		destino[0] = posicionaMapa(new Vector2(3, 5));
		origen[1] = posicionaMapa(new Vector2(5, 8));
		destino[1] = posicionaMapa(new Vector2(5, 11));
		origen[2] = posicionaMapa(new Vector2(13, 32));
		destino[2] = posicionaMapa(new Vector2(13, 28));
		origen[3] = posicionaMapa(new Vector2(21, 16));
		destino[3] = posicionaMapa(new Vector2(21, 22));
		origen[4] = posicionaMapa(new Vector2(11, 13));
		destino[4] = posicionaMapa(new Vector2(17, 13));
//POSICION INICIAL DE LOS NPC
		for (int i = 0; i < numeroNPC; i++) {
			posicionNPC[i] = new Vector2();
			posicionNPC[i].set(origen[i]);
		}

		//Inicializamos la musica de fondo del juego
		musicaJuego = Gdx.audio.newMusic(Gdx.files.internal("mortadelo/musicaAmbiente.wav"));
		musicaJuego.setLooping(true);


		//Inicializamos los sonidos
		tesoroEncontrado = Gdx.audio.newSound(Gdx.files.internal("mortadelo/tesoro.mp3"));
		pillado = Gdx.audio.newSound(Gdx.files.internal("mortadelo/fracaso.mp3"));
		fracaso = Gdx.audio.newSound(Gdx.files.internal("mortadelo/fin.mp3"));
		exito = Gdx.audio.newSound(Gdx.files.internal("mortadelo/ganar.mp3"));


	}

	@Override
	public void render () {

		//ponemos a la escucha de eventos la propia clase del juego
		Gdx.input.setInputProcessor(this);
		//Centramos la camara en el jugador principal
		camara.position.set(posicionJugador, 0);

//Comprobamos que la cámara no se salga de los límites del mapa de baldosas con el método MathUtils.clamp
		camara.position.x = MathUtils.clamp(camara.position.x,
				camara.viewportWidth / 2f,
				anchoMapa - camara.viewportWidth / 2f);
		camara.position.y = MathUtils.clamp(camara.position.y,
				camara.viewportHeight / 2f,
				altoMapa - camara.viewportHeight / 2f);

//Actualizamos la cámara del juego
		camara.update();
//Vinculamos el objeto que dibuja el mapa con la cámara del juego
		mapaRenderer.setView(camara);
		ScreenUtils.clear(1, 0, 0, 1);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();

		//Para borrar la pantalla
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//Vinculamos el objeto que dibuja el mapa con la cámara del juego
		mapaRenderer.setView(camara);

//Dibujamos las capas del mapa
//Posteriormente quitaremos la capa de profundidad para intercalar a los personajes
		int[] capas = {0, 1, 2, 3};
		mapaRenderer.render(capas);

		//ANIMACION DEL JUGADOR

//En este método actualizaremos la posición del jugador principal

// Indicamos al SpriteBatch que se muestre en el sistema de coordenadas específicas de la cámara.
		sb.setProjectionMatrix(camara.combined);

//Inicializamos el objeto SpriteBatch
		sb.begin();

		detectaColisiones();


		//Pintamos la capa de profundidad del mapa de baldosas.
//cuadroActual contendrá el frame que se va a mostrar en cada momento.
		TextureRegion cuadroActual = jugador.getKeyFrame(stateTime);
		sb.draw(cuadroActual, posicionJugador.x, posicionJugador.y);

		actualizaPosicionJugador();


//Finalizamos el objeto SpriteBatch

		//Dibujamos las animaciones de los NPC
		for (int i = 0; i < numeroNPC; i++) {
			actualizaPosicionNPC(i);
			cuadroActual = (TextureRegion) npc[i].getKeyFrame(stateTimeNPC);
			sb.draw(cuadroActual, posicionNPC[i].x, posicionNPC[i].y);
		}
		stateTimeNPC += Gdx.graphics.getDeltaTime();

		capas = new int[1];
		capas[0] =4; //Número de la capa de profundidad2
		mapaRenderer.render(capas);

		sb.end();

		//Reproducimos la musica del juego
		if (!musicaJuego.isPlaying())
			musicaJuego.play();

	}
	//Método que detecta si se producen colisiones usando rectángulos
	private void detectaColisiones() {
		//Vamos a comprobar que el rectángulo de contacto del jugador
		//no se solape con el rectángulo de contacto del npc
		Rectangle rJugador = new Rectangle((float) (posicionJugador.x + 0.25 * anchoJugador), (float) (posicionJugador.y + 0.25 * altoJugador),
				(float) (0.5 * anchoJugador), (float) (0.5 * altoJugador));
		Rectangle rNPC;
		//Ahora recorremos el array de NPC, para cada uno generamos su rectángulo de contacto
		for (int i = 0; i < numeroNPC; i++) {
			rNPC = new Rectangle((float) (posicionNPC[i].x + 0.1 * anchoJugador), (float) (posicionNPC[i].y + 0.1 * altoJugador),
					(float) (0.8 * anchoJugador), (float) (0.8 * altoJugador));
			//Si hay colision
			if (rJugador.overlaps(rNPC)) {
				fracaso.play();
				//Código de fin de partida
				System.out.println("Fin de la partida");
				posicionJugador.set(posicionaMapa(celdaInicial));
				return; //Acabamos el bucle si hay una sola colisión
			}
		}//Si no hay colisión no se hace nada
		//Deteccion de colisiones con NPC

		//Ahora recorremos el array de NPC, para cada uno generamos su rectángulo de contacto
		for (int i = 0; i < numeroNPC; i++) {
			rNPC = new Rectangle((float) (posicionNPC[i].x + 0.1 * anchoJugador), (float) (posicionNPC[i].y + 0.1 * altoJugador),
					(float) (0.8 * anchoJugador), (float) (0.8 * altoJugador));
			//Si hay colision
			if (rJugador.overlaps(rNPC)) {
				//pausamos la musica, guardando el instante actual de reproducción
				float posicionMusica = musicaJuego.getPosition();
				musicaJuego.pause();
				//reproducimos el sonido "pillado"
				pillado.play();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				musicaJuego.setPosition(posicionMusica);
				//reanudamos la musica del juego
				musicaJuego.play();
			}
		}
	}


	//Método que permite actualizar la posición de los NPC para cada iteración
//Los npc harán un recorrido de izquierda a derecha y volver, o de arriba a abajo y volver.
	private void actualizaPosicionNPC(int i) {

		if (posicionNPC[i].y < destino[i].y) {
			posicionNPC[i].y += velocidadNPC;
			npc[i] = npcArriba[i];
		}
		if (posicionNPC[i].y > destino[i].y) {
			posicionNPC[i].y -= velocidadNPC;
			npc[i] = npcAbajo[i];
		}
		if (posicionNPC[i].x < destino[i].x) {
			posicionNPC[i].x += velocidadNPC;
			npc[i] = npcDerecha[i];
		}
		if (posicionNPC[i].x > destino[i].x) {
			posicionNPC[i].x -= velocidadNPC;
			npc[i] = npcIzquierda[i];
		}

		posicionNPC[i].x = MathUtils.clamp(posicionNPC[i].x, 0, anchoMapa - anchoJugador);
		posicionNPC[i].y = MathUtils.clamp(posicionNPC[i].y, 0, altoMapa - altoJugador);

		//Dar la vuelta al NPC cuando llega a un extremo
		if (posicionNPC[i].epsilonEquals(destino[i])) {
			destino[i].set(origen[i]);
			origen[i].set(posicionNPC[i]);
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		//TiledMap
		mapa.dispose();
		//Texture
		imagenPrincipal.dispose();
//SpriteBatch
		if (sb.isDrawing())
			sb.dispose();

		imgNPC[0].dispose();
		imgNPC[1].dispose();
		imgNPC[2].dispose();
		imgNPC[3].dispose();
		imgNPC[4].dispose();

		//Music
		musicaJuego.dispose();
		//Sound
		tesoroEncontrado.dispose();
		pillado.dispose();
		exito.dispose();
		fracaso.dispose();

	}

	private void actualizaPosicionJugador() {

		//Guardamos la posicion del jugador por si encontramos algun obstaculo
		Vector2 posicionAnterior = new Vector2();
		posicionAnterior.set(posicionJugador);

		//Los booleanos izquierda, derecha, arriba y abajo recogen la dirección del personaje,
		//para permitir direcciones oblícuas no deben ser excluyentes.
		//Pero sí debemos excluir la simultaneidad entre arriba/abajo e izquierda/derecha
		//para no tener direcciones contradictorias
		if (izquierda) {
			posicionJugador.x -= velocidadJugador;
			jugador = jugadorIzquierda;
		}
		if (derecha) {
			posicionJugador.x += velocidadJugador;
			jugador = jugadorDerecha;
		}
		if (arriba) {
			posicionJugador.y += velocidadJugador;
			jugador = jugadorArriba;
		}
		if (abajo) {
			posicionJugador.y -= velocidadJugador;
			jugador = jugadorAbajo;
		}
		if (izquierda || derecha || arriba || abajo) {
			stateTime += Gdx.graphics.getDeltaTime();
		}

		//Limites en el mapa para el jugador
		posicionJugador.x = MathUtils.clamp(posicionJugador.x, 0, anchoMapa - anchoJugador);
		posicionJugador.y = MathUtils.clamp(posicionJugador.y, 0, altoMapa - altoJugador);

		//Detección de obstaculos
		if (obstaculo(posicionJugador))
			posicionJugador.set(posicionAnterior);

		//Deteccion de fin del mapa
		if (celdaActual(posicionJugador).epsilonEquals(celdaFinal)) {
			//Paralizamos el juego 1 segundo para reproducir algún efecto sonoro
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//Deteccion de tesoros
			int limIzq = (int) ((posicionJugador.x + 0.25 * anchoJugador) / anchoCelda);
			int limDrcha = (int) ((posicionJugador.x + 0.75 * anchoJugador) / anchoCelda);
			int limSup = (int) ((posicionJugador.y + 0.25 * altoJugador) / altoCelda);
			int limInf = (int) ((posicionJugador.y) / altoCelda);

			if (tesoro[limIzq][limInf]) {
				TiledMapTileLayer.Cell celda = capaTesoros.getCell(limIzq, limInf);
				celda.setTile(null);
				tesoro[limIzq][limInf] = false;
				tesoroEncontrado.play();
				cuentaTesoros++;
			} else if (tesoro[limDrcha][limSup]) {
				TiledMapTileLayer.Cell celda = capaTesoros.getCell(limDrcha, limSup);
				celda.setTile(null);
				tesoro[limDrcha][limSup] = false;
				tesoroEncontrado.play();
				cuentaTesoros++;
			}

		}


		//Deteccion de tesoros: calculamos la celda en la que se encuentran los límites de la zona de contacto.
		int limIzq = (int) ((posicionJugador.x + 0.25 * anchoJugador) / anchoCelda);
		int limDrcha = (int) ((posicionJugador.x + 0.75 * anchoJugador) / anchoCelda);
		int limSup = (int) ((posicionJugador.y + 0.25 * altoJugador) / altoCelda);
		int limInf = (int) ((posicionJugador.y) / altoCelda);

		//Límite inferior izquierdo
		if (tesoro[limIzq][limInf]) {
			TiledMapTileLayer.Cell celda = capaTesoros.getCell(limIzq, limInf);
			celda.setTile(null);
			tesoro[limIzq][limInf] = false;
			cuentaTesoros++;
		} //Límite superior derecho
		else if (tesoro[limDrcha][limSup]) {
			TiledMapTileLayer.Cell celda = capaTesoros.getCell(limDrcha, limSup);
			celda.setTile(null);
			tesoro[limDrcha][limSup] = false;
			cuentaTesoros++;
		}
	}


	//Metodo que detecta si hay un obstaculo en una determinada posicion
	private boolean obstaculo(Vector2 posicion) {
		int limIzq = (int) ((posicion.x + 0.25 * anchoJugador) / anchoCelda);
		int limDrcha = (int) ((posicion.x + 0.75 * anchoJugador) / anchoCelda);
		int limSup = (int) ((posicion.y + 0.25 * altoJugador) / altoCelda);
		int limInf = (int) ((posicion.y) / altoCelda);

		return obstaculo[limIzq][limInf] || obstaculo[limDrcha][limSup];
	}

	private void setIzquierda(boolean izq) {
		if (derecha && izq) derecha = false;
		izquierda = izq;
	}

	private void setDerecha(boolean der) {
		if (izquierda && der) izquierda = false;
		derecha = der;
	}

	private void setArriba(boolean arr) {
		if (abajo && arr) abajo = false;
		arriba = arr;
	}

	private void setAbajo(boolean abj) {
		if (arriba && abj) arriba = false;
		abajo = abj;
	}
	//Método que convierte la posición del jugador en la celda en la que está
	private Vector2 celdaActual(Vector2 posicion) {
		return new Vector2((int) (posicion.x / anchoCelda), (altoTiles - 1 - (int) (posicion.y / altoCelda)));
	}

	private Vector2 posicionaMapa(Vector2 celda) {
		Vector2 res = new Vector2();
		if (celda.x + 1 > anchoTiles ||
				celda.y + 1 > altoTiles) {  //Si la peticion esta mal, situamos en el origen del mapa
			res.set(0, 0);
		}
		res.x = celda.x * anchoCelda;
		res.y = (altoTiles - 1 - celda.y) * altoCelda;
		return res;
	}


	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Input.Keys.LEFT:
				setIzquierda(true);
				break;
			case Input.Keys.RIGHT:
				setDerecha(true);
				break;
			case Input.Keys.UP:
				setArriba(true);
				break;
			case Input.Keys.DOWN:
				setAbajo(true);
				break;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {

		switch (keycode) {
			case Input.Keys.LEFT:
				setIzquierda(false);
				break;
			case Input.Keys.RIGHT:
				setDerecha(false);
				break;
			case Input.Keys.UP:
				setArriba(false);
				break;
			case Input.Keys.DOWN:
				setAbajo(false);
				break;
		}

		//Para ocultar/mostrar las distintas capas pulsamos desde el 1 en adelante...
		int codigoCapa = keycode - Input.Keys.NUM_1;
		if (codigoCapa <= 4)
			mapa.getLayers().get(codigoCapa).setVisible(!mapa.getLayers().get(codigoCapa).isVisible());

		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		Vector3 clickCoordinates = new Vector3(screenX, screenY, 0f);
		//Transformamos las coordenadas del vector a coordenadas de nuestra camara
		Vector3 pulsacion3d = camara.unproject(clickCoordinates);
		Vector2 pulsacion = new Vector2(pulsacion3d.x, pulsacion3d.y);

		//Calculamos la diferencia entre la pulsacion y el centro del jugador
		Vector2 centroJugador = new Vector2(posicionJugador).add((float) anchoJugador / 2, (float) altoJugador / 2);
		Vector2 diferencia = new Vector2(pulsacion.sub(centroJugador));

		//Vamos a determinar la intencion del usuario para mover al personaje en funcion del
		//angulo entre la pulsacion y la posicion del jugador
		float angulo = diferencia.angleDeg();

		if (angulo > 30 && angulo <= 150) setArriba(true);
		if (angulo > 120 && angulo <= 240) setIzquierda(true);
		if (angulo > 210 && angulo <= 330) setAbajo(true);
		if ((angulo > 0 && angulo <= 60) || (angulo > 300 && angulo < 360)) setDerecha(true);

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {

		setArriba(false);
		setAbajo(false);
		setIzquierda(false);
		setDerecha(false);

		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		//mismo caso que touchDown
		touchDown(screenX,screenY,pointer,0);
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}}
