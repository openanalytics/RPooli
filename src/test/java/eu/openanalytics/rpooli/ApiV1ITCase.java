
package eu.openanalytics.rpooli;

import static com.github.fge.jackson.JsonLoader.fromString;
import static com.github.fge.jsonschema.SchemaVersion.DRAFTV3;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.net.URI;

import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.fge.jsonschema.cfg.ValidationConfiguration;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.module.jsv.JsonSchemaValidator;

import de.walware.rj.servi.RServiUtil;

public class ApiV1ITCase
{
    private static final String RMI_POOL_ADDRESS = "rmi://127.0.1.1/rpooli-pool";

    private static JsonSchemaFactory jsonSchemaFactory;

    @BeforeClass
    public static void configureRestAssured() throws Exception
    {
        RestAssured.port = Integer.getInteger("api.server.port");
        RestAssured.basePath = System.getProperty("api.server.path") + "/api/v1";

        jsonSchemaFactory = JsonSchemaFactory.newBuilder()
            .setValidationConfiguration(
                ValidationConfiguration.newBuilder().setDefaultVersion(DRAFTV3).freeze())
            .freeze();
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
            .body(matchesJsonSchema("pool"))
            .and()
            .body("address", is(RMI_POOL_ADDRESS));
    }

    @Test
    public void getNodes() throws Exception
    {
        final String json = expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/nodes")
            .then()
            .assertThat()
            .body("nodes", hasSize(1))
            .extract()
            .asString();

        // can't use matchesJsonSchema because of bug:
        // https://code.google.com/p/rest-assured/issues/detail?id=346
        jsonSchemaFactory.getJsonSchema(getSchemaUri("nodes").toString()).validate(fromString(json));
    }

    private static Matcher<?> matchesJsonSchema(final String schema)
    {
        return JsonSchemaValidator.matchesJsonSchema(getSchemaUri(schema)).using(jsonSchemaFactory);
    }

    private static URI getSchemaUri(final String schema)
    {
        return new File("src/main/webapp/raml/schema/" + schema + ".json").toURI();
    }
}
