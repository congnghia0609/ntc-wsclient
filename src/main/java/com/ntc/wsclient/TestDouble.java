/*
 * Copyright 2018 nghiatc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ntc.wsclient;

/**
 *
 * @author nghiatc
 * @since Feb 1, 2018
 */
public class TestDouble {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //Double2DoubleRBTreeMap dtree = new Double2DoubleRBTreeMap(DoubleComparators.NATURAL_COMPARATOR);
        
        double db = 1.222222222222222222222222222222222222222222222D;
        System.out.println("" + db);
        System.out.println("Double.SIZE: " + Double.SIZE); // 64
        System.out.println("Double.BYTES: " + Double.BYTES); // 8
        System.out.println("Double.MAX_VALUE: " + Double.MAX_VALUE); //1.7976931348623157E308
        System.out.println("Long.SIZE: " + Long.SIZE); // 64
        System.out.println("Long.BYTES: " + Long.BYTES); // 8
        System.out.println("Long.MAX_VALUE: " + Long.MAX_VALUE); //9223372036854775807 | 19 character --> 18
        
    }

}

























