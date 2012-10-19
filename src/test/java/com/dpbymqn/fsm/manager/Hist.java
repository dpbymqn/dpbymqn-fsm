/*
 * To change this template, choose Tools | Templates
 * and open the template in*
 */
package com.dpbymqn.fsm.manager;

/**
 *
 * @author dpbymqn
 */
public class Hist {
    StringBuilder sb = new StringBuilder();

    public void app(String s) {
        System.out.println("Hist: "+s);
        if (sb.length() != 0) {
            sb.append("_");
        }
        sb.append(s);
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public void clear() {
        sb = new StringBuilder();
    }
    
}
