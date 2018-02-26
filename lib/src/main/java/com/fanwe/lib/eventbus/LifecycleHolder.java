package com.fanwe.lib.eventbus;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by zhengjun on 2018/2/26.
 */
public final class LifecycleHolder
{
    private Callback mCallback;

    void setCallback(Callback callback)
    {
        mCallback = callback;
    }

    //---------- Activity start ----------

    private WeakReference<Activity> mActivity;

    private Activity getActivity()
    {
        return mActivity == null ? null : mActivity.get();
    }

    public synchronized void setActivity(Activity activity)
    {
        Activity old = getActivity();
        if (old != activity)
        {
            if (old != null)
            {
                old.getApplication().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
            }

            if (activity != null)
            {
                mActivity = new WeakReference<>(activity);
                activity.getApplication().registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
            } else
            {
                mActivity = null;
            }
        }
    }

    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks()
    {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState)
        {
        }

        @Override
        public void onActivityStarted(Activity activity)
        {
        }

        @Override
        public void onActivityResumed(Activity activity)
        {
        }

        @Override
        public void onActivityPaused(Activity activity)
        {
        }

        @Override
        public void onActivityStopped(Activity activity)
        {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState)
        {
        }

        @Override
        public void onActivityDestroyed(Activity activity)
        {
            if (activity == getActivity())
            {
                if (mCallback != null)
                {
                    mCallback.onActivityDestroyed(activity);
                }
                setActivity(null);
            }
        }
    };

    //---------- Activity end ----------

    //---------- View start ----------

    private WeakReference<View> mView;

    private View getView()
    {
        return mView == null ? null : mView.get();
    }

    public synchronized void setView(View view)
    {
        View old = getView();
        if (old != view)
        {
            if (old != null)
            {
                old.removeOnAttachStateChangeListener(mOnAttachStateChangeListener);
            }

            if (view != null)
            {
                mView = new WeakReference<>(view);
                view.addOnAttachStateChangeListener(mOnAttachStateChangeListener);
            } else
            {
                mView = null;
            }
        }
    }

    private View.OnAttachStateChangeListener mOnAttachStateChangeListener = new View.OnAttachStateChangeListener()
    {
        @Override
        public void onViewAttachedToWindow(View v)
        {
            if (mCallback != null)
            {
                mCallback.onViewAttachedToWindow(v);
            }
        }

        @Override
        public void onViewDetachedFromWindow(View v)
        {
            if (mCallback != null)
            {
                mCallback.onViewDetachedFromWindow(v);
            }
        }
    };

    //---------- View end ----------

    public interface Callback
    {
        void onActivityDestroyed(Activity activity);

        void onViewAttachedToWindow(View v);

        void onViewDetachedFromWindow(View v);
    }
}
