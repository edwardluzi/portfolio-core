package org.goldenroute;

public interface Listener<T>
{
    void update(T event);
}
