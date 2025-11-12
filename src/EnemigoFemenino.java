import java.awt.image.BufferedImage; // Importa la clase para manejar imágenes

/**
 * CLASE CONCRETA - EnemigoFemenino (Hereda de Enemigo)
 *
 * Propósito: Segunda implementación de la plantilla abstracta 'Enemigo'.
 * Demuestra el poder de la herencia y el polimorfismo. Al igual que
 * 'EnemigoMasculino', define estadísticas y una IA únicas.
 */
public class EnemigoFemenino extends Enemigo {

    // --- Bloque 1: Constructor (La "Fábrica") ---
    //
    // Propósito: Crear una nueva instancia de 'EnemigoFemenino'.
    //
    public EnemigoFemenino(int x, int y, GamePanel gamePanel,
                           BufferedImage[] runRight, BufferedImage[] runLeft,
                           BufferedImage[] attackRight, BufferedImage[] attackLeft) {

        // --- 1. Llamada a la Superclase ---
        // Pasa toda la información común (posición, panel, imágenes)
        // al constructor de la plantilla 'Enemigo' (el 'super').
        super(x, y, gamePanel, runRight, runLeft, attackRight, attackLeft);

        // --- 2. Especialización (Aquí está la diferencia) ---
        // Define las estadísticas que hacen a ESTE enemigo diferente.
        // Es una "glass cannon" (cañón de cristal): rápida pero débil.
        this.vidas = 1; // Más débil (el masculino tiene 2)
        this.velocidadMovimiento = 4; // Más rápida (el masculino tiene 3)

        // --- 3. Ajuste Fino del Hitbox ---
        // Define el "relleno" (padding) para el hitbox de ESTE enemigo.
        // (En este caso es igual al masculino, pero podría ser diferente).
        this.hitboxPaddingX = 25;
        this.hitboxPaddingY = 20;

        // Calcula el tamaño final del hitbox
        this.hitbox.width = gamePanel.tileSize - (hitboxPaddingX * 2);
        this.hitbox.height = gamePanel.tileSize - (hitboxPaddingY * 2);

        // 'numFramesCorrer' y 'numFramesAtacar' se establecen
        // automáticamente en el constructor de la superclase 'Enemigo'.
    }

    // --- ¡MÉTODO ELIMINADO A PROPÓSITO! ---
    // No hay 'cargarImagenes()'. Las imágenes se precargan en GamePanel
    // y se "inyectan" a través del constructor para evitar lag.


    // --- Bloque 2: El "Cerebro" (Implementación de IA) ---
    //
    // Propósito: Cumplir el "contrato" de 'Enemigo' implementando
    // el método abstracto 'ejecutarIA()'.
    //
    @Override
    protected void ejecutarIA() {
        // 1. Obtiene la distancia horizontal al jugador
        int distanciaX = gamePanel.jugador.getX() - this.x;

        // 2. Cláusula de Guarda: La IA solo toma decisiones si NO está
        //    atacando Y NO está en cooldown.
        if (!this.atacando && !this.enCooldown) {

            // 3. DECISIÓN: ¿Atacar o Perseguir?
            // Si la distancia es corta (menos de 50px)...
            if (Math.abs(distanciaX) < 50) {
                // ...mira al jugador...
                if (distanciaX < 0) this.direction = "left";
                else this.direction = "right";
                // ...y llama al método 'atacar()' (heredado de 'Enemigo').
                atacar();
            }
            // Si la distancia es larga...
            else {
                // ...entra en estado de "corriendo"...
                this.estado = "corriendo";
                // ...y persigue al jugador.
                if (distanciaX > 0) { // Jugador a la derecha
                    x += velocidadMovimiento;
                    this.direction = "right";
                } else { // Jugador a la izquierda
                    x -= velocidadMovimiento;
                    this.direction = "left";
                }
            }
        }
        // 4. Lógica de Cooldown: Si está en cooldown, no puede atacar,
        //    pero SÍ puede seguir persiguiendo al jugador.
        else if (!this.atacando && this.enCooldown) {
            this.estado = "corriendo";
            if (distanciaX > 0) {
                x += velocidadMovimiento;
                this.direction = "right";
            } else {
                x -= velocidadMovimiento;
                this.direction = "left";
            }
        }
    }
}