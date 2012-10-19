/*
    This file is part of FiniteStateMachine.

    FiniteStateMachine is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FiniteStateMachine is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FiniteStateMachine.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.dpbymqn.fsm.manager;

import com.dpbymqn.fsm.StateListener;
import com.dpbymqn.fsm.StatefulObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.LinkedList;
import java.util.Queue;
import java.util.WeakHashMap;

/**
 *
 * @author dpbymqn
 */
@Singleton
public class FsmQueue {

    @Inject
    FsmManager fsm;
    WeakHashMap<StatefulObject, Queue<String>> queues = new WeakHashMap<StatefulObject, Queue<String>>();
 
    public String getState(StatefulObject st) {
        return fsm.getState(st);
    }

    public void reset() {
        fsm.reset();
    }

    public void register(Class<? extends StateListener> clzLst, StateListener sl, Class<? extends StatefulObject> stClz, StatefulObject st) {
        fsm.register(clzLst, sl, stClz, st);
    }

    public void send(StatefulObject so, String state) {
        synchronized (so) {
            if (queues.containsKey(so)) {
                queues.get(so).add(state);
                return;
            } else {
                final LinkedList<String> q = new LinkedList<String>();
                q.add(state);
                queues.put(so, q);
            }
        }

        do {
            synchronized (so) {
                final Queue<String> q = queues.get(so);
                final String nextState = q.remove();
                fsm.changeState(so, nextState);
                if (q.isEmpty()) {
                    queues.remove(so);
                }
            }
        } while (queues.containsKey(so));
    }
}
