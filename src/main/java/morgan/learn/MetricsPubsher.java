package morgan.learn;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by morgan on 16/9/25.
 */
public class MetricsPubsher {

    public static void main(String[] args) throws Exception {

        MetricRegistry registry = new MetricRegistry();
        HystrixCodaHaleMetricsPublisher publisher = new HystrixCodaHaleMetricsPublisher(registry);
        HystrixPlugins.getInstance().registerMetricsPublisher(publisher);

        Graphite graphite = new Graphite(new InetSocketAddress("10.16.6.138",2003));

        GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(registry)
                .prefixedWith("hystrix")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
        graphiteReporter.start(1, TimeUnit.SECONDS);

        for (int i = 0; i < 100000; i++) {

            HelloWorldCommand synchronous = new HelloWorldCommand("synchronous");
            String result = synchronous.execute();

            System.out.println("result = " + result);

            HelloWorldCommand asynchronous = new HelloWorldCommand("asynchronous");
            Future<String> future = asynchronous.queue();
            result = future.get(100, TimeUnit.MILLISECONDS);

            System.out.println("result = " + result);
            System.out.println("mainThread = " + Thread.currentThread().getName());

            Thread.sleep(1000);
        }
    }

}
