package org.example;

import java.util.*;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;


public class Main {
    public static void main(String[] args) {

        class LabelsMultiMap<L, V> {
            private final Map<L, BitSet> labelsBitSets = new HashMap<>();
            private final List<V> values = new ArrayList<>();

            public void put(Set<L> labels, V value) {
                int i = addValue(value);
                for (L label : labels) {
                    BitSet bitSet = getOrCreateLabel(label);
                    bitSet.set(i);

                }
            }

            private BitSet getOrCreateLabel(L label) {
                BitSet ret = labelsBitSets.get(label);
                if (ret == null) {
                    ret = new BitSet(values.size());
                    labelsBitSets.put(label, ret);
                }
                return ret;
            }

            private int addValue(V value) {
                values.add(value);
                return values.size() - 1;
            }


            public List<V> getValues() {
                return Collections.unmodifiableList(values);
            }

            public Collection<V> findValues(Set<L> labels) {
                Iterator<L> it = labels.iterator();
                if (!it.hasNext()) {
                    return getValues();
                }
                BitSet firstBitSet = labelsBitSets.get(it.next());
                if (firstBitSet == null) {
                    return Collections.emptySet();
                }
                BitSet accumulator = (BitSet) firstBitSet.clone();
                while (it.hasNext()) {
                    BitSet nextBitSet = labelsBitSets.get(it.next());
                    if (nextBitSet == null) {
                        return Collections.emptySet();
                    }
                    accumulator.and(nextBitSet);
                }
                return new ValuesByBitSetCollection<>(accumulator, values);
            }

            class ValuesByBitSetCollection<V> extends AbstractCollection<V> {
                private final BitSet bitSet;
                private final List<V> values;
                private int size = -1;

                private ValuesByBitSetCollection(BitSet bitSet, List<V> values) {
                    this.bitSet = bitSet;
                    this.values = values;
                }

                @Override
                public boolean isEmpty() {
                    return bitSet.isEmpty();
                }

                @Override
                public Iterator<V> iterator() {
                    return new ValuesByBitSetIterator<V>(bitSet, values);
                }

                @Override
                public int size() {
                    if (size < 0) {
                        size = bitSet.cardinality();
                    }
                    return size;
                }

                public Collection<V> findValuesOnlyIn(Set<L> labels) {
                    if (labels.isEmpty()) {
                        return Collections.emptySet();
                    }
                    BitSet inAccumulator = new BitSet(values.size());
                    BitSet outAccumulator = new BitSet(values.size());

                    for (Map.Entry<L, BitSet> bitSetEntry : labelsBitSets.entrySet()) {
                        BitSet accumulator = labels.contains(bitSetEntry.getKey()) ? inAccumulator : outAccumulator;
                        accumulator.or(bitSetEntry.getValue());
                    }

                    inAccumulator.andNot(outAccumulator);

                    return new ValuesByBitSetCollection<>(inAccumulator, values);
                }

                static class ValuesByBitSetIterator<V> implements Iterator<V> {
                    private final BitSet bitSet;
                    private final List<V> values;
                    private int index;

                        private ValuesByBitSetIterator(BitSet bitSet, List<V> values) {
                            this.bitSet = bitSet;
                            this.values = values;
                            index = bitSet.nextSetBit(0);
                        }

                        @Override
                        public boolean hasNext() {
                            return index >= 0;
                        }

                        @Override
                        public V next() {
                            if (index < 0) {
                                throw new NoSuchElementException();
                            }
                            V ret = values.get(index);
                            index = bitSet.nextSetBit(index + 1);
                            return ret;
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    }
                }
            }
        }
    }
