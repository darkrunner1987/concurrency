package org.example;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class CalculatorConsumer implements Runnable {

    private final int id;
    private final NumberProducer producer;
    private final BlockingQueue<Integer> writeQueue;
    private final FileWriter sharedWriter;
    private final FileWriter privateWriter;

    public CalculatorConsumer(
            int id,
            NumberProducer producer,
            BlockingQueue<Integer> writeQueue,
            FileWriter sharedWriter,
            FileWriter privateWriter
    ) {
        this.id = id;
        this.producer = producer;
        this.writeQueue = writeQueue;
        this.sharedWriter = sharedWriter;
        this.privateWriter = privateWriter;
    }

    @Override
    public void run() {
        var n = enterQueueAndGetNumber();
        while (n != null) {
            checkAndUpdateQueue(n, isPrimeNumber(n.doubleValue()));
            n = enterQueueAndGetNumber();
        }
    }

    private Number enterQueueAndGetNumber() {
        synchronized (writeQueue) {
            try {
                writeQueue.put(id);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return producer.produce();
        }
    }

    private void checkAndUpdateQueue(Number n, boolean isPrime) {
        while (true) {
            if (id == writeQueue.element()) {
                if (isPrime) {
                    write(n);
                }
                leaveQueue();
                break;
            }
        }
    }

    private void leaveQueue() {
        try {
            writeQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(Number n) {
        try {
            System.out.printf("%s ", n);
            sharedWriter.append("%s ".formatted(n));
            privateWriter.append("%s ".formatted(n));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isPrimeNumber(double n) {
        if (n == 2 || n == 3) {
            return true;
        }

        for (var i = 2; i <= n / 2; i++) {
            if (n % i == 0) {
                return false;
            }
        }

        return true;
    }
}
