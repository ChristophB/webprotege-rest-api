package de.uni_leipzig.imise.webprotege.rest_api.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_leipzig.imise.webprotege.rest_api.metaproject.MetaProjectManager;
import de.uni_leipzig.imise.webprotege.rest_api.project.ProjectManager;

public abstract class Resource {
	final static Logger logger = LoggerFactory.getLogger(MetaProjectResource.class);
	/**
	 * Path to WebProtegés data folder.
	 */
	protected String dataPath;
	
	
	public Resource(String dataPath) {
		this.dataPath = dataPath;
	}
	
	
	protected ProjectManager getProjectManager(String projectId) throws Exception {
		return new MetaProjectManager(dataPath).getProjectManager(projectId);
	}
}
