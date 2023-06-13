package io.openim.android.ouicore.vm;

import java.util.Timer;
import java.util.TimerTask;

import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;

public class CounterVM extends BaseVM {
    public State<Integer> num = new State<>(0);
    private Timer timer;

    public void setCountdown(int numL) {
        schedule(numL, new TimerTask() {
            @Override
            public void run() {
                if (num.getValue() <= 0) {
                    timer.cancel();
                    timer = null;
                    return;
                }
                num.postValue(num.getValue() - 1);
            }
        });
    }

    public void setIncrease() {
        setIncrease(0);
    }

    public void setIncrease(int numL) {
        schedule(numL, new TimerTask() {
            @Override
            public void run() {
                num.postValue(num.getValue() + 1);
            }
        });
    }


    private void schedule(int numL, TimerTask timerTask) {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        num.setValue(numL);
        timer.schedule(timerTask, 1000, 1000);
    }


    public void stopCountdown() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopCountdown();
    }
}
