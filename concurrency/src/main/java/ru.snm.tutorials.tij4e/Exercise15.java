package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author sine-loco
 */
public class Exercise15 {
    private final static Logger logger = LogManager.getLogger();
    private final static int COUNT = 5;

    static void printMe() {
        for ( int i = 0; i < COUNT; i++ ) {
            logger.info( "!" );
        }
    }

    static void runWithDelay( ExecutorService service, SyncRunner syncRunner )
            throws InterruptedException
    {
        logger.info( "\n{}", syncRunner.getClass().getSimpleName() );
        for ( int i = 0; i < 5; i++ ) {
            service.execute( syncRunner::runFirst );
            service.execute( syncRunner::runSecond );
            service.execute( syncRunner::runThird );
        }
        TimeUnit.MILLISECONDS.sleep( 1 );
    }

    public static void main( String[] args ) throws InterruptedException {
        ExecutorService service = Executors.newCachedThreadPool();
        runWithDelay( service, new ThreeCriticalsOneSync() );
        runWithDelay( service, new ThreeCriticalsThreeSync() );
        service.shutdown();
    }

    static class ThreeCriticalsThreeSync implements SyncRunner {
        private final Object lockOne = new Object();
        private final Object lockTwo = new Object();
        private final Object lockThree = new Object();

        @Override public void runFirst() { synchronized ( lockOne ) { printMe(); } }

        @Override public void runSecond() { synchronized ( lockTwo ) { printMe(); } }

        @Override public void runThird() { synchronized ( lockThree ) { printMe(); } }
    }

    static class ThreeCriticalsOneSync implements SyncRunner {
        @Override public synchronized void runFirst() { printMe(); }

        @Override public synchronized void runSecond() { printMe(); }

        @Override public synchronized void runThird() { printMe(); }
    }

    interface SyncRunner {
        void runFirst();

        void runSecond();

        void runThird();
    }
}
