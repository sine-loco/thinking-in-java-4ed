package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author sine-loco
 */
public class Exercise11 {
    private final static Logger logger = LogManager.getLogger();

    public static void main( String[] args ) throws InterruptedException {
        final Integer N_OF_THREADS = 10;

        Container container = new Container();
        ExecutorService service = Executors.newCachedThreadPool();
        for ( int i = 0; i < N_OF_THREADS; i++ ) {
            service.execute( new Reader( container ) );
        }

        try {
            TimeUnit.MILLISECONDS.sleep( 100 );
        } catch ( InterruptedException e ) {
            // not interested
        }
        service.shutdownNow();
        logger.info( "finished" );

    }

    static class Container {
        private String field1;
        private String field2;

        public synchronized void change() {
            field1 = "corrupted 1";
            Thread.yield();
            field2 = "corrupted 2";
            Thread.yield();
            field1 = "correct 1";
            Thread.yield();
            field2 = "correct 2";
        }

        public synchronized String getField1() {
            return field1;
        }

        public synchronized String getField2() {
            return field2;
        }

        @Override public String toString() {
            return "Container " + Thread.currentThread();
        }
    }

    static class Reader implements Runnable {
        final Container container;

        public Reader( Container container ) {
            this.container = container;
        }

        @Override public void run() {

                while ( !Thread.interrupted() ) {
                    container.change();
                    logger.info( "{}, {}", container.getField1(), container );
                    logger.info( "{}, {}", container.getField2(), container );
                }

            logger.info( "run finished" );
        }
    }
}
