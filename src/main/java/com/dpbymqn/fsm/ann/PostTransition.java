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
package com.dpbymqn.fsm.ann;

import com.dpbymqn.fsm.StatefulObject;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author dpbymqn Uses of PostTransition:
 *
 * @PostTransition("TX1") void myCheck1(StatefulObject1 so, String fromState,
 * String toState)
 *
 *
 * @PostTransition("TX1") public void myCheck2(StatefulObject2 so, String
 * fromState, String toState)
 *
 *
 * @PostTransition(value = "TX2", smClass = StatefulObject1.class) public void
 * myCheck3(String fromState, String toState)
 *
 *
 * @PostTransition void myCheck(StatefulObject so, String fromState, String
 * toState);
 *
 * @PostTransition(value = "TX2", smClass = StatefulObject2.class) public void
 * myCheck4(String fromState, String toState)
 *
 *
 * If this is an instance of StatefulObject, smClass and StatefulObject
 * parameter is assumed to be this as well.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PostTransition {

    String value() default "";

    String next() default "";

    Class<? extends StatefulObject> smClass() default StatefulObject.class;
}
