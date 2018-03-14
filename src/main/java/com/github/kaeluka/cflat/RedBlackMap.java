package com.github.kaeluka.cflat;

import com.github.kaeluka.cflat.annotations.Cflat;
import com.github.kaeluka.cflat.storage.ArrayStorage;
import com.github.kaeluka.cflat.storage.Storage;
import com.github.kaeluka.cflat.traversal.GenericShape;
import com.github.kaeluka.cflat.util.Mutable;
import com.github.kaeluka.cflat.util.plot.Dot;

import java.util.*;

@Cflat("*(|(left,right))->ok")
public class RedBlackMap<K,V> extends AbstractMap<K,V> {
    public Storage<K> keys;
    public Storage<V> vals;
    public Storage<Boolean> color;
    public Storage<Integer> depth = new ArrayStorage<>();
    public Comparator<? super K> comparator = null;
    private int size = 0;
    private int modCount = 0;
    private final static boolean ASSERTIONS = false;

    private void assertRBTree() {
        if (!ASSERTIONS) return;
        final Storage<K> k = this.keys;
        final Storage<V> v = this.vals;
        final Storage<Boolean> c = this.color;

        k.foreachNonNull(i -> {
            assert(v.has(i));
        });
        v.foreachNonNull(i -> {
            assert(k.has(i));
        });
        c.foreachNonNull(i -> {
            assert(k.has(i));
        });
        k.foreachNonNull(i -> {
            assert(c.has(i));
        });
        //connected:
        final Mutable<Integer> integerMutable = new Mutable<>(0);
        k.foreachNonNull(i -> {
            if (i != 0) {
                if (!k.has(GenericShape.parentOf(i, GenericShape.mkStar(2, 1)))) {
                    throw new AssertionError(i+" has lost its parent");
                }
            }
        });

        //sorted:
        k.foreachNonNull(i -> {
            int leftC  = i*2+1;
            int rightC = i*2+2;
            if (k.has(i)) {
                Comparable<? super K> p = (Comparable<? super K>) k.get(i);
                if (k.has(leftC)) {
                    final K l = k.get(leftC);
                    if (p.compareTo(l) < 0) {
                        throw new AssertionError(p + "must be > " + l);
                    }
//                assert p.compareTo(l) < 0;
                }
                if (k.has(rightC)) {
                    final K l = k.get(rightC);
                    if (p.compareTo(l) > 0) {
                        throw new AssertionError(p + "must be < " + l);
                    }
                }
            }
        });

    }

    public String toDot() {
        return Dot.from(
                this.keys,
                RedBlackMapIdx.shape,
                i -> {
                    final String color =
                            (!this.color.has(i) ||
                                    this.color.get(i) == BLACK) ?
                                    "grey" : "red";
                    int row = 0;
                    int i2 = i;
                    while (i2 > 0) {
                        i2 = GenericShape.parentOf(i2, RedBlackMapIdx.shape);
                        row++;
                    }
                    final int rowBegin = ((int) (Math.round(Math.pow(2, row)) - 1));
                    int col = i - rowBegin;
                    return String.format(
                            "label=\"%d: %s\", color=\"%s\", style=\"filled\",pos=\"%d,%d!\"",
                            i, keys.get(i), color, row*10, col*10);
                    });
    }

    private static final Boolean RED   = false;
    private static final Boolean BLACK = true;

    public RedBlackMap(final Storage<K> keys, final Storage<V> vals, final Storage<Boolean> cols) {
        this.keys = keys;
        this.vals = vals;
        this.color = cols;
    }

    public int size() {
        return size;
    }

    @Override
    public boolean containsKey(Object key) {
        final RedBlackMapIdx p = new RedBlackMapIdx();
        getEntry(key, p);
        return keys.has(p.ok());
    }

    final void getEntryUsingComparator(Object key, RedBlackMapIdx p) {
        throw new UnsupportedOperationException();
    }

    final int getEntry_(Object key) {
        if (comparator != null) {
            throw new UnsupportedOperationException();
        }
        if (key == null) {
            throw new NullPointerException();
        }
        int ret = 0;
        Comparable<? super K> k = (Comparable<? super K>) key;
        while (this.keys.has(ret)) {
            int cmp = k.compareTo(keys.get(ret));
            if (cmp < 0)
                ret = ret*2 + 1;
            else if (cmp > 0)
                ret = ret*2 + 2;
            else
                break;
        }
        return ret;
    }
    final void getEntry(Object key, RedBlackMapIdx p) {
        // Offload comparator-based version for sake of performance
        if (comparator != null) {
            getEntryUsingComparator(key, p);
        }
        if (key == null) {
            throw new NullPointerException();
        }
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        while (this.keys.has(p.ok())) {
            int cmp = k.compareTo(keys.get(p.ok()));
            if (cmp < 0)
                p.left();
            else if (cmp > 0)
                p.right();
            else
                return;
        }
    }

    public boolean containsValue(Object value) {
        //FIXME: lazy index iterator!
        final Iterator<Integer> indices = this.vals.nonNullIndices();
        while (indices.hasNext()) {
            if (vals.get(indices.next()).equals(value)) {
                return true;
            }
        }
        return false;
    }

    public V get(Object key) {
        final RedBlackMapIdx p = new RedBlackMapIdx();
        int pi = getEntry_(key);
//        int p = getEntry(key, p).ok();
        return vals.get(pi);
    }

    public K firstKey() {
        throw new UnsupportedOperationException();
    }

    public K lastKey() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    private int compare(Object k1, Object k2) {
        return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)
                : comparator.compare((K)k1, (K)k2);
    }

//    private String nodeToString(RedBlackMapIdx i) {
//        if (!keys.has(i.ok()) && !vals.has(i.ok()) && !color.has(i.ok())) {
//            return "null";
//        }
//        return keys.get(i.ok()) + "=" + vals.get(i.ok()) + "(" + (colorOf(i) == RED ? "red" : "black") + ")";
//    }

    public V put(K key, V value) {
        RedBlackMapIdx t = new RedBlackMapIdx();
        if (size == 0) {
            compare(key, key); // type (and possibly null) check
            keys.set(t.ok(), key);
            vals.set(t.ok(), value);
            color.set(t.ok(), BLACK);
            size = 1;
            modCount++;
            assertRBTree();
            return null;
        }
        // split comparator and comparable paths
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            do {
                int cmp = cpr.compare(key, keys.get(t.ok()));
                if (cmp < 0) {
                    t.left();
                } else if (cmp > 0) {
                    t.right();
                } else {
                    vals.set(t.ok(), value);
                    assertRBTree();
                    return value;
                }
            } while (keys.has(t.ok()));
        }
        else {
            if (key == null) {
                throw new NullPointerException();
            }
            @SuppressWarnings("unchecked")
            Comparable<? super K> k = (Comparable<? super K>) key;
            do {
                final K curK = keys.get(t.ok());
                int cmp = k.compareTo(keys.get(t.ok()));
                if (cmp < 0) {
                    t.left();
                } else if (cmp > 0) {
                    t.right();
                } else {
                    final V oldVal = vals.get(t.ok());
                    keys = keys.set(t.ok(), key);
                    vals = vals.set(t.ok(), value);
                    color = color.set(t.ok(), null);
                    assertRBTree();
                    return oldVal;
                }
            } while (keys.has(t.ok()));
        }
        keys =keys.set(t.ok(), key);
        vals = vals.set(t.ok(), value);
        color = color.set(t.ok(), BLACK);
        fixAfterInsertion(key, value, t);
        size++;
        modCount++;
        assertRBTree();
        return null;
    }

    private void fixAfterInsertion(K key, V value, RedBlackMapIdx x) {
        color = color.set(x.ok(), RED);

        while (keys.has(x.ok()) && x.ok() != 0 && colorOf(parentOf(x)) == RED) {
            if (parentOf(x).ok() == leftOf(parentOf(parentOf(x))).ok()) {
                RedBlackMapIdx y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x.ok() == rightOf(parentOf(x)).ok()) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x.copy())));
                }
            } else {
                RedBlackMapIdx y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x.ok() == leftOf(parentOf(x)).ok()) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        color = color.set(0, BLACK);
    }

    private void setData(RedBlackMapIdx dest, RedBlackMapIdx source) {
        color = color.set(dest.ok(), color.get(source.ok()));
        keys = keys.set(dest.ok(), keys.get(source.ok()));
        vals = vals.set(dest.ok(), vals.get(source.ok()));
    }

    private void copyData(RedBlackMapIdx dest, RedBlackMapIdx source) {
        color = color.moveSubtree(source.ok(), RedBlackMapIdx.shape, dest.ok());
        keys = keys.moveSubtree(source.ok(), RedBlackMapIdx.shape, dest.ok());
        vals = vals.moveSubtree(source.ok(), RedBlackMapIdx.shape, dest.ok());
    }

    private void rotateLeft(RedBlackMapIdx p) {
        if (p != null) {
            Mutable<Integer> sum = new Mutable<>(0);
            keys.foreachSuccessor(p.ok(), GenericShape.mkStar(2, 1), i -> sum.x++);

            /*
                p
              /   \
           alpha  y
                /  \
              beta gamma

                |
                V

                y
              /   \
             p    gamma
           /   \
        alpha  beta
         */
            assert keys.has(p.ok());
            final RedBlackMapIdx y = p.copy().right();
            final RedBlackMapIdx alpha = p.copy().left();
            final RedBlackMapIdx beta = y.copy().left();
            final RedBlackMapIdx gamma = y.copy().right();
            final boolean loud = keys.get(p.ok()).equals(387591904);
            copyData(alpha.copy().left(), alpha);
            setData(alpha, p);
            setData(p, y);
            copyData(alpha.copy().right(), beta);
            copyData(y, gamma);

            Mutable<Integer> sumAfter = new Mutable<>(0);
            keys.foreachSuccessor(p.ok(), GenericShape.mkStar(2, 1), i -> sumAfter.x++);
            p.left();
        }
    }

    private void rotateRight(RedBlackMapIdx p) {
        /*
                p
              /   \
             x    gamma
           /   \
        alpha  beta

                |
                V

                x
              /   \
           alpha  p
                /  \
              beta gamma
         */
        if (p != null) {
//            System.out.println("rotateRight(" + nodeToString(p) + ")");
            Mutable<Integer> sum = new Mutable<>(0);
            if (ASSERTIONS) {
                keys.foreachSuccessor(p.ok(), GenericShape.mkStar(2, 1), i -> sum.x++);
            }
            final RedBlackMapIdx x = p.copy().left();
            final RedBlackMapIdx alpha = x.copy().left();
            final RedBlackMapIdx beta = x.copy().right();
            final RedBlackMapIdx gamma = p.copy().right();

            assert keys.has(p.ok());
            assert keys.has(x.ok());

            copyData(gamma.copy().right(), gamma);
            copyData(gamma.copy().left(), beta);
            setData(gamma, p);
            setData(p, x);
            copyData(x, alpha);

            if (ASSERTIONS) {
                Mutable<Integer> sumAfter = new Mutable<>(0);
                this.keys.foreachSuccessor(p.ok(), GenericShape.mkStar(2, 1), i -> sumAfter.x++);
                assert sum == sumAfter;
            }
            p.right();
        }
    }

    private boolean colorOf(RedBlackMapIdx p) {
        return (p == null || p.ok() < 0 || !color.has(p.ok())) ? BLACK : (boolean)color.get(p.ok());
    }

    private RedBlackMapIdx parentOf(RedBlackMapIdx p) {
        if (p == null || p.ok() == 0) {
            return null;
        } else {
            RedBlackMapIdx parent = p.copy();
            parent.left_back();
            return parent;
        }
    }

    private void setColor(RedBlackMapIdx p, boolean c) {
        if (p != null) { color = color.set(p.ok(), c); }
    }

    private RedBlackMapIdx leftOf(RedBlackMapIdx p) {
        if (p == null) {
            return null;
        } else {
            RedBlackMapIdx l = p.copy();
            l.left();
            return l;
        }
    }

    private RedBlackMapIdx rightOf(RedBlackMapIdx p) {
        if (p == null) {
            return null;
        } else {
            RedBlackMapIdx r = p.copy();
            r.right();
            return r;
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}
