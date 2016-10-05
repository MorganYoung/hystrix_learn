package morgan.learn;


import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import java.util.concurrent.*;

/**
 * Created by morgan on 16/9/25.
 */
public class JettyServer {

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private Server server = null;
    public void init() {
        try{
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        server = new Server(8088);
                        WebAppContext webAppContext = new WebAppContext();
                        webAppContext.setContextPath("/");
                        webAppContext.addServlet(HystrixMetricsStreamServlet.class, "/hys.stream");
//                        webAppContext.addServlet(Turbin, "/hys.stream");
                        webAppContext.setResourceBase(".");
                        server.setHandler(webAppContext);
                        server.start();
                        server.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destory() {
        if (server != null) {
            try{
                server.stop();
                server.destroy();
                System.out.println("jetty stop ...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        JettyServer server = new JettyServer();
        server.init();



        for (int i = 0; i < 100000; i++) {

            HelloWorldCommand synchronous = new HelloWorldCommand("synchronous");
            String result = synchronous.execute();

            System.out.println("result = " + result);

            HelloWorldCommand asynchronous = new HelloWorldCommand("asynchronous");
            Future<String> future = asynchronous.queue();
            result = future.get(100, TimeUnit.MILLISECONDS);

            System.out.println("result = " + result);
            System.out.println("mainThread = " + Thread.currentThread().getName());

            /*HelloWorldWithFalllCommand helloWorldWithFalllCommand = new HelloWorldWithFalllCommand("test fall");
            result = helloWorldWithFalllCommand.execute();
            System.out.println(result);

            CommandWithFallBackWiaNetWork cmd = new CommandWithFallBackWiaNetWork(1);
            System.out.println(cmd.execute());
*/
            Thread.sleep(1000);
        }
    }
}
