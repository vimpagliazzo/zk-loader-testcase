import org.eclipse.jetty.util.resource.Resource;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by impagliazzo on 08/02/16.
 */
public class Runner {

    public static void main(String[] args) throws Exception {

        new Main(getWebResources());

    }

    private static Resource[] getWebResources() {
        try {
            Resource res = Main.getWebUIrootResource("ui");
            Resource res2 = Main.getWebUIrootResource("ui2");
            return new Resource[]{res, res2};
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
