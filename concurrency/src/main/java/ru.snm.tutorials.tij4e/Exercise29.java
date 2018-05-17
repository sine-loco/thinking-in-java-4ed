package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author sine-loco
 */
public class Exercise29 {
    private final static Logger logger = LogManager.getLogger();

    public static void main( String[] args ) throws InterruptedException {
        ToastQueue dryQueue = new ToastQueue();
        ToastQueue butteredQueue = new ToastQueue();
        ToastQueue jellieddQueue = new ToastQueue();
        ToastQueue jammedQueue = new ToastQueue();
        MultytoastQueue assembled = new MultytoastQueue();
        final ThreadNameSwitch threadNameSwitch = new ThreadNameSwitch();
        ExecutorService service = Executors.newCachedThreadPool( r ->
                new Thread( r, threadNameSwitch.name ) );

        threadNameSwitch.name = "toaster";
        service.execute( new Toaster( dryQueue ) );
        threadNameSwitch.name = "butterer";
        service.execute( new Butterer( dryQueue, butteredQueue ) );
        threadNameSwitch.name = "jammer";
        service.execute( new Jammer( dryQueue, jammedQueue ) );
        threadNameSwitch.name = "jellier";
        service.execute( new Jellier( dryQueue, jellieddQueue ) );
        /*threadNameSwitch.name = "eater";
        service.execute( new Eater( finishedQueue ) );*/
        threadNameSwitch.name = "merger";
        service.execute(
                new Merger( assembled, butteredQueue, jellieddQueue, jammedQueue ) );
        TimeUnit.SECONDS.sleep( 2 );
        service.shutdownNow();
    }

    static class Toast {
        public enum Status { DRY, BUTTERED, JAMMED, JELLIED, MERGED; }

        private Status status = Status.DRY;
        private final int id;

        public Toast( int id ) { this.id = id; }

        public void butter() { status = Status.BUTTERED; }

        public void jam() { status = Status.JAMMED; }

        public void jelly() { status = Status.JELLIED; }

        public int getId() { return id; }

        public Status getStatus() { return status; }

        @Override public String toString() { return "Toast: " + id + ": " + status; }
    }

    static class Multitoast {
        Toast[] parts;

        public Multitoast( Toast[] parts ) {
            this.parts = parts;
        }

        @Override public String toString() {

            StringBuilder sb = new StringBuilder( "Multitoast: " );
            for ( Toast part: parts ) {
                sb.append( "{}, " + part );
            }
            sb.setCharAt( sb.lastIndexOf( "," ), '.' );
            return sb.toString();
        }
    }

    static class ToastQueue extends LinkedBlockingQueue<Toast> {}

    static class MultytoastQueue extends LinkedBlockingQueue<Multitoast> {}

    static class Toaster implements Runnable {
        private ToastQueue toastQueue;

        private int count = 0;

        private Random rand = new Random( 47 );

        public Toaster( ToastQueue toastQueue ) { this.toastQueue = toastQueue; }

        @Override public void run() {
            try {
                while ( !Thread.interrupted() ) {
                    TimeUnit.MILLISECONDS.sleep( 100 + rand.nextInt( 500 ) );
                    Toast toast = new Toast( count++ );
                    logger.info( "toasted: {}", toast );
                    toastQueue.put( toast );
                }
            } catch ( InterruptedException e ) {
                 logger.info( "interrupted" );
            }
            logger.info( "turned off" );
        }
    }

    static class Butterer implements Runnable {
        private ToastQueue dryQueue;
        private ToastQueue butteredQueue;

        public Butterer( ToastQueue dryQueue, ToastQueue butteredQueue ) {
            this.dryQueue = dryQueue;
            this.butteredQueue = butteredQueue;
        }

        @Override public void run() {
            try {
                while ( !Thread.interrupted() ) {
                    Toast toast = dryQueue.take();
                    toast.butter();
                    logger.info( "buttered: {}", toast );
                    butteredQueue.put( toast );
                }
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }
            logger.info( "turned off" );
        }
    }

    static class Jammer implements Runnable {
        private ToastQueue butteredQueue;
        private ToastQueue finishedQueue;

        public Jammer( ToastQueue butteredQueue, ToastQueue finishedQueue ) {
            this.butteredQueue = butteredQueue;
            this.finishedQueue = finishedQueue;
        }

        @Override public void run() {
            try {
                while ( !Thread.interrupted() ) {
                    Toast toast = butteredQueue.take();
                    toast.jam();
                    logger.info( "jammed: {}", toast );
                    finishedQueue.put( toast );
                }
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }
            logger.info( "turned off" );
        }
    }

    static class Jellier implements Runnable {
        private ToastQueue dryQueue;
        private ToastQueue jelliedQueue;

        public Jellier( ToastQueue dryQueue, ToastQueue jelliedQueue ) {
            this.dryQueue = dryQueue;
            this.jelliedQueue = jelliedQueue;
        }

        @Override public void run() {
            try {
                while ( !Thread.interrupted() ) {
                    Toast toast = dryQueue.take();
                    toast.jelly();
                    logger.info( "jellied: {}", toast );
                    jelliedQueue.put( toast );
                }
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }
            logger.info( "turned off" );
        }
    }

    static class Merger implements Runnable {
        private static final Toast[] TEMPLATE = new Toast[] {};
        ToastQueue[] queues;
        MultytoastQueue finishedQueue;

        public Merger( MultytoastQueue finishedQueue, ToastQueue... queues ) {
            this.finishedQueue = finishedQueue;
            this.queues = queues;
        }

        @Override public void run() {
            try {
                while ( !Thread.interrupted() ) {
                    List<Toast> parts = new ArrayList<>( queues.length );
                    for ( ToastQueue queue : queues ) {
                        parts.add( queue.take() );
                    }
                    Multitoast multitoast = new Multitoast( parts.toArray( TEMPLATE ) );
                    logger.info( "merged: {}", multitoast );
                    finishedQueue.put( multitoast );
                }
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }
            logger.info( "turned off" );
        }
    }

    static class Eater implements Runnable {
        private ToastQueue finishedQueue;

        private int counter = 0;

        public Eater( ToastQueue finishedQueue ) { this.finishedQueue = finishedQueue; }

        @Override public void run() {
            try {
                while ( !Thread.interrupted() ) {
                    Toast toast = finishedQueue.take();
                    if ( toast.getId() != counter++
                            || toast.getStatus() != Toast.Status.JAMMED )
                    {
                        logger.error( "defective toast!" );
                        System.exit( 1 );
                    } else  {
                        logger.info( "consumed: {}", toast );
                    }
                }
            } catch ( InterruptedException e ) {
                logger.info( "interrupted" );
            }
            logger.info( "turned off" );
        }
    }



}
