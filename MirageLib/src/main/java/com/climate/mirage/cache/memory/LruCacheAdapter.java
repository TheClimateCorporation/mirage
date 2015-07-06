/*
 * Copyright (c) 2012 Joshua Musselwhite
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.climate.mirage.cache.memory;

import android.support.v4.util.LruCache;

public class LruCacheAdapter<K, V> implements MemoryCache<K, V> {
	
	private LruCache<K, V> lruCache;
	
	public LruCacheAdapter(LruCache<K, V> lruCache) {
		this.lruCache = lruCache;
	}
	
	public LruCache<K, V> getLruCache() {
		return lruCache;
	}
	
	@Override
	public V get(K key) {
		return lruCache.get(key);
	}
	
	@Override
	public void put(K key, V value) {
		if (key == null || value == null) return;
		lruCache.put(key, value);
	}

	@Override
	public void remove(K key) {
		lruCache.remove(key);
	}

	@Override
	public void clear() {
		lruCache.evictAll();
	}

	@Override
	public int getCurrentSize() {
		return lruCache.size();
	}

	@Override
	public int getMaxSize() {
		return lruCache.maxSize();
	}

	@Override
	public boolean has(K key) {
		if (key == null) return false;
		V value = lruCache.get(key);
//		return (bm != null && !bm.isRecycled());
		return (value != null);
	}
	
}