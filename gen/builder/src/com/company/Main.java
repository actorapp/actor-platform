package com.company;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String path = "/Users/ex3ndr/Documents/actor.json";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(new File(path), JsonNode.class);
        node.toString();
    }
}
