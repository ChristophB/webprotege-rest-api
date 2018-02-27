package de.onto_med.ontology_service.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.onto_med.ontology_service.api.Timer;
import de.onto_med.ontology_service.data_model.Phenotype;
import de.onto_med.ontology_service.data_model.Property;
import de.onto_med.ontology_service.manager.PhenotypeManager;
import de.onto_med.ontology_service.views.PhenotypeView;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.lha.phenoman.exception.WrongPhenotypeTypeException;
import org.lha.phenoman.model.phenotype.AbstractSinglePhenotype;
import org.lha.phenoman.model.phenotype.top_level.AbstractPhenotype;
import org.lha.phenoman.model.phenotype.top_level.Category;
import org.lha.phenoman.model.phenotype.top_level.RestrictedPhenotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.UnsupportedDataTypeException;
import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Path("/phenotype")
public class PhenotypeResource extends Resource {
	private static final Logger LOGGER = LoggerFactory.getLogger(PhenotypeManager.class);

	private String phenotypePath;
	private boolean navigationVisible = true;

	/**
	 * This is the Cache, which contains all previously loaded phenotypeManagers.
	 * Expiration time is set to 10 minutes after last access.
	 * If a non existent key is used, the cache tries to instantiate a respective PhenotypeManager.
	 */
	private LoadingCache<String, PhenotypeManager> managers = CacheBuilder.newBuilder()
		.expireAfterAccess(10, TimeUnit.MINUTES)
		.build(
			new CacheLoader<String, PhenotypeManager>() {
				@Override
				public PhenotypeManager load(@Nonnull String key) {
					Timer timer = new Timer();
					PhenotypeManager manager = new PhenotypeManager(phenotypePath.replace("%id%", key));

					LOGGER.info("Populated cache with phenotype ontology '" + key + "'. " + timer.getDiff());
					return manager;
				}
			}
		);

	public PhenotypeResource(String rootPath, String phenotypePath) {
		super(rootPath);
		this.phenotypePath = phenotypePath;
	}

	public void setNavigationVisible(boolean navigationVisible) {
		this.navigationVisible = navigationVisible;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getPhenotypeSelectionView(@QueryParam("id") String id) {
		if (StringUtils.isBlank(id)) id = null;
		PhenotypeView view = new PhenotypeView("PhenotypeView.ftl", rootPath, id);
		view.setNavigationVisible(navigationVisible);
		return Response.ok(view).build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.TEXT_HTML)
	public Response getPhenotypeView(@PathParam("id") String id) {
		try {
			return Response.ok(managers.getUnchecked(id).getFullRdfDocument())
				.header(HttpHeaders.CONTENT_DISPOSITION,
					String.format("attachment; filename='cop_%s.owl'", id))
				.build();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage());
		}
	}
	
	@GET
	@Path("/{id}/{iri}")
	@Produces(MediaType.APPLICATION_JSON)
	public Category getPhenotype(@PathParam("id") String id, @PathParam("iri") String iri) {
		PhenotypeManager manager = managers.getUnchecked(id);
		return manager.getPhenotype(iri);
	}

	@GET
	@Path("{id}/{iri}/dependents")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Category> getDependentPhenotypes(@PathParam("id") String id, @PathParam("iri") String iri) {
		PhenotypeManager manager = managers.getUnchecked(id);
		List<Category> phenotypes = manager.getDependentPhenotypes(iri);
		phenotypes.add(manager.getPhenotype(iri));
		return phenotypes;
	}

	@POST
	@Path("{id}/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	public Response deletePhenotypes(@PathParam("id") String id, Set<String> phenotypes) {
		try {
			managers.getUnchecked(id).deletePhenotypes(phenotypes);
		} catch (Exception e) {
			return Response.serverError().build();
		}
		return Response.ok("Phenotypes deleted.").build();
	}

	@GET
	@Path("/{id}/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPhenotypes(@PathParam("id") String id, @QueryParam("type") String type) {
		if ("list".equals(type)) {
			return Response.ok(managers.getUnchecked(id).getList()).build();
		} else {
			return Response.ok(managers.getUnchecked(id).getTaxonomy(true)).build();
		}
	}
	
	@GET
	@Path("/{id}/decision-tree")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getDecisionTree(
		@PathParam("id") String id, @QueryParam("phenotype") String phenotype, @QueryParam("format") String format
	) {
		if (StringUtils.isBlank(phenotype)) throw new WebApplicationException("Query parameter 'phenotype' missing.");

		PhenotypeManager manager = managers.getUnchecked(id);

		try {
			return Response.ok(manager.getPhenotypeDecisionTree(phenotype, format), MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename='" + phenotype + "_decisiontree." + format + "'")
				.build();
		} catch (IllegalArgumentException | IOException e) {
			throw new WebApplicationException(e.getMessage());
		}
	}

	@GET
	@Path("/{id}/phenotype-form")
	@Produces(MediaType.TEXT_HTML)
	public Response getPhenotypeForm(@PathParam("id") String id) {
		PhenotypeView view = new PhenotypeView("PhenotypeForm.ftl", rootPath, id);
		view.setNavigationVisible(navigationVisible);
		return Response.ok(view).build();
	}

	@POST
	@Path("/{id}/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createCategory(@PathParam("id") String id, Phenotype formData) {
		PhenotypeManager manager = managers.getUnchecked(id);

		try {
			if (!formData.getIsPhenotype()) {
				Category category = manager.createCategory(formData);
				return Response.ok("Category '" + category.getName() + "' created.").build();
			} else if (!formData.getIsRestricted()) {
				AbstractPhenotype phenotype = manager.createAbstractPhenotype(formData);
				return Response.ok("Abstract phenotype '" + phenotype.getName() + "' created.").build();
			} else {
				RestrictedPhenotype phenotype = manager.createRestrictedPhenotype(formData);
				return Response.ok("Phenotype '" + phenotype.getName() + "' created.").build();
			}
		} catch (NullPointerException | UnsupportedDataTypeException | WrongPhenotypeTypeException e) {
			throw new WebApplicationException(e.getMessage());
		}
	}

	@GET
	@Path("/{id}/reason-form")
	@Produces({ MediaType.TEXT_HTML })
	public Response getReasonForm(@PathParam("id") String id) {
		PhenotypeView view = new PhenotypeView("PhenotypeReasonForm.ftl", rootPath, id);
		view.setNavigationVisible(navigationVisible);
		return Response.ok(view).build();
	}

	@GET
	@Path("/{id}/reason-form/{iri}")
	@Produces({ MediaType.TEXT_HTML })
	public Response getReasonFormForPhenotype(@PathParam("id") String id, @PathParam("iri") String iri) {
		PhenotypeManager manager = managers.getUnchecked(id);
		List<Category> relatedPhenotypes = manager.getDependentPhenotypes(iri);
		relatedPhenotypes.add(manager.getPhenotype(iri));
		List<AbstractSinglePhenotype> phenotypes = new ArrayList<>();

		for (Category relatedPhenotype : relatedPhenotypes)
			if (relatedPhenotype.isAbstractSinglePhenotype())
				phenotypes.add(relatedPhenotype.asAbstractSinglePhenotype());

		PhenotypeView view = new PhenotypeView("PhenotypeReasonFormForPhenotype.ftl", rootPath, id);
		view.setPhenotypes(phenotypes);
		view.setNavigationVisible(navigationVisible);

		return Response.ok(view).build();
	}

	@POST
	@Path("{id}/reason")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
	public synchronized Response classifyIndividualAsImage(
		@Context HttpHeaders headers, @PathParam("id") String id, List<Property> properties, @QueryParam("format") String format
	) {
		if (properties == null || properties.isEmpty())
			throw new WebApplicationException("No properties were provided.");

		PhenotypeManager manager = managers.getUnchecked(id);

		try {
			if ("png".equals(format)) {
				return Response.ok(Base64.encodeBase64(manager.classifyIndividualAsImage(properties)), MediaType.APPLICATION_OCTET_STREAM)
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename='reasoner_report.png'")
					.build();
			} else {
				if (acceptsMediaType(headers, MediaType.APPLICATION_JSON_TYPE)) {
					return Response.ok(manager.classifyIndividualAsList(properties)).build();
				} else {
					return Response.ok(manager.classifyIndividualAsString(properties)).build();
				}
			}
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage());
		}
	}
}
