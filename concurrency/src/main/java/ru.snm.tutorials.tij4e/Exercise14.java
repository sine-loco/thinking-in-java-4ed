package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author sine-loco
 */
public class Exercise14 {
    private final static Logger logger = LogManager.getLogger();

    public static void main( String[] args ) {
        for ( int i = 0; i < 1000; i++ ) {
            new Timer().schedule( new TimerTask() {
                @Override public void run() {
                    logger.info( "finished" );
                }
            }, 100 );
        }
    }

}
