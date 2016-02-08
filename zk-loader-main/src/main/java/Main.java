import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.au.http.DHtmlUpdateServlet;
import org.zkoss.zk.ui.http.DHtmlLayoutServlet;
import org.zkoss.zk.ui.http.HttpSessionListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by impagliazzo on 08/02/16.
 */
public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class.getName());

    private final Resource[] webResources;

    public Main(Resource[] webResources) throws Exception {
        this.webResources = webResources;

        Server server = new Server();
        setupHttpConnector(server);

        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        contextHandler.setBaseResource(new ResourceCollection(this.webResources));
        contextHandler.addEventListener(new MyContextListener());

        server.setHandler(contextHandler);
        logger.info("Starting server");
        server.start();

        //start CLI
        boolean exit = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String command;
        while (!exit) {
            System.out.print("QClassic> ");
            command = br.readLine();
            try {
                exit = runCommand(command);
            } catch (Exception e) {
                logger.warn("Unexpected error on CLI: " + e.getMessage());
            }
        }

        System.exit(0);


    }

    private boolean runCommand(String command) {
        if (command == null || command.equals("exit")) {
            return true;
        }
        System.out.println("Unrecognized command " + command+", type exit to exit");
        return false;
    }

    private void setupHttpConnector(Server server){
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setRequestHeaderSize(1024*24);
        ServerConnector impaqtConnector = new ServerConnector(
                server, new HttpConnectionFactory(httpConfiguration));
        impaqtConnector.setPort(8090);
        impaqtConnector.setName("UI");
        server.addConnector(impaqtConnector);
    }

    protected class MyContextListener implements ServletContextListener {

        public void contextDestroyed(ServletContextEvent event) {
        }

        public void contextInitialized(ServletContextEvent event) {
            ServletContext context = event.getServletContext();

            // This method register servlets in the contexts
            // It can be overridden and therefore new servlets registered
            registerServlets(context);

        }
    }

    protected void registerServlets(ServletContext context) {
        ServletRegistration.Dynamic zul = context.addServlet("zul", DHtmlLayoutServlet.class);
        zul.setInitParameter("update-uri", "/zkau");
        zul.setLoadOnStartup(1);
        zul.addMapping("*.zul", "*.zhtml");

        ServletRegistration.Dynamic zkau = context.addServlet("zkau", DHtmlUpdateServlet.class);
        zkau.addMapping("/zkau/*");

        ServletRegistration.Dynamic files = context.addServlet("files", DefaultServlet.class);
        files.addMapping("/");

        context.addListener(HttpSessionListener.class);
    }

    public static Resource getWebUIrootResource(String uiName) throws IOException, URISyntaxException {
        Resource resource;

        String path = "WEB-INF/"+uiName+".txt";
        URL uiURL = Main.class.getClassLoader().getResource(path);

        if(uiURL == null) return null;
        System.out.println("uiURL: " + uiURL.toString());
        String resourcePath = uiURL.toString();
        logger.info("Found web resources in " + uiName);

        //remove WEB-INF... from path
        if (resourcePath.startsWith("jar:file")) {
            uiURL = new URL(resourcePath.substring("jar:".length(), resourcePath.length() - ("!"+path).length() - 1));
            resource = JarResource.newJarResource(new PathResource(uiURL));
        } else {
            String filePath = resourcePath.substring(0, resourcePath.length() - path.length());
            if (filePath.endsWith("target/classes/")) {
                //try to find resources directory if we are in a maven project
                String mavenPath = filePath.substring(0, filePath.length() - "target/classes/".length()) + "src/main/resources/";
                if (new File(mavenPath.substring("file:".length())).exists()) {
                    filePath = mavenPath;
                }
            }
            uiURL = new URL(filePath);
            resource = new PathResource(uiURL);
        }

        return resource;
    }

}
