package de.tudresden.aerospace.contrails.Utility;

/*-
 * #%L
 * RF-Contrails
 * %%
 * Copyright (C) 2024 Institute of Aerospace Engineering, TU Dresden
 * %%
 * Copyright 2,024 Institute of Aerospace Engineering, TU Dresden
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * #L%
 */

import neureka.Shape;
import neureka.Tensor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MathHelpers {
    /**
     * Private constructor to prevent creation of instance object.
     */
    private MathHelpers() {}

    /**
     * Compares two {@link Double} values with a threshold.
     *
     * @param v1 The first double value
     * @param v2 The second double value
     * @param epsilon The difference threshold under which both doubles are considered equal
     * @return {@code true} if both values are equal, {@code false} otherwise
     */
    public static boolean compareDouble(Double v1, Double v2, Double epsilon) {
        return Math.abs(v1 - v2) < epsilon;
    }

    /**
     * Returns the index of the minimum value in the list.
     *
     * @param list The {@link List} of {@link Number}s to find the minimum index of
     * @return The minimum index or {@code -1} if {@code list} is null or empty
     * @param <T> The type of list elements
     */
    public static <T extends Number & Comparable<T>> int argMin(List<T> list) {
        if (list == null || list.size() == 0)
            return -1;

        int index = 0;
        T v = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).compareTo(v) < 0) {
                v = list.get(i);
                index = i;
            }
        }
        return index;
    }

    /**
     * Flattens a nested list (2D array) to a list (1D array).
     *
     * @param nestedList The 2D list to flatten
     * @return The flattened {@link List}
     * @param <T> The type of list elements
     */
    public static <T> List<T> flatten(List<List<T>> nestedList) {
        List<T> res = new ArrayList<>();
        nestedList.forEach(res::addAll);
        return res;
    }

    /**
     * Flattens a nested 2D array to a 1D array. Requires all rows to have the same length.
     *
     * @param nestedArray The 2D list to flatten
     * @return The flattened {@link List}
     * @param <T> The type of the list elements
     */
    public static <T> T[] flatten(T[][] nestedArray, Class<T> c) {
        T[] res = (T[]) Array.newInstance(c, nestedArray.length * nestedArray[0].length);
        for (int i = 0; i < nestedArray.length; i++) {
            for (int j = 0; j < nestedArray[0].length; j++) {
                res[i * j] = nestedArray[i][j];
            }
        }
        return res;
    }

    /**
     * Creates a 1D tensor from the given list.
     *
     * @param list The {@link List} with numeric values
     * @return The {@link Tensor} with the given values. It's shape matches the size of the list
     * @param <T> A numeric type that can be stored in a {@link Tensor}
     */
    public static <T extends Number> Tensor<T> oneDimfromList(List<T> list) {
        Shape shape = Shape.of(list.size());
        return Tensor.of(shape, list);
    }

    /**
     * Creates a 2D tensor from the given table.
     *
     * @param nestedList A nested {@link List} representing a table of numeric values
     * @return The {@link Tensor} with the given values. It's shape matches the number of rows and columns of the table
     * @param <T> A numeric type that can be stored in a {@link Tensor}
     */
    public static <T extends Number> Tensor<T> twoDimfromList(List<List<T>> nestedList) {
        Shape shape = Shape.of(nestedList.size(), nestedList.get(0).size());
        return Tensor.of(shape, flatten(nestedList));
    }

    /**
     * Gets the sum of a double tensor's values.
     *
     * @param t The {@link Tensor} to sum up
     * @return The sum of all of {@code t}'s values
     */
    public static Double sum(Tensor<Double> t) {
        return t.sum().getDataAt(0);
    }

    /**
     * Stores a 2-tuple of {@link Comparable} values.
     *
     * @param <X> The first value of the tuple
     * @param <Y> The second value of the tuple
     */
    public static final class ComparableTuple<X extends Comparable, Y extends Comparable> implements Comparable<ComparableTuple<X, Y>> {
        public X x;
        public Y y;

        /**
         * Creates the comparable tuple with the given values.
         * @param x The first {@link Comparable} value of the tuple
         * @param y The second {@link Comparable} value of the tuple
         */
        public ComparableTuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        /**
         * This method checks if both values of this tuple are equal to the given tuple using
         * {@link java.lang.Object#equals(Object)} as specified by {@link Comparable}.
         *
         * @param o The tuple to compare the values to
         * @return {@code true} if the tuple values are equal, {@code false} otherwise
         */
        public boolean equals(ComparableTuple o) {
            return this.x.equals(o.x) && this.y.equals(o.y);
        }

        /**
         * Implements the {@link Comparable} interface. The comparison prioritizes x over y, therefore (1, 1) > (0, 2),
         * (1, 2) > (1, 1) and (2, 10) < (3, 0).
         *
         * @param o The {@link ComparableTuple} to compare to
         * @return {@code 0} if both values are equal, {@code 1} if the value of this tuple is greater than the value
         * of the given {@link ComparableTuple} and {@code -1} otherwise
         */
        @Override
        public int compareTo(ComparableTuple<X, Y> o) {
            if (this.x.compareTo(o.x) == 0 && this.y.compareTo(o.y) == 0)
                return 0;
            // Return GT if x is greater than other.X and use y as a tie-breaker if x is the same as other.x
            if (this.x.compareTo(o.x) > 0 || (this.x.compareTo(o.x) == 0 && this.y.compareTo(o.y) > 0))
                return 1;
            return -1;
        }
    }

    /**
     * Stores a simple N-tuple of arbitrary values of the same type.
     *
     * @param <T> The type of elements in the tuple
     */
    public static final class Tuple<T> {
        private List<T> elements;

        public Tuple(T... elements) {
            this.elements = Arrays.asList(elements);
        }

        public T get(int i) {
            return this.elements.get(i);
        }

        public T set(int i, T element) {
            return this.elements.set(i, element);
        }

        public int size() {
            return elements.size();
        }
    }

    /**
     * Slices a 2D array to keep a certain range of columns.
     *
     * @param array The 2D array to slice
     * @param startColumn The index of the first column to include (inclusive)
     * @param endColumn The index of the last column to include (exclusive)
     * @return The sliced array
     * @param <T> The type of elements in the array
     */
    public static <T> T[][] sliceTableColumns(T[][] array, int startColumn, int endColumn) {
        return Arrays.stream(array)
                .map(row -> Arrays.copyOfRange(row, startColumn, endColumn))
                .toArray(size -> Arrays.copyOf(array, size));
    }
}
