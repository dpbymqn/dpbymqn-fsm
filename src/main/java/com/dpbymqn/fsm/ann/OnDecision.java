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
 * if( {@code prev} == null ) -> called in any state.\n Otherwise it will be
 * called only in states "prev".\n if return value is boolean, then
 *
 * @next must be set\n if return value is String, then the return value will set
 * the next state. On null return value it doesn't change state
 *
 * @author dpbymqn
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnDecision {

    String prev() default "";

    String next();

    Class<? extends StatefulObject> smClass() default StatefulObject.class;

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface List {

        OnDecision[] value() default {};
    }
}
