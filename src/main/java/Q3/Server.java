package Q3;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private static Map<String, Map<Integer, String>> fileParts = new HashMap<>();

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port: " + port);
            while (true) {
                clientSocket = serverSocket.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("put ")) {
                        String[] parts = inputLine.split(" ");
                        String fileName = parts[1];
                        int partIndex = Integer.parseInt(parts[2]);
                        String filePart = parts[3];
                        fileParts.computeIfAbsent(fileName, k -> new HashMap<>()).put(partIndex, filePart);
                        out.println("File part " + fileName + " stored at port " + port);
                    } else if (inputLine.startsWith("get ")) {
                        String fileName = inputLine.substring(4);
                        Map<Integer, String> filePartMap = fileParts.get(fileName);
                        if (filePartMap == null) {
                            out.println("No such file: " + fileName +"\n");
                        } else {
                            for (Map.Entry<Integer, String> entry : filePartMap.entrySet()) {
                                out.println("File part " + entry.getKey() + ": " + entry.getValue());
                            }
                        }
                    } else if (inputLine.startsWith("check ")) {
                        String[] parts = inputLine.split(" ");
                        String fileName = parts[1];
                        int partIndex = Integer.parseInt(parts[2]);
                        Map<Integer, String> filePartMap = fileParts.get(fileName);
                        if (filePartMap == null) {
                            out.println("No such file: " + fileName);
                        } else {
                            String filePart = filePartMap.get(partIndex);
                            if (filePart == null) {
                                out.println("No such file part: " + partIndex + " for file " + fileName);
                            } else {
                                out.println("File part " + partIndex + " for file " + fileName + " is at port " + port);
                            }
                        }
                    } else if (".".equals(inputLine)) {
                        break;
                    } else {
                        System.out.println("Received: " + inputLine);
                    }
                }
                this.stop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Check if port number is passed as an argument
        if (args.length != 1) {
            System.out.println("Usage: java Server <port number>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        Server server = new Server();
        server.start(port);
    }
}