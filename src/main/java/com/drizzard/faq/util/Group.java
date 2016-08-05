package com.drizzard.faq.util;

/**
 * Created by jasper on 8/4/16.
 */
public class Group<K, V> {
	public K a;
	public V b;

	public Group(K a, V b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public boolean equals(Object other) {
		Group ot = ((Group) other);
		return (a.equals(ot.a) && b.equals(ot.b)) || (a.equals(ot.b) && b.equals(ot.a));
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
