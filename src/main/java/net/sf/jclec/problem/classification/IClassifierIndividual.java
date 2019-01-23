package net.sf.jclec.problem.classification;

import net.sf.jclec.IIndividual;

/**
 * Individual definition for evolutionary algorithms on classification problems.<p/>
 * 
 * Extends the general-purpose individual interface with a getPhenotype()
 * method to get the classifier represented by the individual genotype.
 * 
 * 
 * 
 *  
 * 
 * 
 */

public interface IClassifierIndividual extends IIndividual 
{
	/**
	 * Access to individual phenotype
	 * 
	 * @return A classifier contained in this individual
	 */
	
	public IClassifier getPhenotype();
}