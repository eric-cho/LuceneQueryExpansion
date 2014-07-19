package in.student.project.jgibblda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LDAInferencer {

	private static int NUMITER = 100;
	
	private static LDAInferencer wrapper;
	private LDACmdOption ldaOption;
	private Inferencer inferencer;
	
	// For inferencing
	public static LDAInferencer getInstance(String modelDir, String modelName)
	{
		if(wrapper == null)
			wrapper = new LDAInferencer(modelDir, modelName);
		return wrapper;
	}
		
	public LDAInferencer (String modelDir, String modelName)
	{
		LDACmdOption ldaOption = new LDACmdOption(); 
		ldaOption.inf = true; 
		ldaOption.dir = modelDir;//"models\\casestudy-en"; 
		ldaOption.modelName = modelName;//"model-final"; 
		ldaOption.niters = NUMITER; 
		Inferencer inferencer = new Inferencer(); 
		inferencer.init(ldaOption); 
		this.inferencer = inferencer;
		
	}
	
	// For learning
	public static LDAInferencer getInstance()
	{
		if(wrapper == null)
			wrapper = new LDAInferencer();
		return wrapper;
	}
	
	public LDAInferencer ()
	{
		
	}
	
	public Topic [] extractTopicViaLDA(String [] input)
	{
		Topic t [] = new Topic[input.length];
		Model newModel = inferencer.inference(input);
		int[] topicIndex = new int[input.length];
		
		double max=-1;
		int maxIndex=-1;
		for(int i=0; i<newModel.theta.length ; i++) // i-> document
		{
			max=-1;
			maxIndex=-1;
			for(int j=0;j<newModel.theta[i].length; j++)
			{
				if(max < newModel.theta[i][j])
				{
					max = newModel.theta[i][j];
					maxIndex = j;
				}
			}
			topicIndex[i]=maxIndex;
		}
		

		for(int i=0;i<topicIndex.length;i++)
		{
			max = -1;
			maxIndex = -1;
			
			for (int w = 0; w < newModel.V; w++){
				
				if (max < newModel.phi[topicIndex[i]][w])
				{
					max = newModel.phi[topicIndex[i]][w];
					maxIndex = w;
				}				
			}//end foreach word
			
			if(newModel.data.localDict.contains((Integer) maxIndex))
			{
				t[i] = new Topic(newModel.data.localDict.getWord(maxIndex), newModel.theta[i][topicIndex[i]]);
			}
		}
		
		return t;
	}
	
	
		
	
}
