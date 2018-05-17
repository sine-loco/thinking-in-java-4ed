package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author sine-loco
 */
public class Exercise18 {
    private final static Logger logger = LogManager.getLogger();

    public static void main( String[] args ) throws InterruptedException {
        ExecutorService service = Executors.newCachedThreadPool();
        Sleeper sleeper = new Sleeper();
        Future<?> task = service.submit( sleeper::callSleep );
        logger.info( "sleeper launched" );
        service.shutdown();
        task.cancel( true );

    }


    static class Sleeper {
        public void callSleep() {
            try {
                logger.info( "will fall asleep" );
                TimeUnit.MINUTES.sleep( 5 );
            } catch ( InterruptedException e ) {
                logger.info( "sleep interrupted" );
            }
            logger.info( "sleep ended" );
        }
    }
}
