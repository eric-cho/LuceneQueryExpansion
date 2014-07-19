package in.student.project.queryexpansion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;



public class BatchSearch {

	private BatchSearch() {}
	
	public static void main(String[] args) throws Exception {
		String usage =
				"Usage:\tjava BatchSearch [-file topic_filename] [-qe method] [-field f] [-queries file] [-outfile filename]";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		String file = "";
		String field = "contents";
		String queries = null;
		String queryexpansion = "rocchio";
		String outFileName = "outTitle.txt";

		for(int i = 0;i < args.length;i++) {
			if ("-file".equals(args[i])) {
				file = args[i+1];
				i++;
			} else if ("-field".equals(args[i])) {
				field = args[i+1];
				i++;
			} else if ("-queries".equals(args[i])) {
				queries = args[i+1];
				i++;
			} else if ("-qe".equals(args[i])) {
				queryexpansion = args[i+1];
				i++;
			} else if ("-outfile".equals(args[i])) {
				outFileName = args[i+1];
				i++;
			}
		}



        BufferedReader reader = null;
        BufferedWriter writer = null;
		GZIPInputStream in =null;
		InputStream in2 = null;
		Boolean gettingTitleFlag = false;
		
		try {
			in2 = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in2));
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		try {
	        writer = new BufferedWriter( new FileWriter( new File( outFileName ) ) );

			String line = null;
			String sep = null;
			String newline = System.getProperty("line.separator");

			StringBuffer sb = null;
			String title = "";
			while (null != (line = reader.readLine())) {
				if (line.startsWith("<title>") || gettingTitleFlag) {
					if (gettingTitleFlag) { // when the title located in below line.
						title = line.trim();
						gettingTitleFlag = false;
					}
					else { // title is appeared in the same line.
			    		title = line.substring(7).trim();
			    		if (title.isEmpty()) {
			    			gettingTitleFlag = true;
			    			continue;
			    		}
					}
			    		
		    		// query expansion here
		    		String[] args_file = {"search.prop", title};
		    		
		    		if (queryexpansion.equalsIgnoreCase("rocchio")) {
			    		SearchFilesRocchio.main ( args_file );
			    		if (SearchFilesRocchio.expandedQuery.length() > 0)
			    			title = SearchFilesRocchio.expandedQuery; 
		    		} else if (queryexpansion.equalsIgnoreCase("lda")) {
			    		SearchFilesLDA.main ( args_file );
			    		if (SearchFilesLDA.expandedQuery.length() > 0)
			    			title = SearchFilesLDA.expandedQuery; 
		    		}
		    		
		    		writer.write("<title> " + title);
					writer.write("\n");
					System.out.println(title);
					continue;
				}
				writer.write(line);
				writer.write("\n");
			}
		} finally {
			reader.close();
			if ( in2 != null)
				in2.close();

	        writer.flush();
	        writer.close();
	        
		}
        
	}
}
