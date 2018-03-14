package com.github.kaeluka.cflat;

import com.github.kaeluka.cflat.annotations.Cflat;
import com.github.kaeluka.cflat.storage.Storage;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("unchecked")
@Cflat("*(next)->ok")
public class Sequence<T> extends AbstractList<T> implements Iterable<T>, Cloneable {
    public Storage<T> storage;
    private SequenceIdx tail = new SequenceIdx();

    public Sequence(final Storage<T> storage) {
        ArrayList x;
        this.storage = storage;
        tail.next_nth(storage.maxIdx());
    }

    @Override
    public boolean add(final T x) {
        storage = storage.set(tail.ok(), x);
        tail.next();
        return true;
    }

    @Override
    public boolean remove(final Object o) {
        int i = storage.find((T) o, tail.ok());
        if (i >= 0) {
            remove(i);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        storage = storage.emptyCopy();
        tail = new SequenceIdx();
    }

    @Override
    public int indexOf(Object x) {
        return storage.find((T)x, -1);
    }

    @Override
    public boolean contains(final Object o) {
        return storage.find((T)o, -1) != -1;
    }

    @Override
    public T get(int index) {
        if (index < tail.ok()) {
            return storage.get(index);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public T set(final int index, final T element) {
        final T ret = storage.get(index);
        storage = storage.set(index, element);
        return ret;
    }

    @Override
    public void add(final int index, final T element) {
        storage = storage.moveRange(index,index+1, size() - index);
        storage = storage.set(index, element);
        tail.next();
    }

    @Override
    public T remove(final int index) {
        T ret = get(index);
        storage.moveSubtree(index+1, SequenceIdx.shape, index);
        tail.next_back();
        return ret;
    }

    @Override
    public int size() {
        return tail.ok();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override @Nonnull
    public Iterator<T> iterator() {
        return new Iter();
    }

    @Override @Nonnull
    public Object[] toArray() {
        final Object[] ret = new Object[tail.ok()];
        return toArray(ret);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        final Sequence<T> sequence = new Sequence<>(storage.copy());
        sequence.tail = tail.copy();
        return sequence;
    }

    @Override @Nonnull
    public <T1> T1[] toArray(final @Nonnull T1[] a) {
        T1[] ret;
        if (a.length > tail.ok()) {
            ret = a;
        } else {
            ret = (T1[]) Array.newInstance(
                    a.getClass().getComponentType(),
                    tail.ok());
        }
        for (SequenceIdx i=new SequenceIdx(); i.ok()<ret.length; i.next()) {
            ret[i.ok()] = (T1) storage.get(i.ok());
        }
        return ret;
    }

    private class Iter implements Iterator<T> {
        private final SequenceIdx cur = new SequenceIdx();

        private T get() {
            return storage.get(cur.ok());
        }

        @Override
        public boolean hasNext() {
            return get() != null;
        }

        @Override
        public T next() {
            final T ret = get();
            cur.next();
            return ret;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"("+storage.getClass()+")"+super.toString();
    }
}
