/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dpbymqn.fsm.event;

import com.dpbymqn.fsm.event.ann.OnEvent;
import com.dpbymqn.fsm.manager.FsmManager;
import com.google.common.collect.Lists;
import java.util.List;
import lombok.extern.java.Log;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dobyman
 */
@Log
public class FsmEventDispatcherTest {

    final FsmManager fsm = new FsmManager();
    
    List<String> results = Lists.newArrayList();

    public class MySEL implements StatefulEventListener {

        @OnEvent("alma")
        public void alma(Object o) {
            log.info("alma:" + o);
            results.add("alma-"+fsm.getState(this)+"-"+o);
        }

        @OnEvent("korte")
        public void korte(Object o) {
            log.info("korte:" + o);
            results.add("korte-"+fsm.getState(this)+"-"+o);
        }

        @OnEvent(all = true)
        public void szilva(Object o) {
            log.info("szilva:" + o);
            results.add("szilva-"+fsm.getState(this)+"-"+o);
        }

        @OnEvent(other = true)
        public void barack(Object o) {
            log.info("barack:" + o);
            results.add("barack-"+fsm.getState(this)+"-"+o);
        }
    }

    @Test
    public void test() {
        System.out.println("testing dispatcher");
        FsmEventDispatcher dispatcher = new FsmEventDispatcher(fsm);
        assertNotNull(dispatcher);
//        dispatcher.register(MySEL.class);
        MySEL sel = new MySEL();
        dispatcher.fireEvent(sel, "1st");
        fsm.setState(sel, "alma");
        dispatcher.fireEvent(sel, "2nd");
        fsm.setState(sel, "korte");
        dispatcher.fireEvent(sel, "3rd");
        fsm.setState(sel, "szolo");
        dispatcher.fireEvent(sel, "4nd");
        System.out.println(""+results);
        assertEquals("[szilva-null-1st, barack-null-1st, szilva-alma-2nd, alma-alma-2nd, szilva-korte-3rd, korte-korte-3rd, szilva-szolo-4nd, barack-szolo-4nd]", results.toString());
//        dispatcher.unregister(MySEL.class);
        dispatcher.fireEvent(sel, "5th");
//        assertEquals("[szilva-null-1st, barack-null-1st, szilva-alma-2nd, alma-alma-2nd, szilva-korte-3rd, korte-korte-3rd, szilva-szolo-4nd, barack-szolo-4nd]", results.toString());
    }
}
