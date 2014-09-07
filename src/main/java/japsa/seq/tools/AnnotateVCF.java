/*****************************************************************************
 * Copyright (c) Minh Duc Cao, Monash Uni & UQ, All rights reserved.         *
 *                                                                           *
 * Redistribution and use in source and binary forms, with or without        *
 * modification, are permitted provided that the following conditions        *
 * are met:                                                                  * 
 *                                                                           *
 * 1. Redistributions of source code must retain the above copyright notice, *
 *    this list of conditions and the following disclaimer.                  *
 * 2. Redistributions in binary form must reproduce the above copyright      *
 *    notice, this list of conditions and the following disclaimer in the    *
 *    documentation and/or other materials provided with the distribution.   *
 * 3. Neither the names of the institutions nor the names of the contributors*
 *    may be used to endorse or promote products derived from this software  *
 *    without specific prior written permission.                             *
 *                                                                           *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS   *
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, *
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR    *
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR         *
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,     *
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,       *
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR        *
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING      *
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS        *
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.              *
 ****************************************************************************/

/**************************     REVISION HISTORY    **************************
 * 25/08/2014 - Minh Duc Cao: Created                                        
 *  
 ****************************************************************************/

package japsa.seq.tools;

import japsa.seq.JapsaAnnotation;
import japsa.seq.JapsaFeature;
import japsa.seq.SequenceOutputStream;
import japsa.seq.SequenceReader;
import japsa.seq.XAFReader;
import japsa.util.CommandLine;
import japsa.util.deploy.Deployable;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author minhduc
 *
 */
@Deployable(scriptName = "jsa.seq.annovcf", scriptDesc = "Annotate variation from a vcf file using annotation from gff file ")
public class AnnotateVCF {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		/*********************** Setting up script ****************************/
		Deployable annotation = AnnotateVCF.class.getAnnotation(Deployable.class);		 		
		CommandLine cmdLine = new CommandLine("\nUsage: " + annotation.scriptName() + " [options]", annotation.scriptDesc());		
		/**********************************************************************/		

		cmdLine.addString("gffin", "-",  "GFF file");
		cmdLine.addInt("upstream", 0, "Add upstream ");
		cmdLine.addInt("downstream", 0, "Add downstream ");		
		cmdLine.addString("output", "-", "Name of output file, - for standard out");
		cmdLine.addString("vcf", null, "Name of vcf file", true);
		
		args = cmdLine.stdParseLine(args);	
		/**********************************************************************/
		String gffIn   =  cmdLine.getStringVal("gffin");
		String output  =  cmdLine.getStringVal("output");
		String vcf  =  cmdLine.getStringVal("vcf");
		
		int upStr   = cmdLine.getIntVal("upstream");
		int downStr   = cmdLine.getIntVal("downstream");
		
		
		BufferedReader in = SequenceReader.openFile(gffIn);
		
		JapsaAnnotation anno = readGFF(in, upStr, downStr);		
		in.close();
		
		SequenceOutputStream out =  SequenceOutputStream.makeOutputStream(output);
		//for (JapsaFeature feature : anno.getFeatureList()){
		//	out.print(feature.getLength() + " " + feature.getType() + " " + feature.getID() + " " + feature.getParent());
		//	out.println();
		//}		
		//out.close();
		
		XAFReader xaf = new XAFReader(vcf);		
		while (xaf.next() != null){
			//System.out.println(xaf.recordNo());			
			String [] toks = xaf.getCurrentRecord().split("\t");	
			int pos = Integer.parseInt(xaf.getField(1));				
			out.print(toks[0] + "\t" + toks[1] + "\t" + toks[3] + "\t" + toks[4]);
			for (int i = 9; i < toks.length; i++){
				out.print("\t" + toks[i].charAt(0));
			}
			
			for (int i =0; i < anno.numFeatures(); i++){
				JapsaFeature feature = anno.getFeature(i);
				if (feature.getStart() <= pos && feature.getEnd() >= pos){
					out.print("\t" + feature.getID());
				}else if (feature.getStart() > pos)
					break;
			}
			out.println();
		}
		
		xaf.close();		
		out.close();
	}
	
	public static JapsaAnnotation readGFF(BufferedReader in, int upStr, int downStr) throws IOException{
		String line;		
		JapsaAnnotation anno =  new JapsaAnnotation();
		while ( (line = in.readLine()) != null){
			//lineNo ++;
			if (line.trim().startsWith("#"))
				continue;			
			
			String [] toks = line.split("\t");
			//if (toks[2].equals("region"))
			//	continue;
			
			int start = Integer.parseInt(toks[3]);
			int end = Integer.parseInt(toks[4]);
			
			String parent = toks[0];
			String ID = "";
			char strand = toks[6].charAt(0);
			String desc = toks[8];
			
			String [] featureChars = toks[8].split(";");
			for (String fch:featureChars){
				if (fch.startsWith("ID="))
					ID = fch.substring(3);
				
				if (fch.startsWith("Parent="))
					parent = fch.substring(7);
			}
			
			JapsaFeature feature = new JapsaFeature(start, end, toks[2], ID, strand, parent);
			feature.addDesc(desc);			
			anno.add(feature);
			
			//Add upstream
			if (upStr > 0 && feature.getType().equals("gene")){
				if (feature.getStrand() == '-'){
					JapsaFeature uFeature = new JapsaFeature(end + 1, end + upStr, "upstream", "u" + ID, strand, feature.getID());
					uFeature.addDesc("Upstream region added automatically");
					anno.add(uFeature);
				}else{
					JapsaFeature uFeature = new JapsaFeature(start - upStr, start - 1, "upstream", "u" + ID, strand, feature.getID());
					uFeature.addDesc("Upstream region added automatically");
					anno.add(uFeature);
				}
			}
			
			//add downstream
			if (downStr > 0 && feature.getType().equals("gene")){
				if (feature.getStrand() == '-'){
					JapsaFeature dFeature = new JapsaFeature(start - downStr, start - 1, "downtream", "d" + ID, strand, feature.getID());
					dFeature.addDesc("Downstream region added automatically");
					anno.add(dFeature);					
				}else{
					JapsaFeature dFeature = new JapsaFeature(end + 1, end + downStr, "downtream", "d" + ID, strand, feature.getID());
					dFeature.addDesc("Downstream region added automatically");
					anno.add(dFeature);					
				}
			}						
		}	
		anno.sortFeatures();
		return anno;
	}

}