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
import java.io.*;
import java.nio.CharBuffer;

//import kr.ac.kaist.wikipedia.WikipediaManager;

import org.apache.lucene.document.*;
import in.student.project.index.html.HTMLParser;

/** 
 * A utility for making Lucene Documents for HTML documents.
 */
public class TrecDocument {
    static char dirSep = System.getProperty("file.separator").charAt(0);

    public static String uid(File f) {
        return f.getPath().replace(dirSep, '\u0000') +"\u0000" +
                DateTools.timeToString(f.lastModified(), DateTools.Resolution.SECOND);
    }

    public static String uid2url(String uid) {
        String url = uid.replace('\u0000', '/');	  // replace nulls with slashes
        return url.substring(0, url.lastIndexOf('/')); // remove date from end
    }

    public static Document Document(InputStream is, String docNo) throws IOException, InterruptedException  {
        // make a new, empty document
        Document doc = new Document();
        HTMLParser parser = new HTMLParser(is);
        String parser_title = parser.getTitle();
        if (parser_title.isEmpty()) {
        	try {
	        	if (!parser.getContents().isEmpty())
	        		parser_title = parser.getContents().substring(0, (parser.getContents().length() - 4 > parser.SUMMARY_LENGTH) ? parser.SUMMARY_LENGTH : parser.getContents().length());
        	} catch (Exception e) {
        		System.out.println("parser title is empty");
        		return null;
        	}
	        	
        	if (parser_title.isEmpty())
        		return null;
        }
        
        BufferedReader in = new BufferedReader(parser.getReader());
        String line = null;
        StringBuilder rslt = new StringBuilder();
        while ((line = in.readLine()) != null) {
            rslt.append(line);
        }
      
        // Add the tag-stripped contents as a Reader-valued Text field so it will
        // get tokenized and indexed.

		FieldType type = new FieldType();
		type.setIndexed(true);
		type.setStored(true);
		type.setStoreTermVectors(true);
		if (parser.getContents().isEmpty())
			doc.add(new Field("contents", rslt.toString().trim(), type));
		else 
			doc.add(new Field("contents", parser.getContents().trim(), type));

        // Add the summary as a field that is stored and returned with
        // hit documents for display.
        doc.add(new Field("summary", parser.getSummary(), Field.Store.YES, Field.Index.NO));

        // Add the title as a field that it can be searched and that is stored.
        doc.add(new Field("title", parser_title, type));
        doc.add(new Field("path", docNo, Field.Store.YES, Field.Index.ANALYZED));
        
        doc.add(new Field("DOCNO", docNo, Field.Store.YES, Field.Index.NOT_ANALYZED));


    
        return doc;
    }

    private TrecDocument() {}
}
