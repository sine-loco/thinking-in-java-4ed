package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sine-loco
 */
public class Exercise12 {
    private static final Logger logger = LogManager.getLogger();

    public static void main( String[] args ) {
        ExecutorService exec = Executors.newCachedThreadPool();
        AtomicityTest test = new AtomicityTest();
        exec.execute( test );
        while ( true ) {
            int value = test.getValue();
            if ( value % 2 != 0 ) {
                logger.info( "{} is not even!", value );
                System.exit( 0 );
            }
        }
    }


    static class AtomicityTest implements Runnable {
        private int i = 0;

        public synchronized int getValue() { return i; }

        private synchronized void evenIncrement() {
            i++;
            i++;
        }

        @Override public void run() {
            while ( true ) {
                evenIncrement();
            }
        }
    }
}
