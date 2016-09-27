package morgan.learn;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by morgan on 16/9/26.
 */
public class JettyDashboardServer {

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    private Server server = null;
    public void init() {
        try{
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        server = new Server(8081);
                        WebAppContext webAppContext = new WebAppContext();
                        webAppContext.setContextPath("/");
                        webAppContext.setWar("/Users/morgan/github/hystrix_learn/hystrix-dashboard-1.5.5.war");
                        //webAppContext.setResourceBase(".");
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

    public static void main(String[] args) {
        JettyDashboardServer server = new JettyDashboardServer();
        server.init();
    }


}
