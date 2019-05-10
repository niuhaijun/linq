package com.bestvike.linq.enumerable;

import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.Linq;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by 许崇雷 on 2019-05-08.
 */
public class EmptyEnumerableTest extends EnumerableTest {
    private <T> void TestEmptyCached() {
        IEnumerable<T> enumerable1 = Linq.empty();
        IEnumerable<T> enumerable2 = Linq.empty();

        Assert.assertSame(enumerable1, enumerable2); // Enumerable.empty is not cached if not the same.
    }

    @Test
    public void EmptyEnumerableCachedTest() {
        this.<Integer>TestEmptyCached();
        this.<String>TestEmptyCached();
        this.TestEmptyCached();
        this.<EmptyEnumerableTest>TestEmptyCached();
    }

    private <T> void TestEmptyEmpty() {
        assertEquals(Linq.asEnumerable(), Linq.empty());
        Assert.assertEquals(0, Linq.<T>empty().count());
        Assert.assertSame(Linq.<T>empty().enumerator(), Linq.<T>empty().enumerator());
    }

    @Test
    public void EmptyEnumerableIsIndeedEmpty() {
        this.<Integer>TestEmptyEmpty();
        this.<String>TestEmptyEmpty();
        this.TestEmptyEmpty();
        this.<EmptyEnumerableTest>TestEmptyEmpty();
    }
}