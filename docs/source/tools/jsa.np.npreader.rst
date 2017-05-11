-------------------------------------------------------------------------
*npReader*: real-time conversion and analysis of Nanopore sequencing data
-------------------------------------------------------------------------

*npReader* (jsa.np.npreader) is a program that extracts Oxford Nanopore
sequencing data from FAST5 files, performs an initial analysis of the date and
streams them to real-time analysis pipelines. These pipelines can run on the
same computer or on computing clouds/high performance clusters.

npReader is included in the `Japsa package <http://mdcao.github.io/japsa/>`_.
It requires
`JAVA HDF5 INTERFACE (JHI5) library <https://www.hdfgroup.org/products/java/JNI/jhi5/index.html>`_
to be installed prior to setting up Japsa. Details of installation as follows:

**On Windows/Mac**

1. Download and install HDF-View from
https://www.hdfgroup.org/products/java/release/download.html.
Note the folder that the JHI library is installed, e.g.,
*C:\\Program Files\\HDF_Group\\HDFView\\2.11.0\\lib*

2. Follow the instructions to install Japsa on
http://japsa.readthedocs.org/en/latest/install.html.
Upon prompting for "Path to HDF library", enter the above path.

**On Linux**

You can either install the JHI5 library by downloading the software from
*https://www.hdfgroup.org/products/java/JNI/jhi5/index.html* or from your
Linux distribution software repository, such as::

   sudo apt-get install libjhdf5-jni

The library is typically installed to */usr/lib/jni*. Enter this path when
prompted for "Path to HDF library" during installation of Japsa.

HDF-View (https://www.hdfgroup.org/products/java/release/download.html) also 
contains the neccessary library. Please install HDF-2.10.1 instead of the 
latest version.

~~~~~~~~
Synopsis
~~~~~~~~

*jsa.np.npreader*: Extract and stream Oxford Nanopore sequencing data in real-time. Demultiplexe included.

~~~~~
Usage
~~~~~
::

   jsa.np.npreader [options]

~~~~~~~
Options
~~~~~~~
  --GUI           Run with a Graphical User Interface
                  (default='false')
  --realtime      Run the program in real-time mode, i.e., keep waiting for new data from Metrichor agent
                  (default='false')
  --folder=s      The folder containing base-called reads
                  (default='null')
  --fail          Get sequence reads from fail folder
                  (default='false')
  --output=s      Name of the output file, - for stdout
                  (default='-')
  --streams=s     Stream output to some servers, format "IP:port,IP:port" (no spaces)
                  (default='null')
  --format=s      Format of sequence reads (fastq or fasta)
                  (default='fastq')
  --minLength=i   Minimum read length
                  (default='1')
  --number        Add a unique number to read name
                  (default='false')
  --stats         Generate a report of read statistics
                  (default='false')
  --time          Extract the sequencing time of each read -- experimental
                  (default='false')
  --exhaustive    Whether to traverse the input directory exhaustively (albacore) or lazily (metrichor)
                  (default='false')
  --barcode=s     The file containing all barcode sequences for demultiplexing.
                  (default='null')
  --help          Display this usage and exit
                  (default='false')


~~~~~~~~
See also
~~~~~~~~

jsa.np.filter_, jsa.util.streamServer_, jsa.util.streamClient_, jsa.np.rtSpeciesTyping_, jsa.np.rtStrainTyping_, jsa.np.rtResistGenes_

.. _jsa.np.filter: jsa.np.filter.html
.. _jsa.util.streamServer: jsa.util.streamServer.html
.. _jsa.util.streamClient: jsa.util.streamClient.html
.. _jsa.np.rtSpeciesTyping: jsa.np.rtSpeciesTyping.html
.. _jsa.np.rtStrainTyping: jsa.np.rtStrainTyping.html
.. _jsa.np.rtResistGenes: jsa.np.rtResistGenes.html



~~~~~~~~~~~~~~
Usage examples
~~~~~~~~~~~~~~

A summary of npReader usage can be obtained by invoking the --help option::

   jsa.np.npreader --help

The simplest way to run *npReader* in GUI mode is by typing::

   jsa.np.npreader -GUI -realtime

and specify various options in the GUI. All of these options can be specified
from the command line::

   jsa.np.npreader -GUI -realtime -folder c:\Downloads\ -fail -output myrun.fastq --minLength 200 --stats

npReader can run natively on a Windows laptop that runs the Metrichor agent. It
can stream sequence data to multiple analysis pipelines on the same computer
and/or on high performance clusters and computing clouds.

Start several analysis pipelines on some remote machines. Such a pipeline can
be to count how many reads aligned to chromosomes A and B::

   jsa.util.streamServer --port 3456 | \
   bwa mem -t 8 -k11 -W20 -r10 -A1 -B1 -O1 -E1 -L0 -Y -K 10000 index - | \
   awk -F "\t" 'BEGIN{A=0;B=0;N++} NF>4 \
       {if ($3=="chrA") A++; if ($3=="chrB") B++; \
        if (NR %100==0) \
          {print "At " NR " reads, " A " aligned to chr A; " B " aligned to chr B"} \
       }'  

In this pipeline, the *jsa.util.streamServer* program receives stream data
from *npReader* and forwards to *bwa*, which aligns the data to a reference
and in turn streams the alignment in sam format to the awk program to perform
a simple analysis of counting reads aligned to chrA and chrB.

The Japsa package contains several real-time analysis (jsa.np.speciesTyping,
jsa.np.geneStrainTyping, jsa.np.resistGenes). They can be used to set up
analysis pipelines, such as::

   jsa.util.streamServer --port 3457 \
   bwa mem -t 8 -k11 -W20 -r10 -A1 -B1 -O1 -E1 -L0 -Y -K 10000 index - | \   
   jsa.np.speciesTyping  -bam - --index speciesIndex -output output.dat

Once these pipelines are ready, npReader can start streaming data off the
MinION and the Metrichor agent to these pipelines::

   jsa.np.npreader -realtime -folder c:\Downloads\ -fail -output myrun.fastq \
      --minLength 200 --streams server1IP:3456,server2IP:3457

One can run *npReader* on a computing cloud if the download folder (containing
base-called data) can be mounted to the cloud. In such case, npReader can
direct stream data to the pipelines without the need of
*jsa.util.streamServer*::

   jsa.np.npreader -realtime -folder c:\Downloads\ -fail -output - | \
   bwa mem -t 8 -k11 -W20 -r10 -A1 -B1 -O1 -E1 -L0 -Y -K 10000 index - | \
   jsa.np.speciesTyping  -bam - --index speciesIndex -output output.dat

*npReader* now supports barcode sequencing demultiplex. For this analysis, it
requires a FASTA file of barcode tag sequences and will classify output sequences
based on alignment. User can specify the threshold for alignment confidence from 
the GUI. Demultiplexing results are illustrated as prefix Barcode:<sample>:<score>|
added to each output sequence name.

	jsa.np.npreader -GUI -barcode barcode.fasta

Japsa also provides *jsa.np.filter*, a tool to bin sequence data in groups of
the user's liking. Like any other streamline tools, jsa.np.filter can run
behind *jsa.util.streamServer* on a remote machine, or can get data directly
from npReader via pipe::

   jsa.np.npreader -realtime -folder c:\Downloads\ -fail -output - | \
   jsa.np.filter -input - -lenMin 2000 --qualMin 10 -output goodreads.fq

One can also use *tee* to group data into different bins *in real-time* with
*jsa.np.filter*::

   jsa.np.npreader -realtime -folder c:\Downloads\ -fail -output - | \   
   tee >(jsa.np.filter -input - -lenMax 2000 -output 0k2k.fq) \ 
   >(jsa.np.filter -lenMin 2000 -lenMax 4000 -input - -output 2k4k.fq) \ 
   >(jsa.np.filter -lenMin 4000 -lenMax 6000 -input - -output 4k6k.fq) \
   >(jsa.np.filter -lenMin 6000 -input - -output 6k.fq) \
   > all.fq

These bins can also be piped/streamed to different analysis pipelines as above.

