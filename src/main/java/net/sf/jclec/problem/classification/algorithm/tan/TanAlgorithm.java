package net.sf.jclec.problem.classification.algorithm.tan;

import java.util.ArrayList;
import java.util.List;

import net.sf.jclec.IConfigure;
import net.sf.jclec.IEvaluator;
import net.sf.jclec.IIndividual;
import net.sf.jclec.IMutator;
import net.sf.jclec.IRecombinator;
import net.sf.jclec.ISelector;
import net.sf.jclec.base.FilteredMutator;
import net.sf.jclec.base.FilteredRecombinator;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.problem.classification.base.ClassificationAlgorithm;
import net.sf.jclec.problem.classification.base.Rule;
import net.sf.jclec.problem.classification.crisprule.CrispRuleBase;
import net.sf.jclec.problem.classification.syntaxtree.SyntaxTreeRuleIndividual;
import net.sf.jclec.problem.util.dataset.instance.IInstance;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Classifier Algorithm for Tan et al. 2002 - Mining multiple comprehensible classification rules using genetic programming<p/>
 * 
 * The Tan et al. algorithm performs reproduction, recombination and mutation, keeping an elite population.
 * Its execution is repeated as many times as the number of data classes.
 * At each execution, a population of rules is evolved for a particular data class.
 * When evolution is finished, the elite population is included in the classification rule base (more than one rule per class).
 * 
 * The algorithm performs token competition within the elite population in order to keep non-redundant individuals that cover complementary instances.
 * 
 * The configure() method set ups the algorithm according to the parameters from the configuration file.
 * The doSelection() method selects the parents from the current population via tournament selection.
 * The doGeneration() method applies the reproduction, recombination and mutation operators and evaluates the fitness of the offspring.
 * The doUpdate() method preforms the token competition and selects the best individuals from the current population and the offspring for the next generation.
 * The doControl() method defines the stop criterion that is the maximum number of generations, and controls the execution for each data class.
 * 
 */

public class TanAlgorithm extends ClassificationAlgorithm 
{
	// ///////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	// ///////////////////////////////////////////////////////////////

	/** Generated by Eclipse */

	private static final long serialVersionUID = -8711970425735016406L;

	// ///////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	// ///////////////////////////////////////////////////////////////

	/** Parents selector */

	protected ISelector parentsSelector;

	/** Individuals recombinator */

	protected FilteredRecombinator recombinator;

	/** Mutation operator */

	protected FilteredMutator mutator;

	/** Copy probability */

	private double copyProb;
	
	/** Elitist probability */
	
	private double elitistProb;
	
	/** Support threshold */
	
	private double support;

	// ///////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	// ///////////////////////////////////////////////////////////////

	/**
	 * Empty (default) constructor
	 */

	public TanAlgorithm() {
		super();
	}

	// ///////////////////////////////////////////////////////////////
	// ------------------------------------------------ Public methods
	// ///////////////////////////////////////////////////////////////
	
	/**
	 * Access to parents selector
	 * 
	 * @return Actual parents selector
	 */
	
	public ISelector getParentsSelector() {
		return parentsSelector;
	}

	/**
	 * Set the parents selector
	 * 
	 * @param parentsSelector the parents selector
	 */
	
	public void setParentsSelector(ISelector parentsSelector) {
		// Set parents selector
		this.parentsSelector = parentsSelector;
		// Contextualize selector
		parentsSelector.contextualize(this);
	}

	/**
	 * Access to parents recombinator
	 * 
	 * @return Actual parents recombinator
	 */

	public FilteredRecombinator getRecombinator() {
		return recombinator;
	}

	/**
	 * Sets the parents recombinator.
	 * 
	 * @param recombinator the parents recombinator
	 */

	public void setRecombinator(IRecombinator recombinator) {	
		if(this.recombinator == null)
			this.recombinator = new FilteredRecombinator(this);
		
		this.recombinator.setDecorated(recombinator);
	}

	/**
	 * Access to individuals mutator.
	 * 
	 * @return the mutator
	 */

	public FilteredMutator getMutator() {
		return mutator;
	}

	/**
	 * Set individuals mutator.
	 * 
	 * @param mutator the mutator
	 */

	public void setMutator(IMutator mutator) {
		if(this.mutator == null)
			this.mutator = new FilteredMutator(this);
		
		this.mutator.setDecorated(mutator);
	}
	
	/**
	 * Access to "copyProb" property.
	 * 
	 * @return Current copy probability
	 */

	public double getCopyProb() {
		return copyProb;
	}

	/**
	 * Set the "copyProb" property.
	 * 
	 * @param copyProb the copy probability
	 */

	public void setCopyProb(double copyProb) {
		this.copyProb = copyProb;
	}
	
	/**
	 * Access to "elitist" property.
	 * 
	 * @return Current elitist probability
	 */

	public double getElitistProb() {
		return elitistProb;
	}
	
	/**
	 * Set the "elitist" property.
	 * 
	 * @param elitistProb the elitist probability
	 */

	public void setElitistProb(double elitistProb) {
		this.elitistProb = elitistProb;
	}

	/**
	 * Set the "support" property.
	 * 
	 * @param support the support
	 */

	public void setSupport(double support) {
		this.support = support;
	}
	
	/**
	 * Access to "support" property.
	 * 
	 * @return Current support
	 */

	public double getSupport() {
		return this.support;
	}
	
	/**
	 * Set the recombinator probability
	 * 
	 * @param recProb recombination probability
	 */	
	private void setRecombinationProb(double recProb) 
	{
		((FilteredRecombinator) this.recombinator).setRecProb(recProb);
	}	
	
	/**
	 * Set the mutator probability.
	 * 
	 * @param mutProb mutation probability
	 */
	private void setMutationProb(double mutProb) 
	{
		((FilteredMutator) this.mutator).setMutProb(mutProb);
	}	
	
	// ///////////////////////////////////////////////////////////////
	// ---------------------------- Implementing IConfigure interface
	// ///////////////////////////////////////////////////////////////

	/**
	 * Configuration method.
	 * 
	 * @param settings Configuration settings
	 */

	public void configure(Configuration settings)
	{
		settings.addProperty("species[@type]", "net.sf.jclec.problem.classification.algorithm.tan.TanSyntaxTreeSpecies");
		settings.addProperty("evaluator[@type]", "net.sf.jclec.problem.classification.algorithm.tan.TanEvaluator");
		settings.addProperty("evaluator.w1", settings.getDouble("w1",0.7));
		settings.addProperty("evaluator.w2", settings.getDouble("w2",0.8));
		settings.addProperty("provider[@type]", "net.sf.jclec.syntaxtree.SyntaxTreeCreator");
		settings.addProperty("parents-selector[@type]", "net.sf.jclec.selector.TournamentSelector");
		
		settings.addProperty("recombinator[@type]", "net.sf.jclec.syntaxtree.SyntaxTreeRecombinator");
		settings.addProperty("recombinator[@rec-prob]", settings.getDouble("recombination-prob",0.8));
		settings.addProperty("recombinator.base-op[@type]", "net.sf.jclec.syntaxtree.rec.SelectiveCrossover");
		
		settings.addProperty("mutator[@type]", "net.sf.jclec.syntaxtree.SyntaxTreeMutator");
		settings.addProperty("mutator[@mut-prob]", settings.getDouble("mutation-prob",0.1));
		settings.addProperty("mutator.base-op[@type]", "net.sf.jclec.problem.classification.algorithm.tan.TanMutator");
		
		// Call super.configure() method
		super.configure(settings);
		
		classifier = new TanClassifier();
		
		// Establishes the metadata for the species
		((TanSyntaxTreeSpecies) species).setMetadata(getTrainSet().getMetadata());
		
		// Establishes the training set for evaluating
		((TanEvaluator) evaluator).setDataset(getTrainSet());

		//Get max-tree-depth
		int maxDerivSize = settings.getInt("max-deriv-size");
		((TanSyntaxTreeSpecies) species).setGrammar();
		((TanSyntaxTreeSpecies) species).setMaxDerivSize(maxDerivSize);
		
		// Parents selector
		SetParentsSelectorSettings(settings);
		
		// Recombinator 
		SetRecombinatorSettings(settings);
		
		// Mutator 
		SetMutatorSettings(settings);
		
		// Set copy probability
		double copyProb = settings.getDouble("copy-prob",0.1);
		setCopyProb(copyProb);
		
		// Set elitism probability
		double elitProb = settings.getDouble("elitist-prob",0.1);
		setElitistProb(elitProb);
		
		// Set support
		double support = settings.getDouble("support",0.1);
		setSupport(support);
	}
	
	/////////////////////////////////////////////////////////////////
	//----------------------------------------------- Private methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Set the mutator settings
	 * 
	 * @param settings Configuration settings
	 */
	@SuppressWarnings("unchecked")
	private void SetMutatorSettings(Configuration settings) {
		try {
			// Mutator classname
			String mutatorClassname = 
				settings.getString("mutator[@type]");
			// Mutator class
			Class<? extends IMutator> mutatorClass = 
				(Class<? extends IMutator>) Class.forName(mutatorClassname);
			// Mutator instance
			IMutator mutator = mutatorClass.newInstance();
			// Configure mutator if necessary
			if (mutator instanceof IConfigure) {
				// Extract mutator configuration
				Configuration mutatorConfiguration = settings.subset("mutator");
				// Configure mutator
				((IConfigure) mutator).configure(mutatorConfiguration);
			}
			// Set mutator
			setMutator(mutator);
		} 
		catch (ClassNotFoundException e) {
			throw new ConfigurationRuntimeException("Illegal mutator classname");
		} 
		catch (InstantiationException e) {
			throw new ConfigurationRuntimeException("Problems creating an instance of mutator", e);
		} 
		catch (IllegalAccessException e) {
			throw new ConfigurationRuntimeException("Problems creating an instance of mutator", e);
		}
		// Mutation probability 
		double mutProb = settings.getDouble("mutator[@mut-prob]",0.05);
		setMutationProb(mutProb);
	}

	/**
	 * Set the recombinator settings
	 * 
	 * @param settings Configuration settings
	 */
	@SuppressWarnings("unchecked")
	private void SetRecombinatorSettings(Configuration settings) {
		try {
			// Recombinator classname
			String recombinatorClassname = 
				settings.getString("recombinator[@type]");
			// Recombinator class
			Class<? extends IRecombinator> recombinatorClass = 
				(Class<? extends IRecombinator>) Class.forName(recombinatorClassname);
			// Recombinator instance
			IRecombinator recombinator = recombinatorClass.newInstance();
			// Configure recombinator if necessary
			if (recombinator instanceof IConfigure) {
				// Extract recombinator configuration
				Configuration recombinatorConfiguration = settings.subset("recombinator");
				// Configure species
				((IConfigure) recombinator).configure(recombinatorConfiguration);
			}
			// Set species
			setRecombinator(recombinator);
		} 
		catch (ClassNotFoundException e) {
			throw new ConfigurationRuntimeException("Illegal recombinator classname");
		} 
		catch (InstantiationException e) {
			throw new ConfigurationRuntimeException("Problems creating an instance of recombinator", e);
		} 
		catch (IllegalAccessException e) {
			throw new ConfigurationRuntimeException("Problems creating an instance of recombinator", e);
		}
		// Recombination probability 
		double recProb = settings.getDouble("recombinator[@rec-prob]",0.7);
		setRecombinationProb(recProb);
	}

	/**
	 * Set the parent selector settings
	 * 
	 * @param settings Configuration settings
	 */
	@SuppressWarnings("unchecked")
	private void SetParentsSelectorSettings(Configuration settings) {
		try {
			// Selector classname
			String parentsSelectorClassname = 
				settings.getString("parents-selector[@type]");
			// Species class
			Class<? extends ISelector> parentsSelectorClass = 
				(Class<? extends ISelector>) Class.forName(parentsSelectorClassname);
			// Species instance
			ISelector parentsSelector = parentsSelectorClass.newInstance();
			
			// Configure species if necessary
			if (parentsSelector instanceof IConfigure) {
				// Extract species configuration
				Configuration parentsSelectorConfiguration = settings.subset("parents-selector");
				// Configure species
				((IConfigure) parentsSelector).configure(parentsSelectorConfiguration);
			}
			
			// Set species
			setParentsSelector(parentsSelector);
		} 
		catch (ClassNotFoundException e) {
			throw new ConfigurationRuntimeException("Illegal parents selector classname");
		} 
		catch (InstantiationException e) {
			throw new ConfigurationRuntimeException("Problems creating an instance of parents selector", e);
		} 
		catch (IllegalAccessException e) {
			throw new ConfigurationRuntimeException("Problems creating an instance of parents selector", e);
		}
	}

	/**
	 * Performs the token competition between two populations.
	 * 
	 * @param newpop the offspring
	 * @param epop the external population
	 * @param evaluator the evaluator
	 * @return new external population with the best individuals
	 */
	private List<IIndividual> doTokenCompetition(List<IIndividual> newpop, List<IIndividual> epop, IEvaluator evaluator) 
	{
		int coversCount, nPatternsCovered;

		List<IIndividual> unitepopulation = new ArrayList<IIndividual>();

		unitepopulation.addAll(newpop);
		unitepopulation.addAll(epop);
		
		eset.clear();
		
		int bsetSize = bset.size();
		
		if (unitepopulation.size() > bsetSize) 
			unitepopulation = bettersSelector.select(unitepopulation, bsetSize);
		else
			unitepopulation = bettersSelector.select(unitepopulation);
		
		ArrayList<IInstance> instances = getTrainSet().getInstances();
		int numInstances = instances.size();
		
		boolean[] patternsCovered = new boolean[numInstances];
		
		int uniteSize = unitepopulation.size();
		// For each individual from the population
		for (int i = 0; i < uniteSize; i++)
		{
			// Individual conversion
			Rule rule = (Rule) ((SyntaxTreeRuleIndividual) unitepopulation.get(i)).getPhenotype();

			// Number of times that the patterns is covered
			coversCount = 0;
			// Number of patterns covered
			nPatternsCovered = 0;
			
			for(int j=0; j<numInstances; j++)
			{
				IInstance instance = instances.get(j);
				
				if(instance.getValue(getTrainSet().getMetadata().getClassIndex()) == execution) 
				{
					if((Boolean) rule.covers(instance))
					{
						coversCount++;
					
						if(!patternsCovered[j])
						{
							patternsCovered[j] = true;
							nPatternsCovered++;
						}
					}
				}
			}
			
			// The fitness is modified based on the token competition
			if (nPatternsCovered != 0) 
			{
				double fitness = ((SimpleValueFitness) unitepopulation.get(i).getFitness()).getValue();
				fitness = fitness * ((double) nPatternsCovered / coversCount);
				
				unitepopulation.get(i).setFitness(new SimpleValueFitness(fitness));
				
				if (((double) nPatternsCovered / coversCount) >= getSupport()) 					
					eset.add(unitepopulation.get(i).copy());
			} 
			else
				unitepopulation.get(i).setFitness(new SimpleValueFitness(0.0));
		}
		
		return eset;
	}
	
	// ///////////////////////////////////////////////////////////////
	// ------------------------- Overwriting java.lang.Object methods
	// ///////////////////////////////////////////////////////////////

	public boolean equals(Object other) {
		if (other instanceof TanAlgorithm) {
			TanAlgorithm cother = (TanAlgorithm) other;
			EqualsBuilder eb = new EqualsBuilder();
			// Call super method
			eb.appendSuper(super.equals(other));
			// Parents selector
			eb.append(parentsSelector, cother.parentsSelector);
			// Mutator
			eb.append(mutator, cother.mutator);
			// Recombinator
			eb.append(recombinator, cother.recombinator);
			// Return test result
			return eb.isEquals();
		} else {
			return false;
		}
	}

	// ///////////////////////////////////////////////////////////////
	// ---------------------------- Overwriting BaseAlgorithm methods
	// ///////////////////////////////////////////////////////////////

	@Override
	protected void doSelection() 
	{
		int elitistPopulationSize = (int) Math.round(populationSize * elitistProb);
		List<IIndividual> bsetAux = new ArrayList<IIndividual>();
		cset = new ArrayList<IIndividual>();
		
		List<IIndividual> bsetBest = bettersSelector.select(bset);

		for (int i = 0; i < elitistPopulationSize; i++)
			cset.add(bsetBest.get(i));
		
		for (int i = elitistPopulationSize; i < populationSize; i++)
			bsetAux.add(bsetBest.get(i)); 

		pset = parentsSelector.select(bsetAux);
	}

	@Override
	protected void doGeneration() 
	{
		// Recombine parents
		rset = recombinator.recombine(pset);	
		// Add non-recombined inds
		rset.addAll(recombinator.getSterile());
		// Mutate filtered inds
		List<IIndividual> mset = new ArrayList<IIndividual>();
		mset = mutator.mutate(rset);
		// Add non-mutated inds
		mset.addAll(mutator.getSterile());
		
		evaluator.evaluate(mset);
		
		// Reproduction
		for (IIndividual ind : bset)
			if (randgen.coin(copyProb))
				cset.add(ind.copy());
		
		cset.addAll(mset);
	}

	@Override
	protected void doReplacement() {
	}

	@Override
	protected void doUpdate()
	{
		// Do token competition
		eset = doTokenCompetition(cset, eset, evaluator);
		
		bset = bettersSelector.select(cset, populationSize);

		// Clears parents and offsprings
		cset = pset = rset = null;
	}

	protected void doControl() 
	{
		// Set the rule consequent to the current execution
		for(IIndividual ind : bset)
			((SyntaxTreeRuleIndividual) ind).getPhenotype().setConsequent(execution);
		
		// If maximum number of generations is exceeded, evolution is finished
		if (generation >= maxOfGenerations) 
		{
			execution++;
			
			for(IIndividual ind : eset)
			{
				Rule rule = (Rule) ((SyntaxTreeRuleIndividual) ind).getPhenotype();
				rule.setConsequent(execution - 1);
				
				((CrispRuleBase) classifier).addClassificationRule(rule);				
			}
			
			// If all classes have been covered then finish
			if (execution == getTrainSet().getMetadata().numberOfClasses()) 
			{
				state = FINISHED;
				
				// Sort the rules of the classifier 
				((TanClassifier) classifier).sortClassifier(getTrainSet());
				
				return;
			}
			else
			{
				// Execute the algorithm with other class
				((TanEvaluator) evaluator).setClassifiedClass(execution);

				eset.clear();
				generation = 0;
				doInit();
			}
		}
	}
}