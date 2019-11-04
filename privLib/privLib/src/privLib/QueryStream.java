package privLib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import privLib.Constants;
import privLib.AttributeDAG;

public class QueryStream {
	
	AttributeDAG intent_dag = new AttributeDAG("intents");
	AttributeDAG ops_dag = new AttributeDAG("ops");
	
	//Check if the current time window
	public static boolean checkTimeWindow(long start, long end, String line) {
		
		boolean addPolicies = false;
		
		String timestamp_str = line.split(",")[0];
		long timestamp = Long.parseLong(timestamp_str);
		
		if (start <= timestamp && timestamp <= end) {
			addPolicies = true;
			//System.out.println(timestamp);
		}
		
		return addPolicies;
	}
	
	//Gets a policy from the line of a file
	// Policies are separated by a colon
	public static String[] getPolicyFromLine(String line) {
		
		String policy_str = line.split(":")[1];
		String[] policies = policy_str.split(" ");
		
		return policies;
	}
	
	//
	
	//Get the relevant policies for a query's time window for a stream file
	public static ArrayList<String> getPolicies(long start, long end, String filepath, String policy_filename) {
		
		ArrayList<String> policies = new ArrayList<String>();
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(
					filepath + policy_filename));
			String line = reader.readLine();
			while (line != null) {
				
				//Check if this policy stream is within the window.  If so, begin adding policies.
				if( checkTimeWindow(start, end, line)) {
					String[] current_policies = getPolicyFromLine(line);
					
					for(String pol : current_policies) {
						if(!policies.contains(pol)) { //If the current policy list doesn't have this policy, add it in.
							policies.add(pol);
						}
					}
					
				}
				
				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return policies;
	}
	
	public static ArrayList<String> getTerms(String ops_intents) {
		
		ArrayList<String> terms = new ArrayList<String>();
		
		//Cut out the logic parts.
		String[] compares = ops_intents.split("ANY|ALL|NONE");
		
		//System.out.println(compares.length);
		for(String x :compares) {
			
			if(!x.isBlank()) {
				terms.add(x);
			}
			
		}
		return terms;
	}
	
	//Separate Logic from Operations and Intents
	public static ArrayList<String> getLogic(String ops_intents) {
		
		ArrayList<String> logics = new ArrayList<String>();
		
		
		//Get the Logic Components
		String[] logic_compares = ops_intents.split(" ");
		for(String x :logic_compares) {
			
			if(x.equals(Constants.ALL) || x.equals(Constants.ANY) || x.equals(Constants.NONE)) {
				logics.add(x);
			}
		}
		return logics;
	}
	
	//Removes whitespace and quotations
	public String cleanStr(String s) {
		return s.strip().split("\"")[1];
	}
	
	
	//Evaluate a statement of bools based on the logics invovled.
	public boolean evaluateStatement(ArrayList<Boolean> term_bools, String logic) {
		boolean satisfied = false;
		//Determine which logic is involved
		if( logic.equals(Constants.ALL)) {
			
			//For ALL, if any boolean is false, the entire statement is false
			satisfied = true;
			for (boolean b : term_bools) {
				if(!b) satisfied = false;
			}
		}
		else if(logic.equals(Constants.ANY)) {
			
			//For ANY, if any boolean is true, the entire statement is true
			satisfied = false;
			for (boolean b : term_bools) {
				if(b) satisfied = true;
			}
			
		}
		else if(logic.equals(Constants.NONE)) {
			
			//For NONE, if any boolean is true, the entire statement is false
			satisfied = true;
			for (boolean b : term_bools) {
				if(b) satisfied = false;
			}
		}
		//System.out.println("EVAL2: " + logic + " " + satisfied);
		return satisfied;
	}
	
	//Checks the terms to determine a boolean array
	public boolean evaluateTerms(ArrayList<String> statements, String operation, String intent,
			ArrayList<String> logics) {
		
		boolean neg = false;
		boolean is_operation = true;
		boolean isSatisfied = true;
		
		//Do the intents/ops match?
		boolean current_term_eval = false;
		int current_statement_index = 0;
		for (String statement : statements) {
			
			String[] terms = statement.split("\\(|,|\\)");
			ArrayList<Boolean> term_bools = new ArrayList<Boolean>(); //These are the bools for a statement.
			
			boolean complete_statement = false;
			boolean matches_op = false;
			boolean matches_intent = false;
			
			//System.out.println(terms.length);
			for (String t : terms) {  //Iterate through a statement (i.e. "op" "intent" NOT "op2" "intent2")
				if(!t.isBlank()) { //Ignore all blank terms
					
					//System.out.println(t);
					
					if(t.strip().equals(Constants.NOT)) { //Apply negation
						neg = true;
					}
					else { //No negation, just check if this is the operation or intent
						if(is_operation) {
							matches_op = ops_dag.queryMatch(operation, cleanStr(t));
							//System.out.println("OP: " + cleanStr(t));
							is_operation = false;
						}
						else {
							matches_intent = intent_dag.queryMatch(intent, cleanStr(t));
							//System.out.println("INTENT: " + cleanStr(t));
							is_operation = true;
							complete_statement = true;
						}
					}
					
					//If both intents and ops match, we return true.  If it's negative, we flip the return.
					
					if(complete_statement) {
						if(matches_op && matches_intent) {
							//System.out.println("Both are true!");
							current_term_eval = true;
						}
						if(neg) {
							current_term_eval = !current_term_eval;
							neg = false;
						}
						//System.out.println("EVAL1: " + current_term_eval);
						matches_op = false; //Set everything back to false
						matches_intent = false; //Set everything back to false
						term_bools.add(current_term_eval);
						current_term_eval = false; //Set everything back to false
						complete_statement = false;
					}
					
					
				}
				
			}
			
			
			if(!evaluateStatement(term_bools, logics.get(current_statement_index))) {
				//If this statement is not satisfied, we return false
				isSatisfied = false;
				break;
			}
			
			current_statement_index++;
			
		}
		
		return isSatisfied;
	}
	
	//Query the stream using the timestamps.  Check the relevant policies and see if they 
	// satisfy the ops and intents.
	// Use the Policy ID to map to the policies
	public boolean queryStream(long start, long end, String filepath, String data_filename,
			Map<String, ArrayList<Policy>> ids_to_policies, Map<String, ArrayList<Policy>> entities_to_policies,
			String entity_name, String data_type, String operation, String intent) {
		
		ArrayList<String> policies = getPolicies(start, end, filepath, "policystream.txt");
		
		ArrayList<Policy> query_relevant_policies = new ArrayList<Policy>();
		
		//Grabs the policies for this entity
		ArrayList<Policy> entity_pols = entities_to_policies.get(entity_name);
		//Get the relevant policies for this entity and matching the stream type
		for (Policy pol : entity_pols) {
			//Policy ID should be in the relevant policies, and the stream types should match.
			if (policies.contains(pol.policy_id) && pol.data_stream_type.equals(data_type)) {
				query_relevant_policies.add(pol);
			}
		}
		
		boolean satisfies_all_policies = true;
		//Iterate through relevant policies, check if any intents or operations match.
		for(Policy pol : query_relevant_policies) {
			
			//System.out.println(pol.data_ops_intents);
			ArrayList<String> logics = getLogic(pol.data_ops_intents);
			ArrayList<String> terms = getTerms(pol.data_ops_intents);
			
			//If any of the policies are not satisfied, we break and return false.
			if(!evaluateTerms(terms, operation, intent, logics)) {
				satisfies_all_policies = false;
				break;
			}
		}
		return satisfies_all_policies;
		
		
		
	}
	
	
}
