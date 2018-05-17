package ru.snm.tutorials.tij4e;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author sine-loco
 */
public class Exercise19 {
    private final static Logger logger = LogManager.getLogger();

    public static void main( String[] args ) {
        OrnamentalGarden garden = new OrnamentalGarden();
        garden.start();

        try {
            TimeUnit.MILLISECONDS.sleep( 15 );
        } catch ( InterruptedException e ) {
            logger.info( "main flow interrupted" );
        } finally {
            garden.stop();
        }
   }

    static abstract class Turnstile {
        private static AtomicInteger currentId;
        private final Counter counter;
        private final Integer id;

        static {
            currentId = new AtomicInteger( 0 );
        }

        Turnstile( Counter counter ) {
            id = currentId.incrementAndGet();
            this.counter = counter;
            logger.trace( "{} created", this.getClass().getSimpleName() );
        }

        abstract int getIncrement();

        final void onPass() {
            getCounter().increment( getIncrement() );
            logger.info( "{} passed", this.getClass().getSimpleName() );
        }

        protected Counter getCounter() { return counter; }

        @Override public String toString() {
            return this.getClass().getSimpleName() + " '" + id + '\'';
        }
    }

    final static class SingleTurnstile extends Turnstile {
        public SingleTurnstile( Counter counter ) {
            super( counter );
        }

        @Override int getIncrement() { return 1; }
    }

    final static class DoubleTurnstile extends Turnstile {
        public DoubleTurnstile( Counter counter ) {
            super( counter );
        }

        @Override int getIncrement() { return 2; }
    }

    final static class NegativeSingleTurnstile extends Turnstile {
        public NegativeSingleTurnstile( Counter counter ) {
            super( counter );
        }

        @Override int getIncrement() { return -1; }
    }

    static class Counter {
        final AtomicInteger counter = new AtomicInteger( 0 );

        void increment( int incrementBy ) {
            counter.addAndGet( incrementBy );
        }

        int getPassedCount() { return counter.get(); }
    }

    static class OrnamentalGarden {
        public static final int N_OF_SETS = TurnstilesSet.values().length;
        public static final int N_OF_TASKS = 5;

        private final Counter counter;
        private final List<Future<Integer>> tasks;
        private final List<Entrance> entrances;
        private final ExecutorService service;

        OrnamentalGarden() {
            counter = new Counter();
            tasks = new ArrayList<>( N_OF_TASKS );
            entrances = new ArrayList<>( N_OF_TASKS );
            Random random = new Random( 12345 );
            for ( int i = 0; i < N_OF_TASKS; i++ ) {
                TurnstilesSet set = TurnstilesSet.valueOf( random.nextInt( N_OF_SETS ) );
                entrances.add( new Entrance( set.getTurnstiles( counter ) ) );
            }
            service = Executors.newCachedThreadPool();
            logger.info( "garden initialized" );
        }

        void start() {
            entrances.forEach( entrance -> tasks.add( service.submit( entrance ) ) );
            logger.info( "garden working day has just begun" );
        }

        void stop() {
            tasks.forEach( task -> task.cancel( false ) );
            try {
                if ( !service.awaitTermination( 1, TimeUnit.MICROSECONDS ) ) {
                    service.shutdownNow();
                }

            } catch ( InterruptedException ignored ) { }
            // fixme приделать завершение
            logger.info( "overall passed: {}", counter.getPassedCount() );

        }

        /** для генерации наборов турникетов */
        private enum TurnstilesSet {
            ONE_SINGLE( 0 ) {
                @Override
                protected Function<Counter, List<? extends Turnstile>> generate() {
                    return counter -> {
                        List<Turnstile> ts = new ArrayList<>( 1 );
                        ts.add( new SingleTurnstile( counter ) );
                        return ts;
                    };
                }
            },

            ONE_DOUBLE( 1 ) {
                @Override
                protected Function<Counter, List<? extends Turnstile>> generate() {
                    return counter -> {
                        List<Turnstile> ts = new ArrayList<>( 1 );
                        ts.add( new DoubleTurnstile( counter ) );
                        return ts;
                    };
                }
            },

            SINGE_AND_DOUBLE( 2 ) {
                @Override
                protected Function<Counter, List<? extends Turnstile>> generate() {
                    return counter -> {
                        List<Turnstile> ts = new ArrayList<>( 2 );
                        ts.add( new SingleTurnstile( counter ) );
                        ts.add( new DoubleTurnstile( counter ) );
                        return ts;
                    };
                }
            },

            ONE_SINGLE_NEGATIVE( 3 ) {
                @Override
                protected Function<Counter, List<? extends Turnstile>> generate() {
                    return counter -> {
                        List<Turnstile> ts = new ArrayList<>( 1 );
                        ts.add( new NegativeSingleTurnstile( counter ) );
                        return ts;
                    };
                }
            };

            private final Integer code;

            TurnstilesSet( int code ) { this.code = code; }

            public static TurnstilesSet valueOf( int code ) {
                for ( TurnstilesSet one : TurnstilesSet.values() ) {
                    if ( one.code == code ) { return one; }
                }
                throw new IllegalArgumentException(
                        "no generated turnstiles set with code: " + code );
            }

            public final List<? extends Turnstile> getTurnstiles( Counter counter ) {
                return getNew( counter, generate() );
            }
            /** генерация турникетов для конкретной реализации */
            protected abstract Function<Counter, List<? extends Turnstile>> generate();

            private List<? extends Turnstile> getNew(
                    Counter counter,
                    Function<Counter, List<? extends Turnstile>> function )
            {
                List<? extends Turnstile> ts = function.apply( counter );
                return Collections.unmodifiableList( ts );
            }
        }
    }

    static class Entrance implements Callable<Integer> {
        private static AtomicInteger currentId;
        private List<? extends Turnstile> turnstiles;
        private final Integer id;

        static {
            currentId = new AtomicInteger( 0 );
        }

        Entrance( List<? extends Turnstile> turnstiles ) {
            id = currentId.incrementAndGet();
            this.turnstiles = turnstiles;
        }

        @Override public Integer call() {
            final Integer counter = 0;
            try {
                while ( !Thread.currentThread().isInterrupted() ) {
                    logger.trace( "entrance activated: {}", this );
                    turnstiles.forEach( Turnstile::onPass );
                    TimeUnit.MICROSECONDS.sleep( 1 );
                }
                logger.info( "interrupted via flag" );
            } catch ( InterruptedException e ) {
                logger.info( "interrupted while blocked" );
            }
            return counter;
        }

        @Override public String toString() {
            return "entrance id: '" + id + '\'';
        }
    }

}

