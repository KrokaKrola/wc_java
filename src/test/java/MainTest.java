import com.kroka.Main;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest {
    private final String filePath;

    private String getTestFileAbsolutePath() {
        final String testFilePath = "/com/example/test.txt";
        File file = new File(Objects.requireNonNull(getClass().getResource(testFilePath)).getFile());

        return file.getAbsolutePath();
    }

    private MainTest() {
        filePath = getTestFileAbsolutePath();
    }

    @Test
    public void testHelpCommand() {
        CommandLine cmd = new CommandLine(new Main());
        CommandLine.ParseResult parseResult = cmd.parseArgs("--help");

        assertTrue(parseResult.isUsageHelpRequested());

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));
        cmd.execute("--help");

        String output = sw.toString();

        assertTrue(output.contains("Usage: ccwc"));
    }

    @Test
    public void testBytesCountCommand() {
        CommandLine cmd = new CommandLine(new Main());

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute(filePath, "-c");
        String output = sw.toString();

        assertEquals(0, exitCode);
        assertEquals( "342190" + " " + filePath + "\r\n", output);
    }

    @Test
    public void testNewLinesCountCommand() {
        CommandLine cmd = new CommandLine(new Main());

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute(filePath, "-l");
        String output = sw.toString();

        assertEquals(0, exitCode);
        assertEquals( "7145" + " " + filePath + "\r\n", output);
    }

    @Test
    public void testCharsCountCommand() {
        CommandLine cmd = new CommandLine(new Main());

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute(filePath, "-m");
        String output = sw.toString();

        assertEquals(0, exitCode);
        assertEquals( "339292" + " " + filePath + "\r\n", output);
    }

    @Test
    public void testWordsCountCommand() {
        CommandLine cmd = new CommandLine(new Main());

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute(filePath, "-w");
        String output = sw.toString();

        assertEquals(0, exitCode);
        assertEquals( "58164" + " " + filePath + "\r\n", output);
    }

    @Test
    public void testDefaultOptions() {
        CommandLine cmd = new CommandLine(new Main());

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute(filePath);
        String output = sw.toString();

        assertEquals(0, exitCode);
        assertEquals( "7145 58164 342190" + " " + filePath + "\r\n", output);
    }

    @Test
    public void testAllOptionsFullNamesAtOnce() {
        CommandLine cmd = new CommandLine(new Main());

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        int exitCode = cmd.execute(filePath, "--bytes", "--words", "--lines", "--chars");
        String output = sw.toString();

        assertEquals(0, exitCode);
        assertEquals( "7145 58164 342190 339292" + " " + filePath + "\r\n", output);
    }

    @Test
    public void testIoException() {
        CommandLine cmd = new CommandLine(new Main());

        StringWriter sw = new StringWriter();
        cmd.setOut(new PrintWriter(sw));

        String notExistedFilePath = "not-existed-file.txt";

        int exitCode = cmd.execute(notExistedFilePath);
        String output = sw.toString();

        assertEquals(1, exitCode);
        assertEquals( "Failed to read file" + " " + notExistedFilePath + "\r\n", output);
    }

    @Test
    public void testPassingOfDataWithUnixPipe() {
        // Capture the original System.in and System.out
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        InputStream  originalIn = System.in;

        // Set up a ByteArrayOutputStream to capture the output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(baos);
        System.setOut(newOut);
        System.setErr(newOut);

        // Set up input data
        String inputData = "Some test data from System.in";
        ByteArrayInputStream bais = new ByteArrayInputStream(inputData.getBytes());
        System.setIn(bais);

        try {
            CommandLine cmd = new CommandLine(new Main());

            StringWriter sw = new StringWriter();
            cmd.setOut(new PrintWriter(sw));

            int exitCode = cmd.execute();
            String output = sw.toString();

            assertEquals(0, exitCode);
            assertEquals( "1 5 29" + "\r\n", output);
        } finally {
            // Restore the original System.in, System.out, and System.err
            System.setIn(originalIn);
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
}
