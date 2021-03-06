package com.sd.event;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.sd.lib.eventbus.FEventBus;
import com.sd.lib.eventbus.FEventObserver;

public class MainActivity extends AppCompatActivity
{
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置调试模式
        FEventBus.getDefault().setDebug(true);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // 发送事件
                FEventBus.getDefault().post(new TestEvent());
            }
        });
    }

    /**
     * 事件观察者
     */
    private FEventObserver<TestEvent> mEventObserver = new FEventObserver<TestEvent>()
    {
        @Override
        public void onEvent(TestEvent event)
        {
            // 在主线程回调
            Log.i(TAG, String.valueOf(event));
        }
    }.setLifecycle(this); //设置生命周期对象(会自动注册和取消注册观察者)，支持Activity，Dialog，View
}
