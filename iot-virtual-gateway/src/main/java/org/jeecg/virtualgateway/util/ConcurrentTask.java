package org.jeecg.virtualgateway.util;

import cn.hutool.cron.task.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jtcl
 * @date 2022/6/30
 */
@Slf4j
public abstract class ConcurrentTask implements Task {
    private final Lock lock = new ReentrantLock();

    @Override
    public void execute() {
        if (log.isDebugEnabled()) {
            log.debug("调试观察线程数");
        }

        // 同一任务不能同时执行 (o´ω`o)
        boolean flag = false;
        try {
            flag = lock.tryLock(3, TimeUnit.SECONDS);
            if (flag) {
                execute1();
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("定时任务执行错误", e);
            }
        } finally {
            if (flag) {
                lock.unlock();
            }
        }
    }

    public abstract void execute1();
}
