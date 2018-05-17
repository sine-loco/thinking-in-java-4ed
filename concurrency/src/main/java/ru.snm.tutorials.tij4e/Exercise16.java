package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sine-loco
 */
public class Exercise16 {
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
        runWithDelay( service, new ThreeCriticalsOneLock() );
        runWithDelay( service, new ThreeCriticalsThreeLocks() );
        service.shutdown();
    }

    static class ThreeCriticalsThreeLocks implements SyncRunner {
        final Lock lockOne = new ReentrantLock();
        final Lock lockTwo = new ReentrantLock();
        final Lock lockThree = new ReentrantLock();

        @Override public void runFirst() {
            lockOne.lock();
            try {
                 printMe();
            } finally {
                lockOne.unlock();
            }
        }

        @Override public void runSecond() {
            lockTwo.lock();
            try {
                printMe();
            } finally {
                lockTwo.unlock();
            }
        }

        @Override public void runThird() {
            lockThree.lock();
            try {
                printMe();
            } finally {
                lockThree.unlock();
            }
        }
    }

    static class ThreeCriticalsOneLock implements SyncRunner {
        final Lock lock = new ReentrantLock();

        @Override public void runFirst() {
            lock.lock();
            try {
                printMe();
            } finally {
                lock.unlock();
            }
        }

        @Override public synchronized void runSecond() {
            lock.lock();
            try {
                printMe();
            } finally {
                lock.unlock();
            }
        }

        @Override public synchronized void runThird() {
            lock.lock();
            try {
                printMe();
            } finally {
                lock.unlock();
            }
        }
    }

    interface SyncRunner {
        void runFirst();

        void runSecond();

        void runThird();
    }
}
