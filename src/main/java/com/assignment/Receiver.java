package com.assignment;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.msgpack.core.MessageFormat;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

public class Receiver {

    // Warning: port 8080 in Sender is a placeholder
    static int port = 8080;
    public static void main(String[] args) throws Exception {
        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Waiting for connection...");
            try(Socket socket = serverSocket.accept()){
                DataInputStream in = new DataInputStream(socket.getInputStream());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                System.out.println("Connected with: " + socket.getInetAddress());

                while(true) {
                    try {
                        String typeOfMessage = in.readUTF();
                        int numPackages = in.readInt();
                        byte[] data = new byte[numPackages];

                        for (int i = 0; i < numPackages; i++) {
                            int size = in.readInt();
                            data = new byte[size];
                            in.readFully(data);
                        }

                        for (int j=0;j<numPackages;j++) {

                            if(typeOfMessage.equals("json")){
                                String JsonMessage = new String(data, StandardCharsets.UTF_8);
                                JSONObject jsonObject = new JSONObject(JsonMessage);
                                System.out.println("JSON package received");
                            }
                            else if(typeOfMessage.equals("msgpack")){
                                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
                                try {
                                    unpackRecursive(unpacker);
                                } finally {
                                    unpacker.close();
                                }
                            }
                        }

                        out.println("ACK");
                        out.flush();
                    }
                    catch (EOFException e) {
                        System.out.println("Closing connection");
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void unpackRecursive(MessageUnpacker unpacker) throws IOException {
        MessageFormat format = unpacker.getNextFormat();

        if (format.getValueType().isMapType()) {

            int mapSize = unpacker.unpackMapHeader();
            for (int i = 0; i < mapSize; i++) {
                unpacker.unpackString();
                unpackRecursive(unpacker);
            }
        } else if (format.getValueType().isStringType()) {
            String leafData = unpacker.unpackString();
        }
    }
}