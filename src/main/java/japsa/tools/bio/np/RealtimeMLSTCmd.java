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

/*****************************************************************************
 *                           Revision History                                
 * 7 Aug 2015 - Minh Duc Cao: Created                                        
 * 
 ****************************************************************************/
package japsa.tools.bio.np;

import java.io.IOException;

import japsa.bio.np.RealtimeMLST;
import japsa.util.CommandLine;
import japsa.util.deploy.Deployable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author minhduc
 *
 */
@Deployable(
	scriptName = "jsa.np.rtMLST", 
	scriptDesc = "Realtime Multi-Locus Strain Typing using Nanopore Sequencing data",
	seeAlso = "jsa.np.npreader, jsa.np.rtSpeciesTyping, jsa.np.rtStrainTyping, jsa.np.rtResistGenes, jsa.util.streamServer, jsa.util.streamClient"
	)
public class RealtimeMLSTCmd extends CommandLine{
//    private static final Logger LOG = LoggerFactory.getLogger(RealtimeMLSTCmd.class);
	public RealtimeMLSTCmd(){
		super();
		Deployable annotation = getClass().getAnnotation(Deployable.class);		
		setUsage(annotation.scriptName() + " [options]");
		setDesc(annotation.scriptDesc());
		
		addString("output", "output.dat",  "Output file");
		addString("mlstScheme", null, "Path to mlst scheme",true);
		addString("bamFile", null,  "The bam file");		
		//////////////////////////////////////////////////////////////////////////		
		addDouble("qual", 0,  "Minimum alignment quality");
		addBoolean("twodonly", false,  "Use only two dimentional reads");		
		addInt("read", 50,  "Minimum number of reads between analyses");		
		addInt("time", 30,   "Minimum number of seconds between analyses");
		
		addStdHelp();		
	} 

	public static void main(String[] args) throws IOException, InterruptedException{
		CommandLine cmdLine = new RealtimeMLSTCmd();
		args = cmdLine.stdParseLine(args);			
		/**********************************************************************/

		
		String output = cmdLine.getStringVal("output");
		String mlstDir = cmdLine.getStringVal("mlstScheme");
		String bamFile = cmdLine.getStringVal("bamFile");		
				
		int read       = cmdLine.getIntVal("read");
		int time       = cmdLine.getIntVal("time");		
		double qual      = cmdLine.getDoubleVal("qual");		
		boolean twodonly = cmdLine.getBooleanVal("twodonly");
				
		
		int top = 10;
		
		
		RealtimeMLST paTyping = new RealtimeMLST(mlstDir, output, read, time);
		paTyping.setTwoDOnly(twodonly);
		paTyping.setMinQual(qual);
		paTyping.typing(bamFile,  top);

	}
}

/*RST*
------------------------------------------------------------------------------------
*jsa.np.rtMLST*: Multi-locus Sequencing Typing in real-time with Nanopore sequencing 
------------------------------------------------------------------------------------

*jsa.np.rtMLST* performs MLST typing from real-time sequencing with Nanopore MinION. 

<usage> 

~~~~~~~~~~
Setting up
~~~~~~~~~~

Refer to real-time analysis page at https://github.com/mdcao/npAnalysis/

*RST*/