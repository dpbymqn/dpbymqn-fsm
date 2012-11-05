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

import com.dpbymqn.fsm.ann.PostTransition;
import com.dpbymqn.fsm.ann.OnDecision;
import com.dpbymqn.fsm.ann.PreTransition;
import com.dpbymqn.fsm.StateListener;
import com.dpbymqn.fsm.StatefulObject;
import com.dpbymqn.fsm.lazy.LazySingleKeyMap;
import com.dpbymqn.fsm.lazy.LazyWeakMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import org.apache.commons.lang.StringUtils;


/*
 * TODO
 *
 * -- take care of calling the default states (marked by null)
 *
 * add more callback method signatures
 *
 * support for classes implementing both StatedObject and StateListener (should
 * not pass self as parameter)
 *
 * -- separate callback method branching from runtime (execution time) to
 * registration time
 *
 * -- plural pre, post and decision
 *
 * -- decision
 *
 * avoid duplicate registrations (especially for static registrators) with a
 * composit key
 *
 * -- pre and post callback implementation
 */
/**
 *
 * @author dpbymqn
 */
public class FsmManager extends FsmManagerBase {

    LazySingleKeyMap<Class<? extends StatefulObject>, Collection<Method>> lazyAllMethods = new LazySingleKeyMap<Class<? extends StatefulObject>, Collection<Method>>() {
        @Override
        protected Collection<Method> generate(Class<? extends StatefulObject> clzl) {
            final ArrayList<Method> res = new ArrayList<Method>();
            final ArrayList<Method> mls = new ArrayList<Method>(Arrays.asList(clzl.getDeclaredMethods()));
            Collections.sort(mls, new Comparator<Method>() {
                @Override
                public int compare(Method o1, Method o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            res.addAll(mls);
            if (!Object.class.equals(clzl) && clzl.getSuperclass() != null) {
                final Collection<Method> superMethods = lazyAllMethods.get((Class<? extends StatefulObject>) clzl.getSuperclass());
                res.addAll(superMethods);
            }
            for (Class<?> clzi : clzl.getInterfaces()) {
                if (StateListener.class.isAssignableFrom(clzi)) {
                    final Collection<Method> interfaceMethods = lazyAllMethods.get((Class<? extends StatefulObject>) clzi);
                    res.addAll(interfaceMethods);
                }
            }
            return res;
        }
    };
    LazySingleKeyMap<Class<? extends StatefulObject>, Multimap<PreTransition, Method>> lazyPreMethods = new LazySingleKeyMap<Class<? extends StatefulObject>, Multimap<PreTransition, Method>>() {
        @Override
        protected Multimap<PreTransition, Method> generate(Class<? extends StatefulObject> clzl) {
            final Multimap<PreTransition, Method> res = LinkedHashMultimap.create();
            for (Method m : lazyAllMethods.get(clzl)) {
                if (m.isAnnotationPresent(PreTransition.class)) {
                    res.put(m.getAnnotation(PreTransition.class), m);
                }
                if (m.isAnnotationPresent(PreTransition.List.class)) {
                    for (PreTransition pr : m.getAnnotation(PreTransition.List.class).value()) {
                        res.put(pr, m);
                    }
                }
            }
            return res;
        }
    };
    LazySingleKeyMap<Class<? extends StatefulObject>, Multimap<PostTransition, Method>> lazyPostMethods = new LazySingleKeyMap<Class<? extends StatefulObject>, Multimap<PostTransition, Method>>() {
        @Override
        protected Multimap<PostTransition, Method> generate(Class<? extends StatefulObject> clzl) {
            final Multimap<PostTransition, Method> res = LinkedHashMultimap.create();
            for (Method m : lazyAllMethods.get(clzl)) {
                if (m.isAnnotationPresent(PostTransition.class)) {
                    res.put(m.getAnnotation(PostTransition.class), m);
                }
                if (m.isAnnotationPresent(PostTransition.List.class)) {
                    for (PostTransition pr : m.getAnnotation(PostTransition.List.class).value()) {
                        res.put(pr, m);
                    }
                }
            }
            return res;
        }
    };
    LazySingleKeyMap<Class<? extends StatefulObject>, Multimap<OnDecision, Method>> lazyDecisionMethods = new LazySingleKeyMap<Class<? extends StatefulObject>, Multimap<OnDecision, Method>>() {
        @Override
        protected Multimap<OnDecision, Method> generate(Class<? extends StatefulObject> clzl) {
            final Multimap<OnDecision, Method> res = LinkedHashMultimap.create();
            for (Method m : lazyAllMethods.get(clzl)) {
                if (m.isAnnotationPresent(OnDecision.class)) {
                    res.put(m.getAnnotation(OnDecision.class), m);
                }
                if (m.isAnnotationPresent(OnDecision.List.class)) {
                    for (OnDecision pr : m.getAnnotation(OnDecision.List.class).value()) {
                        res.put(pr, m);
                    }
                }
            }
            return res;
        }
    };
    LazySingleKeyMap<Class<? extends StateListener>, Boolean> lazyStLiClzReg = new LazySingleKeyMap<Class<? extends StateListener>, Boolean>() {
        @Override
        protected Boolean generate(Class<? extends StateListener> clz) {
            register(clz, null, StatefulObject.class.isAssignableFrom(clz) ? (Class<? extends StatefulObject>) clz : null, null);
            return true;
        }
    };
    LazyWeakMap<StateListener, Boolean> lazyStLiInstReg = new LazyWeakMap<StateListener, Boolean>() {
        @Override
        protected Boolean generate(StateListener key) {
            registerListener(key);
            return true;
        }
    };
    LazyWeakMap<StatefulObject, Boolean> lazyStObjInstReg = new LazyWeakMap<StatefulObject, Boolean>() {
        @Override
        protected Boolean generate(StatefulObject key) {
            if (key instanceof StateListener) {
                lazyStLiInstReg.get((StateListener) key);
            }
            return true;
        }
    };

    @Override
    public void reset() {
        super.reset();
        lazyStLiClzReg.clear();
        lazyStLiInstReg.clear();
        lazyStObjInstReg.clear();
    }

    @Override
    public String getState(StatefulObject st) {
        return super.getState(st);
    }

    /**
     * register static-to-static calls
     *
     * @param clzLst
     * @param stClz
     */
    public void register(Class< ? extends StateListener> clzLst, final StateListener sl, Class<? extends StatefulObject> stClz, StatefulObject st) {
        assert clzLst != null || sl != null;
        if (clzLst == null && sl == null) {
            throw new UnsupportedOperationException();
        }
//        Class<? extends StateListener> clzl = clzLst;
        Class<? extends StateListener> clzl = clzLst;
        if (clzl == null) {
            clzl = sl.getClass();
        }
//        log.finest("Register ( " + clzl + " / " + sl + " / " + stClz + " / " + st + " )");
        for (Map.Entry<PreTransition, Method> e : lazyPreMethods.get((Class<? extends StatefulObject>) clzl).entries()) {
            final Method m = e.getValue();
            final PreTransition pre = e.getKey();
            onPreTransition(m, pre, stClz, clzLst, sl, st);
        }
        for (Map.Entry<PostTransition, Method> e : lazyPostMethods.get((Class<? extends StatefulObject>) clzl).entries()) {
            final Method m = e.getValue();
            final PostTransition a = e.getKey();
            onPostTransition(m, a, stClz, clzLst, sl, st);
        }
        for (Map.Entry<OnDecision, Method> e : lazyDecisionMethods.get((Class<? extends StatefulObject>) clzl).entries()) {
            final Method m = e.getValue();
            final OnDecision a = e.getKey();
            onDecision(m, a, stClz, clzLst, sl, st);
        }
//        if (getState(st) != null) {
//            log.info("Must invoke the newly registered listener...");
//            transitAtomic(st, getState(st));
//        }
    }

    private void onDecision(final Method m, final OnDecision a, Class<? extends StatefulObject> stObjectClz,
            Class<? extends StateListener> clzLst, StateListener sl, StatefulObject st) {
        if (!testDeclaredAndActualClassConformity(m, a.smClass(), st, sl, stObjectClz, clzLst)) {
            return;
        }
        final Class<? extends StatefulObject> stcls = StatefulObject.class.equals(a.smClass()) ? stObjectClz : a.smClass();
        final String prev = StringUtils.isEmpty(a.prev()) ? null : a.prev();
        final String next = StringUtils.isEmpty(a.next()) ? null : a.next();
        final WeakReference<StateListener> slRef = new WeakReference<StateListener>(sl);
        if (String.class.equals(m.getReturnType())) {
            if (m.getParameterTypes() != null) {
                switch (m.getParameterTypes().length) {
                    case 1:
                        if (String.class.equals(m.getParameterTypes()[0])) {
                            regDecisionCbk(stcls, st, sl, prev, next, new DecisionCallback() {
                                @Override
                                public Boolean query(StatefulObject sm, String fromState, String toState) {
                                    return null;
                                }

                                @Override
                                public String query(StatefulObject sm, String fromState) {
                                    return (String) invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), fromState);
                                }
                            });
                        } else if (StatefulObject.class.isAssignableFrom(m.getParameterTypes()[0])) {
                            regDecisionCbk(stcls, st, sl, prev, next, new DecisionCallback() {
                                @Override
                                public Boolean query(StatefulObject sm, String fromState, String toState) {
                                    return null;
                                }

                                @Override
                                public String query(StatefulObject sm, String fromState) {
                                    return (String) invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), sm);
                                }
                            });

                        }
                        break;
                    case 2:
                        regDecisionCbk(stcls, st, sl, prev, next, new DecisionCallback() {
                            @Override
                            public Boolean query(StatefulObject sm, String fromState, String toState) {
                                return null;
                            }

                            @Override
                            public String query(StatefulObject sm, String fromState) {

                                return (String) invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), sm, fromState);
                            }
                        });
                        break;
                }
            } else {
                regDecisionCbk(stcls, st, sl, prev, next, new DecisionCallback() {
                    @Override
                    public Boolean query(StatefulObject sm, String fromState, String toState) {
                        return null;
                    }

                    @Override
                    public String query(StatefulObject sm, String fromState) {
                        return (String) invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get());
                    }
                });
            }
        } else if (Boolean.class.equals(m.getReturnType()) || boolean.class.equals(m.getReturnType())) {

            if (m.getParameterTypes() != null) {
                switch (m.getParameterTypes().length) {
                    case 1:
                        if (String.class.equals(m.getParameterTypes()[0])) {
                            regDecisionCbk(stcls, st, sl, prev, next, new DecisionCallback() {
                                @Override
                                public Boolean query(StatefulObject sm, String fromState, String toState) {
                                    return (Boolean) invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), toState);
                                }

                                @Override
                                public String query(StatefulObject sm, String fromState) {
                                    return null;
                                }
                            });
                        } else if (StatefulObject.class.isAssignableFrom(m.getParameterTypes()[0])) {
                            regDecisionCbk(stcls, st, sl, prev, next, new DecisionCallback() {
                                @Override
                                public Boolean query(StatefulObject sm, String fromState, String toState) {
                                    return (Boolean) invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), sm);
                                }

                                @Override
                                public String query(StatefulObject sm, String fromState) {
                                    return null;
                                }
                            });
                        }
                        break;
                    case 2:
                        if (String.class.equals(m.getParameterTypes()[0])) {
                            regDecisionCbk(stcls, st, sl, prev, next, new DecisionCallback() {
                                @Override
                                public Boolean query(StatefulObject sm, String fromState, String toState) {
                                    return (Boolean) invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), fromState, toState);

                                }

                                @Override
                                public String query(StatefulObject sm, String fromState) {
                                    return null;
                                }
                            });
                        } else if (StatefulObject.class.isAssignableFrom(m.getParameterTypes()[0])) {
                            regDecisionCbk(stcls, st, sl, prev, next, new DecisionCallback() {
                                @Override
                                public Boolean query(StatefulObject sm, String fromState, String toState) {
                                    return (Boolean) invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), sm, toState);

                                }

                                @Override
                                public String query(StatefulObject sm, String fromState) {
                                    return null;
                                }
                            });
                        }
                        break;
                    case 3:
                        regDecisionCbk(stcls, st, sl, prev, next, new DecisionCallback() {
                            @Override
                            public Boolean query(StatefulObject sm, String fromState, String toState) {
                                return (Boolean) invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), sm, fromState, toState);
                            }

                            @Override
                            public String query(StatefulObject sm, String fromState) {
                                return null;
                            }
                        });
                        break;
                    default:
                        regDecisionCbk(stcls, st, sl, prev, next, new DecisionCallback() {
                            @Override
                            public Boolean query(StatefulObject sm, String fromState, String toState) {
                                return (Boolean) invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get());
                            }

                            @Override
                            public String query(StatefulObject sm, String fromState) {
                                return null;
                            }
                        });
                }
            }
        }
    }

    private boolean testDeclaredAndActualClassConformity(final Method m, Class<? extends StatefulObject> annClass, StatefulObject st, StateListener sl, Class<? extends StatefulObject> stObjectClz, Class<? extends StateListener> clzLst) {
        Class<? extends StatefulObject> declaredSOclz = StatefulObject.class.equals(annClass) ? null : annClass;
        if (m.getParameterTypes() != null && m.getParameterTypes().length > 0 && StatefulObject.class.isAssignableFrom(m.getParameterTypes()[0])) {
            if (StatefulObject.class.isAssignableFrom(m.getParameterTypes()[0])) {
                declaredSOclz = (Class<? extends StatefulObject>) m.getParameterTypes()[0];
            }
        }
        if (declaredSOclz == null) {
            if (st != null && sl != null && sl.equals(st)) {
                declaredSOclz = st.getClass();
            } else if (stObjectClz != null && clzLst != null && (stObjectClz.isAssignableFrom(clzLst) || clzLst.isAssignableFrom(stObjectClz))) {
                declaredSOclz = st.getClass();
            }
        }
        if (declaredSOclz != null) {
            if (st != null && !declaredSOclz.isAssignableFrom(st.getClass())) {
                return false;
            }
            if (stObjectClz != null && !declaredSOclz.isAssignableFrom(stObjectClz)) {
                return false;
            }
        }
        return true;
    }

    private void regDecisionCbk(Class<? extends StatefulObject> stcls, StatefulObject st, StateListener sl,
            String prev, String next, DecisionCallback cbk) {
        assert (cbk != null);
        if (stcls != null) {
            addDecision(stcls, prev, next, cbk);
        }
        if (st != null) {
            addDecision(st, sl, prev, next, cbk);
        }
    }

    private void onPreTransition(final Method m, final PreTransition a,
            Class<? extends StatefulObject> stObjectClz, Class<? extends StateListener> clzLst, StateListener sl, StatefulObject st) {
        if (!testDeclaredAndActualClassConformity(m, a.smClass(), st, sl, stObjectClz, clzLst)) {
            return;
        }

        final String prev = StringUtils.isEmpty(a.prev()) ? null : a.prev();
        final String next = StringUtils.isEmpty(a.value()) ? null : a.value();
        TransitionCallback cbk = invokeTransitor(m, sl);
        if (cbk != null) {
            if (stObjectClz != null) {
                addPreTransition(stObjectClz, prev, next, cbk);
            }
            if (st != null) {
                addPreTransition(st, sl, prev, next, cbk);
            }
        }
    }

    private void onPostTransition(final Method m, final PostTransition a, Class<? extends StatefulObject> stObjectClz,
            Class<? extends StateListener> clzLst, StateListener sl, StatefulObject st) {
        if (!testDeclaredAndActualClassConformity(m, a.smClass(), st, sl, stObjectClz, clzLst)) {
            return;
        }

        final String prev = StringUtils.isEmpty(a.value()) ? null : a.value();
        final String next = StringUtils.isEmpty(a.next()) ? null : a.next();
        TransitionCallback cbk = invokeTransitor(m, sl);
        if (cbk != null) {
            if (stObjectClz != null) {
                addPostTransition(stObjectClz, prev, next, cbk);
            }
            if (st != null) {
                addPostTransition(st, sl, prev, next, cbk);
            }
        }
    }

    private TransitionCallback invokeTransitor(final Method m, StateListener sl) {
        final WeakReference<StateListener> slRef = new WeakReference<StateListener>(sl);
        final TransitionCallback cbk = new TransitionCallback() {
            @Override
            public void onTransition(StatefulObject st, String fromState, String toState) {
                if (m.getParameterTypes() != null) {
                    // many different types and possibilities
                    switch (m.getParameterTypes().length) {
                        case 0:
                            invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get());
                            break;
                        case 1:
                            if (String.class.equals(m.getParameterTypes()[0])) {
                                if (fromState != null) {
                                    invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), toState);
                                } else {
                                    invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), fromState);
                                }
                            } else if (StatefulObject.class.isAssignableFrom(m.getParameterTypes()[0])) {
                                invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), st);
                            } else {
//                                invoke(m, Modifier.isStatic(m.getModifiers()) ? null : sl, st);
                                throw new UnsupportedOperationException("Single parameter, but not recognized:" + m.getParameterTypes()[0].getSimpleName());
                            }
                            break;
                        case 2:
                            if (String.class.equals(m.getParameterTypes()[0])) {
                                invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), fromState, toState);
                            } else if (StatefulObject.class.isAssignableFrom(m.getParameterTypes()[0])) {
                                if (fromState != null) {
                                    invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), st, toState);
                                } else {
                                    invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), st, fromState);
                                }
                            }
                            break;
                        case 3:
                            // format: void method( Stated st, String fromState, String toState )
                            invoke(m, Modifier.isStatic(m.getModifiers()) ? null : slRef.get(), st, fromState, toState);
                            break;
                    }
                }
            }
        };
        return cbk;
    }

    /**
     * This registers a listener to a stateful object instance.
     *
     * @param clzLst
     * @param st
     */
    public void registerListener(StateListener clzLst, StatefulObject st) {
        assert clzLst != null;
        assert st != null;
//        register(clzLst.getClass(), clzLst, st.getClass(), st);
        register(null, clzLst, null, st);
    }

    public void registerListener(StateListener clzLst, Class<? extends StatefulObject> stClz) {
        assert clzLst != null;
        register(clzLst.getClass(), clzLst, null, null);
    }

    public void registerListener(StateListener clzLst) {
        assert clzLst != null;
        if (clzLst instanceof StatefulObject) {
            register(clzLst.getClass(), clzLst, ((StatefulObject) clzLst).getClass(), (StatefulObject) clzLst);
        } else {
            register(clzLst.getClass(), clzLst, null, null);
        }
    }

    public String changeState(StatefulObject st, String toState) {
        String nextState = toState;
        do {
            nextState = transitAtomic(st, nextState);
        } while (toState != null && nextState != null && !toState.equals(nextState));
        return nextState;
    }
    private LazySingleKeyMap<Class<?>, Collection<Class<?>>> lazySuper = new LazySingleKeyMap<Class<?>, Collection<Class<?>>>() {
        @Override
        protected Collection<Class<?>> generate(Class<?> key) {
            Set<Class<?>> r = Sets.newHashSet();
            for (Class clz = key; !Object.class.equals(clz) && StatefulObject.class.isAssignableFrom(clz); clz = clz.getSuperclass()) {
                r.add(clz);
                for (Class clzi : clz.getInterfaces()) {
                    if (StatefulObject.class.isAssignableFrom(clzi)) {
                        r.add(clzi);
                    }
                }
            }
            return r;
        }
    };

    private String transitAtomic(StatefulObject st, String toState) {
        String currentState = getState(st);
        String nextState = toState;
        // do pre-callbacks
        Collection<TransitionCallback> preCallbacks = new ArrayList<TransitionCallback>();
        synchronized (classPreTrCallbackMap) {
            for (Class<?> clz : lazySuper.get(st.getClass())) {
                final Map<String, Multimap<String, TransitionCallback>> clzPreStateFromMap = classPreTrCallbackMap.get(clz);
                if (clzPreStateFromMap != null) {
                    if (clzPreStateFromMap.get(null) != null || clzPreStateFromMap.get(currentState) != null) {
                        Multimap<String, TransitionCallback> clzStateToMap = LinkedHashMultimap.create();
                        Multimap<String, TransitionCallback> clzStateToMap1 = clzPreStateFromMap.get(currentState);
                        Multimap<String, TransitionCallback> clzStateToMap0 = clzPreStateFromMap.get(null);
                        if (clzStateToMap1 != null) {
                            clzStateToMap.putAll(clzStateToMap1);
                        }
                        if (clzStateToMap0 != null) {
                            clzStateToMap.putAll(clzStateToMap0);
                        }
                        if (clzStateToMap.containsKey(nextState) && clzStateToMap.get(nextState) != null) {
                            preCallbacks.addAll(clzStateToMap.get(nextState));
                        }
                        if (clzStateToMap.get(null) != null) {
                            preCallbacks.addAll(clzStateToMap.get(null));
                        }
                    }
                }
            }
        }
        synchronized (instancePreTrCallbackMap) {
            if (instancePreTrCallbackMap.containsKey(st)) {
                for (final Map<String, Multimap<String, TransitionCallback>> instPreStateFromMap : instancePreTrCallbackMap.get(st).values()) {
                    if (instPreStateFromMap != null) {
                        if (instPreStateFromMap.get(null) != null || instPreStateFromMap.get(currentState) != null) {
                            Multimap<String, TransitionCallback> instStateToMap = LinkedHashMultimap.create();
                            Multimap<String, TransitionCallback> instStateToMap1 = instPreStateFromMap.get(currentState);
                            Multimap<String, TransitionCallback> instStateToMap0 = instPreStateFromMap.get(null);
                            if (instStateToMap1 != null) {
                                instStateToMap.putAll(instStateToMap1);
                            }
                            if (instStateToMap0 != null) {
                                instStateToMap.putAll(instStateToMap0);
                            }
                            if (instStateToMap.containsKey(nextState) && instStateToMap.get(nextState) != null) {
                                preCallbacks.addAll(instStateToMap.get(nextState));
                            }
                            if (instStateToMap.get(null) != null) {
                                preCallbacks.addAll(instStateToMap.get(null));
                            }
                        }
                    }
                }
            }
        }
        // - call pre callbacks 
        // this "if" would prevent calling Pre methods when the transition is triggered by the newly registered listener(s)
//        if (currentState == null || !currentState.equals(nextState)) {
        for (TransitionCallback trCallback : preCallbacks) {
            trCallback.onTransition(st, currentState, nextState);
        }
//        }
        // move to next state

        setState(st, nextState);
        // post-callbacks
        // - collect post callbacks
        Collection<TransitionCallback> postCallbacks = new ArrayList<TransitionCallback>();
        synchronized (classPostTrCallbackMap) {
            for (Class<?> clz : lazySuper.get(st.getClass())) {
                final Map<String, Multimap<String, TransitionCallback>> clzPostStateFromMap = classPostTrCallbackMap.get(clz);
                if (clzPostStateFromMap != null) {
                    if (clzPostStateFromMap.get(null) != null || clzPostStateFromMap.get(currentState) != null) {
                        Multimap<String, TransitionCallback> clzStateToMap = LinkedHashMultimap.create();
                        Multimap<String, TransitionCallback> clzStateToMap1 = clzPostStateFromMap.get(currentState);
                        Multimap<String, TransitionCallback> clzStateToMap0 = clzPostStateFromMap.get(null);
                        if (clzStateToMap1 != null) {
                            clzStateToMap.putAll(clzStateToMap1);
                        }
                        if (clzStateToMap0 != null) {
                            clzStateToMap.putAll(clzStateToMap0);
                        }
                        if (clzStateToMap.containsKey(nextState) && clzStateToMap.get(nextState) != null) {
                            postCallbacks.addAll(clzStateToMap.get(nextState));
                        }
                        if (clzStateToMap.get(null) != null) {
                            postCallbacks.addAll(clzStateToMap.get(null));
                        }
                    }
                }
            }
        }
        synchronized (instancePostTrCallbackMap) {
            if (instancePostTrCallbackMap.containsKey(st)) {
                for (final Map<String, Multimap<String, TransitionCallback>> instPostStateFromMap : instancePostTrCallbackMap.get(st).values()) {
                    if (instPostStateFromMap != null) {
                        if (instPostStateFromMap.get(null) != null || instPostStateFromMap.get(currentState) != null) {
                            Multimap<String, TransitionCallback> instStateToMap = LinkedHashMultimap.create();
                            Multimap<String, TransitionCallback> instStateToMap1 = instPostStateFromMap.get(currentState);
                            Multimap<String, TransitionCallback> instStateToMap0 = instPostStateFromMap.get(null);
                            if (instStateToMap1 != null) {
                                instStateToMap.putAll(instStateToMap1);
                            }
                            if (instStateToMap0 != null) {
                                instStateToMap.putAll(instStateToMap0);
                            }
                            if (instStateToMap.containsKey(nextState) && instStateToMap.get(nextState) != null) {
                                postCallbacks.addAll(instStateToMap.get(nextState));
                            }
                            if (instStateToMap.get(null) != null) {
                                postCallbacks.addAll(instStateToMap.get(null));
                            }
                        }
                    }
                }
            }
        }
        // - call post callbacks
        for (TransitionCallback trCallback : postCallbacks) {
            trCallback.onTransition(st, currentState, nextState);
        }
        // check decisions
        Set<String> decisions = new HashSet<String>();
        // collect decision callbacks
        Multimap<String, DecisionCallback> possibleStates = HashMultimap.create();
        synchronized (classDecCallbackMap) {
            for (Class<?> clz : lazySuper.get(st.getClass())) {
                final Map<String, Multimap<String, DecisionCallback>> clzDecStateFromMap = classDecCallbackMap.get(clz);
                if (clzDecStateFromMap != null) {
                    if (clzDecStateFromMap.get(null) != null || clzDecStateFromMap.get(nextState) != null) {
                        Multimap<String, DecisionCallback> clzStateToMap1 = clzDecStateFromMap.get(nextState);
                        Multimap<String, DecisionCallback> clzStateToMap0 = clzDecStateFromMap.get(null);
                        if (clzStateToMap1 != null) {
                            possibleStates.putAll(clzStateToMap1);
                        }
                        if (clzStateToMap0 != null) {
                            possibleStates.putAll(clzStateToMap0);
                        }
                    }
                }
            }
        }
        synchronized (instanceDecCallbackMap) {
            if (instanceDecCallbackMap.containsKey(st)) {
                for (final Map<String, Multimap<String, DecisionCallback>> instDecStateFromMap : instanceDecCallbackMap.get(st).values()) {
                    if (instDecStateFromMap != null) {
                        if (instDecStateFromMap.get(null) != null || instDecStateFromMap.get(nextState) != null) {
                            Multimap<String, DecisionCallback> instStateToMap1 = instDecStateFromMap.get(nextState);
                            Multimap<String, DecisionCallback> instStateToMap0 = instDecStateFromMap.get(null);
                            if (instStateToMap1 != null) {
                                possibleStates.putAll(instStateToMap1);
                            }
                            if (instStateToMap0 != null) {
                                possibleStates.putAll(instStateToMap0);
                            }
                        }
                    }
                }
            }
        }
        // - call post callbacks
        for (Map.Entry<String, DecisionCallback> e : possibleStates.entries()) {
            DecisionCallback decCallback = e.getValue();
            String intoState = e.getKey();
            String suggest = decCallback.query(st, nextState);
            if (suggest != null) {
                decisions.add(suggest);
            } else {
                if (intoState != null && !intoState.equals(nextState)) {
                    final Boolean query = decCallback.query(st, nextState, intoState);
                    if (query != null && query) {
                        decisions.add(intoState);
                    }
                }
            }
        }
        if (decisions.size() == 1) {
            return decisions.iterator().next();
        }
        return null;
    }
}
