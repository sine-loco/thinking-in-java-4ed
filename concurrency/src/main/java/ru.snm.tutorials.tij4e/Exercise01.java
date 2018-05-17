package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author sine-loco
 */
public class Exercise01 {
    private final static Logger logger = LogManager.getLogger();

    public static void main( String[] args ) {
        final int NUM_OF_THREADS = 10;
        Thread[] threads = new Thread[NUM_OF_THREADS];
        for ( int i = 0; i < NUM_OF_THREADS; i++ ) {
            threads[i] = new Thread( new Task( i ) );
        }
        for ( Thread thread : threads ) {
            thread.start();
        }

    }

    static class Task implements Runnable {
        private final static int PRINT_COUNTER = 3;

        private final int id;

        Task( int id ) {
            this.id = id;
            logger.info( "task {} has started", id );
        }

        @Override public void run() {
            for ( int i = 0; i < PRINT_COUNTER; i++ ) {
                logger.info( "{} at {}", this, i );
                Thread.yield();
            }
            logger.info( "{} has finished", this );

        }

        @Override public String toString() {
            return "task #" + id;
        }
    }
}
