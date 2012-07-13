package ch.wepcom.nonogram;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Instance {

	ArrayList<ConstraintCollection> rows = new ArrayList<ConstraintCollection>();
	ArrayList<ConstraintCollection> cols = new ArrayList<ConstraintCollection>();
	ArrayList<Constraint[]> constraintTuples = new ArrayList<Constraint[]>();
	ArrayList<Constraint[]> supportTuples = new ArrayList<Constraint[]>();
	ArrayList<Constraint> constraintsCache = new ArrayList<Constraint>();
	StringBuffer sbOut = new StringBuffer();
	boolean constraintCacheacheValid = false;
	
	int[] solution;
	
	int rowLength = 0;
	int colLength = 0;
	
	int nCollections = 0;
	boolean busy = false;
	
	public Instance(int rowLength, int colLength) {
		this.rowLength = rowLength;
		this.colLength = colLength;
		for (int r=0; r<colLength; r++) {
			this.rows.add(null);
		}
		for (int c=0; c<rowLength; c++) {
			this.cols.add(null);
		}
		this.constraintCacheacheValid = false;
	}
	
	public synchronized void setCollectionThreaded(ConstraintCollection cc) {
		while (busy)	{
			System.out.println("---setCollectionThreaded---");
		}
		busy = true;
		if (cc.type.equalsIgnoreCase("Z")) {
			rows.set(cc.index-1,cc);
		} else {
			cols.set(cc.index-1,cc);
		}
		nCollections++;
		this.constraintCacheacheValid = false;
		busy = false;
	}

	public void setRow(int index, int[] rules) {

		InstanceThread t = new InstanceThread(this,index,rules,rowLength,"Z");
		t.start();
	}
	
	public void setCol(int index , int[] rules) {
		InstanceThread t = new InstanceThread(this,index,rules,colLength,"S");
		t.start();
	}

	public synchronized int getNumberOfCollections() {
		while (busy)	{
			System.out.println("---getNumberOfCollections:waiting---");
		}
		busy = true;
		int n = nCollections;
		busy = false;
		return n;
	}
	
	public void setSolution(int[] b) {
		this.solution = b;
	}

	public void setConstraintCacheValid(boolean b) {
		this.constraintCacheacheValid = b;
	}
	
	public synchronized ArrayList<Constraint> getConstraints() {

		while (busy)	{
			System.out.println("---getConstraints:waiting---");
		}
		busy = true;
		boolean cacheIsValid = this.constraintCacheacheValid;
		busy = false;
		if (!cacheIsValid) {
//			Set<Constraint> ca = new TreeSet<Constraint>();
			
			for (ConstraintCollection cc : rows) {
				constraintsCache.addAll(cc.getConstraints());			
			}
			for (ConstraintCollection cc : cols) {
				constraintsCache.addAll(cc.getConstraints());			
			}
			while (busy)	{
				System.out.println("---getConstraints:waiting---");
			}
			busy = true;
			this.constraintCacheacheValid = true;
			busy = false;
//			constraintsCache.addAll(ca);
//			ArrayList<Constraint> ca2 = new ArrayList<Constraint>();
//			for (Constraint constraint : ca2) {
//				if (!ca2.contains(constraint)) {
//					ca2.add(constraint);
//				}
//			}
//			constraintsCache.addAll(ca2);
		}
		return constraintsCache;
	}

	public ArrayList<ConstraintCollection> getConstraintCollections() {
		ArrayList<ConstraintCollection> cc = new ArrayList<ConstraintCollection>();
		cc.addAll(rows);
		cc.addAll(cols);
		return cc;
	}
	
	public void drawSolution() {
		System.out.println();
		for (int i = 0; i < solution.length; i++) {
			if (solution[i]==1) {
				Constraint c = getConstraints().get(i);
				if (c.type.equalsIgnoreCase("Z")) {
					c.draw();
				}
			}
		} 
	}
	

//	public void calculatetConstraintsTuples() {
//		
//		System.out.println("calculatetConstraintsTuples");
//		for (int r=0; r<rows.size(); r++) {
//			ConstraintCollection ccRow = rows.get(r);
//			for (int i=0; i<ccRow.getConstraintsSize(); i++) {
//				Constraint cRow = ccRow.getConstraints().get(i);
//				for (int col=0; col<cols.size(); col++) {
//					ConstraintCollection ccCol = cols.get(col);
//					for (int k=0; k<ccCol.getConstraintsSize(); k++) {
//						Constraint cCol = ccCol.getConstraints().get(k);
//						if (! (isValidTuple(cRow, r, cCol, col))) {
//							Constraint[] tuple = new Constraint[2];
//							tuple[0] = cRow;
//							tuple[1] = cCol;
//							constraintTuples.add(tuple);
//						}
//					}
//				}
//			}
//		}
//	}

	public void calculatetConstraintsTuples() {
		
		System.out.println("calculatetConstraintsTuples");
		for (ConstraintCollection row : rows) {
			ArrayList<Constraint> rowConstraints = row.getConstraints();
			for (Constraint cRow : rowConstraints) {
				for (ConstraintCollection col : cols) {
					ArrayList<Constraint> colConstraints = col.getConstraints();
					for (Constraint cCol : colConstraints) {
						int conSup = cRow.checkConstraintSupportType(cCol);
						if (conSup == 1) {
							Constraint[] tuple = new Constraint[2];
							tuple[0] = cRow;
							tuple[1] = cCol;
							constraintTuples.add(tuple);
						} else if (conSup == 2) {
							Constraint[] tuple = new Constraint[2];
							tuple[0] = cRow;
							tuple[1] = cCol;
							supportTuples.add(tuple);
						} else if (conSup == 3) {
							System.out.println("invalid tuple: ["+cRow+";"+cCol);
						}
					}
				}
			}
		}
	}
	
	private void printVariables() {
		//     <variable name="Z11" domain="domT"/>

		System.out.println("printVariables");

		int i=0;
		StringBuffer out = new StringBuffer();
		out.append(System.getProperty("line.separator"));
		for (Constraint c : getConstraints()) {
			i++;
			String sOut = "<variable name=\"" + c.name +"\" domain=\"domT\"/>";
			out.append(sOut);			
		} 
		out.append(System.getProperty("line.separator"));
		out.append( "</variables>");
		out.append(System.getProperty("line.separator"));

		sbOut.append(System.getProperty("line.separator"));
		sbOut.append( "<variables nbVariables=\""+ i + "\">");
		sbOut.append(out);
	}
	
	

	private int printConstraintsTuples(StringBuffer out) {
	// <constraint name="C1" arity="2" scope="Z0101 S0102" reference="rel0" />	

		System.out.println("printConstraintsTuples");
		
		int iConstraint = 0;
		out.append(System.getProperty("line.separator"));
		for (Constraint[] constrTuple : constraintTuples) {
			iConstraint++;
			out.append("<constraint name=\"C" + iConstraint + "\" arity=\"2\" scope=\""+ constrTuple[0].name + " " + constrTuple[1].name + "\" reference=\"rel0\" />");			
		} 
		out.append(System.getProperty("line.separator"));
		return  iConstraint;
	}
	

	private void printRelations() {
		//	<relation name="rel0" arity="2" nbTuples="1" semantics="conflicts">
		//  1 1 
		//  </relation>
		System.out.println("printRelations");

		int i;
		StringBuffer out = new StringBuffer();
		i = printNotZeroRelations(out);
		
		sbOut.append(System.getProperty("line.separator"));
		sbOut.append("<relations nbRelations=\""+ (i+1) +"\">");
		sbOut.append(System.getProperty("line.separator"));

		sbOut.append("<relation name=\"rel0\" arity=\"2\" nbTuples=\"1\" semantics=\"conflicts\">1 1</relation>");
		sbOut.append(System.getProperty("line.separator"));

		sbOut.append(out);
		sbOut.append("</relations>");
		sbOut.append(System.getProperty("line.separator"));
	}
	
	private int printNotZeroRelations(StringBuffer out) {
	//	<relations nbRelations="1">
	//	 <relation name="rel0" arity="2" nbTuples="1" semantics="conflicts">
	//	 1 1 
	//  </relation>
	//	</relations>
	
		System.out.println("printNotZeroRelations");

		int i=0;
		Set<String> relHist = new HashSet<String>();

		for (ConstraintCollection cc : this.getConstraintCollections()) {
			String relName = "R"+ cc.getConstraintsSize();
			if (!relHist.contains(relName)) {
				relHist.add(relName);
			out.append(" <relation name=\"" + relName +"\" arity=\"" +cc.getConstraintsSize() + 
					"\" nbTuples=\"1\" semantics=\"conflicts\">"  +System.getProperty("line.separator"));
			i++;
			for (int j=0; j<cc.getConstraintsSize(); j++) {
				out.append(0 +" " );
			}
			out.append(" </relation>" +System.getProperty("line.separator"));
			}
		}
		return i;
	} 
		
	private int printNotZeroConstraints(StringBuffer out) {
		//    <constraint name="CN1" arity="2" scope="Z0101 Z0102" reference="R2" />
		
		System.out.println("printNotZeroConstraints");
		
		int i=1;
		sbOut.append(System.getProperty("line.separator"));

		for (ConstraintCollection cc : this.getConstraintCollections()) {
			i++;
			String conName = "CN"+ cc.getConstraintsSize() + i;
			out.append(" <constraint name=\""+ conName +"\" arity=\"" + cc.getConstraintsSize() + 
					"\" scope=\" ");
			for (Constraint c : cc.getConstraints()) {
				out.append(c.name +" " );
			}
			out.append("\" reference=\"R"+ cc.getConstraintsSize() +"\" />" +System.getProperty("line.separator"));
		} 
		out.append(System.getProperty("line.separator"));
	//	sbOut.append("<constraints nbConstraints=\""+ i +"\">");
//		sbOut.append(out);			
		return i;

	}

	
	private void printConstraints() {
		StringBuffer out = new StringBuffer();
		int nCon;
		nCon = printConstraintsTuples(out);
		nCon += printNotZeroConstraints(out);

		sbOut.append(System.getProperty("line.separator"));
		sbOut.append("<constraints nbConstraints=\""+ nCon +"\">");
		sbOut.append(out);
		sbOut.append("</constraints>");
	}
	
//	private boolean isValidTuple(Constraint cRow, int row, Constraint cSize, int col) {
//		ArrayList<Boolean>bListRow = cRow.getBoolValsExt();
//		ArrayList<Boolean>bListCol = cSize.getBoolValsExt();
//		boolean bRow = bListRow.get(col).booleanValue();
//		boolean bCol = bListCol.get(row).booleanValue();
//		return bRow==bCol;
//	}		


	public void writeCsp() {
	
//		<instance>
//		   <presentation  name="Nonogram"  nbSolutions="?"  format="XCSP 2.0">
//		     csp representation for nonograms
//		  </presentation>
//		  <domains nbDomains="1">
//		    <domain name="domT" nbValues="2">
//		       0..1
//		    </domain>
//		</domains>
//		...
//		</instance>
		System.out.println("writeCsp");

		sbOut.append("<instance>"+System.getProperty("line.separator"));
		sbOut.append("<presentation  name=\"Nonogram\"  nbSolutions=\"?\"  format=\"XCSP 2.0\">"+System.getProperty("line.separator"));
		sbOut.append("csp representation for nonograms"+System.getProperty("line.separator"));
		sbOut.append("</presentation>"+System.getProperty("line.separator"));
		sbOut.append("<domains nbDomains=\"1\">"+System.getProperty("line.separator"));
		sbOut.append("<domain name=\"domT\" nbValues=\"2\">"+System.getProperty("line.separator"));
		sbOut.append(" 0..1"+System.getProperty("line.separator"));
		sbOut.append("</domain>"+System.getProperty("line.separator"));
		sbOut.append("</domains>"+System.getProperty("line.separator"));
		
		printVariables();
		printRelations();
		printConstraints();
		
		sbOut.append("</instance>"+System.getProperty("line.separator"));
		
	}
	
	
	 public void writeToFile (String fileName) {
			System.out.println("writeToFile:" +fileName);

		 try {
	    	  BufferedWriter out = new BufferedWriter(
	    	                       new FileWriter(fileName));
	    	  String outText = sbOut.toString();
	    	  out.write(outText);
	    	  out.close();
	    } catch (IOException ioe) {
	        ioe.printStackTrace();
	    }
	 }

}
