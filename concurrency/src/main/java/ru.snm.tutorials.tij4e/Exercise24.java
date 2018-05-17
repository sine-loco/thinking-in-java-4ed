package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * (1) Solve a single-producer, single-consumer problem using <b>wait()</b> and
 * <b>notifyAll()</b>. The producer must not overflow the receiver's buffer, which can
 * happen if the producer is faster than the consumer. If the consumer is faster than
 * the producer, then it must not read the same data more than once. Do not assume
 * anything about the relative speeds of the producer or consumer.
 *
 * @author sine-loco
 */
public class Exercise24 {
    private final static Logger logger = LogManager.getLogger();
    private final static int N_OF_PR = 1;
    private final static int N_OF_CO = 3;

    public static void main( String[] args ) throws InterruptedException {
        ExchangeQueue<String> queue = new ExchangeQueue<>();
        final ThreadNameSwitch threadNameSwitch = new ThreadNameSwitch();
        ExecutorService service = Executors.newCachedThreadPool( r ->
                new Thread( r, threadNameSwitch.name ) );

        for ( int i = 0; i < N_OF_CO; i++ ) {
            String name = String.format( "consumer " + Producer.TWO_DIGIT_FORMAT, i );
            threadNameSwitch.name = name;
            service.submit( new Consumer( queue, name ) );
        }
        for ( int i = 0; i < N_OF_PR; i++ ) {
            String name = String.format( "producer " + Producer.TWO_DIGIT_FORMAT, i );
            threadNameSwitch.name = name;
            service.submit( new Producer( queue, name ) );
        }

        TimeUnit.MILLISECONDS.sleep( 100 );

        service.awaitTermination( 10, TimeUnit.MICROSECONDS );
        service.shutdownNow();

    }

    static class ExchangeQueue<T> {
        private final static int MAX_SIZE = 5;
        LinkedList<T> storage = new LinkedList<>();

        synchronized T get() throws InterruptedException {
            while ( storage.isEmpty() ) {
                logger.trace( "empty" );
                wait();
            }
            if ( storage.size() == MAX_SIZE ) {
                logger.trace( "full no more" );
            }
            T one = storage.pollFirst();
            logger.trace( "queue size: {}", storage.size() );

            notifyAll();
            return one;
        }

        synchronized void put( T one ) throws InterruptedException {
            while ( storage.size() == MAX_SIZE ) {
                logger.trace( "full" );
                wait();
            }
            if ( storage.isEmpty() ) {
                logger.trace( "empty no more" );

            }
            storage.push( one );
            logger.trace( "queue size: {}", storage.size() );

            notifyAll();
        }
    }

    static class Consumer implements Runnable {
        public static final int SLEEP_INTERVAL = 15;
        private final ExchangeQueue<String> queue;
        private final String id;

        public Consumer( ExchangeQueue<String> queue, String id ) {
            this.queue = queue;
            this.id = id;
        }

        @Override public void run() {
            Random rand = new Random( 7 );
            try {
                while ( !Thread.interrupted() ) {
                    logger.trace( "will try to get from queue, {}", id );
                    String item = queue.get();
                    logger.info( "got from queue: {} with {}", item, id );
                    TimeUnit.MILLISECONDS.sleep( rand.nextInt( SLEEP_INTERVAL ) );

                }
                logger.info( "{} interrupted", id );
            } catch ( InterruptedException e ) {
                logger.info( "{} interrupted while blocked", id );
            }
        }
    }

    static class Producer implements Runnable {
        public static final int SLEEP_INTERVAL = 15;
        private static final String TWO_DIGIT_FORMAT = "#%02d";
        private volatile static int counter = 0;
        private final static String FORMAT = "item " + TWO_DIGIT_FORMAT + " of %s";

        private final ExchangeQueue<String> queue;
        private final String id;


        public Producer( ExchangeQueue<String> queue, String id ) {
            this.queue = queue;
            this.id = id;
        }

        @Override public void run() {
            Random rand = new Random( 7 );
            try {
                while ( !Thread.interrupted() ) {
                    String item;
                    synchronized ( this.getClass() ) {
                        counter++;
                        item = String.format( FORMAT, counter, id );
                    }
                    logger.trace( "will try to put into queue, {}", id );
                    queue.put( item );
                    logger.info( "put into queue: {}", item );
                    TimeUnit.MILLISECONDS.sleep( rand.nextInt( SLEEP_INTERVAL ) );
                }
                logger.info( "{} interrupted", id );
            } catch ( InterruptedException e ) {
                logger.info( "{} interrupted while blocked", id );
            }
        }
    }
}
