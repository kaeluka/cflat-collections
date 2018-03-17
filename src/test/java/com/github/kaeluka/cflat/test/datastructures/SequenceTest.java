package com.github.kaeluka.cflat.test.datastructures;

import com.github.kaeluka.cflat.Sequence;
import com.github.kaeluka.cflat.storage.AssertionStorage;
import com.github.kaeluka.cflat.storage.Storage;
import com.github.kaeluka.cflat.util.NamedSupplier;
import com.github.kaeluka.cflat.util.Storages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class SequenceTest {

    @Parameterized.Parameter()
    public Supplier<Storage<Integer>> storageSupplier;

    private Sequence<Integer> mkSequence() {
        return new Sequence<>(storageSupplier.get());
    }


    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name="{0}")
    public static Collection<Supplier<Storage>> storages() {
        return Storages.genericStorages();
    }

    @Test
    public void addFrontThenGet() throws Exception {
        final int ADD_N = 10;
        final Sequence<Integer> seq = this.mkSequence();

        for (int i=0; i<ADD_N; ++i) {
            seq.add(0, i);
        }
        for (int i=0; i<ADD_N; ++i) {
            assertThat(seq.get(i), is(ADD_N - i - 1));
        }
    }

    // build a sequence that can only contain even numbers
    // and check that the exception handling works
    @Test
    public void assertionsTest() {
        class UnevenNumberException extends RuntimeException {}
        final Storage<Integer> st = storageSupplier.get();
        final Storage<Integer> ast =
                AssertionStorage.withAssertion(st,
                (start, end, upd) -> {
                    for (int i = start; i < end; i++) {
                        if (upd.get(i) % 2 != 0) {
                            throw new UnevenNumberException();
                        }
                    }
                });
        final Sequence<Integer> seq = new Sequence<>(ast);

        for (int i = 0; i < 10; i++) {
            seq.add(i*2);
        }

        try {
            seq.add(1);
        } catch (UnevenNumberException e) {
            // we expected this!
            return;
        }

        throw new AssertionError("no UnevenNumberException was thrown!");

    }

    @Test
    public void indexOfTest() {
        final List<Integer> seq = this.mkSequence();

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
        final List<Integer> seq = this.mkSequence();

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
        final List<Integer> seq = mkSequence();
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
        final List<Integer> seq = mkSequence();
        for (int i=0; i<ADD_N; ++i) {
            seq.add(i);
        }
        for (int i=0; i<ADD_N; ++i) {
            seq.remove(seq.size()-1);

        }
    }
}