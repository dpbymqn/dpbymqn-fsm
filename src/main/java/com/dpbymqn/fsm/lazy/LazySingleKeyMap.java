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
package com.dpbymqn.fsm.lazy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Almost like LazyMap, but the only key is typed
 *
 * http://en.wikipedia.org/wiki/Memoization
 *
 * @author dpbymqn
 */
public abstract class LazySingleKeyMap<K, T> implements Serializable {

    final Map<K, T> map = getBackingMap();

    /**
     * override this to use different type of backing map
     */
    protected Map<K, T> getBackingMap() {
        return new HashMap<K, T>();
    }

    public T get(K k) {
        synchronized (map) {
            if (!map.containsKey(k)) {
                final T t = generate(k);
                if (t != null) {
                    map.put(k, t);
                }
            }
        }
        return map.get(k);
    }

    public void clear() {
        map.clear();
    }

    public int size() {
        return map.size();
    }

    protected abstract T generate(K key);

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public void remove(K key) {
        map.remove(key);
    }
}
