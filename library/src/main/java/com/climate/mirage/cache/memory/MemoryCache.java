package com.climate.mirage.cache.memory;

public interface MemoryCache<K, V> {

	/**
	 * Returns the sum of the sizes of all the contents of the cache in bytes.
	 */
	int getCurrentSize();

	/**
	 * Returns the current maximum size in bytes of the cache.
	 */
	int getMaxSize();

	public boolean has(K key);
	public V get(K key);
	public void put(K key, V value);
	public void remove(K key);
	public void clear();


	
}