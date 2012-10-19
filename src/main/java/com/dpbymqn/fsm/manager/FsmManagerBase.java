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
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dpbymqn
 */
public class FsmManagerBase {

    private final static Logger log = Logger.getLogger(FsmManagerBase.class.getName());
    
//    final private Map<StatefulObject, String> stateRegistry = Collections.synchronizedMap(new WeakHashMap<StatefulObject, String>());
    final private Map<StatefulObject, String> stateRegistry = new WeakHashMap<StatefulObject, String>();
    final protected Map<StatefulObject, Map<StateListener, Map<String, Multimap<String, TransitionCallback>>>> instancePreTrCallbackMap =
            Collections.synchronizedMap(new WeakHashMap<StatefulObject, Map<StateListener, Map<String, Multimap<String, TransitionCallback>>>>());
    final protected Map<Class<? extends StatefulObject>, Map<String, Multimap<String, TransitionCallback>>> classPreTrCallbackMap =
            Collections.synchronizedMap(new LinkedHashMap<Class<? extends StatefulObject>, Map<String, Multimap<String, TransitionCallback>>>());
    final protected Map<StatefulObject, Map<StateListener, Map<String, Multimap<String, TransitionCallback>>>> instancePostTrCallbackMap =
            Collections.synchronizedMap(new WeakHashMap<StatefulObject, Map<StateListener, Map<String, Multimap<String, TransitionCallback>>>>());
    final protected Map<Class<? extends StatefulObject>, Map<String, Multimap<String, TransitionCallback>>> classPostTrCallbackMap =
            Collections.synchronizedMap(new LinkedHashMap<Class<? extends StatefulObject>, Map<String, Multimap<String, TransitionCallback>>>());
    final protected Map<StatefulObject, Map<StateListener, Map<String, Multimap<String, DecisionCallback>>>> instanceDecCallbackMap =
            Collections.synchronizedMap(new WeakHashMap<StatefulObject, Map<StateListener, Map<String, Multimap<String, DecisionCallback>>>>());
    final protected Map<Class<? extends StatefulObject>, Map<String, Multimap<String, DecisionCallback>>> classDecCallbackMap =
            Collections.synchronizedMap(new LinkedHashMap<Class<? extends StatefulObject>, Map<String, Multimap<String, DecisionCallback>>>());

    protected Object invoke(Method m, Object o, Object... args) {
//        log.finest("Invoking:" + m.getDeclaringClass().getSimpleName() + "." + m.getName() + " on (" + o + ") w/" + (args == null ? "" : Arrays.toString(args)));
        Object invoke = null;
        final boolean accessible = m.isAccessible();
//        synchronized (o == null ? m : o) {
        try {
            if (!accessible) {
                m.setAccessible(true);
            }
            invoke = m.invoke(o, args);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getTargetException());
        } finally {
//            m.setAccessible(accessible);
        }
//        }
        return invoke;
    }

    public void addDecision(StatefulObject st, StateListener sl, String fromState, String toState, DecisionCallback cbk) {
//        log.finest("Adding decision: " + st + " " + fromState + " " + toState);
        synchronized (instanceDecCallbackMap) {
            if (!instanceDecCallbackMap.containsKey(st)) {
                instanceDecCallbackMap.put(st, new WeakHashMap<StateListener, Map<String, Multimap<String, DecisionCallback>>>());
            }
            if (!instanceDecCallbackMap.get(st).containsKey(sl)) {
                instanceDecCallbackMap.get(st).put(sl, new LinkedHashMap<String, Multimap<String, DecisionCallback>>());
            }
            if (!instanceDecCallbackMap.get(st).get(sl).containsKey(fromState)) {
                Multimap<String, DecisionCallback> hm = LinkedHashMultimap.create();
                instanceDecCallbackMap.get(st).get(sl).put(fromState, hm);
            }
            instanceDecCallbackMap.get(st).get(sl).get(fromState).put(toState, cbk);
        }
    }

    public void addDecision(Class<? extends StatefulObject> stClz, String fromState, String toState, DecisionCallback cbk) {
//        log.finest("Adding decision: " + stClz + " " + fromState + " " + toState);
        synchronized (classDecCallbackMap) {
            if (!classDecCallbackMap.containsKey(stClz)) {
                final Map<String, Multimap<String, DecisionCallback>> hm = new LinkedHashMap<String, Multimap<String, DecisionCallback>>();
                classDecCallbackMap.put(stClz, hm);
            }
            if (!classDecCallbackMap.get(stClz).containsKey(fromState)) {
                Multimap<String, DecisionCallback> hm = LinkedHashMultimap.create();
                classDecCallbackMap.get(stClz).put(fromState, hm);
            }
            classDecCallbackMap.get(stClz).get(fromState).put(toState, cbk);
        }
    }

    public void addPostTransition(StatefulObject st, StateListener sl, String fromState, String toState, TransitionCallback cbk) {
//        log.finest("Adding postTransition: " + st + " " + fromState + " " + toState);
        synchronized (instancePostTrCallbackMap) {
            if (!instancePostTrCallbackMap.containsKey(st)) {
                instancePostTrCallbackMap.put(st, new WeakHashMap<StateListener, Map<String, Multimap<String, TransitionCallback>>>());
            }
            if (!instancePostTrCallbackMap.get(st).containsKey(sl)) {
                instancePostTrCallbackMap.get(st).put(sl, new LinkedHashMap<String, Multimap<String, TransitionCallback>>());
            }
            if (!instancePostTrCallbackMap.get(st).get(sl).containsKey(fromState)) {
                Multimap<String, TransitionCallback> hm = LinkedHashMultimap.create();
                instancePostTrCallbackMap.get(st).get(sl).put(fromState, hm);
            }
            instancePostTrCallbackMap.get(st).get(sl).get(fromState).put(toState, cbk);
        }
    }

    public void addPostTransition(Class<? extends StatefulObject> stClz, String fromState, String toState, TransitionCallback cbk) {
//        log.finest("Adding postTransition: " + stClz + " " + fromState + " " + toState);
        synchronized (classPostTrCallbackMap) {
            if (!classPostTrCallbackMap.containsKey(stClz)) {
                final Map<String, Multimap<String, TransitionCallback>> hm = new LinkedHashMap<String, Multimap<String, TransitionCallback>>();
                classPostTrCallbackMap.put(stClz, hm);
            }
            if (!classPostTrCallbackMap.get(stClz).containsKey(fromState)) {
                Multimap<String, TransitionCallback> hm = LinkedHashMultimap.create();
                classPostTrCallbackMap.get(stClz).put(fromState, hm);
            }
            classPostTrCallbackMap.get(stClz).get(fromState).put(toState, cbk);
        }
    }

    public void addPreTransition(StatefulObject st, StateListener sl, String fromState, String toState, TransitionCallback cbk) {
//        log.finest("Adding preTransition: " + st + " " + fromState + " " + toState);
        synchronized (instancePreTrCallbackMap) {
            if (!instancePreTrCallbackMap.containsKey(st)) {
                instancePreTrCallbackMap.put(st, new WeakHashMap<StateListener, Map<String, Multimap<String, TransitionCallback>>>());
            }
            if (!instancePreTrCallbackMap.get(st).containsKey(sl)) {
                instancePreTrCallbackMap.get(st).put(sl, new LinkedHashMap<String, Multimap<String, TransitionCallback>>());
            }
            if (!instancePreTrCallbackMap.get(st).get(sl).containsKey(fromState)) {
                Multimap<String, TransitionCallback> hm = LinkedHashMultimap.create();
                instancePreTrCallbackMap.get(st).get(sl).put(fromState, hm);
            }
            instancePreTrCallbackMap.get(st).get(sl).get(fromState).put(toState, cbk);
        }
    }

    public void addPreTransition(Class<? extends StatefulObject> stClz, String fromState, String toState, TransitionCallback cbk) {
//        log.finest("Adding preTransition: " + stClz + " " + fromState + " " + toState);
        synchronized (classPreTrCallbackMap) {
            if (!classPreTrCallbackMap.containsKey(stClz)) {
                final Map<String, Multimap<String, TransitionCallback>> hm = new LinkedHashMap<String, Multimap<String, TransitionCallback>>();
                classPreTrCallbackMap.put(stClz, hm);
            }
            if (!classPreTrCallbackMap.get(stClz).containsKey(fromState)) {
                Multimap<String, TransitionCallback> hm = LinkedHashMultimap.create();
                classPreTrCallbackMap.get(stClz).put(fromState, hm);
            }
            classPreTrCallbackMap.get(stClz).get(fromState).put(toState, cbk);
        }
    }

    public String getState(StatefulObject st) {
        synchronized (stateRegistry) {
            return stateRegistry.get(st);
        }
    }

    public void reset() {
        synchronized (instancePreTrCallbackMap) {
            instancePreTrCallbackMap.clear();
        }
        synchronized (instancePostTrCallbackMap) {
            instancePostTrCallbackMap.clear();
        }
        synchronized (instanceDecCallbackMap) {
            instanceDecCallbackMap.clear();
        }
        synchronized (classPreTrCallbackMap) {
            classPreTrCallbackMap.clear();
        }
        synchronized (classPostTrCallbackMap) {
            classPostTrCallbackMap.clear();
        }
        synchronized (classDecCallbackMap) {
            classDecCallbackMap.clear();
        }
        synchronized (stateRegistry) {
            stateRegistry.clear();
        }
    }

    public String setState(StatefulObject st, String toState) {
        String prev;
        synchronized (stateRegistry) {
            prev = stateRegistry.put(st, toState);
        }
        return prev;
    }

    public void unregisterListener(StateListener sl) {
        synchronized (instanceDecCallbackMap) {
            for (Map<StateListener, Map<String, Multimap<String, DecisionCallback>>> whm : (instanceDecCallbackMap.values())) {
                whm.remove(sl);
            }
        }
        synchronized (instancePostTrCallbackMap) {
            for (Map<StateListener, Map<String, Multimap<String, TransitionCallback>>> whm : (instancePostTrCallbackMap.values())) {
                whm.remove(sl);
            }
        }
        synchronized (instancePreTrCallbackMap) {
            for (Map<StateListener, Map<String, Multimap<String, TransitionCallback>>> whm : (instancePreTrCallbackMap.values())) {
                whm.remove(sl);
            }
        }
    }

    public void unregister(StatefulObject st) {
        synchronized (instanceDecCallbackMap) {
            instanceDecCallbackMap.remove(st);
        }
        synchronized (instancePreTrCallbackMap) {
            instancePreTrCallbackMap.remove(st);
        }
        synchronized (instancePreTrCallbackMap) {
            instancePostTrCallbackMap.remove(st);
        }
    }
}
