import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.Color; // Para dibujar el hitbox de depuración

/**
 * CLASE ABSTRACTA - Enemigo (Hereda de GameObject)
 *
 * Propósito: Define la "plantilla" base para TODOS los oponentes.
 * Contiene toda la lógica que un 'EnemigoMasculino' y 'EnemigoFemenino'
 * tienen en común (manejo de animación, estado de ataque, cooldown, etc.).
 */
public abstract class Enemigo extends GameObject {

    // --- Bloque 1: Propiedades Comunes del Enemigo ---
    //
    // Propósito: Variables que todo enemigo, sin importar su tipo,
    // necesitará para funcionar.
    //
    protected GamePanel gamePanel; // Referencia al panel (para IA)
    protected int vidas;
    protected int velocidadMovimiento;

    // --- Bloque 2: Variables de Estado y Animación ---
    //
    // Propósito: Rastrea lo que el enemigo está haciendo y qué
    // sprite debería mostrar.
    //
    protected String direction; // "left" o "right"
    protected String estado;    // "corriendo", "atacando", "quieto"
    protected boolean atacando = false;

    // Cooldown: Previene que el enemigo "spamee" ataques.
    protected boolean enCooldown = false;
    protected int contadorCooldown = 0;

    // Arrays para guardar las imágenes.
    // NO se cargan aquí; se reciben en el constructor.
    public BufferedImage[] runRightAnimation;
    public BufferedImage[] runLeftAnimation;
    public BufferedImage[] attackRightAnimation;
    public BufferedImage[] attackLeftAnimation;

    // Contadores para el ciclo de animación
    protected int numFramesCorrer;
    protected int numFramesAtacar;
    protected int spriteCounter = 0;
    protected int spriteNum = 0;

    // Hitboxes
    protected Rectangle hitboxAtaque; // El hitbox del "arma"
    protected int contadorAtaque = 0; // Temporizador para el ataque
    protected int hitboxPaddingX = 0; // Padding para el hitbox del cuerpo
    protected int hitboxPaddingY = 0;


    // --- Bloque 3: Constructor (Optimización de Precarga) ---
    //
    // Propósito: Inicializar un enemigo.
    // ¡CRÍTICO! Este constructor NO carga imágenes. En su lugar,
    // RECIBE los arrays de imágenes que GamePanel ya precargó.
    // Esto evita el "lag" (tirón) cada vez que un enemigo aparece.
    //
    public Enemigo(int x, int y, GamePanel gamePanel,
                   BufferedImage[] runRight, BufferedImage[] runLeft,
                   BufferedImage[] attackRight, BufferedImage[] attackLeft) {
        // Llama al constructor de GameObject (el "super")
        super(x, y);
        this.gamePanel = gamePanel;
        this.direction = "left";
        this.estado = "corriendo";
        this.hitboxAtaque = new Rectangle(0, 0, 0, 0);

        // Asigna las imágenes (punteros a la memoria, no copias)
        this.runRightAnimation = runRight;
        this.runLeftAnimation = runLeft;
        this.attackRightAnimation = attackRight;
        this.attackLeftAnimation = attackLeft;

        // Guarda el número de frames basado en los arrays recibidos
        this.numFramesCorrer = (runRight != null) ? runRight.length : 1;
        this.numFramesAtacar = (attackRight != null) ? attackRight.length : 1;
    }

    // --- Bloque 4: Método Abstracto (El "Contrato") ---
    //
    // Propósito: Esta es la clave del Polimorfismo.
    // Forzamos a que CUALQUIER clase que herede de 'Enemigo'
    // (como EnemigoMasculino) DEBA escribir su propia lógica de IA.
    //
    /**
     * Define la Inteligencia Artificial (IA) específica de este enemigo.
     * (Ej. "¿Debo patrullar?" o "¿Debo perseguir al jugador?").
     * Es 'abstract' porque cada enemigo se comportará diferente.
     */
    protected abstract void ejecutarIA();


    // --- Bloque 5: Lógica Común (Método "Plantilla") ---
    //
    // Propósito: Este método 'actualizar()' es un "Método Plantilla".
    // Define el esqueleto de lo que un enemigo hace cada frame,
    // y llama al método abstracto 'ejecutarIA()' como parte de esa rutina.
    //
    @Override
    public void actualizar() {
        // 1. Mueve el hitbox (común a todos)
        this.hitbox.x = x + hitboxPaddingX;
        this.hitbox.y = y + hitboxPaddingY;

        // 2. Ejecuta la IA (específica de cada hijo)
        ejecutarIA();

        // 3. Actualiza el sprite (común a todos)
        actualizarAnimacion();

        // 4. Maneja el temporizador de ataque (común a todos)
        if (atacando) {
            contadorAtaque++;
            // El ataque dura (núm. de frames * 3 ticks)
            if (contadorAtaque > (numFramesAtacar * 3)) {
                atacando = false;
                contadorAtaque = 0;
                estado = "corriendo";
                hitboxAtaque.width = 0; // Desactiva el hitbox

                // ¡Inicia el Cooldown!
                enCooldown = true;
                contadorCooldown = 0;
            }
        }

        // 5. Maneja el temporizador de Cooldown (común a todos)
        if (enCooldown) {
            contadorCooldown++;
            if (contadorCooldown > 60) { // 60 ticks = 1 segundo de espera
                enCooldown = false;
                contadorCooldown = 0;
            }
        }
    }

    // --- Bloque 6: Métodos Ayudantes Concretos ---
    //
    // Propósito: Lógica que es 100% idéntica para todos los enemigos
    // y no necesita ser reescrita por las clases hijas.
    //

    /**
     * Inicia el estado de ataque. Es llamado por la IA.
     */
    protected void atacar() {
        // Solo puede atacar si no está atacando ya
        if (!atacando) {
            this.atacando = true;
            this.estado = "atacando";
            this.spriteNum = 0; // Reinicia la animación de ataque
            this.contadorAtaque = 0;

            // Define el hitbox de ataque (30px de ancho)
            if (direction.equals("left")) {
                hitboxAtaque.setBounds(x - 30, y, 30, hitbox.height);
            } else {
                hitboxAtaque.setBounds(x + hitbox.width, y, 30, hitbox.height);
            }
        }
    }

    /**
     * Maneja el ciclo de los sprites.
     */
    protected void actualizarAnimacion() {
        spriteCounter++;
        if (spriteCounter > 2) { // Cambia de frame cada 3 ticks
            spriteNum++;
            // Si está atacando, se frena en el último frame de ataque
            if (estado.equals("atacando")) {
                if (spriteNum >= numFramesAtacar) spriteNum = numFramesAtacar - 1;
            } else { // Si está corriendo, reinicia el bucle
                if (spriteNum >= numFramesCorrer) spriteNum = 0;
            }
            spriteCounter = 0;
        }
    }

    /**
     * MÉTODO ABSTRACTO IMPLEMENTADO (De GameObject)
     * Dibuja el sprite correcto basado en el estado.
     */
    @Override
    public void dibujar(Graphics2D g) {
        BufferedImage image = null;

        // Bloque 'try-catch' de seguridad por si las imágenes
        // (precargadas en GamePanel) fallaron y son 'null'.
        try {
            switch (estado) {
                case "atacando":
                    image = (direction.equals("left")) ? attackLeftAnimation[spriteNum] : attackRightAnimation[spriteNum];
                    break;
                default:
                    image = (direction.equals("left")) ? runLeftAnimation[spriteNum] : runRightAnimation[spriteNum];
                    break;
            }
        } catch (Exception e) { image = null; }

        // Si la imagen existe, la dibuja.
        if (image != null) {
            g.drawImage(image, x, y, gamePanel.tileSize, gamePanel.tileSize, null);
        } else {
            // Si la imagen es 'null' (falló la carga), dibuja
            // un cuadro magenta para alertarnos del error sin crashear.
            g.setColor(java.awt.Color.MAGENTA);
            g.fillRect(x, y, gamePanel.tileSize, gamePanel.tileSize);
        }

        // --- Depuración (Debug) - ¡Visible! ---
        // Dibuja el hitbox del cuerpo (rojo) para que podamos ajustarlo.
        g.setColor(Color.RED);
        g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
    }

    // --- Bloque 7: Métodos Públicos (Encapsulamiento) ---
    //
    // Propósito: Permiten a GamePanel interactuar con el Enemigo
    // de una forma controlada.
    //

    /**
     * Llamado por GamePanel cuando el jugador golpea a este enemigo.
     */
    public void perderVida() {
        this.vidas--;
        System.out.println("Enemigo golpeado, vidas: " + vidas);
    }

    /**
     * Llamado por GamePanel para saber si este enemigo debe ser eliminado.
     */
    public int getVidas() {
        return vidas;
    }
}