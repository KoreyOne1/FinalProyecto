import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

/**
 * CLASE DIRECTORA - GamePanel (Hereda de JPanel, Implementa Runnable y KeyListener)
 *
 * Propósito: Es el motor central del juego. Gestiona:
 * 1. El Game Loop (a través de 'Runnable').
 * 2. La Máquina de Estados (Menú, Jugando, Game Over).
 * 3. El renderizado (dibujo) de todos los objetos (a través de 'JPanel').
 * 4. La entrada de teclado (a través de 'KeyListener').
 * 5. La creación y gestión de todos los GameObjects (Jugador, Enemigos).
 * * (Versión con todas las correcciones de declaración y tipeo).
 */
public class GamePanel extends JPanel implements Runnable, KeyListener {

    // --- Bloque 1: Configuración de la Pantalla y el Juego ---
    final int originalTileSize = 16;
    final int scale = 7; // Los personajes están a escala 7 (16*7 = 112px)
    public final int tileSize = originalTileSize * scale;
    public final int screenWidth = 1024; // se forza a la pantalla a mantener ese tamaño sobre x
    public final int screenHeight = 768; // se forza a la pantalla a mantener ese tamaño sobre y

    // --- Bloque 2: El Game Loop ---
    Thread gameThread;
    int FPS = 60; // actualizacion de pantalla por segundo

    // --- Bloque 3: Entrada de Teclado ---
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean spacePressed;

    // --- Bloque 4: La Máquina de Estados ---
    public int gameState;
    public final int menuState = 0;
    public final int playState = 1;
    public final int gameOverState = 2;

    // --- Bloque 5: Objetos del Juego (El Núcleo de POO) ---
    Jugador jugador;
    List<Enemigo> enemigos = new ArrayList<>(); //Extendemos de Enemigos e instanciamos un arraylist para guardar todos los enemigos

    // --- Bloque 6: HUD y Generador ---
    Font hudFont, titleFont, menuFont;
    private int puntuacion = 0; // puntuacion inicial
    private Random rand = new Random(); // cantidaad de enemigos que se generan "aleatorio"
    private int maxEnemigosEnPantalla = 2; // maximo de enemigos en pantalla

    // --- Bloque 7: Caché de Recursos (Optimización) ---
    //
    // ¡CORRECCIÓN! Aquí se declaran TODOS los mapas para ambos tipos de enemigos.
    //
    public static Map<String, BufferedImage[]> enemyMaleRunRight = new HashMap<>();
    public static Map<String, BufferedImage[]> enemyMaleRunLeft = new HashMap<>();
    public static Map<String, BufferedImage[]> enemyMaleAttackRight = new HashMap<>();
    public static Map<String, BufferedImage[]> enemyMaleAttackLeft = new HashMap<>();
    public static Map<String, BufferedImage[]> enemyFemaleRunRight = new HashMap<>();
    public static Map<String, BufferedImage[]> enemyFemaleRunLeft = new HashMap<>();
    public static Map<String, BufferedImage[]> enemyFemaleAttackRight = new HashMap<>();
    public static Map<String, BufferedImage[]> enemyFemaleAttackLeft = new HashMap<>();

    // --- Bloque 8: Recursos (Fondo y Música) ---
    private BufferedImage fondo;
    Sound music = new Sound();


    /**
     * Constructor de GamePanel.
     * Es la "Pantalla de Carga" del juego.
     */
    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setDoubleBuffered(true);
        this.addKeyListener(this);
        this.setFocusable(true);

        // Fuentes
        hudFont = new Font("Arial", Font.BOLD, 24);
        titleFont = new Font("Arial", Font.BOLD, 92);
        menuFont = new Font("Arial", Font.PLAIN, 32);

        cargarFondo();
        preloadEnemyImages();
        jugador = new Jugador(100, 600, this); // posicion del jugador inicial

        iniciarMusica();

        // El juego empieza en el menú
        gameState = menuState;
    }

    /**
     * Carga la imagen de fondo desde 'res/fondo/'.
     */
    public void cargarFondo() {
        try {
            fondo = ImageIO.read(getClass().getResourceAsStream("/fondo/dead forest.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error al cargar la imagen de fondo (dead forest.png).");
            e.printStackTrace();
        }
    }

    /**
     * Carga y reproduce la música de fondo en bucle.
     */
    public void iniciarMusica() {
        music.setFile("/sounds/bandaFondo_01.wav");
        music.setVolume(1.0f); // Volumen al 100%
        music.loop();
    }

    /**
     * ¡Optimización!
     * Carga TODOS los sprites de TODOS los tipos de enemigos UNA SOLA VEZ
     * y los guarda en los 'Mapas Estáticos'.
     */
    private void preloadEnemyImages() {
        // --- ENEMIGO MASCULINO (Carpeta /EnemyMale/) ---
        int numFramesCorrerMale = 12;
        int numFramesAtacarMale = 10;
        BufferedImage[] runRightMale = new BufferedImage[numFramesCorrerMale];
        BufferedImage[] runLeftMale = new BufferedImage[numFramesCorrerMale];
        BufferedImage[] attackRightMale = new BufferedImage[numFramesAtacarMale];
        BufferedImage[] attackLeftMale = new BufferedImage[numFramesAtacarMale];

        for (int i = 0; i < numFramesCorrerMale; i++) {
            runRightMale[i] = GameObject.loadSprite("/EnemyMale/" + String.format("Right - Running_%03d.png", i));
            runLeftMale[i] = GameObject.loadSprite("/EnemyMale/" + String.format("Left - Running_%03d.png", i));
        }
        for (int i = 0; i < numFramesAtacarMale; i++) {
            attackRightMale[i] = GameObject.loadSprite("/EnemyMale/attackEnemyMale/" + String.format("Right - Attacking_%03d.png", i));
            attackLeftMale[i] = GameObject.loadSprite("/EnemyMale/attackEnemyMale/" + String.format("Left - Attacking_%03d.png", i));
        }
        enemyMaleRunRight.put("sprites", runRightMale);
        enemyMaleRunLeft.put("sprites", runLeftMale);
        enemyMaleAttackRight.put("sprites", attackRightMale);
        enemyMaleAttackLeft.put("sprites", attackLeftMale);

        // --- ENEMIGO FEMENINO (Carpeta /EnemyFemale/) ---
        int numFramesCorrerFemale = 12;
        int numFramesAtacarFemale = 10;
        BufferedImage[] runRightFemale = new BufferedImage[numFramesCorrerFemale];
        BufferedImage[] runLeftFemale = new BufferedImage[numFramesCorrerFemale];
        BufferedImage[] attackRightFemale = new BufferedImage[numFramesAtacarFemale];
        BufferedImage[] attackLeftFemale = new BufferedImage[numFramesAtacarFemale];

        for (int i = 0; i < numFramesCorrerFemale; i++) {
            runRightFemale[i] = GameObject.loadSprite("/EnemyFemale/" + String.format("Right - Running_%03d.png", i));
            runLeftFemale[i] = GameObject.loadSprite("/EnemyFemale/" + String.format("Left - Running_%03d.png", i));
        }
        for (int i = 0; i < numFramesAtacarFemale; i++) {
            attackRightFemale[i] = GameObject.loadSprite("/EnemyFemale/attackEnemyFemale/" + String.format("Right - Attacking_%03d.png", i));
            attackLeftFemale[i] = GameObject.loadSprite("/EnemyFemale/attackEnemyFemale/" + String.format("Left - Attacking_%03d.png", i));
        }
        enemyFemaleRunRight.put("sprites", runRightFemale);
        enemyFemaleRunLeft.put("sprites", runLeftFemale);
        enemyFemaleAttackRight.put("sprites", attackRightFemale);
        enemyFemaleAttackLeft.put("sprites", attackLeftFemale);

        System.out.println("Imágenes de enemigos precargadas.");
    }


    /**
     * Genera un enemigo aleatorio (Masc o Fem) fuera de la pantalla.
     */
    public void spawnEnemigo() {
        int tipoEnemigo = rand.nextInt(2);
        int lado = rand.nextInt(2);
        int spawnX;

        if (lado == 0) {
            spawnX = -tileSize;
        } else {
            spawnX = screenWidth + tileSize;
        }

        int spawnY = 600;

        if (tipoEnemigo == 0) {
            enemigos.add(new EnemigoMasculino(spawnX, spawnY, this,
                    enemyMaleRunRight.get("sprites"), enemyMaleRunLeft.get("sprites"),
                    enemyMaleAttackRight.get("sprites"), enemyMaleAttackLeft.get("sprites")));
        } else {
            enemigos.add(new EnemigoFemenino(spawnX, spawnY, this,
                    enemyFemaleRunRight.get("sprites"), enemyFemaleRunLeft.get("sprites"),
                    enemyFemaleAttackRight.get("sprites"), enemyFemaleAttackLeft.get("sprites")));
        }
    }

    /**
     * (ACCIÓN DE REINICIO)
     * Resetea el juego al estado de menú después de un Game Over.
     */
    public void reiniciarJuego() {
        jugador.reiniciar();
        enemigos.clear();
        puntuacion = 0;

        music.stop();
        iniciarMusica();
        gameState = menuState;
    }

    /**
     * (ACCIÓN DE INICIO)
     * Pone el juego en modo "playState" y genera los primeros enemigos.
     */
    public void iniciarJuego() {
        gameState = playState;
        spawnEnemigo();
        spawnEnemigo();
    }


    /**
     * Inicia el Game Loop. Es llamado 1 vez desde 'Main'.
     */
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * MÉTODO DE 'Runnable'
     * Este es el Game Loop.
     */
    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            try {
                actualizar();
                repaint();

                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1000000;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;

            } catch (Exception e) {
                System.err.println("Error en el Game Loop principal:");
                e.printStackTrace();
            }
        }
    }

    /**
     * El "Cerebro" del Juego. Se llama 60 veces por segundo desde 'run()'.
     * Aquí es donde se ejecuta la MÁQUINA DE ESTADOS.
     */
    public void actualizar() {

        if (gameState == playState) {
            jugador.actualizar();

            if (enemigos.size() < maxEnemigosEnPantalla && rand.nextInt(100) < 1) {
                spawnEnemigo();
            }

            for (int i = 0; i < enemigos.size(); i++) {
                Enemigo e = enemigos.get(i);
                e.actualizar();

                if (e.getVidas() <= 0) {
                    enemigos.remove(i);
                    puntuacion += 100;
                    i--;
                }
            }
            checkColisiones();

            if (jugador.getVidas() <= 0) {
                gameState = gameOverState;
                music.stop();
                Sound.playSound("/sounds/GameOver_01.wav", 1.0f);
            }
        }
    }

    /**
     * Comprueba todas las colisiones entre el jugador y los enemigos.
     */
    public void checkColisiones() {

        // 1. ¿El JUGADOR golpea a un ENEMIGO?
        // se hce una verificacion si el jugador golpea a el enemigo, si esto es
        // true se reproduce el sonido
        if (jugador.isAtacando()) {
            for (Enemigo e : enemigos) {
                if (jugador.hitboxAtaque.intersects(e.getHitbox())) {
                    e.perderVida();
                    Sound.playSound("/sounds/ataque-golpe_01.wav", 1.0f);
                }
            }
        }

        for (Enemigo e : enemigos) {
            // A. Colisión por "Pisotón" (cuerpo a cuerpo)
            if (jugador.getHitbox().intersects(e.getHitbox())) {
                if (jugador.getVelocidadY() > 0 &&
                        !jugador.isInvencible() &&
                        !jugador.isAtacando()) {

                    e.perderVida();
                    jugador.rebotar();
                }
            }

            // B. Colisión por "Ataque de Enemigo"
            if (e.atacando && e.hitboxAtaque.intersects(jugador.getHitbox())) {
                jugador.perderVida();
            }
        }
    }

    /**
     * MÉTODO DE 'JPanel'
     * El "Artista" del Juego. Dibuja la pantalla correcta según el gameState.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // --- 1. Dibuja el Fondo (Siempre) ---
        if (fondo != null) {
            g2.drawImage(fondo, 0, 0, screenWidth, screenHeight, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }

        // --- 2. Dibuja según el estado ---
        if (gameState == playState) {
            jugador.dibujar(g2);
            for (Enemigo e : enemigos) {
                e.dibujar(g2);
            }
            dibujarHud(g2); // Dibuja el HUD

        } else if (gameState == menuState) {
            dibujarMenu(g2);

        } else if (gameState == gameOverState) {
            dibujarGameOver(g2);
        }

        g2.dispose();
    }

    /**
     * Dibuja el HUD (Vidas y Puntuación).
     */
    public void dibujarHud(Graphics2D g2) {
        g2.setFont(hudFont);
        g2.setColor(Color.WHITE);
        g2.drawString("Vidas: " + jugador.getVidas(), 20, 30);
        g2.drawString("Puntuación: " + puntuacion, 20, 60);
    }

    /**
     * Dibuja la pantalla de Menú.
     */
    public void dibujarMenu(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.WHITE);
        g2.setFont(titleFont);
        g2.drawString("MI JUEGO POO", screenWidth / 2 - 400, screenHeight / 2 - 100);

        g2.setFont(menuFont);
        g2.drawString("Presiona ENTER para Empezar", screenWidth / 2 - 200, screenHeight / 2 + 50);
    }

    /**
     * Dibuja la pantalla de Game Over.
     */
    public void dibujarGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.RED);
        g2.setFont(titleFont);
        g2.drawString("GAME OVER", screenWidth / 2 - 300, screenHeight / 2 - 100);

        g2.setFont(menuFont);
        g2.setColor(Color.WHITE);
        g2.drawString("Puntuación Final: " + puntuacion, screenWidth / 2 - 150, screenHeight / 2 + 50);
        g2.drawString("Presiona ENTER para volver al Menú", screenWidth / 2 - 250, screenHeight / 2 + 100);
    }


    // --- Bloque 16: El Controlador de Teclado (Métodos 'KeyListener') ---

    /**
     * MÉTODO DE 'KeyListener'
     */
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * MÉTODO DE 'KeyListener'
     * Se llama 1 vez cuando una tecla es PRESIONADA.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // --- Router de Teclado ---
        if (gameState == playState) {
            if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) upPressed = true;
            // ¡CORRECCIÓN! VK_S ahora es 'downPressed'
            if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) downPressed = true;
            if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) leftPressed = true;
            if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) rightPressed = true;
            if (code == KeyEvent.VK_SPACE) spacePressed = true;

        } else if (gameState == menuState) {
            if (code == KeyEvent.VK_ENTER) {
                iniciarJuego();
            }

        } else if (gameState == gameOverState) {
            if (code == KeyEvent.VK_ENTER) {
                reiniciarJuego();
            }
        }
    }

    /**
     * MÉTODO DE 'KeyListener'
     * Se llama 1 vez cuando una tecla es SOLTADA.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) upPressed = false;
        // ¡CORRECCIÓN! VK_S ahora es 'downPressed'
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) downPressed = false;
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) rightPressed = false;
        if (code == KeyEvent.VK_SPACE) spacePressed = false;
    }
}