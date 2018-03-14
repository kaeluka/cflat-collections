package com.github.kaeluka.cflat.test.datastructures;

import com.github.kaeluka.cflat.Sequence;
import com.github.kaeluka.cflat.storage.*;
import com.github.kaeluka.cflat.util.NamedSupplier;
import com.github.kaeluka.cflat.util.Storages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.github.kaeluka.cflat.SequenceIdx;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class SequenceTest {

    @Parameterized.Parameter()
    public Supplier<Sequence<Object>> seqSupplier;

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name="{0}")
    public static Collection<Supplier<Sequence<Object>>> storages() {
        final ArrayList<Supplier<Sequence<Object>>> ret = new ArrayList<>();
        for (final Supplier<Storage> s : Storages.genericStorages()) {
            ret.add(new NamedSupplier<Sequence<Object>>(() -> new Sequence<Object>(s.get()), "Sequence("+s+")"));
        }
        return ret;
    }

    @Test
    public void addFrontThenGet() throws Exception {
        final int ADD_N = 10;
        final Sequence<Object> seq = this.seqSupplier.get();

        for (int i=0; i<ADD_N; ++i) {
            seq.add(0, i);
        }
        for (int i=0; i<ADD_N; ++i) {
            assertThat(seq.get(i), is(ADD_N - i - 1));
        }
    }

    @Test
    public void indexOfTest() {
        final List<Object> seq = this.seqSupplier.get();

        final int ADD_N = 100;
        for (int i = 0; i < ADD_N; i++) {
            seq.add(i);
        }

        for (int i = 0; i < ADD_N; i++) {
            assertThat(seq.indexOf(i), is(i));
        }

        for (int i=0; i<1000; ++i) {
            assertThat(seq.indexOf(i - 1000), is(-1));
            assertThat(seq.indexOf(ADD_N + i), is(-1));
        }
    }

    @Test
    public void addThenGet() throws Exception {
        final int ADD_N = 50000;
        final List<Object> seq = this.seqSupplier.get();

        for (int i=0; i<ADD_N; ++i) {
            seq.add(i);
        }
        for (int i=0; i<ADD_N; ++i) {
            assertThat(seq.get(i), is(i));
        }

    }

    @Test
    public void addThenRemoveFromStart() throws Exception {
        final int ADD_N = 500;
        final List<Object> seq = seqSupplier.get();
        for (int i=0; i<ADD_N; ++i) {
            seq.add(i);
        }
        for (int i=0; i<ADD_N; ++i) {
            seq.remove(0);
        }
        assertThat(seq.size(), is(0));
    }

    @Test
    public void addThenRemoveFromEnd() throws Exception {
        final int ADD_N = 500;
        final List<Object> seq = seqSupplier.get();
        for (int i=0; i<ADD_N; ++i) {
            seq.add(i);
        }
        for (int i=0; i<ADD_N; ++i) {
            seq.remove(seq.size()-1);

        }
    }
}