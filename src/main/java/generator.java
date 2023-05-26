import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class generator {
    private final String filename;
    private final long BYTES;

    public generator(long MB) {
        final int MULTIPLIER = 1024 * 1024;

        this.filename = "test_" + MB + "_mb.txt";
        this.BYTES = MB * MULTIPLIER;
    }

    private void generate() throws IOException {
        final int wordSize = 10;
        final int wordsPerLine = 1;

      FileWriter file = new FileWriter(filename);


        for (long i = 0; i < BYTES;) {
            for (long j = 0; j < wordsPerLine; j++) {
                for (long k = 0; k < wordSize; k++) {
                    char c = (char) (Math.random() * 26 + 'a');
                    file.write(c);
                    i++;
                }
                if (wordsPerLine > 1) {
                    file.write(' ');
                    i++;
                }
            }
            file.write('\n');
            i++;
        }
        file.close();
    }

    public static void usage() {
        System.out.println("Usage: java generator <size in MB>");
        System.exit(1);
    }

    public static void main(String... args) {
        long MB = 0;
        if (args.length != 1) {
            usage();
        }

        try {
            MB = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            usage();
        }

        generator gen = new generator(MB);

        try {
            gen.generate();
        } catch (IOException e) {
            System.out.println("File generation failed. Do you have write access and enough space?");
            System.out.println("Error: " + e.getMessage());
        }

    }

}
