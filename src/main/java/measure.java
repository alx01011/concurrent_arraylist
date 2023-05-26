import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
public class measure {
    ConcurrentArrayList<String> list = new ConcurrentArrayList<>();

    private final int THREADS = 4;
    private final String fname = "test_10_mb.txt";
    private final long MULTIPLIER = 1024 * 1024;
    private final long MB = 10; // size in megebytes
    private final long  FSIZE = MB * MULTIPLIER; // in bytes

    private void addMBFromFile(long start, long MB) throws IOException {
        // read MB megabytes from file starting from start
        // and add them to the list

        // read file from resources
        File input = new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().
                getResource(fname)).getFile());

        RandomAccessFile file = new RandomAccessFile(input, "r");

        // start is in megabytes, so we need to convert it to bytes

        file.seek(start);

        // read approximately MB megabytes from file
        // and add them to the list

        long bytesRead = 0;

        while (bytesRead < MB && file.getFilePointer() < file.length()) {
            String line = file.readLine();

            list.add(line);
            bytesRead += line.length();
        }

    }

    public void testAdd() throws IOException {
        Thread[] threads = new Thread[THREADS];
        long MB = FSIZE / THREADS; // bytes per thread

        System.out.println("Test: testAdd");

        sequentialAdd();
        list.clear();



        double start_time = System.currentTimeMillis();

        for (int i = 0; i < THREADS; i++) {
            long start = i * MB;
            threads[i] = new Thread(() -> {
                try {
                    addMBFromFile(start, MB);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        for (int i = 0; i < THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        double end = System.currentTimeMillis();

        System.out.println("Time: " + (end - start_time) + " ms");

    }

    private void sequentialAdd() throws IOException {
        // read file from resources
        File input = new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().
                getResource(fname)).getFile());

        FileReader fr = new FileReader(input);
        BufferedReader br = new BufferedReader(fr);

        String line;

        while ((line = br.readLine()) != null) {
            list.add(line);
        }

    }

    public void testContains() throws IOException {
        sequentialAdd(); // add all lines to the list sequentially

        ArrayList<String> words = new ArrayList<>();

        int wordsSize = 1000;

        for (int i = 0; i < wordsSize; i++) {
            int randomIndex = (int) (Math.random() * list.size());
            words.add(list.get(randomIndex));
        }

        long res = 0;

        for (int i = 0; i < wordsSize; i++) {
            if (list.contains(words.get(i))) res++;
        }

        Thread[] threads = new Thread[THREADS];
        ThreadInfo[] threadInfos = new ThreadInfo[THREADS];

        int wordsPerThread = wordsSize / THREADS;

        System.out.println("Test: testContains");
        double start_time = System.currentTimeMillis();

        for (int i = 0; i < THREADS; i++) {
            int start = i * wordsPerThread;
            int end = start + wordsPerThread;

            threadInfos[i] = new ThreadInfo(start, end);

            int finalI = i;
            threads[i] = new Thread(() -> {
                for (int j = start; j < end; j++) {
                    threadInfos[finalI].res += list.contains(words.get(j)) ? 1 : 0;
                }
            });
            threads[i].start();
        }

        for (int i = 0; i < THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        double end_time = System.currentTimeMillis();

        System.out.println("Time: " + (end_time - start_time) + " ms");

        long sum = 0;
        for (int i = 0; i < THREADS; i++) {
            sum += threadInfos[i].res;
        }

    }

    static class ThreadInfo {
        long start;
        long end;

        long res;

        ThreadInfo(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}

class Main {
    public static void main(String[] args) {
        measure m = new measure();

        try {
            m.testAdd();
            m.testContains();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}