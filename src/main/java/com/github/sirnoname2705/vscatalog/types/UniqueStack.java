package com.github.sirnoname2705.vscatalog.types;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class UniqueStack<T> {
    private Stack<T> stack;
    private Set<T> set;

    public UniqueStack() {
        stack = new Stack<>();
        set = new HashSet<>();
    }

    public void pushAll(Iterable<T> items) {
        for (T item : items) {
            push(item);
        }
    }

    public void merge(UniqueStack<T> other) {
        pushAll(other.stack);
    }

    public void push(T item) {
        if (set.add(item)) {  // Fügt nur hinzu, wenn das Element nicht schon vorhanden ist
            stack.push(item);
        }
    }

    public T pop() {
        T item = stack.pop();
        set.remove(item);
        return item;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray() {
        return (T[]) stack.toArray();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }
}
