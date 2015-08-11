package com.climate.mirage.utils;

import java.util.ArrayList;
import java.util.List;

public class ObjectPool<T> {
	
	private ObjectFactory<T> factory;
	private int poolSize = 20;
	private final List<T> pool;
	
	public ObjectPool(ObjectFactory<T> factory, int poolSize) {
		this.factory = factory;
		this.poolSize = poolSize;
		this.pool = new ArrayList<T>();
	}
	
	public T getObject() {
		synchronized (pool) {
			if (pool.size() > 0) {
				return pool.remove(pool.size()-1);
			}
		}
		return factory.create();
	}
	
	public void recycle(T object) {
		factory.recycle(object);
		synchronized (pool) {
			if (pool.size() < poolSize && !pool.contains(object)) {
				pool.add(object);
			}
		}
	}	
}