package morgan.learn;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.sun.tools.javac.util.Assert;

/**
 * Created by morgan on 16/9/25.
 */
public class RequestCacheCommand extends HystrixCommand<String> {

    private final int id;

    public RequestCacheCommand(int id ) {
        super(HystrixCommandGroupKey.Factory.asKey("RequestCacheCommand"));
        this.id = id;
    }

    @Override
    protected String run() throws Exception {
        System.out.println(Thread.currentThread().getName() + " id = " +id);
        return "executed = " + id;
    }

    //重写getcachekey，实现区分不同请求的逻辑
    @Override
    protected String getCacheKey() {
        return String.valueOf(id);
    }

    public static void main(String[] args) {
        /**
         * 请求缓存可以让commandkey，commandgroup相同情况下直接共享结果，将第一来调用册数，高并发
         * 和cachekey碰撞率高长江下提升性能
         */
        HystrixRequestContext context = HystrixRequestContext.initializeContext();

        try{

            RequestCacheCommand cmd2a = new RequestCacheCommand(2);
            RequestCacheCommand cmd2b = new RequestCacheCommand(2);
            System.out.println(cmd2a.execute());
            //isResponseFromCache判断是否在缓存中获取结果
            System.out.println(cmd2a.isResponseFromCache());
            System.out.println(cmd2b.execute());
            System.out.println(cmd2b.isResponseFromCache());
        } finally {
            context.shutdown();
        }

        context = HystrixRequestContext.initializeContext();
        try {
            RequestCacheCommand cmd2c = new RequestCacheCommand(2);
            System.out.println(cmd2c.execute());
            System.out.println(cmd2c.isResponseFromCache());
        } finally {
            context.shutdown();
        }

    }
}
