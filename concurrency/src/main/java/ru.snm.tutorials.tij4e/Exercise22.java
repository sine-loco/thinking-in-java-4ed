package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author sine-loco
 */
public class Exercise22 {
    private final static Logger logger = LogManager.getLogger();

    public static void main( String[] args ) {
        Switch aSwitch = new Switch();
        Reader busyReader = new Reader( aSwitch );
        Waiter busyWaiter = new Waiter( aSwitch );
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute( busyReader );
        service.execute( busyWaiter );
        service.shutdown();
    }


    static class Switch {
        private boolean flag = false;

        synchronized void turnOn() { flag = true; }

        synchronized void turnOff() { flag = false; }

        synchronized boolean isOn() { return flag; }
    }

    static class Reader implements Runnable {
        private volatile Switch aSwitch;

        Reader( Switch aSwitch ) { this.aSwitch = aSwitch; }

        @Override public void run() {
            try {
                TimeUnit.SECONDS.sleep( 2 );
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }
            synchronized ( aSwitch ) {
                aSwitch.notifyAll();
            }
            aSwitch.turnOn();
            logger.info( "turned on" );
        }
    }

    static class BusyWaiter implements Runnable {
        private volatile Switch aSwitch;

        BusyWaiter( Switch aSwitch ) { this.aSwitch = aSwitch; }

        @Override public void run() {
            try {
                TimeUnit.SECONDS.sleep( 1 );
                while ( !aSwitch.isOn() ) {
                    TimeUnit.MILLISECONDS.sleep( 10 );
                }
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }
            aSwitch.turnOff();
            logger.info( "turned off" );
        }

    }



    static class Waiter implements Runnable {
        private final Switch aSwitch;

        Waiter( Switch aSwitch ) { this.aSwitch = aSwitch; }

        @Override public void run() {
            try {
                while ( !aSwitch.isOn() ) {
                    synchronized ( aSwitch ) {
                        aSwitch.wait();
                    }
                }
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }
            aSwitch.turnOff();
            logger.info( "turned off" );
        }

    }
}
