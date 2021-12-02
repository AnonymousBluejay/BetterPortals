package com.lauriethefish.betterportals.bukkit.tasks;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lauriethefish.betterportals.shared.logging.Logger;

/**
 * Uses a thread instead of a Bukkit task to finish block view updates
 * Probably less idiomatic, but does provide lower latency
 */
@Singleton
public class ThreadedBlockUpdateFinisher extends BlockUpdateFinisher implements Runnable    {
    private Thread thread;

    @Inject
    public ThreadedBlockUpdateFinisher(Logger logger) {
        super(logger);
    }

    @Override
    public void start() {
        thread = new Thread(this, "BetterPortals View Update Thread");
        thread.start();
    }

    @Override
    public void stop() {
        thread.interrupt();
        super.stop();
    }

    @Override
    public void run() {
        logger.fine("Hello from block view update thread!");

        super.processUpdatesContinually();

        logger.fine("Goodbye from block view update thread!");
    }
}
