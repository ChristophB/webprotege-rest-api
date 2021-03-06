package de.onto_med.ontology_service;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.server.Response;
import org.junit.Test;

public class TasksTest extends AbstractTest {
	private static final String ADMIN_URL = "http://localhost:8081" + RULE.getConfiguration().getRootPath();

	@Test
    public void testClearCacheTask() {
    	javax.ws.rs.core.Response response
			= client.target(ADMIN_URL + "/tasks/clear_cache")
			.request(MediaType.APPLICATION_JSON_TYPE)
			.post(null);
    	
    	assertThat(( response).getStatus()).isEqualTo(Response.SC_OK);
    }
	
}
