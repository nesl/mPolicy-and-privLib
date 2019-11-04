
package privLib;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import privLib.PolicyManager;
import privLib.PropositionalEvaluate;

// Designed to read streams, more specifically lines from a file
//Also designed to attach policies to the correct file
public class StreamReader {
	
	public String entity_name = "";
	
	public StreamReader(String name) {
		entity_name = name;
	}

	
	//First we need to grab all the policies from the policymanager for **this particular entity**
	// We use this to determine the relevant contexts to search the stream for.
	public ArrayList<String> getRelevantContexts(Map<String, ArrayList<Policy>> entities_to_policies) {
		
		//Get all policies for this entity
		ArrayList<Policy> all_policies = entities_to_policies.get(this.entity_name);
		
		//For each policy, get all the unique context types
		ArrayList<String> all_context_types = new ArrayList<String>();
		for (Policy p : all_policies) {
			List<String> data_window_funcs = p.data_window_prop_funcs;
			
			for (String func : data_window_funcs) {
				
				String context_type = Propositions.getContextType(func);
				
				//If it's not already in the context_types, we add it in.
				if(!all_context_types.contains(context_type)) {
					all_context_types.add(context_type);
				}
			}
		}
		
		//We have to convert this policy formatted context type into what fields we are looking for
		// in the context stream.
		ArrayList<String> relevant_contexts = Propositions.getContextStreamTypes(all_context_types);

		return relevant_contexts;
		
	}
	
	//Get the relevant context values, BUT it should be using the relevant indexes
	public ArrayList<String> getRelevantValues(String line, ArrayList<Integer> relevant_indexes) {
		
		ArrayList<String> values_str = new ArrayList<String>(Arrays.asList(line.split(", +")));
		ArrayList<String> relevant_values_str = new ArrayList<String>();
		
		//Add relevant values from context stream
		for(int i = 0; i < values_str.size(); ++i) {
			if(relevant_indexes.contains(i)) {
				relevant_values_str.add(values_str.get(i));
				//System.out.println("WHATF: " + values_str.get(i));
			}
		}
		
		return relevant_values_str;
	}
	
	
	// Read the context file
	//Then, we iterate through the context stream, keeping track of only these relevant contexts.
	//Whenever the context changes, we iterate through each policy
	// We compare each policy to the context stream.  If the context stream satisfies it
	//  *we have to create our own satsifaction code/function
	// then we add this change it policy and attach a timestamp to it with the context stream.
	public int receiveStream(String filedir, String filename, Map<String, ArrayList<Policy>> entities_to_policies) {
		
		//Count the number of lines in the context file (i.e. data points)
		int context_line_count = 0;
		String filepath = filedir + filename;
		PrintWriter policy_stream;
		try {
			policy_stream = new PrintWriter(filedir + "policystream.txt", "UTF-8");
			
			ArrayList<String> relevant_contexts = getRelevantContexts(entities_to_policies);
			ArrayList<String> headers = null;
			ArrayList<Integer> relevant_stream_indexes = new ArrayList<Integer>();
			//This is an integer indexing the relevant context stream representing the index of context names it belongs to.
			// The reason why we have two of these indexes is that one is used for identifying the relevant values of 
			//  the original stream, while the other is used to relate the context stream value to a particular context name
			ArrayList<Integer> relevant_context_to_index_mapping = new ArrayList<Integer>();
			ArrayList<String> current_stream_values = null;
			
			
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(
						filepath));
				String line = reader.readLine();
				while (line != null) {
					//If we haven't received the headers, we set them up here.
					if(headers == null) {
						headers = new ArrayList<String>(Arrays.asList(line.split(", +")));
						
						//If a header matches a relevant context, keep track of the index
						// This should be linking the correct indexes to the context names
						for(String header : headers) {
							
							//System.out.println("DUH: " + header);
							//System.out.println("DUH2: " + relevant_contexts.indexOf(header.split("\'")[1]));
							//System.out.println("DUH3: " + headers.indexOf(header));
							
							if(relevant_contexts.contains(header.split("\'")[1])) {
								relevant_stream_indexes.add(headers.indexOf(header));
								//System.out.println("Relevant");
								relevant_context_to_index_mapping.add(relevant_contexts.indexOf(header.split("\'")[1]));
							}
						}
					}
					
					// We received the headers, now we just check if the relevant values have changed.
					else {
						//System.out.println(line);
						ArrayList<String> current_vals_str = getRelevantValues(line, relevant_stream_indexes);
						if(current_stream_values == null) { //If null, we just set the current values
							current_stream_values = current_vals_str;
						}
						else {  //We have to check if the values have changed.  If so, we evaluate the values against policies.
							
							if(!current_stream_values.equals(current_vals_str)) {
								//Pass along the current stream values and context type names to be evaluated
								String relevant_policy_ids = PropositionalEvaluate.getRelevantPolicies(current_vals_str, relevant_contexts,
										relevant_context_to_index_mapping,
										entities_to_policies.get(this.entity_name));
								
								//If the policy sets have actually changed, then print to file.
								if(!relevant_policy_ids.isBlank()) {
									policy_stream.println(line + ":" + relevant_policy_ids);
								}
								
							}
							current_stream_values = current_vals_str;
						}
						
						
					}
					
					

					// read next line
					line = reader.readLine();
					context_line_count++;
				}
				//System.out.println(count2);
				reader.close();
				policy_stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return context_line_count;
		
	}
	
	

	
	

}
