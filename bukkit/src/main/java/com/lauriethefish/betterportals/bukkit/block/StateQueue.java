package com.lauriethefish.betterportals.bukkit.block;

import com.lauriethefish.betterportals.shared.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents a queue of viewable block states
 */
public class StateQueue {
    private List<IViewableBlockInfo> viewableStates = null;
    private int size = 0;

    private final LinkedBlockingQueue<List<IViewableBlockInfo>> newStateQueue = new LinkedBlockingQueue<>();

    private volatile boolean hasFinishedInit = false;

    private final Logger logger;

    public StateQueue(Logger logger) {
        this.logger = logger;
    }

    public List<IViewableBlockInfo> getViewableStates() {
        // If the initial states haven't yet been added, then we return an empty list
        if(!hasFinishedInit) {
            logger.fine("Init not finished");
            return Collections.emptyList();
        }

        // Take all the newly queued states and add them to the viewable states list
        while(!newStateQueue.isEmpty()) {
            logger.fine("Adding queued states");
            try {
                List<IViewableBlockInfo> newStates = newStateQueue.take();
                size += newStates.size();
                viewableStates.addAll(newStates);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        return viewableStates;
    }

    public void addStatesInitially(List<IViewableBlockInfo> blockInfoList) {
        if(hasFinishedInit) {
            throw new IllegalStateException("Cannot add initial states multiple times");
        }

        viewableStates = blockInfoList;
        size = blockInfoList.size();
        hasFinishedInit = true;
    }

    public void enqueueStates(List<IViewableBlockInfo> blockInfoList) {
        logger.fine("Enqueueing states");
        newStateQueue.add(blockInfoList);
    }

    public int stateCount() {
        return size;
    }
}
