package server;

import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String clientId;
    private static final ConcurrentHashMap<String, ClientManager> activeClients = new ConcurrentHashMap<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
    }

    //base de cada hilo(cliente)
    @Override
    public void run() {
        try {
            setupStreams();
            if (!registerClient()) {
                output.println("El DNI ya está registrado. Desconectando...");
                cleanUp();
                return;
            }

            String message;
            while ((message = input.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            System.err.println("ClientManager Error: " + e.getMessage());
        } finally {
            cleanUp();
        }
    }

    //inicia la entrada y la salida
    private void setupStreams() throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    //registra un cliente en la "base de datos"
    private boolean registerClient() throws IOException {
        clientId = input.readLine();
        if (activeClients.containsKey(clientId)) {
            output.println("DNI_DUPLICADO");
            return false;
        }

        activeClients.put(clientId, this);
        System.out.println("Cliente registrado: " + clientId);
        return true;
    }

    //al recibir un mensaje mira lo que tiene que hacer con el. Si el usuario esta conectado

    private void processMessage(String jsonMsg) {
        JSONObject message = new JSONObject(jsonMsg);
        String recipientId = message.getString("destinatario");
        String text = message.getString("mensaje");

        // Guardar mensaje en el archivo
        guardarMensajeEnArchivo(clientId, recipientId, text);

        // Si el destinatario está conectado, enviar el mensaje y el chat completo
        ClientManager recipientManager = activeClients.get(recipientId);
        if (recipientManager != null) {
            // Leer el archivo de chat y enviarlo tanto al emisor como al receptor
            String chatCompleto = leerArchivoChat(clientId, recipientId);
            recipientManager.sendMessage(chatCompleto);
        } else {
            // El destinatario no está conectado
            sendMessage("El usuario está offline");
        }
    }




    //envia el mensaje
    public void sendMessage(String msg) {
        output.println(msg);
    }

    private void cleanUp() {
        activeClients.remove(clientId);
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (input != null) input.close();
            if (output != null) output.close();
        } catch (IOException e) {
            System.err.println("Error cleaning up client: " + e.getMessage());
        }
    }

    //guarda la conversacion en un chat
    private void guardarMensajeEnArchivo(String dniRemitente, String dniDestinatario, String mensaje) {
        BufferedWriter writer = null;
        try {
            String[] dnis = {dniRemitente, dniDestinatario};
            Arrays.sort(dnis);
            String fileName = "chat_" + dnis[0] + "_" + dnis[1] + ".txt";
            File file = new File(fileName);
            writer = new BufferedWriter(new FileWriter(file, true)); // Set true for append mode
            writer.write(dniRemitente + " dice: " + mensaje);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error al guardar mensaje en archivo: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el writer: " + e.getMessage());
                }
            }
        }
    }

    //lee el archivo de un chat
    private String leerArchivoChat(String dniRemitente, String dniDestinatario) {
        StringBuilder chat = new StringBuilder();
        BufferedReader reader = null;
        try {
            String[] dnis = {dniRemitente, dniDestinatario};
            Arrays.sort(dnis);
            String fileName = "chat_" + dnis[0] + "_" + dnis[1] + ".txt";
            File file = new File(fileName);
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    chat.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo del chat: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el reader: " + e.getMessage());
                }
            }
        }
        return chat.toString();
    }

    public static ConcurrentHashMap<String, ClientManager> getActiveClients() {
        return activeClients;
    }
}
