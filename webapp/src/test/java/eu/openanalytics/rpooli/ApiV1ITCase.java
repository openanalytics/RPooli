/**
 * This file is part of RPooli.
 *
 * RPooli is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with RPooli.  If not, see <http://www.apache.org/licenses/>.
 */
package eu.openanalytics.rpooli;

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;
import java.net.UnknownHostException;

import org.arquillian.cube.containerobject.Cube;
import org.eclipse.statet.rj.servi.RServiUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.openanalytics.rpooli.api.spec.model.ConfNetResolvedJson;
import eu.openanalytics.rpooli.api.spec.model.ConfNetResolvedJsonParent;
import eu.openanalytics.rpooli.api.spec.model.ConfPoolJson;
import eu.openanalytics.rpooli.api.spec.model.ConfRJson;
import eu.openanalytics.rpooli.api.spec.model.Node;
import eu.openanalytics.rpooli.api.spec.model.Node.State;
import eu.openanalytics.rpooli.api.spec.model.NodesJson;
import eu.openanalytics.rpooli.container.ApiV1Container;
import io.restassured.RestAssured;

/**
 * Run with: <code>mvn clean verify</code>
 *
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@RunWith(Arquillian.class)
public class ApiV1ITCase
{
	private static String RMI_POOL_ADDRESS;
	
	@Cube
    ApiV1Container apiContainer; 
	
	@Before
	public void configureRestAssured() throws UnknownHostException {
		RestAssured.port = apiContainer.getHttpPort();
		RestAssured.basePath = "/rpooli/api/v1";
		RestAssured.baseURI = "http://" + apiContainer.getDockerHost();
		RMI_POOL_ADDRESS = "rmi://" + apiContainer.getCubeIp() + "/rpooli-pool";
	}
    
    @Before
    public void ensureRpooliRmiRunning() throws Exception
    {
    	RServiUtils.getRServi(RMI_POOL_ADDRESS, "integration-tests").close();   
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
            .get("/nodes/" + getOneActiveNodeId())
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("node")));
    }

    @Test
    public void getNodeByIdNotFound() throws Exception
    {
        expect().statusCode(404)
            .when()
            .get("/nodes/_not_a_valid_id")
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("error")));
    }

    @Test
    public void postNodeConsoleFound() throws Exception
    {
        given().expect().statusCode(404).when().post("/nodes/_not_a_valid_id/console");
    }

    @Test
    public void postNodeConsoleEnableAndDisable() throws Exception
    {
        given().expect().statusCode(204).when().post("/nodes/" + getOneActiveNodeId() + "/console");
        given().expect().statusCode(204).when().delete("/nodes/" + getOneActiveNodeId() + "/console");
    }

    @Test
    public void acquireReleaseAndKillTestNode() throws Exception
    {
        assertThat(getActiveNodesCount(), is(1));

        expect().statusCode(204).when().post("/nodes/test");

        // there should be one lent node
        final Node lent = getOneLentNode();
        assertThat(lent, is(notNullValue()));

        expect().statusCode(204).when().delete("/nodes/test");

        // the node should not be lent anymore
        assertThat(getOneLentNode(), is(nullValue()));

        given().expect().statusCode(204).when().delete("/nodes/" + lent.getId());

        // the node should have been killed
        assertThat(getActiveNodesCount(), is(1));
    }

    @Test
    public void getCurrentRConfig() throws Exception
    {
        expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/config/r")
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("conf-r")));
    }

    @Test
    public void getDefaultRConfig() throws Exception
    {
        expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/config/r/default")
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("conf-r")));
    }

    @Test
    public void putInvalidRConfig() throws Exception
    {
        given().contentType(JSON)
            .body("{\"start_stop_timeout\": -2}")
            .expect()
            .statusCode(400)
            .contentType(JSON)
            .when()
            .put("/config/r")
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("error")));
    }

    @Test
    public void putValidRConfigApplyOnly() throws Exception
    {
        final ConfRJson config = fetchCurrentRConfig();

        config.setStartupSnippet("library(RSBXml)\r\nlibrary(RSBJson)");

        given().contentType(JSON).body(config).expect().statusCode(204).when().put("/config/r");
    }

    @Test
    public void putValidRConfigApplyAndSave() throws Exception
    {
        final ConfRJson config = fetchCurrentRConfig();

        config.setStartupSnippet("library(RSBXml)\r\nlibrary(RSBJson)");

        given().contentType(JSON).body(config).expect().statusCode(204).when().put("/config/r?save=true");
    }

    @Test
    public void getCurrentPoolConfig() throws Exception
    {
        expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/config/pool")
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("conf-pool")));
    }

    private ConfRJson fetchCurrentRConfig()
    {
        return expect().statusCode(200).contentType(JSON).when().get("/config/r").body().as(ConfRJson.class);
    }

    @Test
    public void getDefaultPoolConfig() throws Exception
    {
        expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/config/pool/default")
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("conf-pool")));
    }

    @Test
    public void putInvalidPoolConfig() throws Exception
    {
        final ConfPoolJson config = fetchCurrentPoolConfig();

        config.setMaxTotalNodes(-1L);

        given().contentType(JSON)
            .body(config)
            .expect()
            .statusCode(400)
            .contentType(JSON)
            .when()
            .put("/config/pool")
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("error")));
    }

    @Test
    public void putValidPoolConfigApplyOnly() throws Exception
    {
        final ConfPoolJson config = fetchCurrentPoolConfig();

        config.setMaxNodeReuse(100L);

        given().contentType(JSON).body(config).expect().statusCode(204).when().put("/config/pool");
    }

    @Test
    public void putValidPoolConfigApplyAndSave() throws Exception
    {
        final ConfPoolJson config = fetchCurrentPoolConfig();

        config.setMaxNodeReuse(100L);

        given().contentType(JSON).body(config).expect().statusCode(204).when().put("/config/pool?save=true");
    }

    private ConfPoolJson fetchCurrentPoolConfig()
    {
        return expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/config/pool")
            .body()
            .as(ConfPoolJson.class);
    }

    @Test
    public void getCurrentNetConfig() throws Exception
    {
        final ConfNetResolvedJson config = fetchCurrentNetConfig();

        validateNetConfig(config);
    }

    @Test
    public void getDefaultNetConfig() throws Exception
    {
        // same issue
        final ConfNetResolvedJson config = expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/config/net/default")
            .body()
            .as(ConfNetResolvedJson.class);

        validateNetConfig(config);
    }

    @Test
    public void putInvalidNetConfig() throws Exception
    {
        final ConfNetResolvedJsonParent config = asPutableNetConfig(fetchCurrentNetConfig());

        config.setHost("not a valid host name");

        given().contentType(JSON)
            .body(config)
            .expect()
            .statusCode(400)
            .contentType(JSON)
            .when()
            .put("/config/net")
            .then()
            .assertThat()
            .body(matchesJsonSchema(getSchemaUri("error")));
    }

    @Test
    public void putValidNetConfigApplyOnly() throws Exception
    {
        final ConfNetResolvedJsonParent config = asPutableNetConfig(fetchCurrentNetConfig());

        given().contentType(JSON).body(config).expect().statusCode(204).when().put("/config/net");
    }

    @Test
    public void putValidNetConfigApplyAndSave() throws Exception
    {
        final ConfNetResolvedJsonParent config = asPutableNetConfig(fetchCurrentNetConfig());

        given().contentType(JSON).body(config).expect().statusCode(204).when().put("/config/net?save=true");
    }

    private ConfNetResolvedJson fetchCurrentNetConfig()
    {
        return expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/config/net")
            .body()
            .as(ConfNetResolvedJson.class);
    }

    private void validateNetConfig(final ConfNetResolvedJson config)
    {
        // the schema validator doesn't understand extension so all we can check is that the config
        // object has been deserialized and some expected values are there
        assertThat(config, is(instanceOf(ConfNetResolvedJson.class)));
        assertThat(config.getEffectiveHost(), not(emptyOrNullString()));
        assertThat(config.getStartEmbeddedRegistry(), is(true));
    }

    private ConfNetResolvedJsonParent asPutableNetConfig(final ConfNetResolvedJson config)
    {
        return new ConfNetResolvedJsonParent().withEnabledSsl(config.getEnabledSsl())
            .withHost(config.getHost())
            .withPort(config.getPort())
            .withStartEmbeddedRegistry(config.getStartEmbeddedRegistry());
    }

    private Node getOneLentNode()
    {
        for (final Node node : getActiveNodes().getNodes())
        {
            if (node.getState() == State.ALLOCATED)
            {
                return node;
            }
        }
        return null;
    }

    private String getOneActiveNodeId()
    {
        final NodesJson nodes = getActiveNodes();
        assertThat(nodes.getNodes(), hasSize(greaterThan(0)));
        return nodes.getNodes().get(0).getId();
    }

    private int getActiveNodesCount()
    {
        return getActiveNodes().getNodes().size();
    }

    private NodesJson getActiveNodes()
    {
        return expect().statusCode(200)
            .contentType(JSON)
            .when()
            .get("/nodes")
            .thenReturn()
            .as(NodesJson.class);
    }

    private static URI getSchemaUri(final String schema)
    {
        return new File("src/main/webapp/raml/schema/" + schema + ".json").toURI();
    }
}
