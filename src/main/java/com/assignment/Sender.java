package com.assignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import java.util.Timer;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

            // Create input stream to receive responses from receiver
            BufferedReader in = new BufferedReader(new InputStreamReader(receiverSocket.getInputStream()));

            // Create output stream to send data to receiver
            DataOutputStream out = new DataOutputStream(receiverSocket.getOutputStream());

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
                            
                            // Send type of packet and number of packages to receiver
                            out.writeUTF(packet_type);
                            out.writeInt(number_of_packages);

                            if (packet_type.equals("json")) {
                                // Serialize data
                                List<JSONObject> jsonPackages = createJsonPackages(package_size_kb, nesting_level);

                                // Send data
                                for (JSONObject obj : jsonPackages) {
                                    byte[] data = obj.toString().getBytes(StandardCharsets.UTF_8);
                                    out.writeInt(data.length);
                                    out.write(data);
                                }

                            } else if (packet_type.equals("msgpack")) {
                                // Serialize data
                                List<byte[]> msgpackPackages = createMsgPackPackages(package_size_kb, nesting_level);

                                // Send data
                                for (byte[] data : msgpackPackages) {
                                    out.writeInt(data.length);
                                    out.write(data);
                                }
                            }

                            out.flush();
                            
                            // Receive response from receiver   
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

                        /*
                        Overall we are measuring:
                        JSON/MessagePack serialization cost
                        TCP buffering
                        OS scheduling
                        Receiver processing time
                        ACK round trip time
                        */

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


        // Write time measurements to excel file
        writeTimeMeasurementsToExcel(time_measurements);
    }

    private static void writeTimeMeasurementsToExcel(JSONObject timeMeasurements) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Time Measurements");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Packet Type");
            headerRow.createCell(1).setCellValue("Package Size (KB)");
            headerRow.createCell(2).setCellValue("Nesting Level");
            headerRow.createCell(3).setCellValue("Average Time (ns)");

            int rowNum = 1;
            for (String packetType : Arrays.asList("json", "msgpack")) {
                JSONObject packetTypeObj = timeMeasurements.getJSONObject(packetType);
                for (String packageSize : packetTypeObj.keySet()) {
                    JSONObject packageSizeObj = packetTypeObj.getJSONObject(packageSize);
                    for (String nestingLevel : packageSizeObj.keySet()) {
                        long avgTime = packageSizeObj.getLong(nestingLevel);
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(packetType);
                        row.createCell(1).setCellValue(Integer.parseInt(packageSize));
                        row.createCell(2).setCellValue(Integer.parseInt(nestingLevel));
                        row.createCell(3).setCellValue(avgTime);
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            FileOutputStream fileOut = new FileOutputStream("time_measurements.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

        } catch (IOException e) {
            System.out.println("Failed to write time measurements to Excel file: " + e.getMessage());
        }
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
        List<JSONObject> packages = new ArrayList<>();

        // For simplicity, create a single package per call
        JSONObject root = createNestedJson(nestingLevel, packageSizeKB * 1024);
        packages.add(root);

        return packages;
    }

    // Recursive helper to create nested JSON with 2^depth leaves
    private static JSONObject createNestedJson(int depth, int totalBytes) {
        JSONObject node = new JSONObject();
        if (depth <= 1) {
            // Leaf node: fill with dummy data to match totalBytes
            StringBuilder sb = new StringBuilder(totalBytes);
            for (int i = 0; i < totalBytes; i++) sb.append("x");
            node.put("data", sb.toString());
        } else {
            int bytesPerChild = totalBytes / 2; // split equally among 2 children
            node.put("0", createNestedJson(depth - 1, bytesPerChild));
            node.put("1", createNestedJson(depth - 1, bytesPerChild));
        }
        return node;
    }


    // Create multiple MsgPack packages
    private static List<byte[]> createMsgPackPackages(int packageSizeKB, int nestingLevel) throws IOException {
        List<byte[]> packages = new ArrayList<>();
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packNestedMsgPack(packer, nestingLevel, packageSizeKB * 1024);
        packer.close();
        packages.add(packer.toByteArray());
        return packages;
    }

    // Recursive helper for MsgPack
    private static void packNestedMsgPack(MessageBufferPacker packer, int depth, int totalBytes) throws IOException {
        if (depth <= 1) {
            // Leaf node: pack dummy data
            StringBuilder sb = new StringBuilder(totalBytes);
            for (int i = 0; i < totalBytes; i++) sb.append("x");
            packer.packString(sb.toString());
        } else {
            // Internal node: 2 children
            packer.packMapHeader(2);

            packer.packString("0");
            packNestedMsgPack(packer, depth - 1, totalBytes / 2);

            packer.packString("1");
            packNestedMsgPack(packer, depth - 1, totalBytes / 2);
        }
    }
}