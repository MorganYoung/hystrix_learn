package morgan.learn;

import com.netflix.hystrix.*;

import java.util.concurrent.TimeUnit;

/**
 * Created by morgan on 16/9/25.
 */
public class HelloWorldWithFalllCommand extends HystrixCommand<String> {

    private final String name;

    public HelloWorldWithFalllCommand(String name) {
        //使用HystrixCommandGroupKey工厂定义，
        //命令分组用户队依赖操作分组，便于统计、汇总等。
        //Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(name));
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(name))
                //设置依赖超时时间
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(500))
                        //依赖命名：CommandKey,每个CommandKey代表一个依赖抽象，相同的依赖要使用相同的commandkey名称。
                        // 依赖隔离的根本就是对相同的commandkey的依赖做隔离
                .andCommandKey(HystrixCommandKey.Factory.asKey("HelloWorld"))
                        //使用HystrixThreadPoolKey工厂定义线程池
                        //对同一业务来做隔离时使用commandGroup作区分，但是对同一依赖的不同远程调用（例如一个是redis，一个是http），
                        //可以使用HystrixThreadPoolKey最隔离区分。
                        //虽然在业务上都是相同的组，但是需要在资源上做隔离时，可以使用HystrixThreadPoolKey区分。
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("HelloWorldPool")));
        this.name = name;
    }



    @Override
    protected String getFallback() {
        return "execute falled";
    }


    @Override
    protected String run() throws Exception {
        //sleep 1秒会超时，执行getFallback
        TimeUnit.SECONDS.sleep(1);
        return "Hello " + name + " thread:" + Thread.currentThread().getName();
    }

    public static void main(String[] args) {
        HelloWorldWithFalllCommand helloWorldWithFalllCommand = new HelloWorldWithFalllCommand("test fall");
        String result = helloWorldWithFalllCommand.execute();
        System.out.println(result);
    }
}
