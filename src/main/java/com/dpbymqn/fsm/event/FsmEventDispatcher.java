/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dpbymqn.fsm.event;

import com.dpbymqn.fsm.event.ann.OnEvent;
import com.dpbymqn.fsm.lazy.LazySingleKeyMap;
import com.dpbymqn.fsm.manager.FsmManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Method;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author dobyman
 */
@RequiredArgsConstructor
@Log
public class FsmEventDispatcher {

    private final static String OTHER_MARKER = "_______OTHER___765432";
    private final static String ALL_MARKER = "_______ALL___765432";
    @Getter
    private final FsmManager fsm;
    LazySingleKeyMap<Class<? extends StatefulEventListener>, Multimap<String, Method>> clzMap =
            new LazySingleKeyMap<Class<? extends StatefulEventListener>, Multimap<String, Method>>() {
                @Override
                protected Multimap<String, Method> generate(Class<? extends StatefulEventListener> key) {
                    Multimap<String, Method> res = HashMultimap.create();
                    for (Class<?> c = key; !Object.class.equals(c); c = c.getSuperclass()) {
                        for (Method m : c.getDeclaredMethods()) {
                            if (m.isAnnotationPresent(OnEvent.class)) {
                                assert m.getParameterTypes().length == 1;
                                final OnEvent ann = m.getAnnotation(OnEvent.class);
                                if (StringUtils.isEmpty(ann.value())) {
                                    if (ann.all()) {
                                        res.put(ALL_MARKER, m);
                                    } else if (ann.other()) {
                                        res.put(OTHER_MARKER, m);
                                    }
                                } else {
                                    res.put(ann.value(), m);
                                }
                            }
                        }
                    }
                    return res;
                }
            };
//    LazyWeakMap<StatefulEventListener, Multimap<String, Method>> instMap = new LazyWeakMap<StatefulEventListener, Multimap<String, Method>>() {
//        @Override
//        protected Multimap<String, Method> generate(StatefulEventListener key) {
//        }
//    };

    public <T> void fireEvent(StatefulEventListener obj, T event) {
        final String state = fsm.getState(obj);
        final Multimap<String, Method> mm = clzMap.get(obj.getClass());
        for (Method method : mm.get(ALL_MARKER)) {
            try {
                method.invoke(obj, event);
            } catch (Exception ex) {
                log.throwing(obj.getClass().getName(), method.getName(), ex);
            }
        }
        if (!mm.get(state).isEmpty()) {
            for (Method method : mm.get(state)) {
                try {
                    method.invoke(obj, event);
                } catch (Exception ex) {
                    log.throwing(obj.getClass().getName(), method.getName(), ex);
                }
            }
        } else {
            for (Method method : mm.get(OTHER_MARKER)) {
                try {
                    method.invoke(obj, event);
                } catch (Exception ex) {
                    log.throwing(obj.getClass().getName(), method.getName(), ex);
                }
            }
        }
//        for (Method method : instMap.get(obj).get(state)) {
//            try {
//                method.invoke(obj, event);
//            } catch (Exception ex) {
//                log.throwing(obj.getClass().getName(), method.getName(), ex);
//            }
//        }
    }
//    public void register(StatefulEventListener obj) {
//        register(obj.getClass());
//        final Multimap<String, Method> mp = instMap.get(obj);
//    }
//    public void unregister(StatefulEventListener obj) {
//        instMap.remove(obj);
//    }
}
