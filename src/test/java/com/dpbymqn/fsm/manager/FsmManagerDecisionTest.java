/*
 * To change this template, choose Tools | Templates
 * and open the template in*
 */
package com.dpbymqn.fsm.manager;

import com.dpbymqn.fsm.StateListener;
import com.dpbymqn.fsm.StatefulObject;
import com.dpbymqn.fsm.ann.OnDecision;
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
public class FsmManagerDecisionTest {

    public static class LocalModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(FsmManager.class);
            bind(Hist.class);
            bind(SO1.class);
            bind(SL1.class);
            bind(SL2.class);
            bind(SL3.class);
            bind(SL4.class);
        }
    }
    final static Injector injector = Guice.createInjector(new LocalModule());

    public FsmManagerDecisionTest() {
    }

    @Before
    public void setUp() {
        injector.getInstance(FsmManager.class).reset();
//        SL2.h.clear();
    }

    public static class SO1 implements StatefulObject {
    }

    public static class SL2 implements StateListener {

        @Inject
        Hist h;

        @OnDecision(next = "")
        String b1(SO1 sm, String fromState) {
            h.app("b1:" + sm.getClass().getSimpleName() + "/" + fromState);
            return null;
        }
    }

    public static class SL3 implements StateListener {

        @Inject
        Hist h;

        @OnDecision(next = "")
        String b1(SO1 sm, String fromState) {
            h.app("b1:" + sm.getClass().getSimpleName() + "/" + fromState);
            if ("TX1".equals(fromState)) {
                return "TX2";
            }
            return null;
        }
    }

    public static class SL4 implements StateListener {

        @Inject
        Hist h;

        @OnDecision(prev = "TX1", next = "TX2")
        Boolean b1(SO1 sm, String fromState) {
            h.app("b1:" + sm.getClass().getSimpleName() + "/" + fromState);
            return true;
        }
    }

    public static class SL5 implements StateListener {

        @Inject
        Hist h;

        @OnDecision(prev = "B1X1", next = "B1X2")
        Boolean b1(SO1 sm, String toState) {
            h.app("b1:" + sm.getClass().getSimpleName() + "/" + toState);
            return true;
        }

        @OnDecision(prev = "B2X1", next = "B2X2")
        Boolean b2(String toState) {
            h.app("b2:" + toState);
            return true;
        }

        @OnDecision(next = "C1X1")
        Boolean c1(SO1 sm, String fromState) {
            h.app("c1:" + sm.getClass().getSimpleName() + "/" + fromState);
            return true;
        }

        @OnDecision(next = "C2X1")
        Boolean c2(String fromState) {
            h.app("c2:" + fromState);
            return true;
        }

        @OnDecision(prev = "D1X1", next = "D1X2")
        Boolean d1(SO1 sm) {
            h.app("d1:" + sm.getClass().getSimpleName());
            return true;
        }

        @OnDecision(prev = "D2X1", next = "D2X2")
        boolean d2() {
            h.app("d2:");
            return true;
        }
    }

    public static class SL1 implements StateListener {

        @Inject
        Hist h;

        @OnDecision(next = "TX2")
        Boolean b1(SO1 sm, String fromState, String toState) {
            h.app("b1:" + sm.getClass().getSimpleName() + "/" + fromState + "/" + toState);
            return "TX1".equals(fromState);
        }
    }

    /**
     * Test of register method, of class FsmManager.
     */
    @Test
    public void testBasicDecisionBoolean() {
        System.out.println("test basic decision calls");
        FsmManager instance = injector.getInstance(FsmManager.class);
        final SL1 sl = injector.getInstance(SL1.class);
        final SO1 so = injector.getInstance(SO1.class);
        instance.register(null, sl, null, so);

        instance.changeState(so, "TX1");

        System.out.println("" + sl.h.toString());
        assertEquals("TX2", instance.getState(so));
        assertEquals("b1:SO1/TX1/TX2", sl.h.toString());
//        assertEquals("b1:SO1/null/TX1", sl.h.toString());
        sl.h.clear();
        instance.changeState(so, "TX3");
        System.out.println("" + sl.h.toString());
        assertEquals("TX3", instance.getState(so));
        assertEquals("b1:SO1/TX3/TX2", sl.h.toString());        // because SL1.b1 is called to check if we're going to TX2 or not.
    }

    /**
     * Test of register method, of class FsmManager.
     */
    @Test
    public void testBasicDecisionString() {
        System.out.println("test basic decision calls3");
        FsmManager instance = injector.getInstance(FsmManager.class);
        final SL2 sl = injector.getInstance(SL2.class);
        final SO1 so = injector.getInstance(SO1.class);
        instance.register(null, sl, null, so);

        instance.changeState(so, "TX1");

        System.out.println("" + sl.h.toString());
        assertEquals("TX1", instance.getState(so));
        assertEquals("b1:SO1/TX1", sl.h.toString());
        sl.h.clear();
        instance.changeState(so, "TX2");
        System.out.println("" + sl.h.toString());
        assertEquals("TX2", instance.getState(so));
        assertEquals("b1:SO1/TX2", sl.h.toString());
    }

    /**
     * Test of register method, of class FsmManager.
     */
    @Test
    public void testBasicDecisionStringWithBranching() {
        System.out.println("test basic decision calls3");
        FsmManager instance = injector.getInstance(FsmManager.class);
        final SL3 sl = injector.getInstance(SL3.class);
        final SO1 so = injector.getInstance(SO1.class);
        instance.register(null, sl, null, so);

        instance.changeState(so, "TX1");

        System.out.println("" + sl.h.toString());
        assertEquals("TX2", instance.getState(so));
        assertEquals("b1:SO1/TX1_b1:SO1/TX2", sl.h.toString());
        sl.h.clear();
        instance.changeState(so, "TX3");
        System.out.println("" + sl.h.toString());
        assertEquals("TX3", instance.getState(so));
        assertEquals("b1:SO1/TX3", sl.h.toString());
    }

    @Test
    public void testBasicDecisionWithBoolean1() {
        System.out.println("test boolean decision 1");
        FsmManager instance = injector.getInstance(FsmManager.class);
        final SL4 sl = injector.getInstance(SL4.class);
        final SO1 so = injector.getInstance(SO1.class);
        instance.register(null, sl, null, so);

        instance.changeState(so, "TX1");

        System.out.println("" + sl.h.toString());
        assertEquals("TX2", instance.getState(so));
        assertEquals("b1:SO1/TX2", sl.h.toString());
        sl.h.clear();
        instance.changeState(so, "TX3");
        System.out.println("" + sl.h.toString());
        assertEquals("TX3", instance.getState(so));
        assertEquals("", sl.h.toString());
    }
}
