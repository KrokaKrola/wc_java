package com.kroka;

import org.jetbrains.annotations.Nullable;
import picocli.*;
import picocli.CommandLine.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;

@Command(
        name = "ccwc",
        mixinStandardHelpOptions = true,
        version = "ccwc 1.0",
        description = "Cc implementation of wc for printing newline, word, and byte counts"
)
public class Main implements Callable<Integer> {
    @Parameters(
            arity = "0..1",
            description = "The file whose number of bytes, characters, words and newlines to calculate."
    )
    @Nullable
    private String file;

    @Option(names = {"-c", "--bytes"}, description = "Print only the byte count.")
    private Boolean countBytes = false;

    @Option(names = {"-m", "--chars"}, description = "Print only the character counts, as per the current locale. Encoding errors are not counted.")
    private Boolean countChars = false;

    @Option(names = {"-w", "--words"}, description = "Print only the word counts.")
    private Boolean countWords = false;

    @Option(names = {"-l", "--lines"}, description = "Print only the newline character counts.")
    private Boolean countLines = false;

    public static void main(String... args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    private byte[] readSystemIn() throws IOException {
        InputStream inputStream = System.in;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead;

        // Read the input stream into the ByteArrayOutputStream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        // Convert the ByteArrayOutputStream to a byte array
        return byteArrayOutputStream.toByteArray();
    }

    private byte[] readAllBytes(@Nullable String filePath) throws IOException {
        if (filePath == null) {
            return readSystemIn();
        }

        Path path = Paths.get(filePath);

        return Files.readAllBytes(path);
    }

    private int getNumberOfLines(String text) {
        String[] lines = text.split("\n");

        return lines.length;
    }

    private int getNumberOfWords(String text) {
        String[] words = text.split("\\s+");

        return words.length;
    }

    private int getNumberOfChars(String text) {
        String[] chars = text.split("");

        return chars.length;
    }

    private void setupDefaultOptions() {
        if (!countBytes && !countChars && !countWords && !countLines) {
            countBytes = true;
            countWords = true;
            countLines = true;
        }
    }

    private ArrayList<String> getResponseList(byte[] data) {
        String text = new String(data);

        ArrayList<String> responseList = new ArrayList<>();

        if (countLines) {
            int numberOfLines = getNumberOfLines(text);
            responseList.add(String.valueOf(numberOfLines));
        }

        if (countWords) {
            int numberOfWords = getNumberOfWords(text);
            responseList.add(String.valueOf(numberOfWords));
        }

        if (countBytes) {
            responseList.add(String.valueOf(data.length));
        }

        if (countChars) {
            int numberOfChars = getNumberOfChars(text);
            responseList.add(String.valueOf(numberOfChars));
        }

        return responseList;
    }

    @Override
    public Integer call() {
        try {
            byte[] data = readAllBytes(file);

            if (data.length == 0) {
                return 0;
            }

            setupDefaultOptions();

            ArrayList<String> responseList = getResponseList(data);

            if (file != null && !file.isEmpty()) {
                responseList.add(file);
            }

            System.out.println(String.join(" ", responseList));

            return 0;
        } catch (IOException ioException) {
            System.out.println("Failed to read file " + file);
            return 1;
        }
    }
}