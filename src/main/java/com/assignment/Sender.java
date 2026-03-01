package com.assignment;

import java.io.*;
import java.net.Socket;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

public class Sender {
    
    public static void main(String[] args) throws Exception {

        /*
        ---- How to create the data to send ----

        Json Example:
        ObjectMapper mapper = new ObjectMapper();

        // Nested object
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ipAddress", "192.168.1.10");
        metadata.put("port", 8080);

        // Main JSON object
        Map<String, Object> message = new HashMap<>();
        message.put("sender", "Billy");
        message.put("content", "Hello");
        message.put("metadata", metadata);

        String json = mapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(message);

        How it looks:
        {
            "sender" : "Billy",
            "content" : "Hello",
            "metadata" : {
                "ipAddress" : "192.168.1.10",
                "port" : 8080
            }
        }

        MsgPack Example:

        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();

        // Root object has 3 key-value pairs
        packer.packMapHeader(3);

        // "sender": "Billy"
        packer.packString("sender");
        packer.packString("Billy");

        // "content": "Hello"
        packer.packString("content");
        packer.packString("Hello");

        // "metadata": { ... }
        packer.packString("metadata");

        // Nested map with 2 fields
        packer.packMapHeader(2);

        packer.packString("ipAddress");
        packer.packString("192.168.1.10");

        packer.packString("port");
        packer.packInt(8080);

        packer.close();

        byte[] bytes = packer.toByteArray();
        
        */

    }
}