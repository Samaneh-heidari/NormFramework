package valueframework.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import normFramework.Group;
import normFramework.Norm;
import valueframework.Action;
import valueframework.Node;
import valueframework.RandomTree;
import valueframework.example.Agent;

public final class FrameworkBuilder {

	private static ArrayList<Action> allActions = new ArrayList<Action>();
	private static ArrayList<Node> allConcreteValuesFromTrees = new ArrayList<Node>();
	private static ArrayList<String> allConcreteValuesNames = new ArrayList<String>();
	private static Map<String, RandomTree> globalValueTrees = new HashMap<String, RandomTree>();

	private static ArrayList<Group> allGroups;
	private static ArrayList<Norm> allNorms;
	private static ArrayList<Agent> allAgents;
	
	private static int valueNumber = 0;

	/**
	 * Private constructor since constructor should not be used in the static
	 * class
	 * 
	 * @param numOfActions
	 */
	private FrameworkBuilder() {

	}

	public static int getNextValueNumber() {
		int newNumber = valueNumber;
		valueNumber++;
		return newNumber;
	}

	/**
	 * Initialize the framework builder, always call this function first before
	 * using the value framework.
	 */
	public static void initialize() {

		Log.printLog("Initialize FrameworkBuilder");
		valueNumber = 0;

		allActions = new ArrayList<Action>();
		allConcreteValuesFromTrees = new ArrayList<Node>();
		allConcreteValuesNames = new ArrayList<String>();
		globalValueTrees = new HashMap<String, RandomTree>();
		
		allGroups = new ArrayList<Group>();
		allNorms = new ArrayList<Norm>();
		allAgents = new ArrayList<Agent>();
		
		try {
			// first create value files
			readValueTreeFile("inputFiles\\valueTree.txt");

			// then read actions from file
			readActionsFile("inputFiles\\actionList.txt");
			assignRelatedActionsToConcreteValues();
			setValueTreesWaterTanks();
			printTrees();
			
			// checkInitialConditions();
			readNormsFile("inputFiles\\normValueList.txt" );

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setValueTreesWaterTanks() {
		//TODO: this is a sample. It can be read from a UI or an input file
		Map<String, Integer> inputValueImportance = new HashMap<String, Integer>();
		inputValueImportance.put("UNIVERSALISM", 50);
		inputValueImportance.put("POWER", 50);
		inputValueImportance.put("SELFDIRECTION", 50);
		inputValueImportance.put("TRADITION", 50);
		
		
		for (String key : globalValueTrees.keySet()) {
			globalValueTrees.get(key).getWaterTank().setThreshold(inputValueImportance.get(key));
		}
		
	}
	
	
	public static void addConcreteValue(Node nd) {

		if (allConcreteValuesFromTrees != null && !allConcreteValuesFromTrees.contains(nd)) {
			Log.printLog("FrameworkBuilder.addConcreteValue: "
					+ nd.getValueName());
			allConcreteValuesFromTrees.add(nd);
		}
	}

	private static void readValueTreeFile(String filePath) throws IOException {

		Log.printLog("readValueTreeFile");
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(filePath));
		String line = reader.readLine();

		while (line != null) {

			if (line.startsWith("%")) {// means that it is not comment
				line = reader.readLine();
				continue;
			}
			if (line.contains("value tree")) {
				// This is water tank information
				String waterTankInfo = reader.readLine();
				// it's value trees from now on
				line = reader.readLine();
				List<String> treeInfo = new ArrayList<String>();
				while (line != null && !line.contains("value tree")) {
					treeInfo.add(line);
					line = reader.readLine();
				}
				Log.printStars();
				RandomTree tree = createGlobalValueTrees(treeInfo, waterTankInfo);
				globalValueTrees.put(tree.getRoot().getValueName(), tree);
				Log.printLog("Basic-tree\n" + tree.getPrintableTree());
			} else {
				line = reader.readLine();
			}
		}
		reader.close();
	}

	private static void readActionsFile(String filePath) {

		Log.printStars();
		Log.printLog("readActionsFile");
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			while (line != null) {
				if (!line.startsWith("%")) {// means that it is not comment
					List<String> items = Arrays.asList(line.split("\\s*,\\s*"));// ignore
																				// while
																				// space
																				// after
																				// comma

					if (items.size() >= 1) {
						addActionFromString(items);
					} else {
						Log.printError("No elements in items or doesn't contain a concrete value:"
								+ items);
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void assignRelatedActionsToConcreteValues() {

		Log.printStars();
		Log.printLog("assignRelatedActionsToConcreteValues");
		Log.printStars();
		for (Node nd : allConcreteValuesFromTrees) {
			for (Action act : allActions) {
				ArrayList<Node> ndListPositive = act
						.getPositiveRelatedConcreteValues();
				if (ndListPositive.contains(nd)) {
					nd.addPositiveAction(act);
				} else {
					ArrayList<Node> ndListNegative = act
							.getNegativeRelatedConcreteValues();
					if (ndListNegative.contains(nd))
						nd.addNegativeAction(act);
				}
			}
			Log.printLog(nd.toStringActions());
		}
	}

	private static ArrayList<Node> addConcreteValuesFromString(
			List<String> concreteValues) {

		ArrayList<Node> concreteValueNodes = new ArrayList<Node>();
		Node cncrtValueNode;
		for (String concreteValue : concreteValues) {
			if (!allConcreteValuesNames.contains(concreteValue)) {
				allConcreteValuesNames.add(concreteValue);
			}
			cncrtValueNode = findInstanceOfValueWithName(concreteValue);
			if (cncrtValueNode == null)
				Log.printError("addConcreteValuesFromString("
						+ concreteValues
						+ "):"
						+ concreteValue
						+ "\" is not in the list of concreteValues made from valueTree file");
			else
				concreteValueNodes.add(cncrtValueNode);
		}
		return concreteValueNodes;
	}

	private static ArrayList<Node> addAbstractValuesFromString(
			List<String> values) {

		ArrayList<Node> valueNodes = new ArrayList<Node>();
		Node absValueNode;
		for (String abstractValue : values) {
			
			absValueNode = findInstanceOfAbstractValueWithName(abstractValue);
			if (absValueNode == null)
				Log.printError("addValuesFromString("
						+ values
						+ "):"
						+ absValueNode
						+ "\" is not in the list of Values made from valueTree file");
			else
				valueNodes.add(absValueNode);
		}
		return valueNodes;
	}
	private static void printTrees() {

		for (String key : globalValueTrees.keySet()) {
			Log.printLog("Full-tree\n" + globalValueTrees.get(key).getPrintableTree());
		}
	}

	public static void printTrees(Map<String, RandomTree> gValueTrees) {

		for (String key : gValueTrees.keySet()) {
			Log.printLog("Full-tree\n" + gValueTrees.get(key).getPrintableTree());
		}
	}

	private static Node findInstanceOfValueWithName(String concreteValue) {
		for (Node nd : allConcreteValuesFromTrees) {
			if (nd.getValueName().equals(concreteValue))
				return nd;
		}
		return null;
	}

	private static Node findInstanceOfAbstractValueWithName(String valueName) {
		return globalValueTrees.get(valueName).getRoot();
	}
	
	private static void addActionFromString(List<String> actionAndConcreteValues) {

		String actionName = actionAndConcreteValues.get(0);
		ArrayList<String> positiveRelatedConcreteValuesStrings = new ArrayList<String>();
		ArrayList<String> negativeRelatedConcreteValuesStrings = new ArrayList<String>();
		for (int i = 1; i < actionAndConcreteValues.size(); i++) {
			String concreteValueString = actionAndConcreteValues.get(i);
			if (concreteValueString.charAt(0) == '-') {
				negativeRelatedConcreteValuesStrings.add(concreteValueString
						.substring(1, concreteValueString.length()));
			} else {
				positiveRelatedConcreteValuesStrings.add(concreteValueString);
			}
		}
		ArrayList<Node> positiveRelatedConcreteValues = addConcreteValuesFromString(positiveRelatedConcreteValuesStrings);
		ArrayList<Node> negativeRelatedConcreteValues = addConcreteValuesFromString(negativeRelatedConcreteValuesStrings);

		Log.printLog("Add action: " + actionName + ", +cvs: "
				+ positiveRelatedConcreteValuesStrings + ", -cvs:"
				+ negativeRelatedConcreteValuesStrings);

		Action act = new Action(actionName, positiveRelatedConcreteValues,
				negativeRelatedConcreteValues);
		allActions.add(act);
	}

	private static RandomTree createGlobalValueTrees(List<String> treeInfo,
			String waterTankInfo) {

		List<String> items = Arrays.asList(treeInfo.get(0).split("\\s*,\\s*"));
		RandomTree tree = new RandomTree(items.get(0), treeInfo, waterTankInfo);
		globalValueTrees.put(tree.getRoot().getValueName(), tree);
		return tree;
	}

	public static ArrayList<Action> getAllPossibleActions() {
		return allActions;
	}

	public static Map<String, RandomTree> getGlobalValueTrees() {
		return globalValueTrees;
	}

	public static ArrayList<Node> getAllConcreteValues() {
		return allConcreteValuesFromTrees;
	}
	
	private static void readNormsFile(String filePath) {

		Log.printStars();
		Log.printLog("readNormsFile");
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			while (line != null) {
				if (!line.startsWith("%")) {// means that it is not comment
					List<String> items = Arrays.asList(line.split("\\s*,\\s*"));// ignore
																				// while
																				// space
																				// after
																				// comma

					if (items.size() >= 1) {
						addNormFromString(items);
					} else {
						Log.printError("No elements in items or doesn't contain a value:"
								+ items);
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void addNormFromString(List<String> actionAndValues) {

		String actionName = actionAndValues.get(0);
		ArrayList<String> relatedValuesStrings = new ArrayList<String>();
		ArrayList<Integer> relationOfValueIntegers = new ArrayList<Integer>(); 
		ArrayList<String> violation = new ArrayList<String>();
		int idx = 1; 
		while(idx < actionAndValues.size()){
			if(!actionAndValues.get(idx).equals("violation")){
				String valueString = actionAndValues.get(idx);
				String effect = valueString.substring(0,1);
				if(effect.equals("+")) relationOfValueIntegers.add(1);
				else relationOfValueIntegers.add(-1);
				relatedValuesStrings.add(valueString.substring(1));
			}
			else{
				idx++;
				break;
			}
			idx++;
		}
		if(idx <= actionAndValues.size()-1){
			for(int vltionIdx = idx; vltionIdx < actionAndValues.size(); vltionIdx++){
				violation.add(actionAndValues.get(vltionIdx));
			}
		}
		
		Log.printLog("Add action: " + actionName + ", +related values: "
				+ relatedValuesStrings );

		Norm norm = new Norm("", actionName, "", "", relatedValuesStrings, relationOfValueIntegers, violation);
		allNorms.add(norm);
	}

	public static ArrayList<Group> getGroups() {
		return allGroups;
	}

	public static void addGroup(Group group) {
		FrameworkBuilder.allGroups.add(group);
	}

	public static ArrayList<Norm> getNorms() {
		return allNorms;
	}

	public static void addNorm(Norm norm) {
		FrameworkBuilder.allNorms.add(norm);
	}

	public static ArrayList<Agent> getAgents() {
		return allAgents;
	}

	public static void addAgent(Agent agent) {
		FrameworkBuilder.allAgents.add(agent);
	}
	
	public static void setAllAgents(ArrayList<Agent> nAgentList){
		if(allAgents.size() == 0)
			allAgents = nAgentList;
		else
			Log.printError("Trying to replace a list of agents");
	}
	
	public static void setAllGroups(ArrayList<Group> nGroupList){
		if(allGroups.size() == 0)
			allGroups = nGroupList;
		else
			Log.printError("Trying to replace a list of groups");
	}
	
	public static Norm getNormByName(String name){
		for (Norm nr : allNorms) {
			if(nr.getTitle().equals(name))
				return nr;
		}
		Log.printError("Norm " + name + " not found!") ;
		return null;
	}
	
	
}