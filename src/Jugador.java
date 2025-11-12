import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.AlphaComposite; // Para el efecto de "parpadeo" (transparencia)
import java.awt.Rectangle;    // Para los hitboxes
import java.awt.Color;        // Para dibujar los hitboxes de depuración

/**
 * CLASE CONCRETA - Jugador (Hereda de GameObject)
 *
 * Propósito: Define al personaje principal controlado por el usuario.
 * Implementa toda la lógica de movimiento, física (salto/gravedad),
 * estados (ataque, invencibilidad) y sus animaciones específicas.
 */
public class Jugador extends GameObject {

    // --- Bloque 1: Propiedades Específicas del Jugador ---

    // Referencia al GamePanel. Esto es un ejemplo de 'Composición'.
    // El Jugador 'tiene una' referencia al panel para poder LEER
    // el estado de las teclas (ej. gamePanel.leftPressed).
    private GamePanel gamePanel;
    private int vidas;

    // --- Bloque 2: Variables de Estado ---
    //
    // Propósito: Banderas (boolean) y contadores (int) que rastrean
    // lo que el jugador está haciendo en un momento dado.
    //
    private boolean invencible = false;     // ¿Acaba de ser golpeado?
    private int contadorInvencible = 0; // Temporizador para la invencibilidad
    private boolean atacando = false;       // ¿Está en medio de un ataque?
    private int contadorAtaque = 0;   // Temporizador para la animación de ataque

    // --- Bloque 3: Animación ---
    //
    // Propósito: Arrays para almacenar las secuencias de imágenes
    // (sprites) que componen las animaciones.
    //
    public BufferedImage[] runRightAnimation;
    public BufferedImage[] runLeftAnimation;
    public BufferedImage[] attackRightAnimation;
    public BufferedImage[] attackLeftAnimation;
    private int numFramesCorrer = 12; // Constante para saber el tamaño del array
    private int numFramesAtacar = 10; // Constante para saber el tamaño del array

    // Variables de estado para la animación
    private String direction = "stand"; // "stand", "right", "left"
    private String lastDirection = "right"; // Para saber hacia dónde mirar al estar quieto
    private int spriteCounter = 0; // Temporizador para cambiar de frame
    private int spriteNum = 0;     // El frame actual a dibujar (ej. de 0 a 11)

    // --- Bloque 4: Física y Salto ---
    //
    // Propósito: Variables para simular la gravedad y el salto.
    //
    private int gravedad = 1;       // Fuerza que empuja hacia abajo cada frame
    private int fuerzaSalto = -20;    // Impulso hacia arriba (Y es negativo)
    private boolean enElSuelo = false;  // ¿Puede saltar?
    private int sueloY;             // La coordenada Y del "piso"

    // --- Bloque 5: Hitboxes ---
    //
    // 'hitbox' (el verde) se hereda de GameObject. Se usa para ser golpeado.
    // 'hitboxAtaque' (el amarillo) es solo para HACER daño.
    //
    public Rectangle hitboxAtaque;

    // Variables de "padding" (relleno) para ajustar el hitbox principal
    private int hitboxPaddingX;
    private int hitboxPaddingY;

    /**
     * Constructor del Jugador.
     * Se llama 1 vez (desde GamePanel) cuando se crea el jugador.
     */
    public Jugador(int x, int y, GamePanel gamePanel) {
        // --- 1. Inicialización del Padre ---
        // Llama al constructor de GameObject (el "super") para
        // inicializar 'x' e 'y'.
        super(x, y);

        // --- 2. Inicialización de Propiedades ---
        this.gamePanel = gamePanel; // Guarda la referencia al GamePanel
        this.vidas = 3;
        this.velocidadX = 4; // Píxeles que se mueve por frame
        this.velocidadY = 0; // Empieza quieto
        this.sueloY = y;     // Define el "piso" en su posición inicial
        this.enElSuelo = true;

        // --- 3. Ajuste Fino del Hitbox ---
        // Aquí ajustamos el hitbox heredado. El 'tileSize' (80) es el
        // tamaño del SPRITE, pero el hitbox físico será más pequeño
        // para que las colisiones se sientan más justas.
        this.hitboxPaddingX = 20; // 20 píxeles de espacio a la izq/der
        this.hitboxPaddingY = 16; // 16 píxeles de espacio arriba/abajo

        // Cálculo: 80 - (20 * 2) = 40 de ancho
        this.hitbox.width = gamePanel.tileSize - (hitboxPaddingX * 2);
        // Cálculo: 80 - (16 * 2) = 48 de alto
        this.hitbox.height = gamePanel.tileSize - (hitboxPaddingY * 2);

        // Inicializa el hitbox de ataque (vacío)
        this.hitboxAtaque = new Rectangle(0, 0, 0, 0);

        // --- 4. Preparación de Animaciones ---
        // Reserva espacio en memoria para los arrays de imágenes
        this.runRightAnimation = new BufferedImage[numFramesCorrer];
        this.runLeftAnimation = new BufferedImage[numFramesCorrer];
        this.attackRightAnimation = new BufferedImage[numFramesAtacar];
        this.attackLeftAnimation = new BufferedImage[numFramesAtacar];

        // Llama al método que cargará las imágenes del disco
        cargarImagenes();
    }

    /**
     * Carga todos los sprites del jugador desde la carpeta 'res'.
     * Se llama 1 vez desde el constructor.
     */
    public void cargarImagenes() {
        try {
            // Define las rutas a las carpetas de recursos
            String pathCorrer = "/";
            String pathAtacar = "/attackPlayer/";

            // Bucle 'for' para cargar los 12 frames de correr
            for (int i = 0; i < numFramesCorrer; i++) {
                runRightAnimation[i] = ImageIO.read(getClass().getResourceAsStream(pathCorrer + String.format("Right - Running_%03d.png", i)));
                runLeftAnimation[i] = ImageIO.read(getClass().getResourceAsStream(pathCorrer + String.format("Left - Running_%03d.png", i)));
            }
            // Bucle 'for' para cargar los 10 frames de atacar
            for (int i = 0; i < numFramesAtacar; i++) {
                attackRightAnimation[i] = ImageIO.read(getClass().getResourceAsStream(pathAtacar + String.format("Right - Attacking_%03d.png", i)));
                attackLeftAnimation[i] = ImageIO.read(getClass().getResourceAsStream(pathAtacar + String.format("Left - Attacking_%03d.png", i)));
            }
            // El 'catch' es un seguro por si faltan archivos, para que el juego no crashee.
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error al cargar imágenes del jugador.");
            e.printStackTrace();
        }
    }

    /**
     * MÉTODO ABSTRACTO IMPLEMENTADO (De GameObject)
     * Es el "cerebro" del jugador. Se llama 60 veces por segundo desde el GamePanel.
     */
    @Override
    public void actualizar() {

        // --- Bloque 1: Lógica de Movimiento y Física ---
        // (Pasos 1-4 del código)
        //
        // Solo permite moverse si NO está atacando
        if (!atacando) {
            if (gamePanel.leftPressed) {
                x -= velocidadX; direction = "left"; lastDirection = "left";
            } else if (gamePanel.rightPressed) {
                x += velocidadX; direction = "right"; lastDirection = "right";
            } else {
                direction = "stand";
            }
        }
        // Solo permite saltar si está en el suelo Y no está atacando
        if (gamePanel.upPressed && enElSuelo && !atacando) {
            this.velocidadY = fuerzaSalto; // Aplica el impulso de salto
            this.enElSuelo = false;
        }
        // Aplica la gravedad (siempre tira hacia abajo)
        this.velocidadY += gravedad;
        // Mueve al jugador verticalmente
        y += velocidadY;
        // Comprueba si ha chocado con el "piso"
        if (y > sueloY) {
            y = sueloY; // Lo coloca de vuelta en el piso
            this.velocidadY = 0; // Detiene la caída
            this.enElSuelo = true; // Le permite volver a saltar
        }

        // --- Bloque 2: Actualización de Hitbox ---
        // (Paso 6 del código)
        //
        // ¡CRÍTICO! Mueve el hitbox (el cuadro verde) para que coincida
        // con la nueva posición (x, y) del jugador.
        // Se añade el padding para mantener el hitbox centrado en el sprite.
        this.hitbox.x = x + hitboxPaddingX;
        this.hitbox.y = y + hitboxPaddingY;

        // --- Bloque 3: Lógica de Ataque ---
        // (Pasos 7 y 8 del código)
        //
        // 1. Revisa si el jugador QUIERE atacar
        if (gamePanel.spacePressed && !atacando) {
            atacar(); // Llama al método que inicia el estado de ataque
        }

        // 2. Lógica de "Marcos Activos" (si ya ESTÁ atacando)
        if (atacando) {
            contadorAtaque++; // Avanza el temporizador de ataque

            // La animación dura 30 ticks (10 frames * 3 ticks/frame)
            // Activamos el hitbox de ataque SÓLO entre los ticks 12 y 24
            if (contadorAtaque > 12 && contadorAtaque < 24) {

                // Define un hitbox de ataque (30 ancho x 20 alto)
                int attackWidth = 30;
                int attackHeight = 20;
                // Lo centra verticalmente
                int attackY = (y + hitboxPaddingY) + (hitbox.height - attackHeight) / 2;

                if (lastDirection.equals("left")) {
                    // Coloca el hitbox a la izquierda del jugador
                    int attackX = (x + hitboxPaddingX) - attackWidth;
                    hitboxAtaque.setBounds(attackX, attackY, attackWidth, attackHeight);
                } else {
                    // Coloca el hitbox a la derecha del jugador
                    int attackX = (x + hitboxPaddingX) + hitbox.width;
                    hitboxAtaque.setBounds(attackX, attackY, attackWidth, attackHeight);
                }

            } else {
                // Si estamos en los marcos de "inicio" o "recuperación",
                // el hitbox de ataque no tiene tamaño (no hace daño).
                hitboxAtaque.width = 0;
                hitboxAtaque.height = 0;
            }

            // 3. Finaliza el estado de ataque
            if (contadorAtaque > (numFramesAtacar * 3)) { // 30 ticks
                atacando = false;
                contadorAtaque = 0;
            }
        }

        // --- Bloque 4: Lógica de Invencibilidad ---
        // (Paso 9 del código)
        //
        // Si el jugador es invencible, avanza el temporizador.
        if (invencible) {
            contadorInvencible++;
            if (contadorInvencible > 60) { // 60 ticks = 1 segundo
                invencible = false; // Se acaba la invencibilidad
                contadorInvencible = 0;
            }
        }

        // --- Bloque 5: Lógica de Animación ---
        // (Paso 10 del código)
        //
        // Llama al método 'ayudante' que cambia el frame del sprite
        actualizarAnimacion();
    }

    /**
     * Inicia el estado de ataque. Es llamado por 'actualizar()'.
     */
    private void atacar() {
        atacando = true; // Activa el estado
        spriteNum = 0;   // Inicia la animación de ataque desde el frame 0
        contadorAtaque = 0; // Inicia el temporizador de ataque
        Sound.playSound("/sounds/ataque-antes-golpe_01.wav", 0.8f); // Reproduce el "whoosh"
    }

    /**
     * Método ayudante para manejar la lógica de qué frame mostrar.
     * Se llama 60 veces por segundo desde 'actualizar()'.
     */
    private void actualizarAnimacion() {
        spriteCounter++; // Avanza el temporizador de frames

        // Cambiamos de frame cada 3 ticks (60 / 3 = 20 FPS)
        if (spriteCounter > 2) {
            spriteNum++; // Pasa al siguiente frame

            // Si estamos atacando, usamos el array de ataque
            if (atacando) {
                if (spriteNum >= numFramesAtacar) {
                    spriteNum = numFramesAtacar - 1; // Se queda en el último frame
                }
                // Si no, usamos el array de correr
            } else {
                if (spriteNum >= numFramesCorrer) {
                    spriteNum = 0; // Vuelve al inicio del bucle de correr
                }
            }
            spriteCounter = 0; // Reinicia el temporizador de frames
        }
    }

    /**
     * Método público llamado por 'GamePanel' cuando hay una colisión.
     * Esto es Encapsulamiento: GamePanel no le *quita* vidas, le *pide* que pierda una.
     */
    public void perderVida() {
        // Solo pierde vida si NO es invencible
        if (!invencible) {
            this.vidas--;
            this.invencible = true; // Activa la invencibilidad (mercy frames)
            Sound.playSound("/sounds/ataque-golpe_01.wav", 1.0f); // Sonido de daño
            System.out.println("¡Jugador golpeado! Vidas restantes: " + this.vidas);
        }
    }

    /**
     * ¡NUEVO! Resetea al jugador.
     * Llamado por 'GamePanel' cuando el jugador reinicia el juego.
     */
    public void reiniciar() {
        this.x = 100; // Posición inicial
        this.y = 600; // Posición inicial
        this.vidas = 3;
        this.invencible = false;
        this.atacando = false;
        this.contadorAtaque = 0;
        this.contadorInvencible = 0;
        this.velocidadY = 0;
        this.enElSuelo = true;
        this.direction = "stand";
        this.lastDirection = "right";
    }

    // --- Bloque 6: Getters y Setters ---
    //
    // Encapsulamiento. Permiten a GamePanel LEER el estado del jugador.
    //
    public int getVidas() { return vidas; }
    public boolean isInvencible() { return invencible; }
    public boolean isAtacando() { return atacando; }

    /**
     * Método público llamado por 'GamePanel' cuando pisamos a un enemigo.
     */
    public void rebotar() {
        this.velocidadY = -10; // Causa un pequeño "rebote"
        this.enElSuelo = false;
        Sound.playSound("/sounds/aplastado_01.wav", 1.0f);
    }


    /**
     * MÉTODO ABSTRACTO IMPLEMENTADO (De GameObject)
     * Es el "artista" del jugador. Se llama ~60 veces por segundo.
     */
    @Override
    public void dibujar(Graphics2D g) {
        BufferedImage image = null;

        // --- 1. Selección de Sprite ---
        // Decide qué imagen exacta mostrar basado en el estado
        if (atacando) {
            image = (lastDirection.equals("left")) ? attackLeftAnimation[spriteNum] : attackRightAnimation[spriteNum];
        } else {
            switch (direction) {
                case "right": image = runRightAnimation[spriteNum]; break;
                case "left": image = runLeftAnimation[spriteNum]; break;
                case "stand":
                    // Si está quieto, usa el frame 0 de la última dirección
                    if (lastDirection.equals("left")) image = runLeftAnimation[0];
                    else image = runRightAnimation[0];
                    break;
            }
        }

        // --- 2. Efecto de Invencibilidad ---
        // Si es invencible, lo dibuja semitransparente (parpadeo)
        if (invencible) {
            if (contadorInvencible % 10 < 5) { // Alterna la opacidad
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            } else {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }

        // --- 3. Dibujado ---
        if (image != null) {
            // Dibuja el sprite seleccionado en la posición (x, y)
            // escalado al 'tileSize' (80x80)
            g.drawImage(image, x, y, gamePanel.tileSize, gamePanel.tileSize, null);
        } else {
            // Si las imágenes fallaron, dibuja un cuadrado blanco
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(x, y, gamePanel.tileSize, gamePanel.tileSize);
        }

        // --- 4. Reset de Opacidad ---
        // Resetea la opacidad a 100% para que el HUD no sea transparente
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // --- 5. Depuración (Debug) ---
        // Dibuja los hitboxes para afinarlos.
        // Se pueden comentar cuando el juego esté terminado.
        g.setColor(Color.GREEN);
        g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);

        if(atacando) {
            g.setColor(Color.YELLOW);
            g.drawRect(hitboxAtaque.x, hitboxAtaque.y, hitboxAtaque.width, hitboxAtaque.height);
        }
    }
}