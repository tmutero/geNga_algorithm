package net.sf.jclec.problem.util.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.jclec.problem.util.dataset.attribute.CategoricalAttribute;
import net.sf.jclec.problem.util.dataset.attribute.IAttribute;
import net.sf.jclec.problem.util.dataset.attribute.IntegerAttribute;
import net.sf.jclec.problem.util.dataset.attribute.NumericalAttribute;
import net.sf.jclec.problem.util.dataset.instance.IInstance;
import net.sf.jclec.problem.util.dataset.instance.Instance;
import net.sf.jclec.problem.util.dataset.metadata.ClassificationMetadata;
import net.sf.jclec.util.range.Closure;
import net.sf.jclec.util.range.Interval;

/**
 * Dataset implementation for the DAT format (KEEL dataset).
 * 
 * The main method is loadInstances() that reads the dataset file, the metadata information and the instances.
 * 
 * 
 * 
 *  
 * 
 * 
 */

public class KeelDataSet extends FileDataset {

	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////
	
	/** Generated by Eclipse */
	
	private static final long serialVersionUID = 1L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------- Internal Variables
	/////////////////////////////////////////////////////////////////
	
	 /** The keyword used to denote the relation name */
	
	static String KEEL_RELATION = "@relation";
	
	/** The keyword used to denote the attribute description */
	  
	static String KEEL_ATTRIBUTE = "@attribute";

	/** The keyword used to denote the start of the arff data section */
	  
	static String KEEL_DATA = "@data";
	
	/** The keyword used to denote the output attribute */
	  
	static String KEEL_OUTPUT = "@output";
	
	/** Symbol which represents missed values */
	
	protected String missedValue;
	
	/** Symbol which represents commentted values */
	
	protected String commentedValue;
	
	/** Symbol which represents the separation between values */
	
	protected String separationValue;

	/** Buffer Instance */
	
	protected String bufferInstance = new String();
	
	/** Private cursor */
	private Instance cursorInstance;
	
	/////////////////////////////////////////////////////////////////
	// -------------------------------------------------- Constructor
	/////////////////////////////////////////////////////////////////
	
	public KeelDataSet()
	{
		super();
		
		missedValue = "?";
		separationValue = ",";
		commentedValue = "%";
	}
	
	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Load instances from the data set
	 * 
	 * Open dataset, obtain metadata, read instances, close dataset
	 */
	
	public void loadInstances()
	{		
		try {
			
			open();
			
            instances = new ArrayList<IInstance>();
            
            reset();
            
            while(next())
            {
            	instances.add(cursorInstance.copy());
            }
            
            setInstances(instances);
            
            close();
        }
		catch (Exception e) 
        {
            e.printStackTrace();
        }
	}
	
	/**
	 * Set the dataset instances
	 * 
	 * @param instances the instances
	 */
	
	public void setInstances(ArrayList<IInstance> instances)
	{
		this.instances = instances;
	}
	
	/**
	 * Get the dataset instances
	 * 
	 * @return instances
	 */
	
	public ArrayList<IInstance> getInstances()
	{
		return instances;
	}
	
	/**
	 * Add the new instances to the dataset
	 * 
	 * @param newinstances instances to add
	 */
	
	public void addInstances(ArrayList<IInstance> newinstances)
	{
		this.instances.addAll(newinstances);
	}
	
	/////////////////////////////////////////////////////////////////
	// ---------------------------------------------- Private Methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Opens the dataset and obtains the metadata
	 */
	
	private void open()
	{
		// Generate the specification from header of data source file
		obtainMetadata(fileName);
	}
	
	/**
	 * Resets the dataset cursor to the beginning
	 */
	
	private void reset(){
				
		try {
			fileReader.close();
			fileReader = new BufferedReader(new FileReader(new File(fileName)));

			//Read until finding the sentence @DATA
			String line = ((BufferedReader) fileReader).readLine();
			while (!line.equalsIgnoreCase(KEEL_DATA)){
				line = ((BufferedReader) fileReader).readLine();
			}
			bufferInstance = ((BufferedReader) fileReader).readLine();
		
			while(bufferInstance.startsWith(commentedValue) || bufferInstance.equalsIgnoreCase("")){
				bufferInstance = ((BufferedReader) fileReader).readLine();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	
	}
	
	/**
	 * Reads the next instance
	 */
	
	private boolean next() throws Exception
	{
		if(bufferInstance != null)
		{
			try{
				//Get the attributes of this instance
				StringTokenizer token = new StringTokenizer(bufferInstance, separationValue);
				int numAttributes = 0;
				ArrayList<Double> values = new ArrayList<Double>();
			
				while(token.hasMoreTokens()){
					IAttribute attribute = metadata.getAttribute(numAttributes);
					double value = attribute.parse(token.nextToken().trim());
					values.add(value);
								
					numAttributes++;
				}
				
				cursorInstance = new Instance(numAttributes);
				
				for(int i = 0; i < numAttributes; i++)
					cursorInstance.setValue(i,values.get(i));
				
				prepareNextInstance();
			
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Closes the dataset file
	 */

	private void close() throws Exception {
		try {
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate the dataset specification
	 *  
	 * @param fileName Name of data source file
	 */
	
	private void obtainMetadata(String fileName)
	{
		File file = new File(fileName);
		metadata = new ClassificationMetadata();
		
		try {
			
			fileReader = new BufferedReader(new FileReader(file));
			
			//Read until finding the sentence @DATA
			String line = ((BufferedReader) fileReader).readLine();
			StringTokenizer elementLine = new StringTokenizer(line);
			String element = elementLine.nextToken();
			
			while (!element.equalsIgnoreCase(KEEL_DATA)){
				
				if(element.equalsIgnoreCase(KEEL_ATTRIBUTE)){
					//The next attribute	
					String name = elementLine.nextToken();
					String type = elementLine.nextToken();
					
					if(type.equalsIgnoreCase("REAL") || type.equalsIgnoreCase("INTEGER")){
						addAttributeToSpecification(type, line, name);
					}
					else {
						addAttributeToSpecification("STRING", line, name);
					}
					
				}
				if(element.equalsIgnoreCase(KEEL_RELATION)){
					setName(elementLine.nextToken());
				}
		
				//Next line of the file
				line = ((BufferedReader) fileReader).readLine();
				while(line.startsWith(commentedValue) || line.equalsIgnoreCase(""))
					line = ((BufferedReader) fileReader).readLine();
				
				int index = line.indexOf('[');
				if(index != -1)
				line = line.substring(0,index) + " [" + line.substring(index+1);
				
				index = line.indexOf('{');
				if(index != -1)
				line = line.substring(0,index) + " {" + line.substring(index+1);
				
				elementLine = new StringTokenizer(line);
				element = elementLine.nextToken();
			}
					
			bufferInstance = ((BufferedReader) fileReader).readLine();
			
			while(bufferInstance.startsWith(commentedValue) || bufferInstance.equalsIgnoreCase("")){
				bufferInstance = ((BufferedReader) fileReader).readLine();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Store the next instance in bufferInstance
	 */
	private void prepareNextInstance()
	{		
		try
		{
			//Get the next instance
			String lineInstance = ((BufferedReader) fileReader).readLine();
			while(lineInstance.startsWith(commentedValue) || lineInstance.equalsIgnoreCase("")){
				lineInstance = ((BufferedReader) fileReader).readLine();
			}	

			bufferInstance = lineInstance;
		
		}catch(Exception e){
				bufferInstance = null;
		}
	}
	
	
	/**
	 * Add new attribute to the dataset specification
	 * 
	 * @param type Attribute type
	 * @param interval Intervals value
	 * @param name Attribute name
	 */
	private void addAttributeToSpecification(String type, String interval, String name)
	{
		// If the attribute is numerical
		if(type.equalsIgnoreCase("REAL"))
		{
				NumericalAttribute attribute = new NumericalAttribute();
				attribute.setName(name);
					
				// Obtain the intervals
				int minIndex = interval.indexOf("[");
				int maxIndex = interval.indexOf("]");
				
				interval = interval.substring(minIndex+1, maxIndex);
				
				if(minIndex < maxIndex){
										
					StringTokenizer tkInterval = new StringTokenizer(interval, ",");
					
					Interval intervals = new Interval();
				
					intervals.setClosure(Closure.ClosedClosed);
					intervals.setLeft(Double.valueOf((String) tkInterval.nextElement()));
					intervals.setRight(Double.valueOf((String) tkInterval.nextToken()));
									
					attribute.setInterval(intervals);
					
					//Add new attribute to the specification
					metadata.addAttribute(attribute);
				}
		}
		else if(type.equalsIgnoreCase("INTEGER"))
		{			
			IntegerAttribute attribute = new IntegerAttribute();
			attribute.setName(name);
				
			// Obtain the intervals
			int minIndex = interval.indexOf("[");
			int maxIndex = interval.indexOf("]");
			
			interval = interval.substring(minIndex+1, maxIndex);
			
			if(minIndex < maxIndex){
									
				StringTokenizer tkInterval = new StringTokenizer(interval, ",");
				
				net.sf.jclec.util.intset.Interval intervals = new net.sf.jclec.util.intset.Interval();
				
				intervals.setClosure(net.sf.jclec.util.intset.Closure.ClosedClosed);
				intervals.setLeft(Integer.valueOf((String) tkInterval.nextElement()));
				intervals.setRight(Integer.valueOf((String) tkInterval.nextToken().trim()));
								
				attribute.setInterval(intervals);
				
				//Add new attribute to the specification
				metadata.addAttribute(attribute);
			}
		}
		else
		{
			//Obtain the categorical values
			int minIndex = interval.indexOf("{");
			int maxIndex = interval.indexOf("}");
			
			interval = interval.substring(minIndex+1, maxIndex);
			
			if(minIndex < maxIndex){
				CategoricalAttribute attribute = new CategoricalAttribute();
				attribute.setName(name);
		
				StringTokenizer categories = new StringTokenizer(interval, ",");
				List<String> categoriesList = new ArrayList<String>();
				
				while(categories.hasMoreTokens())
					categoriesList.add(categories.nextToken().trim());
			
				attribute.setCategories(categoriesList);

				//Add new attribute to the specification
				metadata.addAttribute(attribute);
			}
		}
	}
	
   /**
    * Copy method
    * 
    * @return A copy of this dataset
    */
	
	@Override
	public IDataset copy() {
		
		KeelDataSet dataset = new KeelDataSet();
		
		dataset.setMetadata(metadata.copy());
		
		ArrayList<IInstance> instances = new ArrayList<IInstance>();
		
		for(IInstance instance : this.instances)
			instances.add(instance.copy());
		
		dataset.setInstances(instances);
			
		return dataset;
	}
}