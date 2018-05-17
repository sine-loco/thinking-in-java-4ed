package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author sine-loco
 */
public class Exercise09 {
    private final static Logger logger = LogManager.getLogger();

    public static void main( String[] args ) {
        ExecutorService service = Executors.newCachedThreadPool( r -> {
            Thread thread = new Thread( r );
            thread.setPriority( Thread.MIN_PRIORITY);
            return thread;
        } );
        for ( int i = 0; i < 5; i++ ) {
            service.execute( new SimplePriorities() );

        }
        service.execute( new SimplePriorities() );
        service.shutdown();
    }

    static class SimplePriorities implements Runnable {
        private int countdown = 5;
        private volatile double d;
        private int priority;

        SimplePriorities( int priority ) {
            this.priority = priority;
        }
        SimplePriorities() {
            // no priority set explicitly
            priority = -1;
        }

        @Override public String toString() {
            return Thread.currentThread() + ": " + countdown;
        }

        @Override public void run() {
            // do not set priority if not specified
            if ( priority != -1 ) {
                Thread.currentThread().setPriority( priority );
            }
            while ( true ) {
                for ( int i = 1; i < 10000; i ++ ) {
                    d += ( Math.PI + Math.E ) / ( double ) i;
                    if ( i % 1000 == 0 ) {
                        Thread.yield();
                    }
                }
                logger.info( "{}", this );
                if ( --countdown == 0 ) {
                    return;
                }
            }

        }
    }
}
