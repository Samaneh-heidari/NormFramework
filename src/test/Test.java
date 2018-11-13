package test;

import java.util.Vector;

import valueframework.DecisionMaker;
import valueframework.common.FrameworkBuilder;

public class Test {
	
	public static void main(String[] args){
		FrameworkBuilder.initialize();
		int numberOfAgents = 100;
		Vector<DecisionMaker> agents = new Vector<>();
		for(int i = 0; i < numberOfAgents; i++){
			agents.add(new DecisionMaker());
		}
	}
	
}
