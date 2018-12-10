package de.onto_med.ontology_service.factory;

import de.onto_med.ontology_service.data_model.PhenotypeFormData;
import org.apache.commons.lang3.StringUtils;
import org.lha.phenoman.man.PhenotypeManager;
import org.lha.phenoman.model.phenotype.*;
import org.lha.phenoman.model.phenotype.top_level.AbstractPhenotype;

import javax.activation.UnsupportedDataTypeException;
import java.util.UUID;

/**
 * Convenient factory to construct AbstractPhenotypes.
 *
 * @author Christoph Beger
 */
public class AbstractPhenotypeFactory extends PhenotypeFactory {
	protected PhenotypeManager manager;

	/**
	 * Constructor for Abstract Phenotype Factories.
	 * This Factory requires a PhenotypeManager, because formulas are only processable by the manager.
	 * @param manager The PhenotypeManager
	 */
	public AbstractPhenotypeFactory(PhenotypeManager manager) {
		this.manager = manager;
	}

	/**
	 * Creates an AbstractPhenotype depending on the provided phenotype data.
	 *
	 * @param data Phenotype data.
	 * @return An AbstractPhenotype.
	 * @throws UnsupportedDataTypeException If the provided phenotype data contains invalid values.
	 */
	public AbstractPhenotype createAbstractPhenotype(PhenotypeFormData data) throws UnsupportedDataTypeException, NullPointerException {
		String datatype = data.getDatatype();

		if (StringUtils.isBlank(data.getIdentifier()))
			data.setIdentifier(UUID.randomUUID().toString());

		AbstractPhenotype phenotype;
		if ("composite-boolean".equals(datatype)) {
			phenotype = createAbstractBooleanPhenotype(data);
		} else if ("calculation".equals(datatype)) {
			phenotype = createAbstractCalculationPhenotype(data);
		} else if (StringUtils.isNoneBlank(datatype)) {
			phenotype = createAbstractSinglePhenotype(data, datatype);
		} else {
			throw new UnsupportedDataTypeException("Datatype is missing.");
		}

		setPhenotypeBasicData(phenotype, data);

		return phenotype;
	}

	/**
	 * Creates an AbstractSinglePhenotype.
	 *
	 * @param data     Phenotype data.
	 * @param datatype An OWL2Datatype.
	 * @return An AbstractSinglePhenotype.
	 */
	private AbstractSinglePhenotype createAbstractSinglePhenotype(PhenotypeFormData data, String datatype) throws NullPointerException, UnsupportedDataTypeException {
		AbstractSinglePhenotype phenotype;
		if ("string".equals(datatype)) {
			phenotype = new AbstractSingleStringPhenotype(data.getIdentifier(), data.getMainTitle(), data.getSuperCategories());
		} else if ("numeric".equals(datatype)) {
			phenotype = new AbstractSingleDecimalPhenotype(data.getIdentifier(), data.getMainTitle(), data.getSuperCategories());
		} else if ("date".equals(datatype)) {
			phenotype = new AbstractSingleDatePhenotype(data.getIdentifier(), data.getMainTitle(), data.getSuperCategories());
		} else if ("boolean".equals(datatype)) {
			phenotype = new AbstractSingleBooleanPhenotype(data.getIdentifier(), data.getMainTitle(), data.getSuperCategories());
		} else {
			throw new UnsupportedDataTypeException("Could not determine Datatype.");
		}

		data.getTitleObjects().forEach(phenotype::addTitle);
		if (StringUtils.isNoneBlank(data.getUcum())) phenotype.setUnit(data.getUcum());

		return phenotype;
	}

	/**
	 * Creates an AbstractBooleanPhenotype.
	 *
	 * @param data Phenotype data.
	 * @return An AbstractBooleanPhenotype
	 */
	private AbstractBooleanPhenotype createAbstractBooleanPhenotype(PhenotypeFormData data) {
		AbstractBooleanPhenotype phenotype =
			new AbstractBooleanPhenotype(data.getIdentifier(), data.getMainTitle(), data.getSuperCategories());

		data.getTitleObjects().forEach(phenotype::addTitle);

		return phenotype;
	}

	/**
	 * Creates an AbstractCalculationPhenotype.
	 *
	 * @param data Phenotype data.
	 * @return An AbstractCalculationPhenotype.
	 */
	private AbstractCalculationPhenotype createAbstractCalculationPhenotype(PhenotypeFormData data) {
		if (StringUtils.isBlank(data.getFormula()))
			throw new NullPointerException("Formula for abstract calculated phenotype is missing.");

		AbstractCalculationPhenotype phenotype =
			new AbstractCalculationPhenotype(data.getIdentifier(), data.getMainTitle(), manager.getFormula(data.getFormula()), data.getSuperCategories());

		data.getTitleObjects().forEach(phenotype::addTitle);
		if (StringUtils.isNoneBlank(data.getUcum())) phenotype.setUnit(data.getUcum());

		return phenotype;
	}
}
