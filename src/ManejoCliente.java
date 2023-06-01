import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ManejoCliente implements Runnable {
    private Socket clienteSocket;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public ManejoCliente(Socket clienteSocket, ObjectOutputStream salida) {
        this.clienteSocket = clienteSocket;
        this.salida = salida;
        try {
            entrada = new ObjectInputStream(clienteSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object mensaje = entrada.readObject();
                if (mensaje instanceof Integer) {
                    int cartaSeleccionada = (int) mensaje;
                    salida.writeObject(cartaSeleccionada);
                    salida.flush();
                } else {
                    System.out.println("Cliente: " + mensaje);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                entrada.close();
                salida.close();
                clienteSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
