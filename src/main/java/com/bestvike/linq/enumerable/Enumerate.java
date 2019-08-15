package com.bestvike.linq.enumerable;

import com.bestvike.function.Func1;
import com.bestvike.function.Predicate1;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.exception.ExceptionArgument;
import com.bestvike.linq.exception.ThrowHelper;

/**
 * Created by 许崇雷 on 2019-07-26.
 */
public final class Enumerate {
    private Enumerate() {
    }

    public static <TSource> IEnumerable<TSource> enumerate(TSource seed, Func1<TSource, TSource> next) {
        if (next == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.next);

        return new EnumerateIterator<>(seed, next);
    }

    public static <TSource> IEnumerable<TSource> enumerate(TSource seed, Predicate1<TSource> condition, Func1<TSource, TSource> next) {
        if (condition == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.condition);
        if (next == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.next);

        return new EnumerateIterator2<>(seed, condition, next);
    }
}


final class EnumerateIterator<TSource> extends Iterator<TSource> {
    private final TSource seed;
    private final Func1<TSource, TSource> next;

    EnumerateIterator(TSource seed, Func1<TSource, TSource> next) {
        this.seed = seed;
        this.next = next;
    }

    @Override
    public Iterator<TSource> clone() {
        return new EnumerateIterator<>(this.seed, this.next);
    }

    @Override
    public boolean moveNext() {
        switch (this.state) {
            case 1:
                this.current = this.seed;
                this.state = 2;
                return true;
            case 2:
                this.current = this.next.apply(this.current);
                return true;
            default:
                return false;
        }
    }
}


final class EnumerateIterator2<TSource> extends Iterator<TSource> {
    private final TSource seed;
    private final Predicate1<TSource> condition;
    private final Func1<TSource, TSource> next;

    EnumerateIterator2(TSource seed, Predicate1<TSource> condition, Func1<TSource, TSource> next) {
        this.seed = seed;
        this.condition = condition;
        this.next = next;
    }

    @Override
    public Iterator<TSource> clone() {
        return new EnumerateIterator2<>(this.seed, this.condition, this.next);
    }

    @Override
    public boolean moveNext() {
        TSource item;
        switch (this.state) {
            case 1:
                item = this.seed;
                if (this.condition.apply(item)) {
                    this.current = item;
                    this.state = 2;
                    return true;
                }
                this.close();
                return false;
            case 2:
                item = this.next.apply(this.current);
                if (this.condition.apply(item)) {
                    this.current = item;
                    return true;
                }
                this.close();
                return false;
            default:
                return false;
        }
    }
}
