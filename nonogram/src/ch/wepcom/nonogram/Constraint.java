package ch.wepcom.nonogram;

import java.util.ArrayList;

public class Constraint implements Comparable<Constraint> {

	static int nConstr = 0;
	
	String name;
	String type = "";
	int index = -1;
	int length;
	int sumOfTrueVals = 0;
	boolean isSelected = false;
	ArrayList<Boolean> boolVals = null;
	ArrayList<Integer> rules = null;

	public Constraint(String name, String type, int index, ArrayList<Integer>rules, ArrayList<Boolean> boolVals) {

		nConstr++;

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

	public Constraint(String name, String type, int index, ArrayList<Integer>rules, int length) {
		nConstr++;
		this.type = type;
		this.index = index;
		this.name = type+String.format("%02d",index)+String.format("%06d",nConstr);
		this.rules = rules;
		this.length = length+2;
		
		this.boolVals = new ArrayList<Boolean>();
		for (int i = 0; i < this.length; i++) {
			this.boolVals.add(new Boolean(false));
		}
		sumOfTrueVals = 0;
	}

	public Constraint(ArrayList<Constraint> cList) {
		nConstr++;
		
		if(cList != null && cList.size()>0) {
			for (int i=0; i<cList.size(); i++) {
				Constraint c = cList.get(i);
				if (i==0) {
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
	
//	private void init(String name, String type, int index, ArrayList<Integer>rules, ArrayList<Boolean> boolVals) {
//		
//	}

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
	
//	public ArrayList<Constraint> getChildren() {
//		ArrayList<Constraint> result =  new ArrayList<Constraint>();
//		for (int i=0; i<rules.size(); i++) {
//			int rule = rules.get(i);
//			if (i==0) break;
//			for (int p=0; p<=length-(rule+2); p++) {
//				int rulePos = findRule(rule);
//				if (rulePos<0) {
//					// create children for new rule
//					if (i+1<rules.size()) {
//						int newRule = rules.get(i+1);
//					}
//				} else if (rulePos+rule+2<length) {
//					// create children for new pos of same rule
//				}
//			}
//		}
//		return result;
//	}
	
	public boolean isEqual(Constraint c) {
		for (int i=0; i<boolVals.size(); i++) {
			if (boolVals.get(i).booleanValue() != (c.getBoolVals().get(i).booleanValue())) {
				return false;
			}
		}
		return true;
	}

	
//	@Override
//	public boolean equals(Object o) {
//		if (!(o instanceof Constraint)) {
//			return false;
//		}
//		return (this.name.equals(((Constraint)o).name));
//	}
	
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
//		out.append(System.getProperty("line.separator"));	
		for (StringBuffer stringBuffer : sbArr) {
			for (int i=0; i<2; i++) {
				System.out.println(stringBuffer);
			}
		}
	}

	public String toString() {
		return "("+this.name+","+this.type+","+this.index+"="+boolVals;
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

