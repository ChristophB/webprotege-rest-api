package de.onto_med.ontology_service;

import de.onto_med.ontology_service.data_model.Phenotype;
import org.eclipse.jetty.server.Response;
import org.junit.*;
import org.lha.phenoman.exception.WrongPhenotypeTypeException;
import org.lha.phenoman.man.PhenotypeOntologyManager;
import org.lha.phenoman.model.phenotype.*;
import org.lha.phenoman.model.phenotype.top_level.Category;
import org.lha.phenoman.model.phenotype.top_level.PhenotypeRange;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdatePhenotypeTest extends AbstractTest {
	private static final String ID            = String.valueOf(new Date().getTime());
	private static final String ONTOLOGY_PATH = RULE.getConfiguration().getPhenotypePath().replace("%id%", ID);
	private static final String UPDATE_PATH   = "/phenotype/" + ID + "/create";

	@Before
	public void createPhenotypes() {
		String id = "Double_Phenotype_1";
		Phenotype phenotype = new Phenotype() {{
			setIsPhenotype(true);
			setIsRestricted(false);
			getTitles().add("Abstract_" + id);
			setDatatype("numeric");
			setSynonyms(Arrays.asList("Label EN", "Label DE"));
			setSynonymLanguages(Arrays.asList("en", "de"));
			setDescriptions(Arrays.asList("Description EN", "Description DE"));
			setDescriptionLanguages(Arrays.asList("en", "de"));
			setRelations(Arrays.asList("IRI 1", "IRI 2"));
			setUcum("kg");
			setIsDecimal(true);
		}};

		javax.ws.rs.core.Response response
			= client.target(url + UPDATE_PATH)
			.request(MediaType.APPLICATION_JSON_TYPE)
			.post(Entity.json(phenotype));

		assertThat(response.getStatus()).isEqualTo(Response.SC_OK);

		phenotype = new Phenotype() {{
			setIsPhenotype(true);
			setIsRestricted(true);
			getTitles().add("Restricted_" + id);
			setDatatype("numeric");
			setSynonyms(Arrays.asList("Label EN", "Label DE"));
			setSynonymLanguages(Arrays.asList("en", "de"));
			setDescriptions(Arrays.asList("Description EN", "Description DE"));
			setDescriptionLanguages(Arrays.asList("en", "de"));
			setRelations(Arrays.asList("IRI 1", "IRI 2"));
			setSuperPhenotype("Abstract_" + id);
			setRangeMin("5.3");
			setRangeMinOperator(">=");
			setRangeMax("10.7");
			setRangeMaxOperator("<");
		}};

		response
			= client.target(url + UPDATE_PATH)
			.request(MediaType.APPLICATION_JSON_TYPE)
			.post(Entity.json(phenotype));

		assertThat(response.getStatus()).isEqualTo(Response.SC_OK);
	}

	@Test
	public void testUpdatePhenotypeWithSameType() {
		String id = "Double_Phenotype_1";

		Phenotype phenotype = new Phenotype() {{
			setIsPhenotype(true);
			setIsRestricted(true);
			getTitles().add("Restricted_" + id);
			setDatatype("numeric");
			setSynonyms(Arrays.asList("Label EN", "Label DE"));
			setSynonymLanguages(Arrays.asList("en", "de"));
			setDescriptions(Arrays.asList("Description EN", "Description DE"));
			setDescriptionLanguages(Arrays.asList("en", "de"));
			setRelations(Arrays.asList("IRI 1", "IRI 2"));
			setSuperPhenotype("Abstract_" + id);
			setRangeMin("8");
			setRangeMinOperator(">");
			setRangeMax("12");
			setRangeMaxOperator("<=");
		}};

		javax.ws.rs.core.Response response
			= client.target(url + UPDATE_PATH)
			.request(MediaType.APPLICATION_JSON_TYPE)
			.post(Entity.json(phenotype));

		assertThat(response.getStatus()).isEqualTo(Response.SC_OK);

		PhenotypeOntologyManager manager = new PhenotypeOntologyManager(ONTOLOGY_PATH, false);
		Category                 actual  = manager.getPhenotype("Restricted_" + id);

		RestrictedSinglePhenotype expected = manager.getPhenotypeFactory().createRestrictedSinglePhenotype(
			"Restricted_" + id, "Abstract_" + id,
			new PhenotypeRange(new OWLFacet[]{ OWLFacet.MIN_EXCLUSIVE, OWLFacet.MAX_INCLUSIVE }, new Double[]{ 8.0, 12.0 }));
		expected.addDescription("Description EN", "en");
		expected.addDescription("Description DE", "de");
		expected.addLabel("Label EN", "en");
		expected.addLabel("Label DE", "de");
		expected.addRelatedConcept("IRI 1");
		expected.addRelatedConcept("IRI 2");

		assertThat(actual.isRestrictedSinglePhenotype()).isTrue();
		assertThat(actual.asRestrictedSinglePhenotype().getDatatype()).isEqualTo(OWL2Datatype.XSD_DOUBLE);
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void testUpdatePhenotypeWithSameTypeByApi() throws WrongPhenotypeTypeException {
		PhenotypeOntologyManager manager = new PhenotypeOntologyManager(ONTOLOGY_PATH, false);
		PhenotypeFactory         factory = manager.getPhenotypeFactory();

		manager.addAbstractSinglePhenotype(factory.createAbstractSinglePhenotype("Weight", OWL2Datatype.XSD_DOUBLE));
		manager.addRestrictedSinglePhenotype(factory.createRestrictedSinglePhenotype(
			"High weight", "Weight", new PhenotypeRange(new OWLFacet[]{ OWLFacet.MIN_INCLUSIVE }, new Double[]{ 100.0 })
		));
		manager.write();

		RestrictedSinglePhenotype update = factory.createRestrictedSinglePhenotype(
			"High weight", "Weight", new PhenotypeRange(new OWLFacet[]{ OWLFacet.MIN_INCLUSIVE }, new Double[]{ 110.0 })
		);
		manager.addRestrictedSinglePhenotype(update);
		manager.write();
		assertThat(manager.getPhenotype(update.getName())).isEqualTo(update);
	}

	@Test
	public void testUpdatePhenotypeWithDifferentType() {
		String title = "Abstract_Double_Phenotype_1";

		Phenotype phenotype = new Phenotype() {{
			setIsPhenotype(true);
			setIsRestricted(false);
			getTitles().add(title);
			setDatatype("boolean");
			setSynonyms(Arrays.asList("Label EN", "Label2 DE"));
			setSynonymLanguages(Arrays.asList("en", "de"));
			setDescriptions(Arrays.asList("Description EN", "Description DE"));
			setDescriptionLanguages(Arrays.asList("en", "de"));
			setRelations(Arrays.asList("IRI 3", "IRI 2"));
		}};

		javax.ws.rs.core.Response response
			= client.target(url + UPDATE_PATH)
			.request(MediaType.APPLICATION_JSON_TYPE)
			.post(Entity.json(phenotype));

		assertThat(response.getStatus()).isEqualTo(Response.SC_OK);
	}

	@Test(expected = WrongPhenotypeTypeException.class)
	public void testUpdatePhenotypeWithDifferentTypeByApi() throws WrongPhenotypeTypeException {
		PhenotypeOntologyManager manager = new PhenotypeOntologyManager(ONTOLOGY_PATH, false);
		PhenotypeFactory         factory = manager.getPhenotypeFactory();

		AbstractSinglePhenotype phenotype = factory.createAbstractSinglePhenotype("Weight", OWL2Datatype.XSD_DOUBLE);
		Category                category  = factory.createCategory("Anthropometric");
		try {
			manager.addAbstractSinglePhenotype(phenotype);
		} catch (WrongPhenotypeTypeException ignored) {	}
		manager.addPhenotypeCategory(category);
		manager.write();

		manager.addAbstractBooleanPhenotype(factory.createAbstractBooleanPhenotype(phenotype.getName(), category.getName()));
		manager.write();
	}

	@Test
	public void testUpdatePhenotypeWithDifferentSingleTypeByApi() throws WrongPhenotypeTypeException {
		PhenotypeOntologyManager manager = new PhenotypeOntologyManager(ONTOLOGY_PATH, false);
		PhenotypeFactory         factory = manager.getPhenotypeFactory();

		AbstractSinglePhenotype phenotype = factory.createAbstractSinglePhenotype("Weight", OWL2Datatype.XSD_DOUBLE);
		Category                category  = factory.createCategory("Anthropometric");

		try {
			manager.addAbstractSinglePhenotype(phenotype);
		} catch (WrongPhenotypeTypeException ignored) { }
		manager.addPhenotypeCategory(category);
		manager.write();

		manager.addAbstractSinglePhenotype(factory.createAbstractSinglePhenotype(phenotype.getName(), OWL2Datatype.XSD_INTEGER, category.getName()));
	}

	@Test
	public void testPhenotypeCreation() throws WrongPhenotypeTypeException {
		PhenotypeOntologyManager manager = new PhenotypeOntologyManager(
			RULE.getConfiguration().getPhenotypePath().replace("%id%", "test0815"), false);
		PhenotypeFactory factory = manager.getPhenotypeFactory();

		manager.addAbstractSinglePhenotype(factory.createAbstractSinglePhenotype("abstract", OWL2Datatype.XSD_DOUBLE));

		factory.createRestrictedSinglePhenotype(
			"restricted", "abstract", new PhenotypeRange(new OWLFacet[]{ OWLFacet.MIN_EXCLUSIVE }, new Double[]{ 5.0 })
		);
	}

	@AfterClass
	public static void cleanUp() throws IOException {
		Path path = Paths.get(ONTOLOGY_PATH);
		if (Files.exists(path)) Files.delete(path);
	}
}
