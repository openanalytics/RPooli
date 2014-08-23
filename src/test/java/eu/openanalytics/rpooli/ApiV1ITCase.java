
package eu.openanalytics.rpooli;

import static com.github.fge.jsonschema.SchemaVersion.DRAFTV3;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static eu.openanalytics.matchers.JsonSchemaMatcher.matchesJsonSchema;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.jayway.restassured.RestAssured;

import de.walware.rj.servi.RServiUtil;
import eu.openanalytics.matchers.JsonSchemaMatcher;
import eu.openanalytics.rpooli.api.spec.model.NodesJson;

public class ApiV1ITCase
{
    private static final String RMI_POOL_ADDRESS = "rmi://127.0.1.1/rpooli-pool";

    @BeforeClass
    public static void configureRestAssured() throws Exception
    {
        RestAssured.port = Integer.getInteger("api.server.port");
        RestAssured.basePath = System.getProperty("api.server.path") + "/api/v1";

        JsonSchemaMatcher.setJsonSchemaFactory(JsonSchemaFactory.newBuilder()
            .setValidationConfiguration(
                ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV3).freeze())
            .freeze());
    }

    @BeforeClass
    public static void ensureRpooliRmiRunning() throws Exception
    {
        RServiUtil.getRServi(RMI_POOL_ADDRESS, "integration-tests").close();
    }

    @Test
    public void getPool() throws Exception
    {
        expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/pool")
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri(("pool"))))
            .and()
            .body("address", is(RMI_POOL_ADDRESS));
    }

    @Test
    public void getNodes() throws Exception
    {
        expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/nodes")
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("nodes")));
    }

    @Test
    public void getNodeById() throws Exception
    {
        expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/nodes/" + getActiveNodeId())
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("node")));
    }

    @Test
    public void getNodeByIdNotFound() throws Exception
    {
        expect().statusCode(404).when().get("/nodes/_not_a_valid_id");
    }

    @Test
    public void postNodeCommandNotFound() throws Exception
    {
        given().contentType(JSON)
            .body("{\"command\":\"STOP\"}")
            .expect()
            .statusCode(404)
            .when()
            .post("/nodes/_not_a_valid_id/command");
    }

    @Test
    public void postNodeCommandEnableConsole() throws Exception
    {
        given().contentType(JSON)
            .body("{\"command\":\"ENABLE_CONSOLE\"}")
            .expect()
            .statusCode(204)
            .when()
            .post("/nodes/" + getActiveNodeId() + "/command");
    }

    private String getActiveNodeId()
    {
        final NodesJson nodes = expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/nodes")
            .thenReturn()
            .as(NodesJson.class);

        assertThat(nodes.getNodes(), hasSize(greaterThan(0)));

        return nodes.getNodes().get(0).getId();
    }

    private static URI getSchemaUri(final String schema)
    {
        return new File("src/main/webapp/raml/schema/" + schema + ".json").toURI();
    }
}
