package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author sine-loco
 */
public class Exercise21 {
    private final static Logger logger = LogManager.getLogger();

    public static void main ( String[] args ) {
        OneThatWaits oneThatWaits = new OneThatWaits();
        OneThatNotifies oneThatNotifies = new OneThatNotifies( oneThatWaits );
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute( oneThatWaits );
        service.execute( oneThatNotifies );
        service.shutdown();
    }

    static class OneThatWaits implements Runnable {
        @Override public void run() {
            logger.info( "{} run started", getClass().getSimpleName() );
            try {
                synchronized ( this ) {
                    logger.info( "will wait" );
                    wait();
                }
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }
            logger.info( "wait ended" );
        }
    }

    static class OneThatNotifies implements Runnable {
        private final OneThatWaits oneThatWaits;

        OneThatNotifies( OneThatWaits oneThatWaits ) {
            this.oneThatWaits = oneThatWaits;
        }

        @Override public void run() {
            logger.info( "{} run started", getClass().getSimpleName() );
            try {
                TimeUnit.MILLISECONDS.sleep( 1000 );
                logger.info( "ready to notify" );
                synchronized ( oneThatWaits ) {
                    oneThatWaits.notifyAll();
                }
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }

        }
    }
}
