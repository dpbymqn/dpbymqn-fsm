/*
 * To change this template, choose Tools | Templates
 * and open the template in*
 */
package com.dpbymqn.fsm.manager;

import com.dpbymqn.fsm.StateListener;
import com.dpbymqn.fsm.StatefulObject;
import com.dpbymqn.fsm.ann.PostTransition;
import com.dpbymqn.fsm.ann.PreTransition;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author dpbymqn
 */
public class FsmQueueTest {

    public static class LocalModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(FsmManager.class);
            bind(FsmQueue.class);
        }
    }
    final static Injector injector = Guice.createInjector(new FsmQueueTest.LocalModule());

    public FsmQueueTest() {
    }

    public static class SO implements StatefulObject {
    }

    public static class SL1 implements StateListener {

        @Inject
        Hist h;
        @Inject
        FsmQueue fsmq;

        @PostTransition(next = "X1")
        void x1(SO so) {
            h.app("x1");
        }

        @PostTransition(next = "X2")
        void x2(SO so) {
            h.app("x2");
            fsmq.send(so, "X3");
        }

        @PostTransition(next = "X3")
        void x3(SO so) {
            h.app("x3");
        }
    }

    public static class SL2 implements StateListener {

        @Inject
        Hist h;
        @Inject
        FsmQueue fsmq;

        @PreTransition("X1")
        void x1(SO so) {
            h.app("x1");
        }

        @PreTransition("X2")
        void x2(SO so) {
            h.app("x2");
            fsmq.send(so, "X3");
        }

        @PreTransition("X3")
        void x3(SO so) {
            h.app("x3");
        }
    }

    @Test
    public void testSendPost() {
        System.out.println("send post");
        SO so = injector.getInstance(SO.class);
        SL1 sl = injector.getInstance(SL1.class);

        FsmQueue fsmq = injector.getInstance(FsmQueue.class);
        fsmq.register(null, sl, null, so);
        //
        fsmq.send(so, "X1");

        assertEquals("X1", fsmq.getState(so));
        //
        fsmq.send(so, "X2");
        assertEquals("X3", fsmq.getState(so));

    }

    /**
     * Even if Post should work well without queue, Pre can only work if the
     * queue is in place. (Pre methods are called before the actual state change
     * is performed, so in case we call the state changes directly without
     * asynchronous queue, then the latter state would have been overwritten
     * with the earlier one.)
     */
    @Test
    public void testSendPre() {
        System.out.println("send pre");
        SO so = injector.getInstance(SO.class);
        SL2 sl = injector.getInstance(SL2.class);

        FsmQueue fsmq = injector.getInstance(FsmQueue.class);
        fsmq.register(null, sl, null, so);
        //
        fsmq.send(so, "X1");

        assertEquals("X1", fsmq.getState(so));
        //
        fsmq.send(so, "X2");
        assertEquals("X3", fsmq.getState(so));
    }
}
