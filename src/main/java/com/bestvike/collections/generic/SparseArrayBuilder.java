package com.bestvike.collections.generic;

import com.bestvike.linq.IEnumerable;
import com.bestvike.out;

/**
 * Created by 许崇雷 on 2017-09-30.
 */
final class Marker {//struct
    private final int count;
    private final int index;

    public Marker(int count, int index) {
        assert count >= 0;
        assert index >= 0;

        this.count = count;
        this.index = index;
    }

    public int getCount() {
        return this.count;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return String.format("index: %s, count: %s", this.index, this.count);
    }
}


final class SparseArrayBuilder<T> {//struct
    private LargeArrayBuilder<T> builder = new LargeArrayBuilder<>();
    private ArrayBuilder<Marker> markers = new ArrayBuilder<>();
    private int reservedCount;

    // The total number of items in this builder, including reserved regions.
    public int getCount() {
        return Math.addExact(this.builder.getCount(), this.reservedCount);
    }

    // The list of reserved regions in this builder.
    public ArrayBuilder<Marker> getMarkers() {
        return this.markers;
    }

    // Adds an item to this builder.
    public void add(T item) {
        this.builder.add(item);
    }


    public void addRange(IEnumerable<T> items) {
        this.builder.addRange(items);
    }


    public void copyTo(Array<T> array, int arrayIndex, int count) {
        assert array != null;
        assert arrayIndex >= 0;
        assert count >= 0 && count <= this.getCount();
        assert array.length() - arrayIndex >= count;

        int copied = 0;
        CopyPosition position = CopyPosition.start();

        for (int i = 0; i < this.markers.getCount(); i++) {
            Marker marker = this.markers.get(i);

            // During this iteration, copy until we satisfy `count` or reach the marker.
            int toCopy = Math.min(marker.getIndex() - copied, count);

            if (toCopy > 0) {
                position = this.builder.copyTo(position, array, arrayIndex, toCopy);

                arrayIndex += toCopy;
                copied += toCopy;
                count -= toCopy;
            }

            if (count == 0)
                return;

            // We hit our marker. Advance until we satisfy `count` or fulfill `marker.Count`.
            int reservedCount = Math.min(marker.getCount(), count);

            arrayIndex += reservedCount;
            copied += reservedCount;
            count -= reservedCount;
        }

        if (count > 0) {
            // Finish copying after the final marker.
            this.builder.copyTo(position, array, arrayIndex, count);
        }
    }


    // Reserves a region starting from the current index.
    public void reserve(int count) {
        assert count >= 0;
        this.markers.add(new Marker(count, this.getCount()));
        this.reservedCount = Math.addExact(this.reservedCount, count);
    }


    // Reserves a region if the items' count can be predetermined; otherwise, adds the items to this builder.
    public boolean reserveOrAdd(IEnumerable<T> items) {
        out<Integer> itemCountRef = out.init();
        if (EnumerableHelpers.tryGetCount(items, itemCountRef)) {
            int itemCount = itemCountRef.getValue();
            if (itemCount > 0) {
                this.reserve(itemCount);
                return true;
            }
        } else {
            this.addRange(items);
        }
        return false;
    }


    // Creates an array from the contents of this builder.
    public Array<T> toArray() {
        // If no regions were reserved, there are no 'gaps' we need to add to the array.
        // In that case, we can just call ToArray on the underlying builder.
        if (this.markers.getCount() == 0) {
            assert this.reservedCount == 0;
            return this.builder.toArray();
        }

        Array<T> array = Array.create(this.getCount());
        this.copyTo(array, 0, array.length());
        return array;
    }
}
