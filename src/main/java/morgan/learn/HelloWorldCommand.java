package morgan.learn;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Action2;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by morgan on 16/9/25.
 */
public class HelloWorldCommand extends HystrixCommand<String> {

    private final String name;

    public HelloWorldCommand(String name) {
        super(HystrixCommandGroupKey.Factory.asKey(name));
        this.name = name;
    }

    @Override
    protected String run() throws Exception {
        return "Hello " + name + " thread:" + Thread.currentThread().getName();
    }


    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        /**
         * 使用命令模式封装依赖逻辑
         */

        HelloWorldCommand synchronous = new HelloWorldCommand("synchronous");
        String result = synchronous.execute();

        System.out.println("result = " + result);

        HelloWorldCommand asynchronous = new HelloWorldCommand("asynchronous");
        Future<String> future = asynchronous.queue();
        result = future.get(100, TimeUnit.MILLISECONDS);

        System.out.println("result = " + result);
        System.out.println("mainThread = " + Thread.currentThread().getName());


        //注册观察者事件拦截
        Observable<String> world = new HelloWorldCommand("world").observe();
        world.subscribe(new Action1<String>() {
            public void call(String s) {
                //执行结果处理，result为HelloWorldcommand返回的结果
                System.out.println("call 123 " + s);
            }
        });


        world.subscribe(new Observer<String>() {
            public void onCompleted() {
                System.out.println("execute comeleted");
            }

            public void onError(Throwable e) {
                System.out.println("error : " + e.getMessage());
                e.printStackTrace();
            }

            public void onNext(String s) {
                System.out.println("onNext : " + s);
            }
        });
    }
}
