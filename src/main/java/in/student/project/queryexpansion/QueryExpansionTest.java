package in.student.project.queryexpansion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

public class QueryExpansionTest {

	private QueryExpansionTest() {}
	
	public static void main(String[] args) throws Exception {
		String usage =
				"Usage:\tjava QueryExpansionTest [-qe method] [-query querystring]";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		String file = "";
		String field = "contents";
		String query = null;
		String queryexpansion = "rocchio";

		for(int i = 0;i < args.length;i++) {
			if ("-query".equals(args[i])) {
				query = args[i+1];
				i++;
			} else if ("-qe".equals(args[i])) {
				queryexpansion = args[i+1];
				i++;
			}
		}

        BufferedReader reader = null;
        BufferedReader in = null;
		
		try {
			
			while (true) {
			    in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
			    
				if (query == null) {  // prompt the user
					System.out.println("Enter query (just enter to exit): ");
				}
				String line = query != null ? query : in.readLine();
				if (line == null || line.length() <= 0) {
			        break;
			    }
				
				// query expansion here
				String[] args_file = {"search.prop", line.trim()};
				
				if (queryexpansion.equalsIgnoreCase("rocchio")) {
		    		SearchFilesRocchio.main ( args_file );
		    		if (SearchFilesRocchio.expandedQuery.length() > 0)
		    			query = SearchFilesRocchio.expandedQuery; 
				} else if (queryexpansion.equalsIgnoreCase("lda")) {
		    		SearchFilesLDA.main ( args_file );
		    		if (SearchFilesLDA.expandedQuery.length() > 0)
		    			query = SearchFilesLDA.expandedQuery; 
				}
				query = null;
			}			
			
		} finally {

		}
        
	}
}
