import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MemoramaCliente {
    public static void main(String[] args) {
        final String SERVIDOR_IP = "127.0.0.1"; // IP del servidor
        final int PUERTO = 1234; // Puerto en el que escucha el servidor

        try {
            Socket clienteSocket = new Socket(SERVIDOR_IP, PUERTO);
            System.out.println("Conectado al servidor.");

            ObjectInputStream entrada = new ObjectInputStream(clienteSocket.getInputStream());
            ObjectOutputStream salida = new ObjectOutputStream(clienteSocket.getOutputStream());

            List<String> cartas = (List<String>) entrada.readObject();
            boolean[] cartasVisibles = new boolean[cartas.size()];

            int jugadorActual = 1;
            boolean juegoTerminado = false;

            while (!juegoTerminado) {
                System.out.println("===========================================");
                System.out.println("Jugador " + jugadorActual + ", es tu turno.");
                System.out.println("Cartas visibles: ");
                for (int i = 0; i < cartas.size(); i++) {
                    if (cartasVisibles[i]) {
                        System.out.print(cartas.get(i) + " ");
                    } else {
                        System.out.print("X ");
                    }
                }
                System.out.println();

                Scanner scanner = new Scanner(System.in);
                System.out.print("Ingrese el número de la carta a seleccionar: ");
                int cartaSeleccionada = scanner.nextInt() - 1;

                salida.writeObject(cartaSeleccionada);
                salida.flush();

                int cartaVolteada = (int) entrada.readObject();
                cartasVisibles[cartaVolteada] = true;

                System.out.println("===========================================");
                System.out.println("Jugador " + jugadorActual + ", seleccionaste la carta " + cartas.get(cartaVolteada));

                for (int i = 0; i < cartas.size(); i++) {
                    if (cartasVisibles[i]) {
                        System.out.print(cartas.get(i) + " ");
                    } else {
                        System.out.print("X ");
                    }
                }
                System.out.println();

                String mensaje = (String) entrada.readObject();
                System.out.println(mensaje);

                if (mensaje.equals("¡Juego terminado!")) {
                    juegoTerminado = true;
                }

                jugadorActual = jugadorActual == 1 ? 2 : 1;
            }

            entrada.close();
            salida.close();
            clienteSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
