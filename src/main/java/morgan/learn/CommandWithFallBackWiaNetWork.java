package morgan.learn;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixThreadPoolKey;

/**
 * Created by morgan on 16/9/25.
 */
public class CommandWithFallBackWiaNetWork extends HystrixCommand<String> {

    private final int id;

    protected CommandWithFallBackWiaNetWork(int id) {
        super(
                Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceX"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("getValueCommand"))
        );
        this.id = id;
    }

    @Override
    protected String run() throws Exception {
        throw new RuntimeException("force failure for example");
    }

    @Override
    protected String getFallback() {
        System.out.println("getFallBackThread:" + Thread.currentThread().getName() );
        return new FallBackViaNetwork(id).execute();
    }


    private static class FallBackViaNetwork extends HystrixCommand<String> {


        private final int id;

        public FallBackViaNetwork(int id) {
            /**
             * 依赖调用和降级调用使用不同的线程池隔离，防止上层线程池泡满影响二级降级逻辑调用
             */
            super(
                    Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceX"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("getValueFallbackCommand"))
                            //使用不同的线程池做隔离，防止上传线程池泡满，影响降级逻辑
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("RemoteServiceXFallback"))
            );
            this.id = id;
        }

        @Override
        protected String run() throws Exception {
            //MemcachedClient.getValue(id)
            System.out.println("FallBackThread:" + Thread.currentThread().getName());
            return "somevalue" ;
        }

        @Override
        protected String getFallback() {
            return null;
        }
    }

    public static void main(String[] args) {
        CommandWithFallBackWiaNetWork cmd = new CommandWithFallBackWiaNetWork(1);
        System.out.println(cmd.execute());
    }
}
