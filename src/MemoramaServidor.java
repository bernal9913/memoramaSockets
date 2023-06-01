import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MemoramaServidor {
    private List<String> cartas;
    private boolean[] cartasVisibles;
    private List<Socket> clientes;
    private ObjectOutputStream salida1;
    private ObjectOutputStream salida2;

    public MemoramaServidor() {
        cartas = Arrays.asList("A", "A", "B", "B", "C", "C", "D", "D", "E", "E", "F", "F", "G", "G", "H", "H");
        cartasVisibles = new boolean[cartas.size()];
        clientes = new ArrayList<>();
    }

    public void iniciar() {
        try {
            ServerSocket servidorSocket = new ServerSocket(1234);
            System.out.println("Servidor iniciado. Esperando jugadores...");

            for (int i = 0; i < 2; i++) {
                Socket clienteSocket = servidorSocket.accept();
                System.out.println("Cliente " + (i + 1) + " conectado.");
                clientes.add(clienteSocket);

                ObjectOutputStream salida = new ObjectOutputStream(clienteSocket.getOutputStream());
                if (i == 0) {
                    salida1 = salida;
                } else {
                    salida2 = salida;
                }
            }

            asignarCartasAleatorias();

            Thread hiloJugador1 = new Thread(new ManejoCliente(clientes.get(0), salida2));
            Thread hiloJugador2 = new Thread(new ManejoCliente(clientes.get(1), salida1));
            hiloJugador1.start();
            hiloJugador2.start();

            jugar();

            servidorSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void asignarCartasAleatorias() {
        Collections.shuffle(cartas);
    }

    private void jugar() {
        int turno = 1;
        int jugadorActual = 1;
        int cartasVolteadas = 0;
        int[] cartasSeleccionadas = new int[2];
        boolean juegoTerminado = false;

        while (!juegoTerminado) {
            for (ObjectOutputStream salida : Arrays.asList(salida1, salida2)) {
                try {
                    salida.writeObject(turno);
                    salida.writeObject(cartasVisibles);
                    salida.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                ObjectInputStream entrada = new ObjectInputStream(clientes.get(jugadorActual - 1).getInputStream());
                cartasSeleccionadas[cartasVolteadas] = (int) entrada.readObject();

                cartasVolteadas++;
                voltearCarta(cartasSeleccionadas[cartasVolteadas - 1]);

                for (ObjectOutputStream salida : Arrays.asList(salida1, salida2)) {
                    salida.writeObject(cartasVisibles);
                    salida.flush();
                }

                if (cartasVolteadas == 2) {
                    if (sonCartasIguales(cartasSeleccionadas)) {
                        for (ObjectOutputStream salida : Arrays.asList(salida1, salida2)) {
                            salida.writeObject("¡Cartas iguales!");
                            salida.flush();
                        }

                        cartasVisibles[cartasSeleccionadas[0]] = true;
                        cartasVisibles[cartasSeleccionadas[1]] = true;
                    } else {
                        for (ObjectOutputStream salida : Arrays.asList(salida1, salida2)) {
                            salida.writeObject("¡Cartas diferentes!");
                            salida.flush();
                        }

                        voltearCarta(cartasSeleccionadas[0]);
                        voltearCarta(cartasSeleccionadas[1]);
                    }

                    cartasVolteadas = 0;
                    turno++;
                }

                if (todasCartasEncontradas()) {
                    for (ObjectOutputStream salida : Arrays.asList(salida1, salida2)) {
                        salida.writeObject("¡Juego terminado!");
                        salida.flush();
                    }
                    juegoTerminado = true;
                }

                jugadorActual = jugadorActual == 1 ? 2 : 1;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void voltearCarta(int indice) {
        cartasVisibles[indice] = !cartasVisibles[indice];
    }

    private boolean sonCartasIguales(int[] cartasSeleccionadas) {
        return cartas.get(cartasSeleccionadas[0]).equals(cartas.get(cartasSeleccionadas[1]));
    }

    private boolean todasCartasEncontradas() {
        for (boolean cartaVisible : cartasVisibles) {
            if (!cartaVisible) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        MemoramaServidor servidor = new MemoramaServidor();
        servidor.iniciar();
    }
}
