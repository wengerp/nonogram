package ch.wepcom.nonogram;

import java.util.ArrayList;

public class Constraint implements Comparable<Constraint> {

	static int nConstr = 0;
	
	String name;
	String type = "";
	int index = -1;
	int length;			// internal length = external length + 2 (i.e. 5+2=7)
	int sumOfTrueVals = 0;
	boolean isSelected = false;
	boolean isForbidden = false;
	ArrayList<Boolean> boolVals = null;
	ArrayList<Integer> rules = null;
	ConstraintCollection parent;

	public Constraint(ConstraintCollection parent, String name, String type, int index, ArrayList<Integer>rules, ArrayList<Boolean> boolVals) {

		nConstr++;
		this.parent = parent;
		this.type = type;
		this.index = index;
		this.name = type+String.format("%02d",index)+String.format("%06d",nConstr);
		this.rules = rules;
		this.boolVals = boolVals;
		this.length = this.boolVals.size();
		for(Boolean b: boolVals) {
			if (b.booleanValue()) sumOfTrueVals++;
		}
	}

	public Constraint(ConstraintCollection parent, String name, String type, int index, ArrayList<Integer>rules, int length) {
		nConstr++;
		this.parent = parent;
		this.type = type;
		this.index = index;
		this.name = type+String.format("%02d",index)+String.format("%06d",nConstr);
		this.rules = rules;
		this.length = length+2;
		
		sumOfTrueVals = 0;
		int sumAllRules = getSumRemainingRules(-1)+1;
		if (sumAllRules==this.length || sumAllRules == 2){
			this.boolVals = constructBoolValsFromRules(rules);
		} else {
			this.boolVals = new ArrayList<Boolean>();
			for (int i = 0; i < this.length; i++) {
				this.boolVals.add(new Boolean(false));
			}
		}
	}

	public Constraint(ConstraintCollection parent, ArrayList<Constraint> cList) {
		nConstr++;
		
		if(cList != null && cList.size()>0) {
			for (int i=0; i<cList.size(); i++) {
				Constraint c = cList.get(i);
				if (i==0) {
					this.parent = parent;
					this.type = c.type;
					this.index = c.index;
					this.name = type+String.format("%02d",index)+String.format("%06d",nConstr);
					this.rules = c.rules;
					this.length = c.boolVals.size();					
					this.boolVals = new ArrayList<Boolean>(length);
					for (int k=0; k<this.length; k++) {
						setBoolVal(k,c.getBoolVal(k) );				
					}
				} else {
					for (int k=0; k<this.length; k++) {
						setBoolVal(k,new Boolean( this.getBoolVal(k) || c.getBoolVal(k) ));				
					}
				}
			}
			for(Boolean b: boolVals) {
				if (b.booleanValue()) sumOfTrueVals++;
			}
		}
	}
	
	private ArrayList<Boolean> constructBoolValsFromRules (ArrayList<Integer>rules) {
		
		ArrayList<Boolean>boolVals = new ArrayList<Boolean>();
		boolVals.add(new Boolean(false));
		this.sumOfTrueVals = 0;
		for (int ruleIndex=0; ruleIndex<rules.size(); ruleIndex++) {
			int rule = rules.get(ruleIndex);
			if (rule==0) {
				// no rule for this cc --> set all constraints to false
				for(int j=0; j<this.length;j++) {
					boolVals.add(false);
				}
			}
			for (int j=0; j<rule; j++) {
				boolVals.add(true);
				this.sumOfTrueVals++;
			}
			boolVals.add(false);
		}
		return boolVals;
		
	}

	private int getSumRemainingRules(int currentRule) {
		int result = 0;
		for (int i=currentRule+1; i<this.rules.size(); i++) {
			result += this.rules.get(i)+1;
		}
		return result;
	}

	public boolean getBoolVal(int index) {
		return this.boolVals.get(index);
	}

	public void addBoolVal(boolean b) {
		this.boolVals.add(new Boolean(b));
		if (b) sumOfTrueVals++;
	}

	public void setBoolVal(int index, boolean b) {
		if (index>=boolVals.size()) {
			for (int j=boolVals.size(); j<=index; j++) {
				boolVals.add(new Boolean(false));
			}
		} 
		boolVals.set(index, new Boolean(b));
		if (b) sumOfTrueVals++;
	}

	public ArrayList<Boolean> getBoolVals() {
		return this.boolVals;
	}
	
	public ArrayList<Boolean> getBoolValsExt() {
		ArrayList<Boolean> result = new ArrayList<Boolean>();
		for(int i=1; i<this.length-1; i++) {
			result.add(this.boolVals.get(i));
		}
		return result;
	}
	
	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	public boolean isForbidden() {
		return isForbidden;
	}

	public void setForbidden(boolean isForbidden ) {
		this.isForbidden = isForbidden;
	}

	public int checkConstraintSupportType(Constraint c) {
		// 0 = not defined
		// 1 = constraint
		// 2 = support
		// 3 = invalid
		boolean bRow;
		boolean bCol;
	
		if (this.type.equalsIgnoreCase(c.type)) {
			return 0;
		} 
		
		if (this.type.equalsIgnoreCase("Z")) {
			// this = row
			// c = col
//			ArrayList<Boolean>bListRow = this.getBoolValsExt();
//			ArrayList<Boolean>bListCol = c.getBoolValsExt();
//			bRow = bListRow.get(c.index-1).booleanValue();
//			bCol = bListCol.get(this.index-1).booleanValue();

			bRow = this.boolVals.get(c.index).booleanValue();
			bCol = c.boolVals.get(this.index).booleanValue();
		} else {
			// this = col
			// c = row
//			ArrayList<Boolean>bListRow = c.getBoolValsExt();
//			ArrayList<Boolean>bListCol = this.getBoolValsExt();
//			bRow = bListRow.get(this.index-1).booleanValue();
//			bCol = bListCol.get(c.index-1).booleanValue();

			bRow = c.boolVals.get(this.index).booleanValue();
			bCol = this.boolVals.get(c.index).booleanValue();
		}
		
//		if (bRow != bCol) return 1;
//		else return 0;
		
		if (this.isSelected && c.isSelected) {
			// both are selected
			if (bRow == bCol) {
				return 2; 
			}
			else {	
				return 3;
			}
		} else if (this.isSelected || c.isSelected) {
			// only one is selected
			if (bRow != bCol) {
				return 3;
			} else {
				return 0;
			}
		} else {
			// none is selected
			if (bRow != bCol) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	public int getFirstFreePosition() {
		for(int i=0; i<this.boolVals.size()-1; i++) {
			if(this.boolVals.get(i).booleanValue()) {
				for(int j=i+1; j<this.boolVals.size(); j++) {
					if(!this.boolVals.get(j).booleanValue()) {
						return j;
					}
				}
			}
		}
		return -1;
	}
	
	public int getLastFreePosition() {
		for(int i=this.boolVals.size()-1; i>1; i--) {
			if(this.boolVals.get(i).booleanValue()) {
				for(int j=i-1; j>0; j--) {
					if(!this.boolVals.get(j).booleanValue()) {
						return j;
					}
				}
			}
		}
		return -1;
	}


	private int findRule(int rule, int start) {
		int p;
		if(rule>sumOfTrueVals) {
			return -1;
		}
		for (p=start; p<=length-(rule+2); p++) {
			
			// check overall size
			if (boolVals.size()>p+rule+1) {
	
				// check left border "=false"
				if ( boolVals.get(p)==false) { 
				
					// check vals "=true"
					boolean ok = true;
					for (int j=0; j<rule; j++) {
						if (boolVals.get(p+j+1)==false) {
							ok = false;
						}
					}
					if (ok) {
						
						// check right border "=false"
						if (boolVals.get(p+rule+1)==false) {
							return p;
						}
					}
				}
			}
		}
		return -1;
	}
	
	public boolean isValid() {
		boolean result = true;
		
		if (rules==null) {
			System.out.println("=== UNEXPECTED SITUIATION:"+this);
			return false;
		}
		int rule = rules.get(0);
		if (rule>0) {
			int pos = findRule(rule,0);
			if (pos<0) {
				return false;
			} else {
				int newPos = pos+rule+1;
				for (int i=1; i<rules.size(); i++) {
					rule = rules.get(i);
					if (rule==0) break;
					pos = findRule(rule,newPos);
					if (pos<0) {
						return false;
					} 
					newPos=pos+rule+1;
				}
			}
		}
		return result;
	}
	
	
	public boolean isEqual(Constraint c) {
		for (int i=0; i<boolVals.size(); i++) {
			if (boolVals.get(i).booleanValue() != (c.getBoolVals().get(i).booleanValue())) {
				return false;
			}
		}
		return true;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Constraint))
			return false;
		Constraint other = (Constraint) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public void draw(){
		ArrayList<StringBuffer> sbArr = new ArrayList<StringBuffer>();
		StringBuffer out = new StringBuffer();
		for (Boolean b : boolVals) {
			if (b) {
				out.append("XXXX");
			} else {
				out.append("    ");
			}
		}
		sbArr.add(out);
		for (StringBuffer stringBuffer : sbArr) {
			for (int i=0; i<2; i++) {
				System.out.println(stringBuffer);
			}
		}
	}

	public String toString() {
		return "("+this.name+","+this.type+","+this.index+",iS:"+this.isSelected+",iF:"+this.isForbidden+"="+boolVals;
	}

	@Override
	public int compareTo(Constraint o) {
		 if (this.name == o.name)
	            return 0;
	        else if (this.name.startsWith("S") &&  o.name.startsWith("Z"))
	            return 1;
	        else if (this.name.startsWith("Z") &&  o.name.startsWith("C"))
	        	return -1;
	        else if (Integer.parseInt(this.name.substring(1)) > Integer.parseInt(o.name.substring(1)))
		 		return 1;
	        else
	            return -1;
	    }
	}

