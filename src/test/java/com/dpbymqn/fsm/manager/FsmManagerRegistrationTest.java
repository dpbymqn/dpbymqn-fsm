/*
 * To change this template, choose Tools | Templates
 * and open the template in*
 */
package com.dpbymqn.fsm.manager;

import com.dpbymqn.fsm.StateListener;
import com.dpbymqn.fsm.StatefulObject;
import com.dpbymqn.fsm.ann.PostTransition;
import com.dpbymqn.fsm.ann.PreTransition;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author dpbymqn
 */
public class FsmManagerRegistrationTest {

    public FsmManagerRegistrationTest() {
    }

    @Before
    public void setUp() {
        SL2.h.clear();
    }

    public static class SO1 implements StatefulObject {
    }

    public static class SO2 implements StatefulObject {
    }

    public static class SL1 implements StateListener {

        Hist h = new Hist();

        @PreTransition
        void check(SO1 so, String fromState, String toState) {
            h.app("Call(" + so.getClass().getSimpleName() + ") [" + fromState + "->" + toState + "]");
        }
    }

    public static class SL2 implements StateListener {

        static Hist h = new Hist();

        @PreTransition
        static void check(SO1 so, String fromState, String toState) {
            h.app("Call2(" + so.getClass().getSimpleName() + ") [" + fromState + "->" + toState + "]");
        }
    }

    public static class SL22 implements StateListener {

        static Hist h = new Hist();

        @PreTransition
        static void check(SO2 so, String fromState, String toState) {
            h.app("Call_2(" + so.getClass().getSimpleName() + ") [" + fromState + "->" + toState + "]");
        }
    }

    public static interface IF1 extends StateListener {

        @PreTransition
        void myCheck(StatefulObject so, String fromState, String toState);
    }

    public static class SL3 implements IF1 {

        Hist h = new Hist();

        @Override
        public void myCheck(StatefulObject so, String fromState, String toState) {
            h.app("CallSL3(" + so.getClass().getSimpleName() + ") [" + fromState + "->" + toState + "]");
        }
    }

    public static class SL4 implements StateListener {

        Hist h = new Hist();

        @PostTransition("TX1")
        public void myCheck1(SO1 so, String fromState, String toState) {
            h.app("CallSL41(" + so.getClass().getSimpleName() + ") [" + fromState + "->" + toState + "]");
        }

        @PostTransition("TX1")
        public void myCheck2(SO2 so, String fromState, String toState) {
            h.app("CallSL42(" + so.getClass().getSimpleName() + ") [" + fromState + "->" + toState + "]");
        }

        @PostTransition(value = "TX2", smClass = SO1.class)
        public void myCheck3(String fromState, String toState) {
            h.app("CallSL43(" + ") [" + fromState + "->" + toState + "]");
        }

        @PostTransition(value = "TX2", smClass = SO2.class)
        public void myCheck4(String fromState, String toState) {
            h.app("CallSL44(" + ") [" + fromState + "->" + toState + "]");
        }
    }

    public static class SL5 implements StateListener, StatefulObject {

        Hist h = new Hist();

        @PostTransition("TX1")
        public void myCheck1(SL5 so, String fromState, String toState) {
            h.app("CallSL51(" + so.getClass().getSimpleName() + ") [" + fromState + "->" + toState + "]");
        }

        @PostTransition("TX1")
        public void myCheck2(SO1 so, String fromState, String toState) {
            h.app("CallSL52(" + so.getClass().getSimpleName() + ") [" + fromState + "->" + toState + "]");
        }

        @PostTransition(value = "TX2", smClass = SL5.class)
        public void myCheck3(String fromState, String toState) {
            h.app("CallSL53(" + ") [" + fromState + "->" + toState + "]");
        }

        @PostTransition(value = "TX2", smClass = SO1.class)
        public void myCheck4(String fromState, String toState) {
            h.app("CallSL54(" + ") [" + fromState + "->" + toState + "]");
        }

        @PostTransition(value = "TX3")
        public void myCheck5(String fromState, String toState) {
            h.app("CallSL55(" + ") [" + fromState + "->" + toState + "]");
        }
    }

    /**
     * Test of register method, of class FsmManager.
     */
    @Test
    public void testRegisterByInstance() {
        System.out.println("register listener and stateful instances");
        FsmManager instance = new FsmManager();
        final SL1 sl = new SL1();
        final SO1 so = new SO1();
        final SO1 so2 = new SO1();
        instance.register(null, sl, null, so);

        instance.changeState(so, "TS1");

        assertEquals("Call(SO1) [null->TS1]", sl.h.toString());
        sl.h.clear();
        instance.changeState(so, "TS2");
        assertEquals("Call(SO1) [TS1->TS2]", sl.h.toString());
        instance.changeState(so2, "TX2");
        assertEquals("Call(SO1) [TS1->TS2]", sl.h.toString());
        instance.register(null, sl, null, so2);
        sl.h.clear();
        instance.changeState(so2, "TX3");
        assertEquals("Call(SO1) [TX2->TX3]", sl.h.toString());
    }

    @Test
    public void testRegisterClz() {
        System.out.println("register listener and stateful classes");
        FsmManager instance = new FsmManager();
        final SO1 so = new SO1();
        final SO2 so2 = new SO2();
        instance.register(SL2.class, null, so.getClass(), null);

        instance.changeState(so, "TS1");

        assertEquals("Call2(SO1) [null->TS1]", SL2.h.toString());
        SL2.h.clear();
        instance.changeState(so, "TS2");
        assertEquals("Call2(SO1) [TS1->TS2]", SL2.h.toString());
//        SL2.h.clear();
        instance.changeState(so2, "TX2");
        assertEquals("Call2(SO1) [TS1->TS2]", SL2.h.toString());
//        assertEquals("Call2(SO2) [null->TX2]", SL2.h.toString());
        instance.register(SL22.class, null, null, so2);
        SL22.h.clear();
        instance.changeState(so2, "TX3");
        assertEquals("Call_2(SO2) [TX2->TX3]", SL22.h.toString());

    }

    @Test
    public void testRegisterClz2Inst() {
        System.out.println("register listener object, stateful class");
        FsmManager instance = new FsmManager();
        final SO1 so = new SO1();
        final SL1 sl = new SL1();
        final SO1 so2 = new SO1();
        instance.register(null, sl, so.getClass(), null);

        instance.changeState(so, "TS1");

        assertEquals("Call(SO1) [null->TS1]", sl.h.toString());
        sl.h.clear();
        instance.changeState(so, "TS2");
        assertEquals("Call(SO1) [TS1->TS2]", sl.h.toString());
        instance.changeState(so2, "TX2");
        assertEquals("Call(SO1) [TS1->TS2]_Call(SO1) [null->TX2]", sl.h.toString());
        instance.register(null, sl, null, so2);
        sl.h.clear();
        instance.changeState(so2, "TX3");
        // double registration: one because of the sl.getClass(), the other is to so2 instance
        assertEquals("Call(SO1) [TX2->TX3]_Call(SO1) [TX2->TX3]", sl.h.toString());

    }

    @Test
    public void testRegisterInst2Clz() {
        System.out.println("register listener class, stateful instance");
        FsmManager instance = new FsmManager();
        final SO1 so = new SO1();
        final SO1 so2 = new SO1();
        instance.register(SL2.class, null, null, so);

        instance.changeState(so, "TS1");

        assertEquals("Call2(SO1) [null->TS1]", SL2.h.toString());
        SL2.h.clear();
        instance.changeState(so, "TS2");
        assertEquals("Call2(SO1) [TS1->TS2]", SL2.h.toString());
        instance.changeState(so2, "TX2");
        // SL2 is not called
        assertEquals("Call2(SO1) [TS1->TS2]", SL2.h.toString());
        instance.register(SL2.class, null, null, so2);
        SL2.h.clear();
        instance.changeState(so2, "TX3");
        assertEquals("Call2(SO1) [TX2->TX3]", SL2.h.toString());
    }

    @Test
    public void testRegisterInterface() {
        System.out.println("register listener interface ");
        FsmManager instance = new FsmManager();
        final SO1 so = new SO1();
        final SL3 sl3 = new SL3();

        instance.register(null, sl3, null, so);

        instance.changeState(so, "TS1");
        System.out.println("" + sl3.h);
        assertEquals("CallSL3(SO1) [null->TS1]", sl3.h.toString());
    }

    /**
     * we make sure that the listener methods are only called when they expect
     * the same type stateful object than the what is actually changing
     *
     * e.g. one listener can specify different methods for the same states on
     * different SO types
     *
     * the listening target can be declared as either
     *
     * - set as first parameter of the annotated method
     *
     * - set as smClass on the annotation
     *
     * - on self listening classes it is assumed automatically when not
     * specified
     */
    @Test
    public void testRegisterSignatureSO() {
        System.out.println("register listener w different SO signatures ");
        FsmManager instance = new FsmManager();
        final SO1 so1 = new SO1();
        final SO2 so2 = new SO2();
        final SL4 sl = new SL4();

        instance.register(null, sl, null, so1);
        instance.register(null, sl, null, so2);

        instance.changeState(so1, "TX1");
        instance.changeState(so2, "TX1");
        instance.changeState(so1, "TX2");
        System.out.println("" + sl.h);
        assertEquals("CallSL41(SO1) [TX1->TX2]", sl.h.toString());
        sl.h.clear();
        instance.changeState(so2, "TX2");
        System.out.println("" + sl.h);
        assertEquals("CallSL42(SO2) [TX1->TX2]", sl.h.toString());
        sl.h.clear();
        instance.changeState(so1, "TX3");
        System.out.println("" + sl.h);
        assertEquals("CallSL43() [TX2->TX3]", sl.h.toString());
        sl.h.clear();
        instance.changeState(so2, "TX3");
        System.out.println("" + sl.h);
        assertEquals("CallSL44() [TX2->TX3]", sl.h.toString());
//        sl.h.clear();
    }

    /**
     *
     */
    @Test
    public void testRegisterSignatureSOSL() {
        System.out.println("register listener w different SO signatures on self listener state machine");
        FsmManager instance = new FsmManager();
        final SO1 so1 = new SO1();
        final SL5 sl = new SL5();

        instance.register(null, sl, null, so1);
        instance.register(null, sl, null, sl);

        instance.changeState(so1, "TX1");
        instance.changeState(sl, "TX1");
        instance.changeState(so1, "TX2");
        System.out.println("" + sl.h);
        assertEquals("CallSL52(SO1) [TX1->TX2]", sl.h.toString());
        sl.h.clear();
        instance.changeState(sl, "TX2");
        System.out.println("" + sl.h);
        assertEquals("CallSL51(SL5) [TX1->TX2]", sl.h.toString());
        sl.h.clear();
        instance.changeState(so1, "TX3");
        System.out.println("" + sl.h);
        assertEquals("CallSL54() [TX2->TX3]", sl.h.toString());
        sl.h.clear();
        instance.changeState(sl, "TX3");
        System.out.println("" + sl.h);
        assertEquals("CallSL53() [TX2->TX3]", sl.h.toString());
        sl.h.clear();
        instance.changeState(sl, "TX4");
        System.out.println("" + sl.h);
        assertEquals("CallSL55() [TX3->TX4]", sl.h.toString());
//        sl.h.clear();
    }
}
