package Q3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class Client {
    private static final Map<String, String> fileContentMap;
    static {
        fileContentMap = new HashMap<String, String>();
        fileContentMap.put("file1", "This is a simulator");
        fileContentMap.put("file2", "to pretend this is a file");
    }
    private static Map<String, List<Integer>> fileToServerMap = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine();

            if (command.startsWith("put ")) {
                String fileName = command.substring(4);
                long startTime = System.currentTimeMillis();
                put(fileName);
                long endTime = System.currentTimeMillis();
                System.out.println("Execution time for put: " + (endTime - startTime) + " ms");
            } else if (command.startsWith("get ")) {
                String fileName = command.substring(4);
                long startTime = System.currentTimeMillis();
                get(fileName);
                long endTime = System.currentTimeMillis();
                System.out.println("Execution time for get: " + (endTime - startTime) + " ms");
            } else if (command.startsWith("check ")) {
                String fileName = command.substring(6);
                long startTime = System.currentTimeMillis();
                check(fileName);
                long endTime = System.currentTimeMillis();
                System.out.println("Execution time for check: " + (endTime - startTime) + " ms");
            } else {
                System.out.println("Invalid command");
            }
        }
    }

    public static void put(String fileName) {
        String content = fileContentMap.get(fileName);
        if (content == null) {
            System.out.println("No such file: " + fileName);
            return;
        }

        System.out.println("Putting content " + content);
        int partSize = content.length() / 3;
        String part1 = content.substring(0, partSize);
        String part2 = content.substring(part1.length(), part1.length() + partSize);
        String part3 = content.substring(part1.length() + part2.length());

        List<Integer> ports = new ArrayList<>();
        ports.add(8000);
        ports.add(8001);
        ports.add(8002);

        Collections.shuffle(ports);

        fileToServerMap.put(fileName, ports);

        putToServer(fileName, part1, "localhost", ports.get(0), 0);
        putToServer(fileName, part2, "localhost", ports.get(1), 1);
        putToServer(fileName, part3, "localhost", ports.get(2), 2);
    }

    private static void putToServer(String fileName, String content, String host, int port, int partIndex) {
        try {
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send data to the server
            out.println("put " + fileName + " " + partIndex + " " + content);

            // Wait for server response
            String serverResponse = in.readLine();
            System.out.println("Server response: " + serverResponse);

            // Close connections after receiving server response
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void get(String fileName) {
        System.out.println("Getting content for " + fileName);
        List<Integer> ports = fileToServerMap.get(fileName);
        if (ports == null) {
            System.out.println("No such file found");
            return;
        }

        String part1 = getFromServer("localhost", ports.get(0), fileName, 0);
        String part2 = getFromServer("localhost", ports.get(1), fileName, 1);
        String part3 = getFromServer("localhost", ports.get(2), fileName, 2);

        String result = part1 + part2 + part3;
        System.out.println("Retrieved content: " + result);
    }

    private static String getFromServer(String host, int port, String fileName, int partIndex) {
        try {
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("get " + fileName + " " + partIndex);
            String response = in.readLine();
            in.close();
            out.close();
            socket.close();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void cput(String fileName) {
        System.out.println("Putting file with continuation " + fileName);
        // TODO: Implement file upload with continuation
    }

    public static void cget(String fileName) {
        System.out.println("Getting file with continuation " + fileName);
        // TODO: Implement file download with continuation
    }

    public static void check(String fileName) {
        for (Integer port : fileToServerMap.get(fileName)) {
            String filePartLocation = checkFromServer("localhost", port, fileName);
            System.out.println("File part " + fileName + " is at " + filePartLocation);
        }
    }

    private static String checkFromServer(String host, int port, String fileName) {
        try {
            Socket socket = new Socket(host, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("check " + fileName);
            String response = in.readLine();
            in.close();
            out.close();
            socket.close();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}