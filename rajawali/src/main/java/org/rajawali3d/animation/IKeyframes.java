package org.rajawali3d.animation;

public interface IKeyframes<K, V> {

        V calculatePoint(K factor);

}
