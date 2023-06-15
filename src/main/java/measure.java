import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;

public class measure {
    //Vector<String> list = new Vector<>();
    ConcurrentArrayList<String> list = new ConcurrentArrayList<>();

    private final int THREADS;
    private final String fname;
    private final long MULTIPLIER = 1024 * 1024;
    private final long MB;// size in megebytes
    private  final long FSIZE; // in bytes

    public measure(long MB, int threads) {
        this.THREADS = threads;
        this.MB = MB;
        this.FSIZE = MB * MULTIPLIER;
        this.fname = "test_" + MB + "_mb.txt";
    }

    private void addMBFromFile(long start, long bytes) throws IOException {
        // read bytes from file starting from start
        // and add them to the list

        File input = new File(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().
                getResource(fname)).getFile());

        final FileChannel channel = new RandomAccessFile(input, "r").getChannel();

        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, start, bytes);

        StringBuilder line = new StringBuilder();
        int limit = buffer.limit();

        for (int i = 0; i < limit; i++) {
            char c = (char) buffer.get();
            if (c == '\n') {
                list.add(line.toString());
                line = new StringBuilder();
            } else {
                line.append(c);
            }
        }

        channel.close();
    }

    public double testAdd() throws IOException {
        Thread[] threads = new Thread[THREADS];
        long bytes = FSIZE / THREADS; // bytes per thread

        //System.out.println("Test: testAdd");

        double start_time = System.currentTimeMillis();

        for (int i = 0; i < THREADS; i++) {
            long start = i * bytes;
            threads[i] = new Thread(() -> {
                try {
                    addMBFromFile(start, bytes);
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

        //System.out.println("Time: " + (end - start_time) + " ms");

        return end - start_time;
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

    @SuppressWarnings("unused")
    public double testContains() throws IOException {
        list.clear();
        sequentialAdd(); // add all lines to the list sequentially

        ArrayList<String> words = new ArrayList<>();

        int wordsSize = 180;

        for (int i = 0; i < wordsSize; i++) {
            int randomIndex = (int) (Math.random() * list.size());
            words.add(list.get(randomIndex));
        }

        Thread[] threads = new Thread[THREADS];
        ThreadInfo[] threadInfos = new ThreadInfo[THREADS];

        int wordsPerThread = wordsSize / THREADS;

        //System.out.println("Test: testContains");
        double start_time = System.currentTimeMillis();

        for (int i = 0; i < THREADS; i++) {
            int start = i * wordsPerThread;
            int end = start + wordsPerThread;

            threadInfos[i] = new ThreadInfo(start, end);

            threads[i] = new Thread(() -> {
                for (int j = start; j < end; j++) {
                    list.contains(words.get(j));
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

        //System.out.println("Time: " + (end_time - start_time) + " ms");

        return end_time - start_time;
    }

    @SuppressWarnings("unused")
    public double testContainsAll() throws IOException {
        list.clear();
        sequentialAdd(); // add all lines to the list sequentially

        ArrayList<String> words = new ArrayList<>();

        int wordsSize = 10;

        for (int i = 0; i < wordsSize; i++) {
            int randomIndex = (int) (Math.random() * list.size());
            words.add(list.get(randomIndex));
        }

        Thread[] threads = new Thread[THREADS];
        ThreadInfo[] threadInfos = new ThreadInfo[THREADS];

        int wordsPerThread = wordsSize / THREADS;

        //System.out.println("Test: testContainsAll");

        double start_time = System.currentTimeMillis();

        for (int i = 0; i < THREADS; i++) {
            int start = i * wordsPerThread;
            int end = start + wordsPerThread;

            threadInfos[i] = new ThreadInfo(start, end);
            threads[i] = new Thread(() -> list.containsAll(words));
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

       // System.out.println("Time: " + (end_time - start_time) + " ms");

        return end_time - start_time;
    }

    public double testRemove() throws IOException {
        list.clear();
        sequentialAdd();

        ArrayList<String> words = new ArrayList<>();

        int wordsSize = 200;

        // randomly select words to remove
        for (int i = 0; i < wordsSize; i++) {
            int randomIndex = (int) (Math.random() * list.size());
            words.add(list.get(randomIndex));
        }

        Thread[] threads = new Thread[THREADS];
        ThreadInfo[] threadInfos = new ThreadInfo[THREADS];

        int wordsPerThread = wordsSize / THREADS;

        //System.out.println("Test: testRemove");

        double start_time = System.currentTimeMillis();

        for (int i = 0; i < THREADS; i++) {
            int start = i * wordsPerThread;
            int end = start + wordsPerThread;

            threadInfos[i] = new ThreadInfo(start, end);
            threads[i] = new Thread(() -> {
                for (int j = start; j < end; j++) {
                    list.remove(words.get(j));
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

        //System.out.println("Time: " + (end_time - start_time) + " ms");

        //System.out.println("Size: " + list.size());

        return end_time - start_time;
    }

    public double testRemoveAll() throws IOException {
        list.clear();
        sequentialAdd();

        ArrayList<ArrayList<String>> words = new ArrayList<>();

        int wordsSize = 200;

        // randomly select words to remove per thread
        for (int i = 0; i < THREADS; i++) {
            words.add(new ArrayList<>());
            for (int j = 0; j < wordsSize; j++) {
                int randomIndex = (int) (Math.random() * list.size());
                words.get(i).add(list.get(randomIndex));
            }
        }

        Thread[] threads = new Thread[THREADS];
        ThreadInfo[] threadInfos = new ThreadInfo[THREADS];

        int wordsPerThread = wordsSize / THREADS;

        //System.out.println("Test: testRemoveAll");

        double start_time = System.currentTimeMillis();

        for (int i = 0; i < THREADS; i++) {
            int start = i * wordsPerThread;
            int end = start + wordsPerThread;



            threadInfos[i] = new ThreadInfo(start, end);
            final int finalI = i;
            threads[i] = new Thread(() -> list.removeAll(words.get(finalI)));
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

//        System.out.println("Time: " + (end_time - start_time) + " ms");
//
//        System.out.println("Size: " + list.size());

        return end_time - start_time;
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
    private static final int ITERATIONS = 5;
    private static final int MAX_THREADS = 4;
    private static final int SIZE = 10; // in MB

    static class measureInfo {
        double add_time;
        double containsAll_time;
        double contains_time;
        double remove_time;
        double removeAll_time;
    }

    public static void main(String[] args) {
        measureInfo[] info = new measureInfo[MAX_THREADS];

        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter("results_" + SIZE + "MB.txt"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bw == null) {
            return;
        }

        try {
            bw.write("Size: " + SIZE + " MB\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= MAX_THREADS; i++) {
            info[i - 1] = new measureInfo();

            System.out.println("Threads: " + i);

            try {
                bw.write("Threads: " + i + "\n\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (int j = 0; j < ITERATIONS; j++) {
                measure m = new measure(SIZE, i);
                double add_time = 0;
                double containsAll_time = 0;
                double contains_time = 0;
                double remove_time = 0;
                double removeAll_time = 0;
                try {
                    add_time = m.testAdd();
                    containsAll_time = m.testContainsAll();
                    contains_time = m.testContains();
                    remove_time = m.testRemove();
                    removeAll_time = m.testRemoveAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Iteration: " + (j + 1));
                System.out.println("Add: " + add_time + " ms");
                System.out.println("ContainsAll: " + containsAll_time + " ms");
                System.out.println("Contains: " + contains_time + " ms");
                System.out.println("Remove: " + remove_time + " ms");
                System.out.println("RemoveAll: " + removeAll_time + " ms\n\n");

                // write to file

                try {
                    bw.write("Iteration: " + (j + 1) + "\n");
                    bw.write("Add: " + add_time + " ms\n");
                    bw.write("ContainsAll: " + containsAll_time + " ms\n");
                    bw.write("Contains: " + contains_time + " ms\n");
                    bw.write("Remove: " + remove_time + " ms\n");
                    bw.write("RemoveAll: " + removeAll_time + " ms\n\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }


                info[i - 1].add_time += add_time;
                info[i - 1].containsAll_time += containsAll_time;
                info[i - 1].contains_time += contains_time;
                info[i - 1].remove_time += remove_time;
                info[i - 1].removeAll_time += removeAll_time;
            }
        }


        try {
            bw.write("\n\n\nMean:\n\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < MAX_THREADS; i++) {
            try {
                bw.write("Threads: " + (i + 1) + "\n");
                bw.write("Add: " + info[i].add_time / ITERATIONS + " ms\n");
                bw.write("ContainsAll: " + info[i].containsAll_time / ITERATIONS + " ms\n");
                bw.write("Contains: " + info[i].contains_time / ITERATIONS + " ms\n");
                bw.write("Remove: " + info[i].remove_time / ITERATIONS + " ms\n");
                bw.write("RemoveAll: " + info[i].removeAll_time / ITERATIONS + " ms\n\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
