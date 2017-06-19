package de.onto_med.webprotege_rest_api.ontology;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.io.XMLUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

import de.onto_med.webprotege_rest_api.api.TaxonomyNode;
import de.onto_med.webprotege_rest_api.api.Timer;
import de.onto_med.webprotege_rest_api.api.json.Entity;
import de.onto_med.webprotege_rest_api.api.json.Individual;
import de.onto_med.webprotege_rest_api.api.json.Property;
import owlapi_utils.binaryowl.BinaryOwlUtils;
import owlapi_utils.owlapi.OwlApiUtils;

/**
 * Instances of this class are parsers for binary formated ontologies.
 * @author Christoph Beger
 */
public class BinaryOwlParser extends OntologyParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(BinaryOwlParser.class);
	private static final Double MATCH_THRESHOLD = 0.8;
	
	private String importsPath;
	private String projectPath;
	private String rootPath;
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private String projectId;
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		BinaryOwlParser parser = new BinaryOwlParser(
			"702fdf23-882e-41cf-9d8d-0f589e7632a0",
			"H:/Projekte/Leipzig Health Atlas/Development/Web-Service/data/webprotege/"
		);
		
		OWLOntology ontology      = parser.getRootOntology();
		OWLDataFactory factory    = parser.manager.getOWLDataFactory();
		OWLNamedIndividual entity = factory.getOWLNamedIndividual(IRI.create("http://www.lha.org/pol#GSE61374_RAW"));
		// ...
	}
	

	/**
	 * Constructor
	 * @param projectId id of the project
	 * @param dataPath path to WebProtégé data folder
	 */
	public BinaryOwlParser(String projectId, String dataPath) {
		super(dataPath);
		this.projectId = projectId;
		projectPath = dataPath + "/data-store/project-data/" + projectId;
		rootPath    = projectPath + "/ontology-data/root-ontology.binary";
		importsPath = projectPath + "/imports-cache";
		
		File projectDir = new File(projectPath);
		if (!(projectDir.exists() && projectDir.isDirectory() && projectDir.canRead()))
			throw new WebApplicationException(String.format(
				"BinaryOwlParser could not access directory for project '%s'.", projectId
			));
		manager = BinaryOwlUtils.getOwlOntologyManager();
	}
	
	/**
	 * Returns the root ontologies IRI as string.
	 * @return root ontology IRI
	 */
	public String getProjectIri() {
		return getRootOntology().getOntologyID().getOntologyIRI().get().toString();
	}
	
	public int countEntities(Class<?> cls) {
		return OwlApiUtils.countEntities(cls, getRootOntology());
	}
	
	/**
	 * Classifies an individual by adding it to the ontology and running a reasoner.
	 * @param individual the individual which will be classifiy
	 * @return list of reasoned classes
	 * @throws NoSuchAlgorithmException
	 */
	public List<String> classifyIndividual(Individual individual) throws NoSuchAlgorithmException {
		List<String> classifications = new ArrayList<String>();
		OWLNamedIndividual tempIndividual = createNamedIndividual(individual);
		
		OWLReasoner reasoner = OwlApiUtils.getHermiTReasoner(getRootOntology());
		
		for (Node<OWLClass> node : reasoner.getTypes(tempIndividual, true)) {
			classifications.addAll(node.getEntities().stream().map(
				e -> e.getIRI().toString()
			).collect(Collectors.toList()));
		}
		
		return classifications;
	}
	
	/**
	 * Searches for entities which match the class expression.
	 * @param string class expression as string
	 * @return List of entities 
	 */
	public List<Entity> getEntityPropertiesByClassExpression(String classExpression) {
		OWLReasoner reasoner     = OwlApiUtils.getHermiTReasoner(getRootOntology());
		OWLClassExpression ce    = OwlApiUtils.convertStringToClassExpression(classExpression, getRootOntology());
		ArrayList<Entity> result = new ArrayList<Entity>();
		
		for (Node<OWLNamedIndividual> node : reasoner.getInstances(ce, false))
			result.add(getEntity(node.iterator().next()));
		
		return result;
	}
	
	
	public List<Entity> getEntityPropertiesByIri(String iri) throws NoSuchAlgorithmException {
		return getEntityProperties(iri, null, null, null, true, false, OWLEntity.class);
	}
	
	/**
	 * Search for OWLNamedIndividuals by name.
	 * @throws NoSuchAlgorithmException
	 */
	public List<Entity> annotate(String name, Boolean exact) throws NoSuchAlgorithmException {
		return getEntityProperties(null, name, null, null, exact, false, OWLNamedIndividual.class);
	}
	
	/**
	 * Search for OWLEntitys by name.
	 * @throws NoSuchAlgorithmException
	 */
	public List<Entity> getEntityProperties(String name, Boolean exact) throws NoSuchAlgorithmException {
		return getEntityProperties(null, name, null, null, exact, false, OWLEntity.class);
	}
	
	/**
	 * Search for OWLEntities without specified IRI.
	 * @throws NoSuchAlgorithmException
	 */
	public List<Entity> getEntityProperties(String name, String property, String value, Boolean exact, Boolean and) throws NoSuchAlgorithmException {
		return getEntityProperties(null, name, property, value, exact, and, OWLEntity.class);
	}
	
	/**
	 * Search for OWLEntitys.
	 * @throws NoSuchAlgorithmException
	 */
	public List<Entity> getEntityProperties(String iri, String name, String property, String value, Boolean exact, Boolean and) throws NoSuchAlgorithmException {
		return getEntityProperties(iri, name, property, value, exact, and, OWLEntity.class);
	}
	
	/**
	 * Searches for entities based on provided arguments and returns them with their properties.
	 * @param iri the IRI
	 * @param name entity name
	 * @param property property name the searched entities must have
	 * @param value property value
	 * @param exact string match method (true = exact, false = loose)
	 * @param and logical operator (true = and, false = or)
	 * @param cls ontology type restriction (OWLClass, OWLIndividual, OWLEntity)
	 * @return set of entities with properties
	 * @throws NoSuchAlgorithmException
	 */
	public List<Entity> getEntityProperties(
		String iri, String name, String property, String value, Boolean exact, Boolean and, Class<?> cls
	) throws NoSuchAlgorithmException {
		List<OWLEntity> resultset = new ArrayList<OWLEntity>();
		
		for (OWLEntity entity : getRootOntology().getSignature(Imports.INCLUDED)) {
			Boolean iriMatch      = false;
			Boolean nameMatch     = false;
			Boolean propertyMatch = false;
			
			if (StringUtils.isNotBlank(iri)) {
				if (exact) {
					if (iri.equals(entity.getIRI().toString()))
						iriMatch = true;
				} else {
					if (StringUtils.getJaroWinklerDistance(iri, entity.getIRI().toString()) >= MATCH_THRESHOLD)
						iriMatch = true;
				}
			}
			
			if (StringUtils.isNotBlank(name)) {
				if (exact) {
					if (iri.equals(entity.getIRI().toString()))
						nameMatch = true;
				} else {
					if (StringUtils.getJaroWinklerDistance(name, StringUtils.defaultString(OwlApiUtils.getLabel(entity, getRootOntology()), "")) >= MATCH_THRESHOLD
						|| StringUtils.getJaroWinklerDistance(name, XMLUtils.getNCNameSuffix(entity.getIRI())) >= MATCH_THRESHOLD
					) nameMatch = true;
				}
			}
			
			if (StringUtils.isNotBlank(property)) {
				if (hasProperty(entity, property, value, exact))
					propertyMatch = true;
			}
			
			if (and) {
				if (StringUtils.isNotBlank(iri) && !iriMatch
					|| StringUtils.isNotBlank(name) && !nameMatch
					|| StringUtils.isNotBlank(property) && !propertyMatch
				) continue;
			} else {
				if (!iriMatch && !nameMatch && !propertyMatch)
					continue;
			}
			
			if (cls.equals(OWLEntity.class)) {
				resultset.add(entity);
			} else if (cls.equals(OWLClass.class)) {
				if (entity.isOWLClass())
					resultset.add(entity);
			} else if (cls.equals(OWLIndividual.class)) {
				if (entity.isOWLNamedIndividual())
					resultset.add(entity);
			} else {
				throw new NoSuchAlgorithmException("Error: class " + cls.getName() + " is not supported by this method.");
			}
		}
		
		return getEntities(resultset);
	}
	
	
	/**
	 * Returns a multidimensional Array of class labels/names.
	 * @return ArrayList containing the taxonomy of this ontology.
	 */
	public TaxonomyNode getTaxonomy() {
		OWLReasoner reasoner = OwlApiUtils.getHermiTReasoner(getRootOntology());
		OWLClass topClass = reasoner.getTopClassNode().iterator().next();
		
		return getTaxonomyForOWLClass(topClass, reasoner);
	}
	
	
	/**
	 * Returns the full RDF document for this ontology as string.
	 * @return string containing the full RDF document.
	 */
	public Object getFullRDFDocument() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		try {
			getRootOntology().getOWLOntologyManager().saveOntology(
				getRootOntology(), new RDFXMLDocumentFormat(), outputStream
			);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
		
		return outputStream.toString();
	}
	
	
	/**
	 * Returns a list of imported ontology ids.
	 * @return List of imported ontology ids
	 */
	public ArrayList<String> getImportedOntologyIds() {
		ArrayList<String> imports = new ArrayList<String>();
		
		for (OWLOntology ontology : getRootOntology().getImports()) {
			imports.add(ontology.getOntologyID().toString());
		}
		
		return imports;
	}
	
	
	/**
	 * Returns shortforms and iris for each loaded ontology.
	 * @return HashMap with key: shortform and value: iri
	 */
	public HashMap<String, String> getOntologyIris() {
		getRootOntology();
		HashMap<String, String> map = new HashMap<String, String>();
		
		for (OWLOntology ontology : manager.getOntologies()) {
			IRI iri = ontology.getOntologyID().getOntologyIRI().get();
			map.put(iri.getShortForm(), iri.toString());
		}
		
		return map;
	}
	
	public boolean isConsistent() {
		OWLReasoner reasoner = OwlApiUtils.getHermiTReasoner(getRootOntology());
		
		return reasoner.isConsistent();
	}
	
	private OWLNamedIndividual createNamedIndividual(Individual individual) throws NoSuchAlgorithmException {
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLAxiom> axioms = new TreeSet<OWLAxiom>();
		OWLNamedIndividual namedIndividual = factory.getOWLNamedIndividual(
			IRI.create(String.valueOf(individual.getProperties().hashCode()))
		);
		
		for (String type : individual.getTypes()) {
			OWLClass cls = factory.getOWLClass(IRI.create(type));
			axioms.add(factory.getOWLClassAssertionAxiom(cls, namedIndividual));
		}
		
		for (Property property : individual.getProperties()) {
			IRI iri = IRI.create(property.getIri());
			
			for (String value : property.getValues()) {
				if (getRootOntology().containsAnnotationPropertyInSignature(iri)) {
					OWLAnnotationProperty annotationProperty = factory.getOWLAnnotationProperty(iri);
					axioms.add(factory.getOWLAnnotationAssertionAxiom(
						namedIndividual.getIRI(), factory.getOWLAnnotation(annotationProperty, OwlApiUtils.getLiteralForValueAndClassName(value, property.getClassName(), manager))
					));
				}
				if (getRootOntology().containsDataPropertyInSignature(iri)) {
					OWLDataProperty dataProperty = factory.getOWLDataProperty(iri);
					axioms.add(factory.getOWLDataPropertyAssertionAxiom(
						dataProperty, namedIndividual, OwlApiUtils.getLiteralForValueAndClassName(value, property.getClassName(), manager)
					));
				}
				if (getRootOntology().containsObjectPropertyInSignature(iri)) {
					OWLObjectProperty objectProperty = factory.getOWLObjectProperty(iri);
					axioms.add(factory.getOWLObjectPropertyAssertionAxiom(
						objectProperty, namedIndividual, factory.getOWLNamedIndividual(IRI.create(value))
					));
				}
			}
		}
		
		manager.addAxioms(getRootOntology(), axioms);
		
		return namedIndividual;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Boolean hasProperty(OWLEntity entity, String property, String value, Boolean exact) {
		if (entity.isOWLNamedIndividual()) {
		    for (OWLDataProperty dataProperty : extractPropertyByNameFromSet(
		    		property, getRootOntology().getDataPropertiesInSignature(Imports.INCLUDED), exact
		    )) {
				Collection<OWLObject> values = (Collection) EntitySearcher.getDataPropertyValues((OWLIndividual) entity, dataProperty, getRootOntology());
		    	
				if (values.isEmpty()) continue;
		    	if (valueCollectionContains(values, value)) return true;
		    }
		    	
		    for (OWLObjectProperty objectProperty : extractPropertyByNameFromSet(
		    		property, getRootOntology().getObjectPropertiesInSignature(Imports.INCLUDED), exact
		    )) {
		    	Collection<OWLObject> values = (Collection) EntitySearcher.getObjectPropertyValues((OWLIndividual) entity, objectProperty, getRootOntology());
		    	
		    	if (values.isEmpty()) continue;
			    if (valueCollectionContains(values, value)) return true;
		    }
		}
	    
	    for (OWLAnnotationProperty annotationProperty : extractPropertyByNameFromSet(
	    		property, getRootOntology().getAnnotationPropertiesInSignature(Imports.INCLUDED), exact
	    )) {
	    	Collection<OWLObject> values = (Collection) EntitySearcher.getAnnotations(entity, getRootOntology(), annotationProperty);
	    		
	    	if (values.isEmpty()) continue;
	    	if (valueCollectionContains(values, value)) return true;
	    }
		
		return false;
	}
	
	
	private TaxonomyNode getTaxonomyForOWLClass(OWLClass cls, OWLReasoner reasoner) {
		TaxonomyNode taxonomy = new TaxonomyNode(
			StringUtils.defaultString(OwlApiUtils.getLabel(cls, getRootOntology()), XMLUtils.getNCNameSuffix(cls.getIRI())),
			cls.getIRI().toString()
		);
		
		for (Node<OWLClass> node : reasoner.getSubClasses(cls, true)) {
			OWLClass subClass = node.iterator().next();
			if (subClass.isBottomEntity()) continue;
			taxonomy.addSubclassNode(getTaxonomyForOWLClass(subClass, reasoner));
		}
		
		for (Node<OWLNamedIndividual> node : reasoner.getInstances(cls, true)) {
			OWLNamedIndividual instance = node.iterator().next();
			taxonomy.addInstance(OwlApiUtils.getLabel(instance, getRootOntology()), instance.getIRI().toString());
		}
		
		return taxonomy;
	}
	
	
	private <T> ArrayList<T> extractPropertyByNameFromSet(String name, Set<T> properties, Boolean exact) {
		ArrayList<T> results = new ArrayList<T>();
		
		for (T property : properties)
			if (exact) {
				if (XMLUtils.getNCNameSuffix(((OWLNamedObject) property).getIRI()).equals(name) && !results.contains(property))
					results.add(property);
			} else {
				if (StringUtils.getJaroWinklerDistance(XMLUtils.getNCNameSuffix(((OWLNamedObject) property).getIRI()), name) >= MATCH_THRESHOLD)
					results.add(property);
			}
		
		return results;
	}
	
	
	/**
	 * Returns a list of OWLEntityProperties for a set of entities.
	 * @param entities set of OWLEntitys
	 * @return List of OWLEntityProperties
	 */
 	private List<Entity> getEntities(List<OWLEntity> entities) {
		List<Entity> properties = new ArrayList<Entity>();

		for (OWLEntity entity : entities) {
	    	properties.add(getEntity(entity));
	    }
		return properties;
	}
	
	
	/**
	 * Returns properties for a single entity.
	 * @param entity entity object
	 * @return properties as OWLEntityProperties
	 */
	private Entity getEntity(OWLEntity entity) {
		Entity properties    = new Entity();
		OWLReasoner reasoner = OwlApiUtils.getHermiTReasoner(getRootOntology());
		
		properties.setProjectId(projectId);
    	properties.setIri(entity.getIRI().toString());
    	properties.setJavaClass(entity.getClass().getName());
    	
    	for (OWLAnnotationAssertionAxiom property : EntitySearcher.getAnnotationAssertionAxioms(entity, getRootOntology())) {
			properties.addAnnotationProperty(property.getProperty(), property.getValue());
		}
    	
    	if (entity.isOWLClass()) {
    		properties.addSuperClassExpressions(reasoner.getSuperClasses(entity.asOWLClass(), true));
    		properties.addSubClassExpressions(reasoner.getSubClasses(entity.asOWLClass(), true));
			properties.addIndividuals(reasoner.getInstances(entity.asOWLClass(), true));
    		properties.addDisjointClasses(reasoner.getDisjointClasses(entity.asOWLClass()));
    		properties.addEquivalentClasses(reasoner.getEquivalentClasses(entity.asOWLClass()));
    	}
    	
    	if (entity.isOWLNamedIndividual()) {
	    	Multimap<OWLDataPropertyExpression, OWLLiteral> dataProperties = EntitySearcher.getDataPropertyValues(entity.asOWLNamedIndividual(), getRootOntology());
			for (OWLDataPropertyExpression property : dataProperties.keySet()) {
				properties.addDataProperty(property, dataProperties.get(property));
			}
			
			Multimap<OWLObjectPropertyExpression, OWLIndividual> objectProperties = EntitySearcher.getObjectPropertyValues(entity.asOWLNamedIndividual(), getRootOntology());
			for (OWLObjectPropertyExpression property : objectProperties.keySet()) {
				properties.addObjectProperty(property, objectProperties.get(property));
			}
			
			properties.addTypes(reasoner.getTypes(entity.asOWLNamedIndividual(), true));
			properties.addSameIndividuals(reasoner.getSameIndividuals(entity.asOWLNamedIndividual()));
    	}
    	
    	reasoner.dispose();
    	return properties;
	}
	
	
	/**
	 * Returns the root-ontology with all loaded imports or null if the ontology loading fails.
	 * This function tries to load all imports before loading the ontology.
	 * Depending on their filename, the order of loading may vary and errors can occure.
	 * When ever an error occures, the concerned document remains as 'not loaded',
	 * so the function can try to load it in the next iteration.
	 * @return root-ontology
	 */	
	@SuppressWarnings("unchecked")
	private OWLOntology getRootOntology() {
		if (ontology != null) return ontology;
		
		Timer timer = new Timer();
		
        ArrayList<File> documents = new ArrayList<File>(
        	Arrays.asList((new File(importsPath)).listFiles())
        );
        documents.removeIf(d -> d.isHidden() || d.isDirectory());
        
        try {
	        for (int i = 0; !documents.isEmpty() && i <= documents.size(); i++) {
	        	for (File ontologyDocument : (ArrayList<File>) documents.clone()) {
	        		try {
	        			manager.loadOntologyFromOntologyDocument(ontologyDocument);
	        			documents.remove(ontologyDocument);
	        		} catch (UnparsableOntologyException e) {}
	        	}
	        }
	        
	        /** this is very slow for large ontologies. Any improvement possible? **/
			ontology = manager.loadOntologyFromOntologyDocument(new File(rootPath));
        } catch (OWLOntologyCreationException e) {
        	e.printStackTrace();
        }
        
        LOGGER.info(String.format("Parsed project '%s' for the first time. " + timer.getDiffFromStart(), projectId));
		return ontology;
	}
	
	
	/**
	 * Checks if set of values contains a value or value is null.
	 * @param values
	 * @param value
	 * @return true if valueSet contains value or value is null, else false
	 */
	private boolean valueCollectionContains(Collection<OWLObject> values, String value) {
		for (OWLObject curValue : values) {
    		if (StringUtils.isEmpty(value)
    			|| curValue.toString().replaceAll("^.*?\"|\"\\^.*$", "").equals(value)
    		) {
    			return true;
    		}
    	}
		return false;
	}
}
