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
package eu.openanalytics.rpooli.container;

import java.net.MalformedURLException;
import java.net.URL;

import org.arquillian.cube.HostIp;
import org.arquillian.cube.HostPort;
import org.arquillian.cube.containerobject.Cube;
import org.arquillian.cube.containerobject.CubeDockerFile;
import org.arquillian.cube.containerobject.Image;

@Cube(value = "api",
		portBinding = {
				ApiV1Container.HTTP_PORT + "->8080/tcp",
				ApiV1Container.RMI_PORT + "->1100/tcp"})
//@CubeDockerFile(value = "webapp/docker/apiv1")
@Image("ubuntu:18.04")
public class ApiV1Container {
	static final int HTTP_PORT = 8087;
	static final int RMI_PORT = 1113;
	
	@HostIp
	private String dockerHost;

	@HostPort(8080)
	private int port;
	
	public URL getConnectionUrl() {
		try {
			return new URL("http://" + dockerHost + ":" + port);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public String getDockerHost() {
		return dockerHost;
	}

	public int getPort() {
		return port;
	}
}
