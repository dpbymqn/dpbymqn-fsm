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
package com.dpbymqn.fsm.event.ann;

import com.dpbymqn.fsm.ann.*;
import com.dpbymqn.fsm.StatefulObject;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for methods which the FsmEventDispatcher should call.
 *
 * If value is empty, then every message will be dispatched to this method,
 * otherwise only the events which arrive when the object is in the given state.
 *
 * If otherwise is set to true, then the method will be called when no other
 * methods are invoked.
 *
 * More than one methods can be marked and they all will be called back, but the
 * order is undefined.
 *
 * @author dpbymqn
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnEvent {

    /**
     * the state name when this listener should be called.
     *
     * @return
     */
    String [] value() default "";

    /**
     * called in any event
     *
     * @return
     */
    boolean all() default false;

    /**
     * called if no other methods are registered.
     *
     * @return
     */
    boolean other() default false;
}
