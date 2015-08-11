package com.climate.mirage.utils;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RoboManifestRunner;
import com.climate.mirage.requests.MirageRequest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.List;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class ObjectPoolTest {

    @Test
    public void testGetsObject() throws Exception {
        ObjectFactory<MirageRequest> factory = Mockito.mock(ObjectFactory.class);
        Mockito.when(factory.create()).thenReturn(Mockito.mock(MirageRequest.class));

        ObjectPool<MirageRequest> pool = new ObjectPool<>(factory, 3);
        Assert.assertNotNull(pool.getObject());
    }

    @Test
    public void testGetsObjectFromPool() throws Exception {
        ObjectFactory<MirageRequest> factory = Mockito.mock(ObjectFactory.class);
        Mockito.when(factory.create()).thenReturn(Mockito.mock(MirageRequest.class));

        ObjectPool<MirageRequest> pool = new ObjectPool<>(factory, 3);
        Assert.assertEquals(0, getPoolSize(pool));
        MirageRequest request = pool.getObject();
        Assert.assertNotNull(request);
        pool.recycle(request);
        Assert.assertEquals(1, getPoolSize(pool));
        request = pool.getObject();
        Assert.assertEquals(0, getPoolSize(pool));
    }

    @Test
    public void testAddsBackToPool() throws Exception {
        ObjectFactory<MirageRequest> factory = Mockito.mock(ObjectFactory.class);
        Mockito.when(factory.create()).thenReturn(Mockito.mock(MirageRequest.class));

        ObjectPool<MirageRequest> pool = new ObjectPool<>(factory, 3);
        Assert.assertEquals(0, getPoolSize(pool));
        MirageRequest request = pool.getObject();
        Assert.assertNotNull(request);
        pool.recycle(request);
        Assert.assertEquals(1, getPoolSize(pool));
    }

    @Test
    public void testNoDoubleAdds() throws Exception {
        ObjectFactory<MirageRequest> factory = Mockito.mock(ObjectFactory.class);
        Mockito.when(factory.create()).thenReturn(Mockito.mock(MirageRequest.class));

        ObjectPool<MirageRequest> pool = new ObjectPool<>(factory, 3);
        MirageRequest request = pool.getObject();
        Assert.assertNotNull(request);
        pool.recycle(request);
        pool.recycle(request);
        pool.recycle(request);
        Assert.assertEquals(1, getPoolSize(pool));
    }

    @Test
    public void testPoolDoesntOverFill() throws Exception {
        ObjectFactory<MirageRequest> factory = Mockito.mock(ObjectFactory.class);

        ObjectPool<MirageRequest> pool = new ObjectPool<>(factory, 3);
        pool.recycle(new MirageRequest());
        pool.recycle(new MirageRequest());
        pool.recycle(new MirageRequest());
        pool.recycle(new MirageRequest());
        pool.recycle(new MirageRequest());
        Assert.assertEquals(3, getPoolSize(pool));
    }


    private int getPoolSize(ObjectPool pool) {
        try {
            Field field = pool.getClass().getDeclaredField("pool");
            field.setAccessible(true);
            List list = (List)field.get(pool);
            return list.size();
        } catch (NoSuchFieldException e) {
            return -1;
        } catch (IllegalAccessException e) {
            return -1;
        }
    }

}