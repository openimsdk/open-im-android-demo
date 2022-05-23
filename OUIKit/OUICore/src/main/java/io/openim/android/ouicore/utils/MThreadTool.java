package io.openim.android.ouicore.utils;



import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MThreadTool {
    /**
     * 总共多少任务（根据CPU个数决定创建活动线程的个数,这样取的好处就是可以让手机承受得住）
     */
    private static final int count = Runtime.getRuntime().availableProcessors() * 3 + 2;
    /**
     * 每次只执行一个任务的线程池
     */
    public static ExecutorService singleTaskExecutor = Executors
            .newSingleThreadExecutor();

    /**
     * 每次执行限定个数个任务的线程池
     */
    public static ExecutorService limitedTaskExecutor = Executors
            .newFixedThreadPool(count);

    /**
     * 所有任务都一次性开始的线程池,线程池大小完全依赖于操作系统（或者说JVM）能够创建的最大线程大小。如果线程池的大小超过了处理任务所需要的线程，
     * 那么就会回收部分空闲（60秒不执行任务）的线程,当任务数增加时，此线程池又可以智能的添加新线程来处理任务。
     */
    public static ExecutorService allTaskExecutor = Executors
            .newCachedThreadPool();

    // 关闭线程
    public static void ThreadShutdown(ExecutorService service) {
        if (service != null && !service.isShutdown()) {
            service.shutdownNow();
            System.out.println("线程销毁");
        }
    }

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    /**
     * 推荐用这种线程池
     *
     * 1.线程池中的核心线程数，当提交一个任务时，线程池创建一个新线程执行任务，直到当前线程数等于corePoolSize；
     * 如果当前线程数为corePoolSize，继续提交的任务被保存到阻塞队列中，等待被执行；
     * 如果执行了线程池的prestartAllCoreThreads()方法，线程池会提前创建并启动所有核心线程。
     * 2.线程池中允许的最大线程数。如果当前阻塞队列满了，且继续提交任务，则创建新的线程执行任务，前提是当前线程数小于maximumPoolSize
     * 3.线程空闲时的存活时间，即当线程没有任务执行时，继续存活的时间。默认情况下，该参数只在线程数大于corePoolSize时才有用
     * 4.存活时间单位
     * 5.workQueue必须是BlockingQueue阻塞队列。当线程池中的线程数超过它的corePoolSize的时候，线程会进入阻塞队列进行阻塞等待。通过workQueue，线程池实现了阻塞功能
     * 6.创建线程的工厂，通过自定义的线程工厂可以给每个新建的线程设置一个具有识别度的线程名
     * 7.线程池的饱和策略，当阻塞队列满了，且没有空闲的工作线程，如果继续提交任务，必须采取一种策略处理该任务，线程池提供了4种策略：
     （1）AbortPolicy：直接抛出异常，默认策略；
     （2）CallerRunsPolicy：用调用者所在的线程来执行任务；
     （3）DiscardOldestPolicy：丢弃阻塞队列中靠最前的任务，并执行当前任务；
     （4）DiscardPolicy：直接丢弃任务；
     *
     */
    public static final ExecutorService executorService = new ThreadPoolExecutor(NUMBER_OF_CORES,
            NUMBER_OF_CORES * 2, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new DefaultThreadFactory(),
            new ThreadPoolExecutor.DiscardPolicy());


    private static class DefaultThreadFactory implements ThreadFactory {

        int threadNum = 0;


        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            final Thread result = new Thread(runnable, "MThreadTool-pool-thread-" + threadNum);
            threadNum++;
            return result;
        }
    }
}
