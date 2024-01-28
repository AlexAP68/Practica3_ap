package server;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConversationManager {
    private static final String BASE_PATH = "conversations/";

    public synchronized void storeMessage(String dni1, String dni2, String message) {
        String fileName = getFileName(dni1, dni2);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BASE_PATH + fileName, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error en guardar el missatge: " + e.getMessage());
        }
    }

    public String retrieveConversation(String dni1, String dni2) {
        String fileName = getFileName(dni1, dni2);
        try {
            return new String(Files.readAllBytes(Paths.get(BASE_PATH + fileName)));
        } catch (IOException e) {
            System.err.println("Error en recuperar la conversa: " + e.getMessage());
            return "";
        }
    }

    private String getFileName(String dni1, String dni2) {
        return dni1.compareTo(dni2) < 0 ? dni1 + "_" + dni2 + ".txt" : dni2 + "_" + dni1 + ".txt";
    }
}
