/*
 * Copyright (C) 2017 zhengjun, fanwe (http://www.fanwe.com)
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
package com.fanwe.lib.eventbus;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by zhengjun on 2018/1/31.
 */
public class FEventBus
{
    private static FEventBus sInstance;
    private final Map<Class, List<FEventObserver>> MAP_OBSERVER = new LinkedHashMap<>();
    private final Map<Class, Object> MAP_STICKY = new HashMap<>();
    private Handler mHandler;

    private boolean mIsDebug;

    public FEventBus()
    {
        // 保持Public，支持创建新的对象
    }

    public static FEventBus getDefault()
    {
        if (sInstance == null)
        {
            synchronized (FEventBus.class)
            {
                if (sInstance == null)
                    sInstance = new FEventBus();
            }
        }
        return sInstance;
    }

    public void setDebug(boolean debug)
    {
        mIsDebug = debug;
    }

    private Handler getHandler()
    {
        if (mHandler == null)
            mHandler = new Handler(Looper.getMainLooper());
        return mHandler;
    }

    /**
     * 发送粘性事件
     *
     * @param event
     */
    public synchronized void postSticky(Object event)
    {
        if (event == null)
            return;

        final Class clazz = event.getClass();
        MAP_STICKY.put(clazz, event);

        if (mIsDebug)
            Log.i(FEventBus.class.getSimpleName(), "postSticky:" + event);

        post(event);
    }

    /**
     * 移除某个粘性事件
     *
     * @param clazz
     */
    public synchronized void removeSticky(Class clazz)
    {
        MAP_STICKY.remove(clazz);
    }

    /**
     * 移除所有粘性事件
     */
    public synchronized void removeAllSticky()
    {
        MAP_STICKY.clear();
    }

    /**
     * 发送事件
     *
     * @param event
     */
    public synchronized void post(final Object event)
    {
        if (event == null)
            return;

        final Class clazz = event.getClass();
        final List<FEventObserver> holder = MAP_OBSERVER.get(clazz);

        if (holder == null)
            return;

        if (mIsDebug)
            Log.i(FEventBus.class.getSimpleName(), "post----->" + event + " " + holder.size());

        int count = 0;
        for (FEventObserver item : holder)
        {
            notifyObserver(item, event);
            if (mIsDebug)
            {
                count++;
                Log.i(FEventBus.class.getSimpleName(), "notify " + count + " " + item);
            }
        }
    }

    private void notifyObserver(final FEventObserver observer, final Object event)
    {
        if (Looper.myLooper() == Looper.getMainLooper())
        {
            observer.onEvent(event);
        } else
        {
            getHandler().post(new Runnable()
            {
                @Override
                public void run()
                {
                    observer.onEvent(event);
                }
            });
        }
    }

    /**
     * 注册观察者
     *
     * @param observer
     */
    public synchronized void register(final FEventObserver observer)
    {
        if (observer == null)
            return;

        final Class clazz = observer.mEventClass;
        if (clazz == null)
            throw new NullPointerException("observer's event class is null");

        List<FEventObserver> holder = MAP_OBSERVER.get(clazz);
        if (holder == null)
        {
            holder = new CopyOnWriteArrayList<>();
            MAP_OBSERVER.put(clazz, holder);
        }

        if (holder.contains(observer))
            return;

        holder.add(observer);

        if (mIsDebug)
            Log.i(FEventBus.class.getSimpleName(), "register:" + observer + " (" + clazz.getName() + " " + holder.size() + ")");

        final Object sticky = MAP_STICKY.get(clazz);
        if (sticky != null)
        {
            notifyObserver(observer, sticky);
            if (mIsDebug)
                Log.i(FEventBus.class.getSimpleName(), "notify sticky when register:" + sticky);
        }
    }

    /**
     * 取消注册观察者
     *
     * @param observer
     */
    public synchronized void unregister(final FEventObserver observer)
    {
        if (observer == null)
            return;

        final Class clazz = observer.mEventClass;
        if (clazz == null)
            throw new NullPointerException("observer's event class is null");

        final List<FEventObserver> holder = MAP_OBSERVER.get(clazz);

        if (holder == null)
            return;

        if (holder.remove(observer))
        {
            if (mIsDebug)
                Log.e(FEventBus.class.getSimpleName(), "unregister:" + observer + " (" + clazz.getName() + " " + holder.size() + ")");
        }

        if (holder.isEmpty())
            MAP_OBSERVER.remove(clazz);
    }
}
