/*******************************************************************************
* Copyright (c) 2017 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package com.microsoft.java.debug.core;

import java.util.logging.Logger;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class EventHub implements IEventHub {
    private static final Logger logger = Logger.getLogger(Configuration.LOGGER_NAME);
    private PublishSubject<DebugEvent> subject = PublishSubject.<DebugEvent>create();

    @Override
    public Observable<DebugEvent> events() {
        return subject;
    }

    private Thread workingThread = null;
    private boolean isClosed = false;

    /**
     * Starts retrieving events from the event queue of the specified virtual machine.
     *
     * @param vm
     *            the target virtual machine.
     */
    @Override
    public void start(VirtualMachine vm) {
        if (isClosed) {
            throw new IllegalStateException("This event hub is already closed.");
        }

        workingThread = new Thread(() -> {
            EventQueue queue = vm.eventQueue();
            while (true) {
                try {
                    if (Thread.interrupted()) {
                        subject.onComplete();
                        return;
                    }

                    EventSet set = queue.remove();

                    boolean shouldResume = true;
                    for (Event event : set) {
                        try {
                            logger.fine("\nJDI Event: " + event + "\n");
                        } catch (VMDisconnectedException e) {
                            // do nothing
                        }
                        DebugEvent dbgEvent = new DebugEvent();
                        dbgEvent.event = event;
                        dbgEvent.eventSet = set;
                        subject.onNext(dbgEvent);
                        shouldResume &= dbgEvent.shouldResume;
                    }

                    if (shouldResume) {
                        set.resume();
                    }
                } catch (InterruptedException e) {
                    isClosed = true;
                    subject.onComplete();
                    return;
                } catch (VMDisconnectedException e) {
                    isClosed = true;
                    subject.onError(e);
                    return;
                }
            }
        }, "Event Hub");

        workingThread.start();
    }

    @Override
    public void close() {
        if (isClosed) {
            return;
        }

        workingThread.interrupt();
        workingThread = null;
        isClosed = true;
    }

    /**
     * Gets the observable object for breakpoint events.
     * @return      the observable object for breakpoint events
     */
    @Override
    public Observable<DebugEvent> breakpointEvents() {
        return this.events().filter(debugEvent -> debugEvent.event instanceof BreakpointEvent);
    }

    /**
     * Gets the observable object for thread events.
     * @return      the observable object for thread events
     */
    @Override
    public Observable<DebugEvent> threadEvents() {
        return this.events().filter(debugEvent -> debugEvent.event instanceof ThreadStartEvent
                || debugEvent.event instanceof ThreadDeathEvent);
    }

    /**
     * Gets the observable object for exception events.
     * @return      the observable object for exception events
     */
    @Override
    public  Observable<DebugEvent> exceptionEvents() {
        return this.events().filter(debugEvent -> debugEvent.event instanceof ExceptionEvent);
    }

    /**
     * Gets the observable object for step events.
     * @return      the observable object for step events
     */
    @Override
    public Observable<DebugEvent> stepEvents() {
        return this.events().filter(debugEvent -> debugEvent.event instanceof StepEvent);
    }

    /**
     * Gets the observable object for vm events.
     * @return      the observable object for vm events
     */
    @Override
    public Observable<DebugEvent> vmEvents() {
        return this.events().filter(debugEvent -> debugEvent.event instanceof VMStartEvent
                || debugEvent.event instanceof VMDisconnectEvent
                || debugEvent.event instanceof VMDeathEvent);
    }
}
