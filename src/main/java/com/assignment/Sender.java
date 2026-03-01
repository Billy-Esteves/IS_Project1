package com.assignment;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.Socket;
import org.json.JSONObject;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import java.util.Timer;

public class Sender {

    static int[] package_sizes_kb = new int[] {10, 100, 1000};
    static int[] nesting_levels = new int[] {1, 5, 10};
    static int number_of_packages = 1000;
    static int number_of_iterations = 5;

    static int receiver_port = 8080;
    static String receiver_address = "localhost";

    static String[] packet_types = new String[] {"json", "msgpack"};

    public static void main(String[] args) throws IOException {

        // Data structure to hold time measurements
        JSONObject time_measurements = timeStorageJson();

        // Create receiver socket
        Socket receiverSocket = new Socket();

        try{
            // Connect to receiver
            receiverSocket.connect(new java.net.InetSocketAddress(receiver_address, receiver_port));
            System.out.println("Connected to receiver at " + receiver_address + ":" + receiver_port);

            long startTime, endTime, elapsedTime, elapsedTimeAvg;
            long[] iteration_times = new long[number_of_iterations];
            
            // Go through all combinations of package sizes and nesting levels
            for (int package_size_kb : package_sizes_kb) {
                for (int nesting_level : nesting_levels) {
                    for (String packet_type : packet_types) {
                        for (int iteration = 0; iteration < number_of_iterations; iteration++) {
                            System.out.println("Testing packet type " + packet_type + " with package size: " + package_size_kb + " KB and nesting level: " + nesting_level);
                            
                            // Start timer
                            startTime = System.nanoTime();

                            if (packet_type.equals("json")) {
                                // Serialize data
                                List<JSONObject> jsonPackages = createJsonPackages(package_size_kb, nesting_level);

                                // Send data


                            } else if (packet_type.equals("msgpack")) {
                                // Serialize data
                                List<byte[]> msgpackPackages = createMsgPackPackages(package_size_kb, nesting_level);

                                // Send data


                            }
                            
                            // Receive response from receiver
                            BufferedReader in = new BufferedReader(new InputStreamReader(receiverSocket.getInputStream()));
                            String response = in.readLine();
                            if (response == null || !response.equals("ACK")) {
                                System.out.println("Failed to receive ACK from receiver");
                            }

                            // Stop timer and calculate elapsed time
                            endTime = System.nanoTime();
                            elapsedTime = endTime - startTime;

                            iteration_times[iteration] = (long) elapsedTime;
                        }
                        
                        // Calculate average time for this package size and nesting level combination
                        elapsedTimeAvg = 0;
                        for (long time : iteration_times) {
                            elapsedTimeAvg += time;
                        }
                        elapsedTimeAvg /= number_of_iterations;

                        // Store time measurement
                        if (packet_type.equals("json")) {
                            time_measurements.getJSONObject("json").getJSONObject(String.valueOf(package_size_kb))
                            .put(String.valueOf(nesting_level), elapsedTimeAvg);
                        } else if (packet_type.equals("msgpack")) {
                            time_measurements.getJSONObject("msgpack").getJSONObject(String.valueOf(package_size_kb))
                            .put(String.valueOf(nesting_level), elapsedTimeAvg);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to connect to receiver: " + e.getMessage());
        }

        // Close connection
        receiverSocket.close();
    }

    private static JSONObject timeStorageJson() {
        // Main time measurement storage object
        JSONObject time_measurement_storage = new JSONObject();

        // JSON and MessagePack objects to hold measurements for each package size and nesting level combination
        JSONObject jsonObj = new JSONObject();
        JSONObject msgpackObj = new JSONObject();

        for (int size : package_sizes_kb) {

            // Nested JSON objects to hold measurements for each nesting level
            JSONObject jsonSizeObj = new JSONObject();
            JSONObject msgpackSizeObj = new JSONObject();

            for (int level : nesting_levels) {
                jsonSizeObj.put(String.valueOf(level), 0);
                msgpackSizeObj.put(String.valueOf(level), 0);
            }

            jsonObj.put(String.valueOf(size), jsonSizeObj);
            msgpackObj.put(String.valueOf(size), msgpackSizeObj);
        }
        
        // Attach to root
        time_measurement_storage.put("json", jsonObj);
        time_measurement_storage.put("msgpack", msgpackObj);

        return time_measurement_storage;
    }

    private static List<JSONObject> createJsonPackages(int packageSizeKB, int nestingLevel) {



        return new ArrayList<>();
    }

    private static List<byte[]> createMsgPackPackages(int packageSizeKB, int nestingLevel) {



        return new ArrayList<>();
    }
}