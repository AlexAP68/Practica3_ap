package client;

import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient2 {
    private final String serverAddress;
    private final int serverPort;
    private BufferedReader input;
    private PrintWriter output;
    private final Scanner scanner = new Scanner(System.in);

    public ChatClient2(String address, int port) {
        this.serverAddress = address;
        this.serverPort = port;
    }

    public static void main(String[] args) {
        ChatClient2 client = new ChatClient2("localhost", 2000);
        client.startChat();
    }

    //registra el DNI
    private void startChat() {
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            setupStreams(socket);
            System.out.println("Conectado al servidor. Introduce tu DNI:");
            String dni = scanner.nextLine();
            output.println(dni);

            System.out.println("Cliente conectado como: " + dni);
            new Thread(this::listenToServer).start();
            chatLoop(dni);
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //inicia la entrada y salida
    private void setupStreams(Socket socket) throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }


    //escucha al servidor por si hay respuesta
    private void listenToServer() {
        try {
            String serverMessage;
            while ((serverMessage = input.readLine()) != null) {
                if ("DNI_DUPLICADO".equals(serverMessage)) {
                    System.out.println("El DNI ya está registrado en el servidor.");
                    break; // Salir del bucle para cerrar el cliente
                }
                System.out.println(serverMessage);
            }
        } catch (IOException e) {
            System.err.println("Error listening to server: " + e.getMessage());
        } finally {
            System.exit(0);
        }
    }

    //el menu del chat
    private void chatLoop(String dni) {
        while (true) {
            System.out.println("Introduce el DNI del destinatario ('adeu' para terminar):");
            String destDni = scanner.nextLine();
            if ("adeu".equalsIgnoreCase(destDni)) {
                break;
            }
            System.out.println("Escribe tu mensaje:");
            String message = scanner.nextLine();

            JSONObject json = new JSONObject();
            json.put("dni", dni);
            json.put("destinatario", destDni);
            json.put("mensaje", message);
            output.println(json.toString());
        }
    }
}
