package morgan.learn;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * Created by morgan on 16/9/25.
 */
public class HelloSemaphoreCommand extends HystrixCommand<String> {

    private final String name;

    public HelloSemaphoreCommand(String name) {
        super(
                Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(name))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE))
        );
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
        return "Thread : " + Thread.currentThread().getName();
    }

    public static void main(String[] args) {
        /**
         * 信号量隔离：SEMAPHORE
         * 隔离本地代码或快速返回远程调用如 mem，redis可以直接使用信号量隔离，降低线程隔离开销
         */
        HelloSemaphoreCommand semaphore = new HelloSemaphoreCommand("semaphore");
        String result = semaphore.execute();
        System.out.println(result);
        System.out.println("main : " + Thread.currentThread().getName());
    }
}
