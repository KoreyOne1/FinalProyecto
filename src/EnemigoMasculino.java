import java.awt.image.BufferedImage; // Importa la clase para manejar imágenes

/**
 * CLASE CONCRETA - EnemigoMasculino (Hereda de Enemigo)
 *
 * Propósito: Esta es una implementación REAL de la plantilla abstracta 'Enemigo'.
 * Define las estadísticas y el comportamiento (IA) específicos
 * para el enemigo de tipo "Masculino".
 */
public class EnemigoMasculino extends Enemigo {

    // --- Bloque 1: Constructor (La "Fábrica") ---
    //
    // Propósito: Crear una nueva instancia de 'EnemigoMasculino'.
    // Es llamado por el 'GamePanel' (en el método spawnEnemigo).
    //
    public EnemigoMasculino(int x, int y, GamePanel gamePanel,
                            BufferedImage[] runRight, BufferedImage[] runLeft,
                            BufferedImage[] attackRight, BufferedImage[] attackLeft) {

        // --- 1. Llamada a la Superclase ---
        // Pasa toda la información "común" (posición, panel, imágenes)
        // al constructor de la plantilla 'Enemigo' (el 'super')
        // para que 'Enemigo' pueda manejarla.
        super(x, y, gamePanel, runRight, runLeft, attackRight, attackLeft);

        // --- 2. Especialización (Aquí es donde se vuelve único) ---
        // Define las estadísticas que hacen a ESTE enemigo diferente.
        // (Por ejemplo, el EnemigoFemenino tendrá 'vidas = 1' y 'velocidad = 4').
        this.vidas = 2; // Es más resistente
        this.velocidadMovimiento = 3; // Es más lento

        // --- 3. Ajuste Fino del Hitbox ---
        // Define el "relleno" (padding) para el hitbox de ESTE enemigo.
        // Estos números se basan en el arte visual de este sprite en particular.
        this.hitboxPaddingX = 25; // 25 píxeles de espacio a la izq/der
        this.hitboxPaddingY = 20; // 20 píxeles de espacio arriba/abajo

        // Calcula el tamaño final del hitbox basado en el padding
        this.hitbox.width = gamePanel.tileSize - (hitboxPaddingX * 2);
        this.hitbox.height = gamePanel.tileSize - (hitboxPaddingY * 2);

        // 'numFramesCorrer' y 'numFramesAtacar' se establecen
        // automáticamente en el constructor de la superclase 'Enemigo'.
    }

    // --- ¡MÉTODO ELIMINADO A PROPÓSITO! ---
    // No existe el método 'cargarImagenes()'.
    // ¿Por qué? Para optimizar. Las imágenes se cargan UNA VEZ en 'GamePanel'
    // y se "inyectan" a través del constructor. Esto previene el lag
    // cada vez que un nuevo enemigo aparece.


    // --- Bloque 2: El "Cerebro" (Implementación de IA) ---
    //
    // Propósito: Este es el cumplimiento del "contrato" de la clase 'Enemigo'.
    // La plantilla 'Enemigo' nos OBLIGÓ a escribir este método.
    // Esta es la lógica que define cómo piensa este enemigo.
    //
    @Override
    protected void ejecutarIA() {
        // 1. Obtiene la distancia horizontal al jugador
        int distanciaX = gamePanel.jugador.getX() - this.x;

        // 2. Cláusula de Guarda: La IA solo toma decisiones si NO está
        //    atacando Y NO está en cooldown (esperando para el próx. ataque).
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
        //    ¡pero SÍ puede seguir persiguiendo al jugador!
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