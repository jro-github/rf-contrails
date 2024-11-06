package de.tudresden.aerospace.contrails.Configuration.IO;

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

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a table with a dynamic number of rows and a name Used to store parameter tables.
 *
 * @param <T> The type of data elements
 */
public class DynamicTable<T> {
    private List<String> colNames;
    private List<List<T>> rows;
    private String name;

    /**
     * Creates the dynamic table. The table name as well as column names and data have to be initialized manually.
     */
    public DynamicTable() {
        colNames = new ArrayList<>();
        rows = new ArrayList<>();
        name = "";
    }

    /**
     * Creates the dynamic table with the given parameters.
     *
     * @param name The name of the table
     * @param colNames The names of the columns
     */
    public DynamicTable(String name, List<String> colNames) {
        this.name = name;
        this.colNames = colNames;
        rows = new ArrayList<>();
    }

    /**
     * Gets the name of the table.
     *
     * @return The name of the table as a {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the table to the given name.
     *
     * @param name The name to set as the table name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value stored at the given index pair.
     *
     * @param row The row index of the element
     * @param col The column index of the element
     * @return The value at the given index pair
     * @throws IndexOutOfBoundsException If the supplied index pair is not within the table's bounds
     */
    public T getValue(int row, int col) throws IndexOutOfBoundsException {
        if (row < 0 || row >= rows.size() || col < 0 || col >= colNames.size())
            throw new IndexOutOfBoundsException();
        return rows.get(row).get(col);
    }

    /**
     * Gets the column with the given column index.
     *
     * @param index The index of the column to get
     * @return The column values as a {@link List}
     * @throws IndexOutOfBoundsException If the supplied index is not within the table's bounds
     */
    public List<T> getColumn(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= colNames.size())
            throw new IndexOutOfBoundsException();

        List<T> res = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            res.add(rows.get(i).get(index));
        }

        return res;
    }

    /**
     * Gets a flattened representation of the table. This turns the two-dimensional table into a one-dimensional
     * list by concatenating rows. Only the specified columns will be included in the output list.
     *
     * @param columnStartIndex The column index starting from which (inclusive) columns will be included in the output
     * @return The {@link List} containing the concatenated table
     * @throws IndexOutOfBoundsException If the supplied index is not within the table's bounds
     */
    public List<T> getFlattened(int columnStartIndex) throws IndexOutOfBoundsException {
        if (columnStartIndex < 0 || columnStartIndex >= colNames.size())
            throw new IndexOutOfBoundsException();
        return getFlattened(columnStartIndex, colNames.size());
    }

    /**
     * Gets a flattened representation of the table. This turns the two-dimensional table into a one-dimensional
     * list by concatenating rows. Only the specified columns will be included in the output list.
     *
     * @param columnStartIndex The column index starting from which (inclusive) columns will be included in the output
     * @param columnEndIndex The column index up to which (non-inclusive) columns will be included in the output
     * @return The {@link List} containing the concatenated table
     * @throws IndexOutOfBoundsException If the supplied index pair is not within the table's bounds
     */
    public List<T> getFlattened(int columnStartIndex, int columnEndIndex) throws IndexOutOfBoundsException {
        if (columnStartIndex < 0 || columnStartIndex > colNames.size() ||
                columnEndIndex < columnStartIndex || columnEndIndex > colNames.size())
            throw new IndexOutOfBoundsException();

        List<T> res = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            for (int j = columnStartIndex; j < columnEndIndex; j++) {
                res.add(rows.get(i).get(j));
            }
        }
        return res;
    }

    /**
     * Gets the row at the given row index.
     *
     * @param index The index of the row to get
     * @return The row values as a {@link List}
     * @throws IndexOutOfBoundsException If the supplied index is not within the table's bounds
     */
    public List<T> getRow(int index)  throws IndexOutOfBoundsException {
        if (index < 0 || index >= rows.size())
            throw new IndexOutOfBoundsException();

        return rows.get(index);
    }

    /**
     * Gets the data stored in the two-dimensional table.
     *
     * @return The data as a nested {@link List}
     */
    public List<List<T>> getRows() {
        return rows;
    }

    /**
     * Sets the table data to the given data.
     *
     * @param rows The data to store in the table as a nested {@link List}
     */
    public void setRows(List<List<T>> rows) {
        this.rows = rows;
    }

    /**
     * Sets the table data to the given data.
     *
     * @param rows The data to store in the table as a two-dimensional array
     */
    public void setRows(T[][] rows) {
        List<List<T>> res = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            List<T> row = Arrays.asList(rows[i]);
            res.add(row);
        }
        this.rows = res;
    }

    /**
     * Adds a row to the table.
     *
     * @param row The row data stored in a {@link List}
     */
    public void addRow(List<T> row) {
        if (row.size() != colNames.size())
            throw new IllegalArgumentException("Length of row does not match the number of columns");
        rows.add(row);
    }

    /**
     * Gets the names of the table's columns.
     *
     * @return The names as a {@link List} of strings
     */
    public List<String> getColumnNames() {
        return colNames;
    }

    /**
     * Sets the column names to the given names.
     *
     * @param names The {@link List} of names to set as the column names
     */
    public void setColumnNames(List<String> names) {
        if (!colNames.isEmpty() && names.size() != colNames.size())
            throw new IllegalArgumentException("Length of names does not match the number of columns");
        colNames.addAll(names);
    }

    /**
     * Checks whether the table is empty.
     *
     * @return {@code true} if the table is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    /**
     * Checks whether the table is initialized. This is the case if column names have been set.
     *
     * @return {@code true} if the table is initialized, {@code false} otherwise
     */
    public boolean isInitialized() {
        return !colNames.isEmpty();
    }

    /**
     * Converts the table into a two-dimensional array.
     *
     * @param c The type of values inside the table
     * @return A nested two-dimensional array with the table's values of the given type
     */
    public T[][] toArray(Class<T> c) {
        @SuppressWarnings("unchecked")
        T[][] res = (T[][]) Array.newInstance(c, rows.size(), colNames.size());
        for (int i = 0; i < rows.size(); i++) {
            List<T> row = rows.get(i);
            for (int j = 0; j < row.size(); j++) {
                res[i][j] = row.get(j);
            }
        }
        return res;
    }

    /**
     * Multiplies the table's values by the given scalar. This does not modify the existing table and instead returns
     * a new table with the resulting values. This method may only be used if {@link T} is numeric.
     *
     * @param scalar The number to multiply the table values by
     * @return The resulting {@link DynamicTable} with the multiplied values
     */
    @SuppressWarnings("unchecked")
    public DynamicTable<Double> multiplyScalar(double scalar) {
        DynamicTable<Double> res = new DynamicTable<>();
        res.setName(this.name);
        res.setColumnNames(this.getColumnNames());
        for (int i = 0; i < rows.size(); i++) {
            List<T> row = rows.get(i);
            List<Double> newRow = new ArrayList<>();
            for (int j = 0; j < row.size(); j++) {
                newRow.add((Double)row.get(j) * scalar);
            }
            res.addRow(newRow);
        }

        return res;
    }

    // Adapted from https://stackoverflow.com/a/45490275

    /**
     * Utility function which transposes the given table, swapping rows and columns.
     * @param list The nested {@link List} to transpose
     * @return The transposed table as a nested {@link List}
     * @param <T> The type of values stored in the table
     */
    public static <T> List<List<T>> transpose(List<List<T>> list) {
        final int N = list.stream().mapToInt(l -> l.size()).max().orElse(-1);
        List<Iterator<T>> iterList = list.stream().map(it->it.iterator()).collect(Collectors.toList());
        return IntStream.range(0, N)
                .mapToObj(n -> iterList.stream()
                        .filter(it -> it.hasNext())
                        .map(m -> m.next())
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    /**
     * Transposes this table, swapping rows and columns. This does not modify the existing table and instead returns
     * a new table with the resulting values.
     *
     * @return The transposed {@link DynamicTable}
     */
    public DynamicTable<T> transpose() {
        DynamicTable<T> res = new DynamicTable<>();
        res.setName(this.name);
        res.setColumnNames(this.getColumnNames());
        res.setRows(DynamicTable.transpose(this.rows));
        return res;
    }

    /**
     * Writes the table to a file. Internally uses the {@link DynamicTable#toString(String)} method to convert the table
     * to a string, which is then written to the file.
     *
     * @param fwOut The {@link FileWriter} to use for writing the file
     * @param delimiter The sequence of characters used to separate values in the output
     */
    public void marshal(FileWriter fwOut, String delimiter) {
        try {
            fwOut.write(this.toString(delimiter));
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    /**
     * Converts the table to a string, which contains the table name, column names and data in separate rows.
     *
     * @param delimiter The sequence of characters used to separate values in the output
     * @return The string representation of the table
     */
    public String toString(String delimiter) {
        String res = "//" + name + "\n";
        res += String.join(delimiter, colNames) + "\n";

        for (List<T> row : rows) {
            String r = row.stream().map(Object::toString).collect(Collectors.joining(delimiter));
            res += r + "\n";
        }

        return res;
    }

    /**
     * Converts the table to a string, with whitespaces as value delimiters. Internally uses the
     * {@link DynamicTable#toString(String)} method.
     * @return The string representation of the table
     */
    public String toString() {
        return toString(" ");
    }
}
