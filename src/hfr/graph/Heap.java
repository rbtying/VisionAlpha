/*******************************************************************************
 * Copyright 2013 Robert Ying, based on code by Ernesto Tapias
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package hfr.graph;

import java.util.ArrayList;

import DataStructures.Overflow;

/**
 * <p>
 * �berschrift:
 * </p>
 * <p>
 * Beschreibung:
 * </p>
 * <p>
 * Copyright: Copyright (c)
 * </p>
 * <p>
 * Organisation:
 * </p>
 * 
 * @author Ernesto Tapia Rodr�guez
 * @version 1.0
 */
public class Heap<T extends Comparable<T>> {
	/**
	 * Construct the binary heap.
	 */
	public Heap() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Construct the binary heap.
	 * 
	 * @param capacity
	 *            the capacity of the binary heap.
	 */
	public Heap(int capacity) {
		currentSize = 0;
		array = new ArrayList<T>(capacity + 1);
		for (int i = 0; i < capacity + 1; i++) {
			array.add(null);
		}
	}

	/**
	 * Insert into the priority queue, maintaining heap order. Duplicates are
	 * allowed.
	 * 
	 * @param x
	 *            the item to insert.
	 * @exception Overflow
	 *                if container is full.
	 */

	public void insert(T x) throws Exception {
		if (isFull())
			throw new Exception("Heap overflow!");

		currentSize++;
		array.set(currentSize, x);
		upHeap(currentSize);

		// Percolate up
		/*
		 * int hole = ++currentSize; for( ; hole > 1 && x.compareTo( array[ hole
		 * / 2 ] ) < 0; hole /= 2 ) array[ hole ] = array[ hole / 2 ]; array[
		 * hole ] = x;
		 */
	}

	/**
	 * Find the smallest item in the priority queue.
	 * 
	 * @return the smallest item, or null, if empty.
	 */
	public T findMin() {
		if (isEmpty())
			return null;
		return array.get(1);
	}

	/**
	 * Remove the smallest item from the priority queue.
	 * 
	 * @return the smallest item, or null, if empty.
	 */
	public T deleteMin() {
		if (isEmpty())
			return null;

		T minItem = findMin();
		array.set(1, array.get(currentSize--));
		downHeap(1);

		return minItem;
	}

	/**
	 * Establish heap order property from an arbitrary arrangement of items.
	 * Runs in linear time.
	 */
	public void buildHeap() {
		for (int i = currentSize / 2; i > 0; i--)
			downHeap(i);
	}

	/**
	 * Test if the priority queue is logically empty.
	 * 
	 * @return true if empty, false otherwise.
	 */
	public boolean isEmpty() {
		return currentSize == 0;
	}

	/**
	 * Test if the priority queue is logically full.
	 * 
	 * @return true if full, false otherwise.
	 */
	public boolean isFull() {
		return currentSize == array.size() - 1;
	}

	/**
	 * Make the priority queue logically empty.
	 */
	public void makeEmpty() {
		currentSize = 0;
	}

	private static final int DEFAULT_CAPACITY = 100;

	private int currentSize; // Number of elements in heap
	private ArrayList<T> array; // The heap array

	/**
	 * Internal method to percolate down in the heap.
	 * 
	 * @param hole
	 *            the index at which the percolate begins.
	 */

	public void downHeap(int hole) {
		int child;
		T tmp = array.get(hole);

		for (; hole * 2 <= currentSize; hole = child) {
			child = hole * 2;
			if (child != currentSize
					&& array.get(child + 1).compareTo(array.get(child)) < 0)
				child++;
			if (array.get(child).compareTo(tmp) < 0)
				array.set(hole, array.get(child));
			else
				break;
		}
		array.set(hole, tmp);
	}

	public void upHeap(int hole) {
		T x;

		x = array.get(hole);
		for (; hole > 1 && x.compareTo(array.get(hole / 2)) < 0; hole /= 2)
			array.set(hole, array.get(hole / 2));
		array.set(hole, x);
	}

	public T get(int i) {
		return array.get(i);
	}

	public int size() {
		return currentSize;
	}

	@Override
	public String toString() {
		String str = "[";

		for (int i = 1; i <= currentSize; i++) {
			str += "[" + i + "]=" + array.get(i);
			if (i != currentSize) {
				str += ", ";
			}

		}
		str += "]";

		return str;
	}

	// Test program
	public static void main(String[] args) {
		int numItems = 10000;
		Heap<Integer> h = new Heap<Integer>(numItems);
		int i = 37;

		try {
			for (i = 37; i != 0; i = (i + 37) % numItems)
				h.insert(new Integer(i));
			for (i = 1; i < numItems; i++)
				if (((Integer) (h.deleteMin())).intValue() != i)
					System.out.println("Oops! " + i);

			for (i = 37; i != 0; i = (i + 37) % numItems)
				h.insert(new Integer(i));
			h.insert(new Integer(0));
			i = 9999999;
			h.insert(new Integer(i));
			for (i = 1; i <= numItems; i++)
				if (((h.deleteMin())).intValue() != i)
					System.out.println("Oops! " + i + " ");
		} catch (Exception e) {
			System.out.println("Overflow (expected)! " + i);
		}
	}
}
