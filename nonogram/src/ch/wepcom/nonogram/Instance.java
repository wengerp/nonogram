package ch.wepcom.nonogram;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Instance extends Thread {

	ArrayList<ConstraintCollection> rows = new ArrayList<ConstraintCollection>();
	ArrayList<ConstraintCollection> cols = new ArrayList<ConstraintCollection>();
	ArrayList<Constraint> constraintsCache = new ArrayList<Constraint>();
	int numberOfConstraintTuples = 0;
	int numberOfSupportsTuples = 0;
	
	boolean constraintCacheValid = false;
	
	int[] solution;
	
	int rowLength = 0;
	int colLength = 0;
	
	int nCollections = 0;
	
	public Instance(int rowLength, int colLength) {
		this.rowLength = rowLength;
		this.colLength = colLength;
		for (int r=0; r<colLength; r++) {
			this.rows.add(null);
		}
		for (int c=0; c<rowLength; c++) {
			this.cols.add(null);
		}
		this.constraintCacheValid = false;
	}
	
	public void run() {
		ArrayList<CollectionThread> threads = null;
		
		int i=0;
		for (ConstraintCollection cc : rows) {
				threads = new ArrayList<CollectionThread>();
				CollectionThread ct = new CollectionThread(this,cc,i);
				threads.add(ct);
				ct.start();
				i++;
		}
		for (CollectionThread collectionThread : threads) {
			try {
				collectionThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		i=0;
		for (ConstraintCollection cc : cols) {
			System.out.println("Starting thread for col collection:"+cc.index);
				cc.createConstraints();
				setCollectionThreaded(i,cc);
				i++;
			System.out.println("Finished thread for col collection:"+cc.index+" (prio: "+cc.prio+")");
		}
	}

	public  synchronized void setCollectionThreaded(int position, ConstraintCollection cc) {
		if (cc.type.equalsIgnoreCase("Z")) {
			rows.set(position,cc);
		} else {
			cols.set(position,cc);
		}
		nCollections++;
		this.constraintCacheValid = false;
	}

	
	public synchronized void setRows(ArrayList<int[]>rules) {
		int index=0;
		for (int[] rule : rules) {
			ConstraintCollection cc = new ConstraintCollection(this,"Z"+String.format("%02d", index+1),"Z", index+1, rowLength, rule);
			rows.set(index,cc);
			nCollections++;
			index++;
		}
		Collections.sort(rows);
		this.constraintCacheValid = false;			
	}

	public synchronized void setCols(ArrayList<int[]>rules) {
		int index=0;
		for (int[] rule : rules) {
			ConstraintCollection cc = new ConstraintCollection(this,"S"+String.format("%02d", index+1),"S", index+1, colLength, rule);
			cols.set(index,cc);
			nCollections++;
			index++;
		}
		Collections.sort(cols);
		this.constraintCacheValid = false;			
	}


	public synchronized int getNumberOfCollections() {
		int n = nCollections;
		return n;
	}
	
	public synchronized void setSolution(int[] b) {
		this.solution = b;
	}

	public synchronized void setConstraintCacheValid(boolean b) {
		this.constraintCacheValid = b;
	}

	public synchronized void addToNumberOfConstraintTuples(int number) {
		this.numberOfConstraintTuples += number;
	}

	public synchronized void addToNumberOfSupportsTuples(int number) {
		this.numberOfSupportsTuples += number;
	}

	public synchronized ArrayList<Constraint> getConstraints() {

		boolean cacheIsValid = this.constraintCacheValid;
		if (!cacheIsValid) {
			
			for (ConstraintCollection cc : rows) {
				constraintsCache.addAll(cc.getConstraints());			
			}
			for (ConstraintCollection cc : cols) {
				constraintsCache.addAll(cc.getConstraints());			
			}
			this.constraintCacheValid = true;
		}
		return constraintsCache;
	}

	public synchronized ArrayList<ConstraintCollection> getConstraintCollections() {
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
				c.setSelected(true);
			}
		} 
		for (int i = 0; i < rows.size(); i++) {
			ConstraintCollection cc = getRowByIndex(i+1);
			for (Constraint c : cc.getConstraints()) {
				if (c.isSelected) {
					c.draw();
				}
			}
		}
	}
	
private ConstraintCollection getRowByIndex(int index) {
	for (ConstraintCollection cc : this.rows) {
		if (cc.index == index) {
			return cc;
		}
	}
	return null;
}

	public void calculatetConstraintsTuplesThreaded() {
		
		System.out.println("calculatetConstraintsTuples");
		ArrayList<OutputThread> tlist = new ArrayList<OutputThread>();
		int i=0;
		for (ConstraintCollection row : rows) {
			System.out.println("row:"+row.index);
			OutputThread t = new OutputThread(this,i);
			t.start();
			tlist.add(t);
			i++;
		}
		
		for (OutputThread instanceThread : tlist) {
			try {
				instanceThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized void updateConstraintsState(Constraint c) {
		ArrayList<ConstraintCollection> ccList;
		if (c.type.equalsIgnoreCase("Z")) {
			ccList = cols;
		} else {
			ccList = rows;
		}	
		for (ConstraintCollection cc : ccList) {
			if (cc!=null) {
				cc.updateConstraintsState(c);
			}
		}			
	}
	
	public synchronized int checkConstraintsState(Constraint c) {
		ArrayList<ConstraintCollection> ccList;
		if (c.type.equalsIgnoreCase("Z")) {
			ccList = cols;
		} else {
			ccList = rows;
		}	
		int result = 0;
		for (ConstraintCollection cc : ccList) {
			if (cc!=null) {
				int conSup = cc.checkConstraintsState(c);
				if (conSup > result) {
					result = conSup;
				}

			}
		}
		return result;
	}
	
	public void showInstance() {
		for (ConstraintCollection cc : rows) {
			System.out.println("========= ROW:"+cc.index+" ===========");
			System.out.println(cc);
		}
		for (ConstraintCollection cc : cols) {
			System.out.println("========= COL:"+cc.index+" ===========");
			System.out.println(cc);
		}
	}

	private void printVariables(PrintWriter sbOut) {
		//     <variable name="Z11" domain="domT"/>

		System.out.println("printVariables");

		sbOut.println();
		sbOut.println( "<variables nbVariables=\""+ getConstraints().size() + "\">");

		sbOut.println();
		for (Constraint c : getConstraints()) {
			String sOut = "<variable name=\"" + c.name +"\" domain=\"domT\"/>";
			sbOut.println(sOut);	
		} 
		sbOut.println();
		sbOut.println( "</variables>");
		sbOut.println();

	}
	
	
	

	private void printRelations(PrintWriter sbOut) {
		//	<relation name="rel0" arity="2" nbTuples="1" semantics="conflicts">
		//  1 1 
		//  </relation>
		System.out.println("printRelations");

		int i;
		
		i = (this.getConstraintCollections().size()+2);
		sbOut.println();
		sbOut.println("<relations nbRelations=\""+ (i+2) +"\">");
		sbOut.println();

		sbOut.println("<relation name=\"rel0\" arity=\"2\" nbTuples=\"1\" semantics=\"conflicts\">1 1</relation>");
		sbOut.println("<relation name=\"rel1\" arity=\"1\" nbTuples=\"1\" semantics=\"supports\">1</relation>");

		printNotZeroRelations(sbOut);
		
		sbOut.println("</relations>");
		sbOut.println();
	}
	
	private int printNotZeroRelations(PrintWriter out) {
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
			out.println(" <relation name=\"" + relName +"\" arity=\"" +cc.getConstraintsSize() + 
					"\" nbTuples=\"1\" semantics=\"conflicts\">");
			i++;
			for (int j=0; j<cc.getConstraintsSize(); j++) {
				out.append(0 +" " );
			}
			out.println(" </relation>");
			}
		}
		return i;
	} 
		
	private int printNotZeroConstraints(PrintWriter out) {
		//    <constraint name="CN1" arity="2" scope="Z0101 Z0102" reference="R2" />
		
		System.out.println("printNotZeroConstraints");
		
		int i=1;
		out.append(System.getProperty("line.separator"));

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
		return i;

	}

	
	private void printConstraints(PrintWriter sbOut) {
		int nCon;
		nCon = this.numberOfConstraintTuples+this.numberOfSupportsTuples+this.getConstraintCollections().size();
		
		sbOut.append("<constraints nbConstraints=\""+ nCon +"\">");

		for (int i = 0; i < rows.size(); i++) {
			String sConstraintFile = "/Volumes/Raid0/_to_delete/nonocsp-constraint-"+rows.get(i).type +rows.get(i).index+".xml";
			File constraintFile = new File(sConstraintFile);
			if (constraintFile.exists()) {
				appendContent(constraintFile, sbOut);
			}
		}
		for (int i = 0; i < rows.size(); i++) {
			String sSupportsFile = "/Volumes/Raid0/_to_delete/nonocsp-support-"+rows.get(i).type +rows.get(i).index+".xml";
			File supportsFile = new File(sSupportsFile);
			if (supportsFile.exists()) {
				appendContent(supportsFile, sbOut);
			}			
		}
		
		printNotZeroConstraints(sbOut);
		sbOut.append("</constraints>");
	}
	
	private static void appendContent(File inFile, PrintWriter out){
		  
		try {

			FileInputStream fstream = new FileInputStream(inFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  out.println(strLine);
			}
			
			//Close the input stream
			in.close();
			    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			  }		  
		  }


	public void writeCsp(PrintWriter pw) {
	
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

		pw.println("<instance>");
		pw.println("<presentation  name=\"Nonogram\"  nbSolutions=\"?\"  format=\"XCSP 2.0\">"+System.getProperty("line.separator"));
		pw.println("csp representation for nonograms");
		pw.println("</presentation>");
		pw.println("<domains nbDomains=\"1\">");
		pw.println("<domain name=\"domT\" nbValues=\"2\">");
		pw.println(" 0..1");
		pw.println("</domain>");
		pw.println("</domains>");

		printVariables(pw);
		printRelations(pw);
		printConstraints(pw);
		
		pw.println("</instance>");
		
	}
	
}
