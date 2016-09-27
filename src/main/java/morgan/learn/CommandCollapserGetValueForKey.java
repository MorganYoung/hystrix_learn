package morgan.learn;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by morgan on 16/9/25.
 */
public class CommandCollapserGetValueForKey extends HystrixCollapser<List<String>, String, Integer> {

    private final Integer key;
    public CommandCollapserGetValueForKey(Integer key) {
        this.key = key;
    }

    @Override
    public Integer getRequestArgument() {
        return key;
    }

    @Override
    protected HystrixCommand<List<String>> createCommand(Collection<CollapsedRequest<String, Integer>> collapsedRequests) {
        return new BatchCommand(collapsedRequests);
    }

    @Override
    protected void mapResponseToRequests(List<String> batchResponse, Collection<CollapsedRequest<String, Integer>> collapsedRequests) {
        int count = 0;
        for (CollapsedRequest<String, Integer> collapsedRequest : collapsedRequests) {
            collapsedRequest.setResponse(batchResponse.get(count ++));
        }
    }

    private static final class BatchCommand extends HystrixCommand<List<String>> {

        private final Collection<CollapsedRequest<String, Integer>> requests;

        private BatchCommand(Collection<CollapsedRequest<String, Integer>> requests) {
            super(
                    Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("expgroup"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("GetValueForKey"))
            );
            this.requests = requests;
        }

        @Override
        protected List<String> run() throws Exception {
            ArrayList<String> response = new ArrayList<String>();
            for (CollapsedRequest<String, Integer> request : requests) {
                response.add("ValueForKey" + request.getArgument());
            }
            return response;
        }
    }

    public static void main(String[] args) {
        /**
         * 使用场景
         * HystrixCollapser用于对对个相同业务的请求合并到一个线程甚至合并到一个连接中执行降低线程交互次数和IO次数，但必须保证他们属于同一依赖
         */
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            Future<String> f1 = new CommandCollapserGetValueForKey(1).queue();
            Future<String> f2 = new CommandCollapserGetValueForKey(2).queue();
            Future<String> f3 = new CommandCollapserGetValueForKey(3).queue();
            Future<String> f4 = new CommandCollapserGetValueForKey(4).queue();

            System.out.println("ValueForKey1".equals(f1.get()));
            System.out.println("ValueForKey2".equals(f2.get()));
            System.out.println("ValueForKey3".equals(f3.get()));
            System.out.println("ValueForKey4".equals(f4.get()));


            System.out.println(HystrixRequestLog.getCurrentRequest().getExecutedCommands().size());
            HystrixCommand<?> hystrixCommand =
                    HystrixRequestLog.getCurrentRequest().getExecutedCommands().toArray(new HystrixCommand<?>[1])[0];

            System.out.println("GetValueForKey".equals(hystrixCommand.getCommandKey().name()));
            System.out.println(hystrixCommand.getExecutionEvents().contains(HystrixEventType.COLLAPSED));
            System.out.println(hystrixCommand.getExecutionEvents().contains(HystrixEventType.SUCCESS));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            context.shutdown();
        }
    }
}
