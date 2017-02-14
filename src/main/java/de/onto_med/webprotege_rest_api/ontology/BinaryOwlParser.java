package de.onto_med.webprotege_rest_api.ontology;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.binaryowl.owlapi.BinaryOWLOntologyDocumentParserFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.OWLParserFactory;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.io.XMLUtils;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;
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
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
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
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

import de.onto_med.webprotege_rest_api.RestApiApplication;
import de.onto_med.webprotege_rest_api.api.Filter;
import de.onto_med.webprotege_rest_api.api.OWLEntityProperties;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

public class BinaryOwlParser extends OntologyParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestApiApplication.class);
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
		manager = getOwlOntologyManager();
	}
	
	
	public String getProjectIri()  {
		return getRootOntology().getOntologyID().getOntologyIRI().get().toString();
	}
	
	
	/**
	 * Returns a list of OWLEntityProperties for all classes with matching localname.
	 * @param name Localename to search for
	 * @param match 'exact' or 'loose', defaults to 'loose'
	 * @return List of found OWLEntityProperties
	 */
	public ArrayList<OWLEntityProperties> getClassPropertiesByName(String name, String match) {	    
	    return getPropertiesForOWLEntities(extractEntitiesWithFilter(name, OWLClassImpl.class, "exact".equals(match)));
	}
	
	
	public int countEntities(Class<?> cls) {
		if (cls.equals(OWLObjectProperty.class)) {
			return getRootOntology().getObjectPropertiesInSignature(Imports.INCLUDED).size();
		} else if (cls.equals(OWLLogicalAxiom.class)) {
			return getRootOntology().getLogicalAxiomCount(Imports.INCLUDED);
		} else if (cls.equals(OWLIndividual.class)) {
			return getRootOntology().getIndividualsInSignature(Imports.INCLUDED).size();
		} else if (cls.equals(OWLDataProperty.class)) {
			return getRootOntology().getDataPropertiesInSignature(Imports.INCLUDED).size();
		} else if (cls.equals(OWLClass.class)) {
			return getRootOntology().getClassesInSignature(Imports.INCLUDED).size();
		} else if (cls.equals(OWLAxiom.class)) {
			return getRootOntology().getAxiomCount(Imports.INCLUDED);
		} else if (cls.equals(OWLAnnotationProperty.class)) {
			return getRootOntology().getAnnotationPropertiesInSignature(Imports.INCLUDED).size();
		} else {
			return 0;
		}
	}
	
	
	/**
	 * Returns a list of OWLEntityProperties for all entities witch matching localname.
	 * @param name Localname to match with
	 * @param match 'exact' or 'loose', defaults to 'loose'
	 * @return List of found OWLEntityProperties
	 */
	public ArrayList<OWLEntityProperties> getEntityPropertiesByName(String name, String match) {
		return getPropertiesForOWLEntities(extractEntitiesWithFilter(
			name, OWLEntity.class, "exact".equals(match)
		));
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
	 * Searches for individuals which match the class expression.
	 * @param string class expression as string
	 * @return List of named individuals 
	 */
	@SuppressWarnings("deprecation")
	public ArrayList<OWLEntityProperties> getIndividualPropertiesByClassExpression(String string) {
		OWLReasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(getRootOntology());
		OWLClassExpression ce = convertStringToClassExpression(string);
		ArrayList<OWLEntityProperties> result = new ArrayList<OWLEntityProperties>();
		
		for (Node<OWLNamedIndividual> node : reasoner.getInstances(ce, false)) {
			result.add(getPropertiesForOWLEntity(node.iterator().next()));
		}
		
		return result;
	}
	
	
	/**
	 * Returns a list of OWLEntityProperties for all individuals with matching localname.
	 * @param name Localname to match with
	 * @param match 'exact' or 'loose', defaults to 'loose'
	 * @return List of found OWLEntityProperties 
	 */
	public ArrayList<OWLEntityProperties> getNamedIndividualPropertiesByName(String name, String match) {		
		return getPropertiesForOWLEntities(extractEntitiesWithFilter(
			name, OWLNamedIndividualImpl.class, "exact".equals(match)
		));
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
	
	
	/**
	 * Searches for OWLEntities which are annotated with a property with given name.
	 * @param type 'entity', 'individual' or 'class', defaults to 'entity'
	 * @param property localename of the property
	 * @param value with property annotated value or null for no value check
	 * @param match 'exact' or 'loose', defaults to 'loose'
	 * @return List of OWLEntityProperties for found entities
	 * @throws NoSuchAlgorithmException If the specified type is not one of 'entity', 'individual' and 'class', or the project was not found
	 */
	public ArrayList<OWLEntityProperties> searchOntologyEntityByProperty(
		String type, String property, String value, String match
	) throws NoSuchAlgorithmException {
		ArrayList<OWLEntity> entities;
		
		if (StringUtils.isEmpty(type)) type = "entity";
		
		switch (type) {
			case "individual":
				entities = extractOWLNamedIndividualByProperty(property, value);
				break;
			case "class":
				entities = extractOWLClassesByProperty(property, value);
				break;
			case "entity":
				entities = extractOWLEntitiesByProperty(property, value);
				break;
			default:
				throw new NoSuchAlgorithmException("OWL type '" + type + "' does not exist or is not implemented.");
		}
		
		return getPropertiesForOWLEntities(entities);
	}
	
	
	private OWLClassExpression convertStringToClassExpression(String expression) {
        ManchesterOWLSyntaxParserImpl parser = (ManchesterOWLSyntaxParserImpl) OWLManager.createManchesterParser();
        OWLEntityChecker owlEntityChecker = new ShortFormEntityChecker(getShortFormProvider());
		parser.setOWLEntityChecker(owlEntityChecker);
        parser.setDefaultOntology(getRootOntology());

        return parser.parseClassExpression(expression);
    }
	
	
	/**
	 * Returns a list of OWLEntities which match the given filter criteria.
	 * @param name entity name
	 * @param filter Filter object which uses parameter name
	 * @return Resulting list of OWLEntities
	 */
	private ArrayList<OWLEntity> extractEntitiesWithFilter(String name, Class<?> cls, Boolean match) {		
		ArrayList<OWLEntity> resultset = new ArrayList<OWLEntity>();
	    
	    for (OWLEntity entity : getRootOntology().getSignature(Imports.INCLUDED)) {
	    	if (!Filter.run(entity, name, cls, match, getRootOntology())) continue;
	    	
	    	if (!resultset.contains(entity))
	    		resultset.add(entity);
	    }
		
	    return resultset;
	}
	
	
	private ArrayList<OWLEntity> extractOWLClassesByProperty(String name, String value) {
		ArrayList<OWLEntity> entities = extractOWLEntitiesByProperty(name, value);
		entities.removeIf(e -> !e.isOWLNamedIndividual());
		
		return entities;
	}
	
	
	private ArrayList<OWLEntity> extractOWLEntitiesByProperty(String name, String value) {
		ArrayList<OWLEntity> resultset = new ArrayList<OWLEntity>();
	    	
	    for (OWLDataProperty property : extractPropertyByNameFromSet(name, getRootOntology().getDataPropertiesInSignature(Imports.INCLUDED))) {
	    	for (OWLEntity entity : getRootOntology().getSignature(Imports.INCLUDED)) {
		    	@SuppressWarnings({ "unchecked", "rawtypes" })
				Collection<OWLObject> values = (Collection) EntitySearcher.getDataPropertyValues((OWLIndividual) entity, property, getRootOntology());
	    		if (values.isEmpty() || resultset.contains(entity))
		    		continue;
	    		
	    		if (valueCollectionContains(values, value))
		    		resultset.add(entity);
	    	}
	    }
	    	
	    for (OWLObjectProperty property : extractPropertyByNameFromSet(name, getRootOntology().getObjectPropertiesInSignature(Imports.INCLUDED))) {
	    	for (OWLEntity entity : getRootOntology().getSignature(Imports.INCLUDED)) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Collection<OWLObject> values = (Collection) EntitySearcher.getObjectPropertyValues((OWLIndividual) entity, property, getRootOntology());
	    		if (values.isEmpty() || resultset.contains(entity))
	    			continue;
		    	
	    		if (valueCollectionContains(values, value))
		    		resultset.add(entity);
	    	}
	    }
	    
	    for (OWLAnnotationProperty property : extractPropertyByNameFromSet(name, getRootOntology().getAnnotationPropertiesInSignature(Imports.INCLUDED))) {
	    	for (OWLEntity entity : getRootOntology().getSignature(Imports.INCLUDED)) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Collection<OWLObject> values = (Collection) EntitySearcher.getAnnotations(entity, getRootOntology(), property);
	    		if (values.isEmpty() || resultset.contains(entity))
	    			continue;
	    		
		    	if (valueCollectionContains(values, value))
		    		resultset.add(entity);
	    	}
	    }
		
	    return resultset;
	}
	
	
	private ArrayList<OWLEntity> extractOWLNamedIndividualByProperty(String name, String value) {
		ArrayList<OWLEntity> entities = extractOWLEntitiesByProperty(name, value);
		
		entities.removeIf(e -> !e.isOWLNamedIndividual());
		
		return entities;
	}
	
	
	private <T> ArrayList<T> extractPropertyByNameFromSet(String name, Set<T> properties) {
		ArrayList<T> results = new ArrayList<T>();
		
		for (T property : properties)
			if (XMLUtils.getNCNameSuffix(((OWLNamedObject) property).getIRI()).equals(name) && !results.contains(property))
				results.add(property);
		
		return results;
	}
	
	
	/**
	 * Creates and returns an OWLOntologyManager
	 * @return ontology manager
	 */
	private OWLOntologyManager getOwlOntologyManager() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		Set<OWLParserFactory> parserFactories = new HashSet<OWLParserFactory>();
		parserFactories.add(new BinaryOWLOntologyDocumentParserFactory());
		manager.setOntologyParsers(parserFactories);

		return manager;
	}
	
	
	/**
	 * Returns a list of OWLEntityProperties for a set of entities.
	 * @param entities set of OWLEntitys
	 * @return List of OWLEntityProperties
	 */
 	private ArrayList<OWLEntityProperties> getPropertiesForOWLEntities(ArrayList<OWLEntity> entities) {
		ArrayList<OWLEntityProperties> properties = new ArrayList<OWLEntityProperties>();

		for (OWLEntity entity : entities) {
	    	properties.add(getPropertiesForOWLEntity(entity));
	    }
		return properties;
	}
	
	
	/**
	 * Returns properties for a single entity.
	 * @param entity entity object
	 * @return properties as OWLEntityProperties
	 */
	private OWLEntityProperties getPropertiesForOWLEntity(OWLEntity entity) {
		OWLEntityProperties properties = new OWLEntityProperties();
    	
    	properties.setIri(entity.getIRI().toString());
    	properties.setJavaClass(entity.getClass().getName());
    	
    	for (OWLAnnotationAssertionAxiom property : EntitySearcher.getAnnotationAssertionAxioms(entity, getRootOntology())) {
			properties.addAnnotationProperty(property.getProperty(), property.getValue());
		}
    	
    	if (entity.isOWLClass()) {
    		properties.addSuperClassExpressions(EntitySearcher.getSuperClasses(entity.asOWLClass(), getRootOntology().getImportsClosure()));
    		properties.addSubClassExpressions(EntitySearcher.getSubClasses(entity.asOWLClass(), getRootOntology().getImportsClosure()));
    	}
    	
    	if (entity.isOWLNamedIndividual()) {
	    	Multimap<OWLDataPropertyExpression, OWLLiteral> dataProperties = EntitySearcher.getDataPropertyValues(entity.asOWLNamedIndividual(), getRootOntology());
			for (OWLDataPropertyExpression property : dataProperties.keySet()) {
				properties.addDataTypeProperty(property, dataProperties.get(property));
			}
			
			Multimap<OWLObjectPropertyExpression, OWLIndividual> objectProperties = EntitySearcher.getObjectPropertyValues(entity.asOWLNamedIndividual(), getRootOntology());
			for (OWLObjectPropertyExpression property : objectProperties.keySet()) {
				properties.addObjectProperty(property, objectProperties.get(property));
			}
			
			for (OWLClassExpression type : EntitySearcher.getTypes(entity.asOWLNamedIndividual(), getRootOntology())) {
				properties.addTypeExpression(type);
			}
    	}
    	
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
		
		LOGGER.info(String.format("Parsing project '%s' for the first time.", projectId));
		
        ArrayList<File> documents = new ArrayList<File>(
        	Arrays.asList((new File(importsPath)).listFiles())
        );
        documents.removeIf(d -> d.isHidden() || d.isDirectory());
        
        try {
	        int counter  = 0;
	        int maxTries = documents.size();
	        while (!documents.isEmpty() && counter <= maxTries) {
	        	counter++;
	        	for (File ontologyDocument : (ArrayList<File>) documents.clone()) {
	        		try {
	        			manager.loadOntologyFromOntologyDocument(ontologyDocument);
	        			documents.remove(ontologyDocument);
	        		} catch (UnparsableOntologyException e) {}
	        	}
	        }
	      
			ontology = manager.loadOntologyFromOntologyDocument(new File(rootPath));
        } catch (OWLOntologyCreationException e) {
        	e.printStackTrace();
        }
        
		return ontology;
	}
	
	
	private BidirectionalShortFormProvider getShortFormProvider() {
        // TODO: fix non-short shortforms
        ShortFormProvider sfp = new ManchesterOWLSyntaxPrefixNameShortFormProvider(manager.getOntologyFormat(getRootOntology()));
        BidirectionalShortFormProvider shortFormProvider = new BidirectionalShortFormProviderAdapter(manager.getOntologies(), sfp);
        
        return shortFormProvider;
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