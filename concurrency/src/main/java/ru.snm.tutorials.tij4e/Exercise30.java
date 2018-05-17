package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.*;

/**
 * @author sine-loco
 */
public class Exercise30 {
    private final static Logger logger = LogManager.getLogger();

    public static void main( String[] args ) throws InterruptedException {
        final ThreadNameSwitch nameSwitch = new ThreadNameSwitch();
        ExecutorService service = Executors.newCachedThreadPool( r ->
                new Thread( r, nameSwitch.name ) );
        BlockingQueue<String> queue = new ArrayBlockingQueue<>( 5 );

        for ( int i = 0; i < 3; i++) {
            nameSwitch.name = String.format( "sender %02d", i );
            service.execute( new Sender( queue ) );
        }
        for ( int i = 0; i < 2; i++) {
            nameSwitch.name = String.format( "receiver %02d", i );
            service.execute( new Receiver( queue ) );
        }
        TimeUnit.SECONDS.sleep( 15 );
        service.shutdownNow();

    }

    static class Sender implements Runnable {
        private Random rand = new Random( 47 );
        private final BlockingQueue<String> queue;

        public Sender( BlockingQueue<String> queue ) { this.queue = queue; }

        @Override public void run() {
            try {
                while ( !Thread.interrupted() ){
                    for ( char c = 'A'; c <= 'z'; c++ ) {
                        queue.put( c + " - " + Thread.currentThread().getName() );
                        TimeUnit.MILLISECONDS.sleep( rand.nextInt( 500 ) );
                    }
                }
            } catch ( InterruptedException e ) {
                 logger.info( "interrupted" );
            }
        }
    }

    static class Receiver implements Runnable {
        private final BlockingQueue<String> queue;

        public Receiver( BlockingQueue<String> queue ) { this.queue = queue; }


        @Override public void run() {
            try {
                while ( !Thread.interrupted() ) {
                    logger.info( "read: {}", queue.take() );
                }
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }
        }
    }
}
