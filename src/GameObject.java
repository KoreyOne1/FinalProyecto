import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage; // Para el método de utilidad
import javax.imageio.ImageIO;     // Para el método de utilidad
import java.io.IOException;     // Para el método de utilidad

/**
 * CLASE ABSTRACTA - GameObject (La Plantilla Maestra)
 *
 * Propósito: Define las propiedades y comportamientos fundamentales
 * que CUALQUIER objeto en el juego (Jugador, Enemigo, Plataforma, etc.)
 * debe tener.
 */
public abstract class GameObject {

    /**
     * Bloque 1: Propiedades Comunes
     * Propósito: Definir las variables que todo objeto compartirá.
     * Usamos 'protected' para que esta clase y sus "hijos" (las clases que
     * heredan de ella, como Jugador) puedan acceder a ellas directamente.
    */
    protected int x, y; // Posición en la pantalla
    protected int velocidadX, velocidadY; // Velocidad de movimiento
    public Rectangle hitbox; // La "caja de colisión" para la física


    /**
     * Constructor de GameObject.
     * Es llamado por los constructores de las clases hijas (ej. super(x, y)).
     */
    public GameObject(int x, int y) {

        //Inicializa las propiedades básicas
        this.x = x;
        this.y = y;
        this.velocidadX = 0;
        this.velocidadY = 0;

        /*
          ¡IMPORTANTE!
          Inicializamos el hitbox con tamaño 0.
          Para asegurarnos de que 'hitbox' NUNCA sea 'null'.
          Esto previene errores 'NullPointerException' más adelante.
          Las clases hijas (Jugador, Enemigo) serán responsables de
          darle el tamaño correcto (ej. 44x44).
         */


        this.hitbox = new Rectangle(x, y, 0, 0);
    }

    /**
    * Bloque 2: Métodos Abstractos
    * Propósito: Forzar a todas las clases hijas a que implementensu propia lógica. 'abstract'
    * significa "No defino CÓMO se hace, pero OBLIGA a que la clase hija lo hagas".
    */

    /**
     * Define la lógica del objeto que se ejecutará 60 veces por segundo.
     * (Ej. El Jugador moverá su 'x', el Enemigo ejecutará su IA).
     */
    public abstract void actualizar();

    /**
     * Define cómo el objeto se dibuja a sí mismo en la pantalla.
     * (Ej. El Jugador dibujará su sprite de correr, el Enemigo el suyo).
     */
    public abstract void dibujar(Graphics2D g);


    /**
     * Bloque 3: Getters (Acceso Seguro) ---
     * Propósito: Permitir que otras clases (como GamePanel) LEAN
     * información de este objeto de forma segura, sin poder MODIFICARLA.
     * Esto es un pilar de la POO llamado Encapsulamiento.
    */
    public int getX() { return x; }
    public int getY() { return y; }
    public Rectangle getHitbox() { return hitbox; } // Esencial para GamePanel.checkColisiones()
    public int getVelocidadY() { return velocidadY; } // Esencial para la lógica de "pisotón"


    // --- Bloque 4: Método de Utilidad Estático ---
    //
    // Propósito: Crear una herramienta "ayudante" para todo el proyecto.
    // 'static' significa que este método pertenece a la CLASE GameObject,
    // no a una instancia (no necesitas "crear" un GameObject para usarlo).
    // Lo usamos en GamePanel para precargar las imágenes de los enemigos.
    //
    /**
     * Carga una imagen de forma segura desde la carpeta 'res'.
     * Incluye manejo de errores para que el juego no "crashee"
     * si no se encuentra una imagen.
     * @param path La ruta a la imagen (ej. "/fondo/miFondo.png")
     * @return El objeto BufferedImage, o 'null' si falló.
     */
    public static BufferedImage loadSprite(String path) {
        try {
            // Lee el archivo desde la ruta de recursos
            return ImageIO.read(GameObject.class.getResourceAsStream(path));
        } catch (IOException | IllegalArgumentException e) {
            // Si no encuentra el archivo (input == null) o falla la lectura,
            // imprime un error en la consola y devuelve null.
            System.err.println("Error al cargar sprite desde: " + path);
            e.printStackTrace();
            return null; // Evita que el juego se rompa
        }
    }
}