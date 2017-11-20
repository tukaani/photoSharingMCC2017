package com.appspot.mccfall2017g12.photoorganizer;

public interface Diffable<T> {
    boolean isTheSameAs(T other);
    boolean hasTheSameContentAs(T other);
}
