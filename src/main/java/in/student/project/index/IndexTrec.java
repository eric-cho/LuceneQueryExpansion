package in.student.project.index;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.Document;
import in.student.project.index.HTMLDocument;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import in.student.project.index.TermEnum;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Indexer for HTML files.
 */
public class IndexTrec {
	private static boolean deleting = false; // true during deletion pass
	private static IndexReader reader; // existing index
	private static IndexWriter writer; // new index being built
	private static TermEnum uidIter; // document id iterator
	
    private IndexTrec() {}

	/** Indexer for Trec files. */
	public static void main(String[] argv) {
		try {
			File index =new File("index");
			boolean create = false;
			File root = null;

			String usage = "IndexTrec [-create] [-index <index>] <root_directory>";

			if (argv.length == 0) {
				System.err.println("Usage: " + usage);
				return;
			}

			for (int i = 0; i < argv.length; i++) {
				if (argv[i].equals("-index")) { // parse -index option
					index = new File(argv[++i]);
				} else if (argv[i].equals("-create")) { // parse -create option
					create = true;
				} else if (i != argv.length - 1) {
					System.err.println("Usage: " + usage);
					return;
				} else
					root = new File(argv[i]);
			}

			Date start = new Date();
			if (!create) { // delete stale docs
				deleting = true;
				indexDocs(root, index, create);
			}

			Directory dir = FSDirectory.open(index);
		    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
		    IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
		    

		    if (create) {
		        // Create a new index in the directory, removing any
		        // previously indexed documents:
		        iwc.setOpenMode(OpenMode.CREATE);
		    } else {
		        // Add new documents to an existing index:
		        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		    }

		    writer = new IndexWriter(dir, iwc);
		    indexDocs(root, index, create); // add new docs
		    
			writer.close();
			Date end = new Date();

			System.out.print(end.getTime() - start.getTime());
			System.out.println(" total milliseconds");
		} catch (Exception e) {
			System.out.println(" caught a " +
                    e.getClass() +
                    "\n with message: " +
                    e.getMessage());
		}
	}

	/*
	 * Walk directory hierarchy in uid order, while keeping uid iterator from /*
	 * existing index in sync. Mismatches indicate one of: (a) old documents to
	 * /* be deleted; (b) unchanged documents, to be left alone; or (c) new /*
	 * documents, to be indexed.
	 */
	private static void indexDocs(File file, File index, boolean create)
			throws Exception {

		if (!create) { // incrementally update
			
		} else {
			indexDocs(file);
        }
	}

	private static void indexDocs(File file) throws Exception {
		if (file.isDirectory()) { // if a directory
			String[] files = file.list(); // list its files
			Arrays.sort(files); // sort the files
			
            for (int i = 0; i < files.length; i++) {
				// recursively index them
				indexDocs(new File(file, files[i]));
            }
		} else if ( file.getPath().substring(file.getPath().lastIndexOf("/")+1).startsWith("B") ){
//			System.out.println(file);
			
			System.out.println("adding " + file.getPath());
			addDocuments(writer, file);
		}
	}

	private static void addDocuments(IndexWriter writer, File file)
			throws IOException, InterruptedException {
		ArrayList<HashMap<String, String>> docsList = readDocs(file);
		
        for ( int i = 0; i < docsList.size(); i++) {
			HashMap<String, String> docsMap = docsList.get(i);
			String docNo = docsMap.get("DOCNO");
			String html = docsMap.get("DOC");
			InputStream is = new ByteArrayInputStream(html.getBytes("UTF-8"));
			
			System.out.println("[docNo--- "+docNo+" ---docNo]");
			
            Document doc = TrecDocument.Document(is, docNo);
            if (doc != null)
            	writer.addDocument(doc);
		}
	}

	public static ArrayList<HashMap<String, String>> readDocs(File file) throws IOException {
		BufferedReader reader=null;
		GZIPInputStream in =null;
		
		if ( file.getPath().endsWith(".gz") ){
			try {
	            in = new GZIPInputStream(new FileInputStream(file));
	            reader = new BufferedReader(new InputStreamReader(in));
	        } catch (IOException e) {
                e.printStackTrace();
	        }
		} else {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
		}
		
		ArrayList<HashMap<String, String>> docsList = new ArrayList<HashMap<String, String>>();
		try {
			String line = null;
			String sep = null;
			String newline = System.getProperty("line.separator");

			StringBuffer sb = null;
			HashMap<String, String> docsMap = null;
			while (null != (line=reader.readLine())) {
				sep = newline;
				if (line.equals("<DOC>")) {
					sb = new StringBuffer();
					docsMap = new HashMap<String, String>();
					sep = "";
				}
               
                sb.append(sep+line);
                
				if (line.startsWith("<DOCNO>")) {
		    		int endIndex = line.indexOf("</DOCNO>");
		    		String docNo = line.substring(7, endIndex);
		    		docsMap.put("DOCNO", docNo);
				}
				
				if (line.equals("</DOC>")) {
					docsMap.put("DOC", sb.toString());
					docsList.add(docsMap);
				}
			}
		} finally {
			reader.close();
			if ( in != null)
				in.close();
		}
		return docsList;
	}
}
