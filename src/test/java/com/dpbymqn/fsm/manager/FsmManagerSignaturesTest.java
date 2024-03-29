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
public class FsmManagerSignaturesTest {

    public FsmManagerSignaturesTest() {
    }

    public static class SO1 implements StatefulObject {
    }

    public static class SL0 implements StateListener {

        Hist h = new Hist();

        @PreTransition
        void check1(SO1 so, String fromState, String toState) {
            h.app("Call1(" + so.getClass().getSimpleName() + ")[" + fromState + "->" + toState + "]");
        }
    }

    public static class SL1 extends SL0 {

        @PostTransition
        void check2(SO1 so, String fromState, String toState) {
            h.app("Call2(" + so.getClass().getSimpleName() + ")[" + fromState + "->" + toState + "]");
        }
    }

    public static class SL2 extends SL0 {

        @PreTransition
        private void check2(SO1 so, String fromState) {
            h.app("Call2(" + so.getClass().getSimpleName() + ")[" + fromState + "]");
        }

        @PreTransition
        void check3(String fromState) {
            h.app("Call3(" + ")[" + fromState + "]");
        }

        @PreTransition
        void check4(SO1 so) {
            h.app("Call4(" + so.getClass().getSimpleName() + ") ");
        }

        @PreTransition(prev = "TX1")
        public void check5(SO1 so, String fromState, String toState) {
            h.app("Call5(" + so.getClass().getSimpleName() + ")[" + fromState + "->" + toState + "]");
        }

        @PreTransition("TX2")
        void check6(SO1 so, String fromState, String toState) {
            h.app("Call6(" + so.getClass().getSimpleName() + ")[" + fromState + "->" + toState + "]");
        }

        @PreTransition(prev = "TX1", value = "TX2")
        void check7(SO1 so, String fromState, String toState) {
            h.app("Call7(" + so.getClass().getSimpleName() + ")[" + fromState + "->" + toState + "]");
        }

        @PreTransition(prev = "TX1", value = "TX2")
        void check8(SO1 so) {
            h.app("Call8(" + so.getClass().getSimpleName() + ") ");
        }
    }

    /**
     * Test of register method, of class FsmManager.
     */
    @Test
    public void testRegisterByInstance() {
        System.out.println("test base pre and post calls");
        FsmManager instance = new FsmManager();
        final SL1 sl = new SL1();
        final SO1 so = new SO1();
        instance.register(null, sl, null, so);

        instance.changeState(so, "TX1");

        assertEquals("Call1(SO1)[null->TX1]_Call2(SO1)[null->TX1]", sl.h.toString());
        sl.h.clear();
        instance.changeState(so, "TX2");
        assertEquals("Call1(SO1)[TX1->TX2]_Call2(SO1)[TX1->TX2]", sl.h.toString());
    }

    @Test
    public void testMoreMethodSignatures() {
        System.out.println("call pre methods with different signatures");
        FsmManager instance = new FsmManager();
        final SL2 sl = new SL2();
        final SO1 so = new SO1();
        instance.register(null, sl, null, so);

        instance.changeState(so, "TX1");
        System.out.println("" + sl.h.toString());

        assertEquals("Call2(SO1)[null]_Call3()[null]_Call4(SO1) _Call1(SO1)[null->TX1]", sl.h.toString());
        sl.h.clear();
        instance.changeState(so, "TX2");
        System.out.println("" + sl.h.toString());
        assertEquals(
                // 7 && 8 are specified by both (prev and current) states, so they are called first
                "Call7(SO1)[TX1->TX2]_"
                + "Call8(SO1) _"
                // current state
                + "Call6(SO1)[TX1->TX2]_"
                // previous state
                + "Call5(SO1)[TX1->TX2]_"
                // in every case
                + "Call2(SO1)[TX2]_"
                + "Call3()[TX2]_"
                + "Call4(SO1) _"
                + "Call1(SO1)[TX1->TX2]", sl.h.toString());
    }
}
