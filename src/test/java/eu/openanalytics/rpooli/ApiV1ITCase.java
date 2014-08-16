
package eu.openanalytics.rpooli;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

import de.walware.rj.servi.RServiUtil;
import eu.openanalytics.rpooli.api.spec.model.Nodes;

public class ApiV1ITCase
{
    private static final String RMI_POOL_ADDRESS = "rmi://127.0.1.1/rpooli-pool";

    @BeforeClass
    public static void configureRestAssured()
    {
        RestAssured.port = Integer.getInteger("api.server.port");
        RestAssured.basePath = System.getProperty("api.server.path") + "/api/v1";
    }

    @BeforeClass
    public static void ensureRpooliRmiRunning() throws Exception
    {
        RServiUtil.getRServi(RMI_POOL_ADDRESS, "integration-tests").close();
    }

    @Test
    public void getNodes()
    {
        final Nodes nodes = expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/nodes")
            .body()
            .as(Nodes.class);

        assertThat(nodes.getNodes(), hasSize(1));

        // TODO validate schema:
        // https://code.google.com/p/rest-assured/wiki/Usage#JSON_Schema_validation
    }
}
