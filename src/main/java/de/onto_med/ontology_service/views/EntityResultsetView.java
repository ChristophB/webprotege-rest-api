package de.onto_med.ontology_service.views;

import java.util.List;

import de.onto_med.ontology_service.data_model.Entity;

public class EntityResultsetView extends RestApiView {
	
	private final List<Entity> resultset;
	
	public EntityResultsetView(String rootPath, List<Entity> resultset) {
		super("EntityResultset.ftl", rootPath);
		this.resultset = resultset;
	}
	
	public List<Entity> getResultset() {
		return resultset;
	}

}
