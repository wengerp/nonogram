package ch.wepcom.nonogram;

import java.util.ArrayList;

public class ConstraintCollection {

	String name = "";
	String type = "";
	int index = -1;
	int length = 0;
	ArrayList<Integer> rules = null;
//	ArrayList<ArrayList<ArrayList<Boolean>>> boolVals = new ArrayList<ArrayList<ArrayList<Boolean>>>();
	ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	
	public ConstraintCollection(String name, String type, int index, int length, int[] rules) {
		this.name = name;
		this.type = type;
		this.index = index;
		this.length = length;
		this.rules = new ArrayList<Integer>();
		for (int i : rules) {
			this.rules.add(new Integer(i));
		}
		createConstraints();
	}
	
	public String toString() {
		return "["+this.name+";"+length+";"+rules+";"+constraints;
	}

//	private void createConstraints() {
//
//		int sumProcessedRules = 0;
//		for (int i=0; i<rules.size(); i++) {
//			int rule = rules.get(i);
//			if (rule==0) break;
//			int sumRemainingRules = getSumRemainingRules(i);
//			ArrayList<ArrayList<Boolean>> boolValsN = createBoolVals(rule,sumProcessedRules, sumRemainingRules);
//			boolVals.add(boolValsN);
//			sumProcessedRules += rule+1;
//		}
//		
//		ArrayList<ArrayList<Boolean>> it = new ArrayList<ArrayList<Boolean>>();
//		generateConstraints(0, it, boolVals);
//
//		return;
//	}
	
	private void createConstraints() {

		int sumProcessedRules = 0;
		ArrayList<ArrayList<Constraint>> valueCandidates = new ArrayList<ArrayList<Constraint>>();
		for (int i=0; i<rules.size(); i++) {
			int rule = rules.get(i);
			if (rule==0) break;
			int sumRemainingRules = getSumRemainingRules(i);
			ArrayList<Constraint> candidates = createCandidates(rule,sumProcessedRules, sumRemainingRules);
			valueCandidates.add(candidates);
			sumProcessedRules += rule+1;
		}
		
		ArrayList<Constraint> it = new ArrayList<Constraint>();
		generateConstraints(0, it, valueCandidates);

		return;
	}

	private int getSumRemainingRules(int currentRule) {
		int result = 0;
		for (int i=currentRule+1; i<this.rules.size(); i++) {
			result += this.rules.get(i)+1;
		}
		return result;
	}

//	private ArrayList<ArrayList<Boolean>> createCandidates(int rule,int sumProcessedRules, int sumRemainingRules) {
//		ArrayList<ArrayList<Boolean>> result = new ArrayList<ArrayList<Boolean>>();
//
//		for (int p=sumProcessedRules; p<=length+2-(rule+2+sumRemainingRules); p++) {
//			ArrayList<Boolean> boolValsN = new ArrayList<Boolean>();
//			for (int i = 0; i < length+2; i++) {
//				boolValsN.add(new Boolean(false));
//			}
//			
//			// first 
//			boolValsN.set(p,new Boolean(false));
//			
//			// middle
//			for (int j=0; j<rule; j++) {
//				boolValsN.set(p+j+1,new Boolean(true));				}
//
//			// last
//			boolValsN.set(p+rule+1,new Boolean(false));
//			result.add(boolValsN);
//		}
//		
//		return result;
//	}

	private ArrayList<Constraint> createCandidates(int rule,int sumProcessedRules, int sumRemainingRules) {
		ArrayList<Constraint> result = new ArrayList<Constraint>();

		for (int p=sumProcessedRules; p<=length+2-(rule+2+sumRemainingRules); p++) {
			Constraint candidate = new Constraint(this.name+String.format("%02d",(this.constraints.size()+1)),this.type,this.index,this.rules,this.length);
			
			// first 
			candidate.setBoolVal(p,new Boolean(false));
			
			// middle
			for (int j=0; j<rule; j++) {
				candidate.setBoolVal(p+j+1,new Boolean(true));				}

			// last
			candidate.setBoolVal(p+rule+1,new Boolean(false));
			result.add(candidate);
		}
		
		return result;
	}

	public void generateConstraints(int d, ArrayList<Constraint> bool, ArrayList<ArrayList<Constraint>> boolList) {
		if (d == boolList.size()) {
//			ArrayList<Boolean> boolVar = combineBoolVars(bool);
//			Constraint c = new Constraint(this.name+String.format("%02d",(this.constraints.size()+1)), this.type, this.index, this.rules, boolVar);
			Constraint c = new Constraint(bool);
			if (!exists(c) && c.isValid()) {
				this.constraints.add(c);
			}
			return;
		  }
//		int currentRule = rules.get(d);
			for (int k = 0; k < boolList.get(d).size(); k++) {
				ArrayList<Constraint> newBool = new ArrayList<Constraint>(bool);
//			  	newBool.add(boolList.get(d).get(k));
				Constraint nextBool = boolList.get(d).get(k);
				if (d>0) {
					Constraint lastBool = bool.get(d-1);
					int firstfree =  lastBool.getFirstFreePosition();
					int lastfree = nextBool.getLastFreePosition();
					if (lastfree-firstfree>=0) {
						newBool.add(nextBool);
						generateConstraints(d + 1, newBool, boolList);
					}
				} else {
					newBool.add(nextBool);
					generateConstraints(d + 1, newBool, boolList);
				}
		  }
		  return;
		}

//	public void generateConstraints(int d, ArrayList<ArrayList<Boolean>> bool, ArrayList<ArrayList<ArrayList<Boolean>>> boolList) {
//		if (d == boolList.size()) {
//			ArrayList<Boolean> boolVar = combineBoolVars(bool);
//			Constraint c = new Constraint(this.name+String.format("%02d",(this.constraints.size()+1)), this.type, this.index, this.rules, boolVar);
//			if (!exists(c) && c.isValid()) {
//				this.constraints.add(c);
//			}
//			return;
//		  }
////		int currentRule = rules.get(d);
//			for (int k = 0; k < boolList.get(d).size(); k++) {
//				ArrayList<ArrayList<Boolean>> newBool = new ArrayList<ArrayList<Boolean>>(bool);
////			  	newBool.add(boolList.get(d).get(k));
//				ArrayList<Boolean> nextBool = boolList.get(d).get(k);
//				if (d>0) {
//					int firstfree =  getFirstFreePosition(bool.get(d-1));
//					int lastfree = getLastFreePosition(nextBool);
//					if (lastfree-firstfree>=0) {
//						newBool.add(nextBool);
//						generateConstraints(d + 1, newBool, boolList);
//					}
//				} else {
//					newBool.add(nextBool);
//					generateConstraints(d + 1, newBool, boolList);
//				}
//		  }
//		  return;
//		}
	
//	private int getFirstFreePosition(ArrayList<Boolean> b) {
//		for(int i=0; i<b.size()-1; i++) {
//			if(b.get(i).booleanValue()) {
//				for(int j=i+1; j<b.size(); j++) {
//					if(!b.get(j).booleanValue()) {
//						return j;
//					}
//				}
//			}
//		}
//		return -1;
//	}

//	private int getLastFreePosition(ArrayList<Boolean> b) {
//		for(int i=b.size()-1; i>1; i--) {
//			if(b.get(i).booleanValue()) {
//				for(int j=i-1; j>0; j--) {
//					if(!b.get(j).booleanValue()) {
//						return j;
//					}
//				}
//			}
//		}
//		return -1;
//	}

//	private ArrayList<Boolean> combineBoolVars(ArrayList<ArrayList<Boolean>> bList) {
//		ArrayList<Boolean> boolVar = new ArrayList<Boolean>();
//		for (int k=0; k<this.length+2; k++) {
//			boolVar.add(new Boolean(false));				
//		}
//		for (int i=0; i<bList.size(); i++) {
//			ArrayList<Boolean> b = bList.get(i);
//			for (int k=0; k<boolVar.size(); k++) {
//				boolVar.set(k,new Boolean(isTrueComb(boolVar.get(k),b.get(k))));				
//			}
//		}
//		return boolVar;
//	}
	
//	private boolean isTrueComb(Boolean b1, Boolean b2) {
//		return (b1.booleanValue() || b2.booleanValue());
//	}
	
//	private void createConstraints() {
//		ArrayList<Constraint> result = new ArrayList<Constraint>();
//		
//		return;
//	}

	public boolean exists(Constraint c)	{
		for (Constraint c2 : constraints) {
			if (c.isEqual(c2)) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Constraint> getConstraints() {
		return this.constraints;
	}
	
	public int getConstraintsSize() {
		return this.constraints.size();
	}
}
