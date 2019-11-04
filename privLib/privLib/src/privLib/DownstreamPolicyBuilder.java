package privLib;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import privLib.Policy;
import privLib.Propositions;
import privLib.QueryStream;


public class DownstreamPolicyBuilder {
	
	public void print(String s) {
		System.out.println(s);
	}
	
	
	//Clean a string, remove spaces and quotations and whatnot
	public String cleanStr(String s) {
		
		String fixed = s;
		if(s.contains("\"")) {
			fixed = s.strip().split("\"")[1];
		}
		return fixed;
	}
	
	//Parse a data window, returning a dictionary of function_name, arraylist of param types
	// IMPORTANT - if we have a policy with two of the same function (i.e. TimeRange)
	//  we have to add multiple sets of params to the same key, resulting in an array of arrays
	public Map<String, ArrayList<ArrayList<String>>> parseDataWindow(ArrayList<String> funcs) {
		
		Map<String, ArrayList<ArrayList<String>>>dict = new HashMap<String, ArrayList<ArrayList<String>>>();
		
		//Iterate through the propositional statements, and break each one into func_name and parameters
		for (String func : funcs) {
			ArrayList<String> params = new ArrayList<String>(Arrays.asList(func.split("\\(|,|\\)")));
			
			String key = params.get(0);
			ArrayList<String> new_params = new ArrayList<String>(params.subList(1, params.size()));
			
			if(!dict.containsKey(key)) { //Key doesn't exist, so we add in a list of lists.
				ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
				dict.put(key, x);
			}
			ArrayList<ArrayList<String>> vals = dict.get(key);
			vals.add(new_params);
			dict.put(key, vals);
		}
		
		return dict;
		
	}
	
	//Produce an English version of the policies we were using
	public ArrayList<String> produceNewDataWindowPolicy(Map<String, ArrayList<ArrayList<String>>> prop_dict) {
		ArrayList<String> out = new ArrayList<String>();
		out.add("DATA-WINDOW:");
		out.add("ALL:");
		
		for(String key : prop_dict.keySet()) {
			
			ArrayList<ArrayList<String>> a = prop_dict.get(key);
			
			
			//For every new set of params, create a new line (each set of params represents a function)
			for(ArrayList<String> b : a) {
				String params = "";
				String line = key;
				for(String c : b) {
					//Determine whether or not we add a comma with the params.
					if(params.isBlank()) {
						params += c;
					}
					else {
						params += "," + c;
					}
					
				}
				
				line += "(" + params + ")";
				out.add(line);
			}
			
		}
		
		//System.out.println(out);
		return out;
	}
	
	//Combine two data windows together
	// For simplicity, assume everything is expressed in the positive and everything is the same logic function (ALL/ANY/NONE)
	public ArrayList<String> combineDataWindows(ArrayList<String> prop_funcs1, ArrayList<String> prop_funcs2) {
		
		Map<String, ArrayList<ArrayList<String>>> prop_dict_1 = parseDataWindow(prop_funcs1);
		Map<String, ArrayList<ArrayList<String>>> prop_dict_2 = parseDataWindow(prop_funcs2);
		
		Map<String, ArrayList<ArrayList<String>>> final_prop_dict = new HashMap<String, ArrayList<ArrayList<String>>>();
		
		//Iterate through each keyset, and check if it matches any of the other ones.
		//  If so, we have to iterate through ALL parameters and decide what the most limiting ones are
		// for a new policy.
		for(String key1 : prop_dict_1.keySet()) {
			
			//If the dictionary doesn't contain the key, we initialize it with an ArrayList<
			if(!final_prop_dict.containsKey(key1)) {
				ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
				final_prop_dict.put(key1, x);
			}
			
			//We have a match, we have to go find the most restrictive parameters
			if(prop_dict_2.containsKey(key1)) {
				
				
				 //Set up the arraylist to add to the dictionary
				 ArrayList<ArrayList<String>> to_add = final_prop_dict.get(key1);
				
				 //Ok, so here we get the best list of list of params for the first prop_dict.
				 to_add = Propositions.getMostRestrictiveParams(prop_dict_1.get(key1), key1,
						 final_prop_dict.get(key1));
				 // We then do the same thing for the second prop_dict.
				 to_add = Propositions.getMostRestrictiveParams(prop_dict_2.get(key1), key1,
						 to_add);

				 // We finally add it to the resulting dict
				 final_prop_dict.put(key1, to_add);
				 
			}
			else { //Keys don't match, we just gotta put this in the policy
				final_prop_dict.put(key1, prop_dict_1.get(key1));
			}
			
		}
		for(String key2 : prop_dict_2.keySet()) {
			//Add params to end policy - if it doesn't exist, we add it.
			if(!final_prop_dict.containsKey(key2)) {
				final_prop_dict.put(key2, prop_dict_2.get(key2));
			}
		}
		
		//Write this out into english
		return produceNewDataWindowPolicy(final_prop_dict);
		
	}
	
	//Breaks a string of terms into multiple strings
	public String[] breakTerms(String term) {
		
		String[] broken_terms = term.split("\\)");
		//System.out.println(broken_terms.length);
		
		return broken_terms;
	}
	
	public ArrayList<String> produceDataOpsIntentsPolicy(ArrayList<String> logics, ArrayList<String> terms) {
		
		ArrayList<String> out = new ArrayList<String>();
		out.add("ALLOWED-OPERATIONS-INTENTS:");
		//Iterate through all logic statements.  Get the corresponding terms and break them up.
		for(int i = 0; i < logics.size(); ++i) {
			
			//String current_statement = "\n" + logics.get(i) + ":";
			out.add(logics.get(i) + ":");
			String[] current_terms = breakTerms(terms.get(i));
			for(String term : current_terms) { //Add each term as a new line to the current statement
				if(!term.isBlank()) { //Ignore all blank terms
					//Add an end parenthesis because we cut it out during breakTerms
					out.add(term + ")");
				}
				
			}
			
		}
		return out;
		
	}
	
	public ArrayList<String> combineDataOpsAndIntents(String p1, String p2) {
		
		//Get logic statements from p1
		ArrayList<String> logics1 = QueryStream.getLogic(p1);
		ArrayList<String> terms1 = QueryStream.getTerms(p1);
		//Get logic statements from p2
		ArrayList<String> logics2 = QueryStream.getLogic(p2);
		ArrayList<String> terms2 = QueryStream.getTerms(p2);
		
		ArrayList<String> result_logics = new ArrayList<String>();
		ArrayList<String> result_terms = new ArrayList<String>();
		
		//Iterate through logic for p1.  If it matches anything in p2,
		// we add the terms from p1 and p2 together.
		for(int i = 0; i < logics1.size(); ++i) {
			
			String result_term = "";
			//If any logic matches, we combine the corresponding terms together
			if(logics2.contains(logics1.get(i))) {
				result_term = terms1.get(i) + " " + terms2.get(i);
			}
			else {
				result_term = terms1.get(i);
			}
			result_logics.add(logics1.get(i));
			result_terms.add(result_term);
		}
		//Iterate through the logic for p2.  If it includes any logic not
		// in the results, then add it in.
		for(int i = 0; i < logics2.size(); ++i) {
			
			String result_term = "";
			//If any logic matches, we combine the corresponding terms together
			if(!result_logics.contains(logics2.get(i))) {
				result_logics.add(logics2.get(i));
				result_term = terms2.get(i);
				result_terms.add(result_term);
			}
		}
		
//		for(String l : result_logics) {
//			System.out.println(l);
//		}
		
		//Now we can create a new policy given the two strings
		return produceDataOpsIntentsPolicy(result_logics, result_terms);
	}
	
	public void printNewPolicy(ArrayList<String> data_windows, ArrayList<String> data_ops_intents, 
			String data_methods,
			String policy_id, String data_stream_type, String data_entity, String entity_type) {
		
		System.out.println("POLICY: " + policy_id);
		System.out.println("ENTITY: " + data_entity);
		System.out.println("ENTITY-TYPE: " + entity_type);
		System.out.println("DATA-STREAM-TYPE: " + data_stream_type);
		
		for(String window : data_windows) {
			System.out.println(window);
		}
		System.out.println("DATA-METHODS:");
		System.out.println("ALL:\n" + data_methods);
//		for(String method_logic : data_methods.keySet()) {
//			System.out.println(method_logic + ":");
//			for(ArrayList<String> method_clause : data_methods.get(method_logic)) {
//				
//				for(String method : method_clause) {
//					System.out.println(data_methods.get(method));
//				}
//				
//			}
//		}
		for(String ops_intents : data_ops_intents) {
			System.out.println(ops_intents);
		}
		
		
	}
	
	//Combine two policies together
	public Policy combinePolicies(Policy p1, Policy p2,
			ArrayList<Integer> trust_vals, ArrayList<Double> weights,
			String policy_id, String policy_entity, String data_stream_type,
			String entity_type) {
		
		Policy downstreamPolicy = new Policy();
		downstreamPolicy.policy_id = policy_id;
		downstreamPolicy.data_stream_type = data_stream_type;
		downstreamPolicy.data_entity = policy_entity;
		downstreamPolicy.data_entity_type = entity_type;
		
		ArrayList<String> policy_data = new ArrayList<String>();
		
		//Combining data windows together
		ArrayList<String> data_windows = combineDataWindows(p1.data_window_prop_funcs, p2.data_window_prop_funcs);
		policy_data.addAll(data_windows);
		
		//Combining operations and intents together
		ArrayList<String> data_ops_intents = combineDataOpsAndIntents(p1.data_ops_intents, p2.data_ops_intents);
		policy_data.addAll(data_ops_intents);
		
		//Read in the data for this entity
		downstreamPolicy.readPolicy(policy_data);
		
		//Adding the data methods from the thresholding
		String data_method = getDataMethodFromThreshold(trust_vals, weights);
		ArrayList<ArrayList<String>> data_method_params = new ArrayList<ArrayList<String>>();
		ArrayList<String> data_method_param = new ArrayList<String>();
		data_method_param.add(data_method);
		data_method_params.add(data_method_param);
		downstreamPolicy.data_methods.put("ALL", data_method_params);
		
//		printNewPolicy(data_windows, data_ops_intents, data_method, 
//				policy_id, data_stream_type, policy_entity, entity_type);
		
		return downstreamPolicy;
	}
	
	
	//params : binary trust values in entity, entity class, operation, data-stream-type
	public double computeThreshold(ArrayList<Integer> trust_vals, ArrayList<Double> weights) {
		
		double result = 0;
		for (int i = 0; i < trust_vals.size(); ++i) {
			result += trust_vals.get(i) * weights.get(i);
		}
		
		return result;
	}
	
	//Use threshold to get the data methods to add
	public String getDataMethodFromThreshold(ArrayList<Integer> trust_vals, ArrayList<Double> weights) {
		double threshold = computeThreshold(trust_vals, weights);
		
		String data_method = "";
		//System.out.println(threshold);
		
		if(threshold < 1.2) {
			//DENY ACCESS
			data_method = "DENY_ACCESS()";
		}
		else if(threshold < 2.4) {
			//PERTURB
			data_method = "PERTURB()";
		}
		else if(threshold < 3.6) {
			//REDUCE GRANULARITY
			data_method = "REDUCE_GRANULARITY()";
		}
		
		return data_method;
		
	}

}
