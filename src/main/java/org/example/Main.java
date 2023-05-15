package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Main {
    private static final int THREADS_NUMBER = 2;

    public static void main(String[] args) throws IOException {
        var pool = Executors.newFixedThreadPool(THREADS_NUMBER);
        var producer = new NumberProducer();
        var writeQueue = new ArrayBlockingQueue<Integer>(THREADS_NUMBER);
        var sharedWriter = new FileWriter("src/main/resources/result.txt");
        var privateWriters = new ArrayList<FileWriter>();
        for (var id = 1; id <= THREADS_NUMBER; id++) {
            var privateWriter = new FileWriter("src/main/resources/result%d.txt".formatted(id));
            privateWriters.add(privateWriter);
            pool.execute(new CalculatorConsumer(id, producer, writeQueue, sharedWriter, privateWriter));
        }

        shutdownAndAwaitTermination(pool);
        var i = 1;
        for (var privateWriter : privateWriters) {
            privateWriter.close();
            System.out.printf("\nWriter %d is closed", i++);
        }
        sharedWriter.close();
        System.out.println("\nShared writer is closed");
    }

    private static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            throw new RuntimeException(e);
        }
    }
}