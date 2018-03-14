package com.github.kaeluka.cflat.test.datastructures;

import com.github.kaeluka.cflat.MapFlat;
import com.github.kaeluka.cflat.storage.IntArrayStorage;
import com.github.kaeluka.cflat.storage.IntTrieStorage;
import com.github.kaeluka.cflat.storage.Storage;
import com.github.kaeluka.cflat.util.NamedSupplier;
import com.github.kaeluka.cflat.util.Storages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class MapTest {

    @Parameterized.Parameter
    public Supplier<MapFlat> mapSupplier;

    @SuppressWarnings("unchecked")
    private <K, V> MapFlat<K, V> mkMap() {
        return mapSupplier.get();
    }

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name = "{0}")
    public static List<Supplier<Map<Integer, Integer>>> maps() {
        final ArrayList<Supplier<Map<Integer, Integer>>> ret = new ArrayList<>();
        for (final Supplier<Storage> s : Storages.genericStorages()) {
            ret.add(new NamedSupplier<Map<Integer, Integer>>(() -> new MapFlat(s.get()), "MapFlat("+s.toString()+")"));
        }

        return ret;
    }

    @Test
    public void addInOrder() {
        final Map<String,String> map = mkMap();
        if (map == null) return; // a skipped test
        if (map instanceof MapFlat) {
            final Storage data = ((MapFlat) map).data;
            System.out.println(data.getClass().getSimpleName());
            if (data instanceof IntTrieStorage ||
                    data instanceof IntArrayStorage) {
                // we can't put strings in there..
                return;
            }
        }
        assertFalse(map.containsKey("BBB"));
        int i=0;
        map.put("AAA", "haha"+(++i));
        map.put("BBB", "haha"+(++i));
        map.put("CCC", "haha"+(++i));
        map.put("DDD", "haha"+(++i));
        assertTrue(map.containsKey("AAA"));
        assertTrue(map.containsKey("BBB"));
        assertTrue(map.containsKey("CCC"));
    }

    @Test
    public void add() {
        final MapFlat<String, String> map = mkMap();
        if (map == null) return; // a skipped test
        final Storage data = ((MapFlat) map).data;
        if (data instanceof IntTrieStorage ||
                data instanceof IntArrayStorage) {
            // we can't put strings in there..
            return;
        }
        System.out.println(data.getClass().getSimpleName());

        assertFalse(map.containsKey("BBB"));
        map.put("BBB", "haha1");
        System.out.println(map);
        map.put("AAA", "haha2");
        System.out.println(map);
        map.put("CCC", "haha3");
        System.out.println(map);
        map.put("DDD", "haha4");
        System.out.println(map);
        assertTrue(map.containsKey("BBB"));
        assertTrue(map.containsKey("AAA"));
        assertTrue(map.containsKey("CCC"));
        assertTrue(map.containsKey("DDD"));
        assertTrue(map.containsValue("haha1"));
        assertTrue(map.containsValue("haha2"));
        assertTrue(map.containsValue("haha3"));
        assertTrue(map.containsValue("haha4"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void randomAddAndGet() {
        final int RANDOM_ADD_AND_GET_SIZE = 8000;
        final MapFlat<Integer, Integer> bst = mkMap();
        if (bst == null) return; // a skipped test
        if (bst instanceof MapFlat) {
            System.out.println(((MapFlat) bst).data.getClass().getSimpleName());
        }
        Random random = new Random(12345L);
        for (int i = 0; i< RANDOM_ADD_AND_GET_SIZE; ++i) {
            final int key = random.nextInt(RANDOM_ADD_AND_GET_SIZE);
            final int value = random.nextInt(RANDOM_ADD_AND_GET_SIZE);
            bst.put(key, value);
        }

        random = new Random(12345L);
        for (int i = 0; i < RANDOM_ADD_AND_GET_SIZE; ++i) {
            final int key = random.nextInt(RANDOM_ADD_AND_GET_SIZE);
            @SuppressWarnings("unused")
            final int value = random.nextInt(RANDOM_ADD_AND_GET_SIZE);
            assert bst.containsKey(key);
        }

//        if (bst instanceof TupleMap) {
//            if (((TupleMap) bst).vals.storage instanceof StatisticsStorage) {
//                System.out.println(((StatisticsStorage<Integer>) ((((TupleMap) bst).vals).storage)).getStatistics("vals"));
//            }
//            if (((TupleMap) bst).keys.storage instanceof StatisticsStorage) {
//                System.out.println(((StatisticsStorage<Integer>) ((((TupleMap) bst).keys).storage)).getStatistics("keys"));
//            }
//        }
//        if (bst instanceof RedBlackMap) {
//            System.out.println(((TimersStorage<Integer>) ((((RedBlackMap) bst).keys))).getStatistics("keys"));
//        }
    }
}