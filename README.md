#Lucene-QueryExpansion-Modules


This is a Lucene's module - Query Expansion using Rocchio/LDA algorithm - for demonstrating its effectiveness/feasibility on Lucene framework in our IR project. 

Following open source projects were referenced and used into ours: 

* LucQE Lucene Query Expansion Module - <http://lucene-qe.sourceforge.net/>
* JGibbLDA - A Java Implementation of LDA - <http://jgibblda.sourceforge.net/>


###Prerequisite
Java 1.7 or higher version

Maven 3.0.4 or higher version

MacOS/Linux supported**

* Please note: We tested it on Windows but it failed.
* Source code is organized based on maven's structure. With the following command files, you can easily test the code and adjust the parameter quickly.


###About Command files (Execution)
Quick shell execution files are available and you can easily run it to test our 
code as follows: 

(To execute it, type './build.sh' or 'sh build.sh' on command-line)

* build.sh - Download all necessary files into local space from the net and 
install them. (mvn install)

* IndexTrec.sh - Make index using the Trec dataset. The root directory for the 
dataset should be given in argument(parameter) part when it executed. (You may 
edit this when you gonna run it on your own running environment, to another 
directory)

* SearchFiles.sh - Searching through the index file that made in the above 
procedure. Basic interpreter has been made for instant testing. 

* QueryExpansion-rocchio.sh - Conduct query expansion using the index file in
'index' sub-folder, with a command-line user interface. This uses the rocchio
algorithm to expand given query. 

* QueryExpansion-LDA.sh - Conduct query expansion in LDA approach. 



###Support and Feedback

Your feedback will be very appreciated! If you have specific problems or bugs with the
**SimpleUcloudStorage**
, please file an issue via Github.

For general feedback and support requests, send an email to:

heyeric.cho__AT__gmail.com

