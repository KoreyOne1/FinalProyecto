// --- Bloque 1: Imports ---
//
// Propósito: Importar la biblioteca de sonido incorporada de Java.
// 'javax.sound.sampled' es la API que nos permite cargar
// y manipular archivos de audio.
//
import javax.sound.sampled.AudioInputStream; // Para leer el stream de datos del archivo
import javax.sound.sampled.AudioSystem;    // El "administrador" de audio
import javax.sound.sampled.Clip;             // El "reproductor" que sostiene el audio
import javax.sound.sampled.FloatControl;   // Para controlar el volumen (ganancia)
import java.net.URL; // Para encontrar la ruta de nuestros archivos de recursos

/**
 * CLASE DE UTILIDAD - Sound (Gestor de Audio)
 *
 * Propósito: Encapsula toda la lógica compleja de Java Sound (javax.sound.sampled)
 * en métodos simples y reutilizables. Esto es Abstracción.
 * GamePanel no necesita saber sobre "AudioInputStream", solo
 * necesita llamar a "music.loop()".
 */
public class Sound {

    // --- Bloque 2: Propiedad de la Clase ---
    //
    // Propósito: 'Clip' es el objeto de Java que representa un "clip" de audio
    // que se puede cargar en la memoria. Lo guardamos como una variable
    // de instancia para poder controlarlo (play, stop, loop)
    // múltiples veces.
    //
    Clip clip;

    /**
     * Carga un archivo de sonido desde la carpeta 'res/sounds/'
     * y lo abre en la variable 'clip'.
     * @param soundFileName La ruta al archivo (ej. "/sounds/bandaFondo_01.wav")
     */
    public void setFile(String soundFileName) {
        try {
            // 1. Encuentra el archivo en la carpeta 'res' (funciona en el JAR)
            URL url = getClass().getResource(soundFileName);
            // 2. Lee el archivo como un stream de audio
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
            // 3. Obtiene un "reproductor" (clip)
            clip = AudioSystem.getClip();
            // 4. "Abre" el stream en el reproductor, cargándolo en memoria
            clip.open(audioStream);

        } catch (Exception e) {
            // Este catch es CRÍTICO. Si el archivo no se encuentra
            // o (como descubrimos) está en un formato incorrecto (32-bit),
            // Java lanza una excepción.
            System.err.println("Error al cargar el archivo de sonido: " + soundFileName);
            e.printStackTrace();
        }
    }

    // --- Bloque 3: Controles de Reproducción ---
    //
    // Propósito: Métodos simples que controlan el 'clip' que cargamos.
    // Incluyen "cláusulas de guarda" (if (clip == null) return;)
    // para prevenir un 'NullPointerException' si 'setFile()' falló.
    //

    /**
     * Reproduce el sonido una vez desde el principio.
     */
    public void play() {
        if (clip == null) return; // Cláusula de guarda
        clip.setFramePosition(0); // Lo "rebobina" al inicio
        clip.start(); // Lo reproduce
    }

    /**
     * Reproduce el sonido en un bucle infinito.
     * Perfecto para la música de fondo (BGM).
     */
    public void loop() {
        if (clip == null) return; // Cláusula de guarda
        clip.loop(Clip.LOOP_CONTINUOUSLY); // Constante de Java para bucle infinito
    }

    /**
     * Detiene la reproducción del sonido.
     */
    public void stop() {
        if (clip == null) return; // Cláusula de guarda
        clip.stop();
    }

    /**
     * Ajusta el volumen del clip.
     * @param volume Nivel de volumen (0.0f = silencio, 1.0f = máximo)
     */
    public void setVolume(float volume) {
        if (clip == null) return; // Cláusula de guarda

        // --- Bloque 4: Abstracción de Volumen ---
        //
        // Propósito: Traducir un número simple (0.0 a 1.0) al
        // complejo sistema de Decibelios (dB) que usa Java.
        //

        // 1. Obtiene el "control de volumen" (Master Gain) del clip
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

        // 2. Calcula el rango de volumen (ej. de -80.0dB a +6.0dB)
        float range = gainControl.getMaximum() - gainControl.getMinimum();

        // 3. Convierte nuestro 'volume' (0.0-1.0) a ese rango de dB
        float gain = (range * volume) + gainControl.getMinimum();

        // 4. Establece el volumen
        gainControl.setValue(gain);
    }

    // --- Bloque 5: Método de Utilidad Estático (SFX) ---
    //
    // Propósito: Un método "dispara y olvida" para efectos de sonido (SFX).
    // 'static' significa que pertenece a la CLASE Sound, no a una INSTANCIA.
    // Jugador y GamePanel pueden llamar a 'Sound.playSound(...)'
    // sin necesidad de tener una variable 'Sound' propia.
    //
    /**
     * Un método "estático" rápido para reproducir un efecto de sonido (SFX).
     * Crea un objeto Sound temporal, carga el archivo, lo reproduce
     * una vez, y luego el 'Garbage Collector' de Java lo elimina.
     * @param soundFileName La ruta al archivo (ej. "/sounds/ataque-golpe_01.wav")
     * @param volume El volumen (0.0f a 1.0f)
     */
    public static void playSound(String soundFileName, float volume) {
        Sound sfx = new Sound(); // 1. Crea un reproductor temporal
        sfx.setFile(soundFileName); // 2. Carga el sonido
        sfx.setVolume(volume);      // 3. Ajusta el volumen
        sfx.play();                 // 4. Lo reproduce
        // (El objeto 'sfx' se destruye solo)
    }
}