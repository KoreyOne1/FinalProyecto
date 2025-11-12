import javax.swing.JFrame; // Importa la clase para crear la ventana
import javax.swing.SwingUtilities; // Importa utilidades para la interfaz gráfica (GUI)

/**
 * Clase principal que inicia el juego.
 * Su única responsabilidad es crear y configurar la ventana (JFrame)
 * y añadir el panel del juego (GamePanel) a ella.
 */
public class Main {
    /**
     Este es el método principal, el punto de entrada de toda aplicación Java.
     Es lo primero que se ejecuta.
     */
    public static void main(String[] args) {

        /* Bloque 1: El Hilo de la Interfaz Gráfica (EDT)
        //Propósito: Usamos SwingUtilities.invokeLater() para asegurarnos de que todo
        //el código relacionado con la interfaz gráfica (crear la ventana,
        //añadir el panel) se ejecute en el hilo correcto, llamado
        //"Event Dispatch Thread" (EDT).
        //Esto previene errores gráficos y problemas de concurrencia.
        Es la forma estándar y segura de iniciar una aplicación Swing.*/

        SwingUtilities.invokeLater(() -> {

            /* Bloque 2: Creación y Configuración de la Ventana ---
            Propósito: Instanciar y establecer las propiedades básicas de la
            ventana que contendrá nuestro juego.
            */

            JFrame frame = new JFrame("Mi Juego POO"); // Crea la ventana con un título

            // Le dice al programa que termine completamente cuando se presiona la 'X'
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Fija el tamaño. Es crucial para un juego, ya que si el usuario
            // redimensiona la ventana, rompería todas nuestras coordenadas.
            frame.setResizable(false);


            /* Bloque 3: Instanciación del Motor de Juego (Composición) ---
            Propósito: Crear una instancia de nuestro GamePanel. Aquí es donde
            aplicamos el principio de Composición: la ventana 'Main' NO ES un juego,
            sino que 'TIENE UN' GamePanel.
            */

            GamePanel gamePanel = new GamePanel();

            // Añadimos nuestro motor de juego (el panel) dentro de la ventana (el marco)
            frame.add(gamePanel);


            /* Bloque 4: Empaquetado y Visualización
            Propósito: Mostrar la ventana al usuario.
            frame.pack() es un comando importante. Le dice al 'frame' (ventana)
            que ajuste su tamaño automáticamente para que quepa perfectamente
            el contenido que le añadimos (el gamePanel y su 1024x768).*/

            frame.pack();

            // Pone la ventana en el centro de la pantalla
            frame.setLocationRelativeTo(null);

            // Finalmente, hace visible la ventana.
            frame.setVisible(true);


            /* --- Bloque 5: Inicio del Game Loop ---
            Propósito: Ceder el control al motor de juego.
            El trabajo de 'Main' (crear la ventana) ha terminado.
            Ahora le decimos al gamePanel que inicie su propio hilo (Thread),
            lo que pondrá en marcha el método run() y comenzará el Game Loop
            (actualizar() y repaint() 60 veces por segundo).
            */
            gamePanel.startGameThread();
        });
    }
}