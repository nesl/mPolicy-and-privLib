package privLib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Propositions {
	
	
//	TimeRange("9pm", "9am")
//    LocationAt("34.062497", "-118.447253")
//    TimeGreaterThan("December 31, 2019")

	public static String TIME_RANGE = "TimeRange";
	public static String LOCATION_AT = "LocationAt";
	public static String TIME_GREATER = "TimeGreaterThan";
	public static String BUTTON_PRESS = "ButtonPress";
	
	public static String[] DATA_WINDOWS_CONTRAINTS = {TIME_RANGE, LOCATION_AT, TIME_GREATER, };
	
	//Used to determine the context
	public static String TIME_12HR = "12hr";
	public static String TIME_24HR = "24hr";
	public static String TIME_DATE = "Date";
	public static String LOC_LATLONG = "latlong";
	
	//Context Stream Types
	public static String CST_TIME_HOUR = "time_hour";
	public static String CST_TIME_DATE = "time_date";
	public static String CST_LAT = "gps_latitude";
	public static String CST_LONG = "gps_longitude";
	public static String CST_BUTTON_PRESS = "button_press";
	
	
	//Use a full propositional function to determine the context
	//Assumes the type is the first argument after double quotes
	public static String getContextType(String function) {
		
		
		String context = "";
		String[] splits = function.split("\"");
		if(function.contains(BUTTON_PRESS)) {
			context = BUTTON_PRESS;
		}
		else {
			context = splits[1];
		}
		return context;
	}
	
	//To Add a new prop function, you need to add it getContextStreamTypes and callPropositionalFuncs
	
	
	//Convert function context into context stream types
	//Remember, some will depend on the value types (i.e. 24hr vs 12hr) and some will depend 
	//  on the actual function itself (i.e. ButtonPress)
	public static ArrayList<String> getContextStreamTypes(ArrayList<String> func_types) {
		
		ArrayList<String> cst = new ArrayList<String>();
		
		for(String func_type : func_types) {
			//System.out.println(func_type);
			if(func_type.equals(TIME_12HR) | func_type.equals(TIME_24HR)) {
				cst.add(CST_TIME_HOUR);
			}
			else if(func_type.equals(TIME_DATE)) {
				cst.add(CST_TIME_DATE);
			}
			else if(func_type.equals(LOC_LATLONG)) {
				cst.add(CST_LAT);
				cst.add(CST_LONG);
			}
			else if(func_type.equals(BUTTON_PRESS)) {
				cst.add(CST_BUTTON_PRESS);
			}

			
		}
		
		return cst;
		
	}

	
	//How do I know that TimeRange should correspond to a particular context_val?
	// I think I need a mapping between the function,type and context values I should be looking at.
	// i.e. 12hr should map to context indexes 2,3
	
	//Call propositional functions using the params, and return a mapping between the function key and boolean value
	public static boolean callPropositionalFuncs(ArrayList<String> params, ArrayList<String> context_vals_str,
			ArrayList<String> context_names) {
		
//		for(String x :context_names) {
//			System.out.println("A: " + x);
//		}
//		for(String x :context_vals_str) {
//			System.out.println("B: " + x);
//		}
//		for(String x :params) {
//			System.out.println("C: " + x);
//		}
		
		boolean satisfies = false;
		
		//Check if valid
		if(params.size() > 0) {
			
			ArrayList<String> func_types = new ArrayList<String>();
			if(params.size() > 1) { //Means we have a multi parameter function (i.e. TimeRange)
				func_types.add(getContextType(params.get(1)));
			}
			else { //Means we have a no parameter function (i.e. ButtonPress) (first value is the function name)
				func_types.add(getContextType(params.get(0)));
			}
			//Gets the context types for this function specifically
			ArrayList<String> context_types = getContextStreamTypes(func_types);
			ArrayList<Integer> relevant_indexes = new ArrayList<Integer>();
			
			
			
			//Iterate through each context type and get the relevant indexes for this specific context type
			for(String ctx : context_types) {
				//System.out.println(ctx);
				//System.out.println(context_names.indexOf(ctx));
				relevant_indexes.add(context_names.indexOf(ctx));
				//System.out.println(relevant_indexes.size());
			}
			
			if(params.get(0).equals(TIME_RANGE)) {
				satisfies = satisfiesTimeRange(params.get(1), params.get(2), params.get(3), context_vals_str.get(relevant_indexes.get(0)));
			}
			else if(params.get(0).equals(LOCATION_AT)) {

				satisfies = locationAt(params.get(1), params.get(2), params.get(3), context_vals_str.get(relevant_indexes.get(0)),
						context_vals_str.get(relevant_indexes.get(1)));
			}
			else if(params.get(0).equals(TIME_GREATER)) {
				
				satisfies = satisfiesTimeGreaterThan(params.get(1), params.get(2), context_vals_str.get(relevant_indexes.get(0)));
			}
			else if(params.get(0).equals(BUTTON_PRESS)) {
				satisfies = buttonPressed(context_vals_str.get(relevant_indexes.get(0)));
			}
			
			
		}
		
		return satisfies;
		
	}
	
	//Check to see if button was pressed (should be 0 or 1 as a string)
	public static boolean buttonPressed(String buttonPressVal) {
		
		boolean satisfies = false;
		if(buttonPressVal.equals("1")) {
			satisfies = true;
		}
		return satisfies;
		
	}
	
	
	//Compare two times.  If time1 < time2, return true, otherwise false.
	// Be sure to compare the query in the first param, and the policy param in the second one
	public static boolean compareTime(String type, String time1, String time2) {
		
		boolean leq = false;
		
//		System.out.println(time1);
//		System.out.println(time2);
		
		// Compare Dates
		if(getContextType(type).equals(TIME_DATE)) {
			try {
				Date time1_date = new SimpleDateFormat("dd/MM/yyyy").parse(time1.split("\'|\"")[1]);
				Date time2_date = new SimpleDateFormat("dd/MM/yyyy").parse(time2.split("\'|\"")[1]);
				
				//If the end date is at or before the current date, this is satisfied.
				if(time1_date.before(time2_date)) {
					leq = true;
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if(getContextType(type).equals(TIME_12HR)) {
				
				//Convert start and stop into 24hr integers.
				int time1_24hr = 0; //convert12hrTo24hr(getContextType(time2.strip()));
				int time2_24hr = convert12hrTo24hr(getContextType(time2.strip()));
				
				if(!time1.contains("m")) {  //Does not contain AM or PM
					time1_24hr = Integer.parseInt(String.valueOf(cleanStr(time1)));
				}
				else { //This is an actual 12hr value
					time1_24hr = convert12hrTo24hr(getContextType(time1.strip()));
				}

				if(time1_24hr <= time2_24hr) {
					leq = true;
				}
				
			}
		
		return leq;
		
	}
	
	
	//Check if a timestamp is greater than endTime
	public static boolean satisfiesTimeGreaterThan(String type, String endTime, String timestamp) {
		boolean satisfies = false;
		
		//Compare to see if timestamp < endTime.
		if (compareTime(type, timestamp, endTime)) {
			satisfies = true;
		}
		//System.out.println(timestamp + " , " + endTime + " : " + satisfies);
		return satisfies;
	}
	
	//Assumes we get a clean (no quotations or spaces) value with am/pm included
	public static int convert12hrTo24hr(String value) {
		int val = Integer.parseInt(String.valueOf(value.charAt(0)));
		
		//Add 12 if we are in pm
		if (value.contains("pm")) {
			val += 12;
		}
		return val;
	}
	
	//Depending on the type, we have different ways of checking if a timestamp is within start and stop.
	public static boolean satisfiesTimeRange(String type, String start, String stop, String timestamp) {

		boolean satisfies = false;
		
		//If the timestamp is not less than the start, and is before the stop, return true
		if(!compareTime(type, timestamp, start) && compareTime(type, timestamp, stop)) {
			satisfies = true;
		}
		
		//System.out.println(start + " , " + timestamp + " " + stop + " : " + satisfies);
		return satisfies;
	}
    
	//Just determines if x1==x2 and y1==y2
	public static boolean locationAt(String type, String x1, String y1, String x2, String y2) {
		boolean satisfies = false;
		
		if(getContextType(x1.strip()).equals(x2) && getContextType(y1.strip()).equals(y2)) {
			satisfies = true;
		}
		return satisfies;
	}
	
	//Remove random whitespace and quotations
	public static String cleanStr(String s) {
		String fixed = s;
		if(s.contains("\"")) {
			fixed = s.strip().split("\"")[1];
		}
		return fixed;
	}
	
	//Get Most Restrictive Time Range between two sets of parameters
	// For time range, we have params type (12hr), start, and end
	// Returns null if we have disjoint sets.
	public static ArrayList<String> getMostRestrictiveTimeRange(ArrayList<String> x, ArrayList<String> y) {
		
		ArrayList<String> most_restrictive_params = new ArrayList<String>();
		
		
		//Check if x is exactly smaller than y
		boolean xstart_less_y = satisfiesTimeRange(y.get(0), y.get(1), y.get(2), x.get(1));
		boolean xend_less_y = satisfiesTimeRange(y.get(0), y.get(1), y.get(2), x.get(2));
		
		//If both booleans are smaller, than x is more restrictive
		if(xstart_less_y && xend_less_y) {
			most_restrictive_params.addAll(x);
		}
		
		//If exactly one boolean is smaller, then it means the other is false, and therefore that x param is larger than y
		// Essentially, we have a new restrictive time range where one param should come from x and another from y
		else if(xstart_less_y || xend_less_y) {
			
			// X starts in y, but y has a smaller upper bound
			if(xstart_less_y) {
				most_restrictive_params.addAll(x);
				most_restrictive_params.set(2, y.get(2));
			}
			else { // y has a more restrictive lower bound, but x has a more restrictive upper bound
				most_restrictive_params.addAll(y);
				most_restrictive_params.set(2, x.get(2));
			}
			
		}
		// Otherwise, y is more restrictive, or they are disjoint
		else {
			boolean ystart_less_x = satisfiesTimeRange(x.get(0), x.get(1), x.get(2), y.get(1));
			boolean yend_less_x = satisfiesTimeRange(x.get(0), x.get(1), x.get(2), y.get(2));
			
			//If both booleans are true, y is exactly smaller than x.
			if(ystart_less_x && yend_less_x) {
				most_restrictive_params.addAll(y);
			}
			//Otherwise, it means we have a disjoint set.
			else {
				most_restrictive_params = null;
			}
		}
		
		return most_restrictive_params;
	}
	
	//Get the most restrictive time for whether or not one is greater than another
	public static ArrayList<String> getMostRestrictiveTimeGreater(ArrayList<String> time1, ArrayList<String> time2) {
		
		ArrayList<String> time_result = time1;
		//If time 2 > time1
		if(satisfiesTimeGreaterThan(time1.get(0), time1.get(1), time2.get(1))) {
			time_result = time2;
		}
		
		return time_result;
		
	}
	
	//Get the most restrictive location of these two parameters
	public static ArrayList<String> getMostRestrictiveLocation(ArrayList<String> loc1, ArrayList<String> loc2) {
		
		ArrayList<String> result_loc = loc1;  //Default to location 1 as the result
		
		//Basically, if the two don't match, then we just return null to express they are disjoint.
		if(!loc1.equals(loc2)) {
			result_loc = null;
		}

		return result_loc;
		//In the future, we should be checking the granularity
	}
	
	
	//Get the most restrictive set of some policies
	// This is using a particular function specific
	// This also has to compare against the current policies in final_prop_dict, to avoid creating multiple disjoint params
	//  that are not restrictive.s
	public static ArrayList<ArrayList<String>> getMostRestrictiveParams(ArrayList<ArrayList<String>> p1_params, 
			String func_name, ArrayList<ArrayList<String>> best_params) {
		
		//For each parameter in p1 and p2, find the most restrictive parameters (could have multiple)
		ArrayList<String> most_restrictive_params = new ArrayList<String>();
		ArrayList<ArrayList<String>> new_best_params = best_params;
		
		for (ArrayList<String> params1 : p1_params) {
			
			int current_best_index = 0; //Keep track of what index we are in for the best params.
			
			//if this hasn't been set yet, we set it to the first set of params in params1
			if(new_best_params.isEmpty()) {
				most_restrictive_params = params1;
				new_best_params.add(most_restrictive_params);
			}
			else {
				//Initialize the result params to be params1
				ArrayList<String> result_params = params1;
				
				//Iterate through each of the best params, and either replace it with a new best param
				//  or if it's null, append to the best params.
				for(current_best_index = 0; current_best_index < new_best_params.size(); ++current_best_index) {
					
					ArrayList<String> current_best_params = new_best_params.get(current_best_index);
//					for(String i :current_best_params) {
//						System.out.println(i);
//					}
					
					//If it's TimeRange
					if(func_name.equals(TIME_RANGE)) {
						
						//We should only add to this restrictive params if it is truly disjoint against ALL the current params
						result_params = getMostRestrictiveTimeRange(current_best_params, params1);
					}
					else if(func_name.equals(LOCATION_AT)) {
						result_params = getMostRestrictiveLocation(current_best_params, params1);
					}
					else if(func_name.equals(TIME_GREATER)) {
						result_params = getMostRestrictiveTimeGreater(current_best_params, params1);
					}
					
				}
				
				//If the result parameters are null, it means we have a new disjoint set of parameters
				// So we add it to the best params
				if(result_params == null) {
					new_best_params.add(params1);
				}
				else { //Otherwise, we replace one of the best params with the result params
					new_best_params.set(current_best_index-1, result_params);
				}
					
				
			}
			
			
		}
		
		return new_best_params;
		
	}

}
