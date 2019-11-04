package privLib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import privLib.Propositions;
import privLib.Constants;

//Evaluates propositional statements
public class PropositionalEvaluate {
		
	
		//Parse a propositional sentence that looks like ("ALL !1 !2")
		public static boolean parsePropSentence(String sentence, Map<Integer, String> index_to_funcs, 
				ArrayList<String> context_vals_str, ArrayList<String> new_mapped_names) {
			
			boolean satisfied = true;
			
			ArrayList<Boolean> sentencebools = new ArrayList<Boolean>();
			
			//Split the sentence.
			String[] splits = sentence.split(" ");
			
			//Generate a list of boolean values
			for(int i = 1; i < splits.length; ++i) {
				
				boolean neg = false;
				int index = -1;
				
				//If it's negative, we find the index and apply neg=true
				if(Character.compare(splits[i].charAt(0), '!') == 0) {
					neg = true;
					index = Integer.parseInt(splits[i].substring(1));
				}
				else {
					index = Integer.parseInt(splits[i]);
				}
				
				//Find the corresponding function
				String func = index_to_funcs.get(index);
				ArrayList<String> params = new ArrayList<String>(Arrays.asList(func.split("\\(|,|\\)")));


				//Get boolean values to determine if this is satisfied
				//Context vals str should correspond to the context_names
				boolean val = Propositions.callPropositionalFuncs(params, context_vals_str, new_mapped_names);

				if(neg) val = !val;  //If we negate it, we flip the val
				sentencebools.add(val);
				
			}
			
			
			//Determine which logic is involved
			if( splits[0].equals(Constants.ALL)) {
				
				//For ALL, if any boolean is false, the entire statement is false
				satisfied = true;
				for (boolean b : sentencebools) {
					if(!b) satisfied = false;
				}
			}
			else if(splits[0].equals(Constants.ANY)) {
				
				//For ANY, if any boolean is true, the entire statement is true
				satisfied = false;
				for (boolean b : sentencebools) {
					if(b) satisfied = true;
				}
				
			}
			else if(splits[0].equals(Constants.NONE)) {
				
				//For NONE, if any boolean is true, the entire statement is false
				satisfied = true;
				for (boolean b : sentencebools) {
					if(b) satisfied = false;
				}
			}
			
			return satisfied;
		}
	
		// Return the relevant policy IDs for a particular context values and corresponding names, as well as the functional
		// comparison names
		public static String getRelevantPolicies(
				ArrayList<String> context_vals_str, ArrayList<String> context_names,
				ArrayList<Integer> context_indices, ArrayList<Policy> all_policies) {
			
			ArrayList<String> ids = new ArrayList<String>();
		
			//Produce new mapped names - now context names and context_vals_str should reference the same index
			ArrayList<String> new_mapped_names = new ArrayList<String>();
			for (int index : context_indices) {
				//System.out.println(context_names.get(index));
				new_mapped_names.add(context_names.get(index));
			}
			
			//For each policy, you want to get the data functions, and extract the comparison you are trying to make
			for(Policy p : all_policies) {
				
				List<String> data_window_funcs = p.data_window_prop_funcs;
				List<String> data_window_sentences = p.data_window_prop_sentences;
				Map<Integer, String> index_to_funcs = p.data_window_index_to_funcs;
				
				boolean sentence_satisfied = true;
				for(String sentence : data_window_sentences) {
					
					//If any sentence returns false, this policy is not relevant
					if(!parsePropSentence(sentence, index_to_funcs, context_vals_str, new_mapped_names)) {
						sentence_satisfied = false;
						break;
					}
				}
				
				//If the sentence is satisfied (all sentences are true), we attach the policy.
				if(sentence_satisfied) {
					ids.add(p.policy_id);
				}
			}
			
			String id_result = "";
			for(String id : ids) {
				id_result += id + " ";
			}
			
			return id_result;
			
		}
	
}
