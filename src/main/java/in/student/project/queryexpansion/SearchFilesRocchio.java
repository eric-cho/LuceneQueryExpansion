package in.student.project.queryexpansion;

/* ====================================================================

 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlsearcheredgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Lucene" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Lucene", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import in.student.project.util.Defs;
import in.student.project.util.Utils;
import in.student.project.queryexpansion.QueryExpansion;


class SearchFilesRocchio
{

    private static Logger logger = Logger.getLogger( "SearchFilesRocchio" );
    public static String expandedQuery = "";

    /**
     *
     * args
     * 0 - properties_file
     * 
     * # index-dir
     * index-dir = index
     * 
     * # query-file
     * query-file = queries.txt
     * 
     * # out-count
     * docs-per-query = 1000
     * 
     * # query-term-count (0 - any count) (ex. queries with only 3 terms - 3 )
     * query-terms-count = 0
     * 
     * # out-file - name of the file where results will be written
     * out-file = search.result
     *
     * #query-expansion
     * For details see <code> QueryExpansion Constants</code> 
     * @throws Exception 
     *
     */
    public static void main( String[] args ) throws Exception
    {
        // Load Properties
        Properties properties = new Properties();
        properties.load( new FileInputStream( args[0] ) );
        String runTag = args[0];
        String remoteQuery = "";
        if (args.length > 1) {
        	remoteQuery = args[1];
        	expandedQuery = "";
        }
        
        properties.setProperty( Defs.RUN_TAG_FLD, runTag );
        String indexDir = properties.getProperty( "index-dir" );
        String queryFile = properties.getProperty( "query-file" );
        int termCount = Integer.valueOf( properties.getProperty( "query-terms-count" ) ).intValue();
        int outCount = Integer.valueOf( properties.getProperty( "docs-per-query" ) ).intValue();
        String queryFileName = properties.getProperty( "query-file" );
        String outFileName = runTag + properties.getProperty( "out-file" );
        String queryExpansionFlag = properties.getProperty( QueryExpansion.METHOD_FLD, "" );

        IndexReader idxReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
        IndexSearcher searcher = new IndexSearcher( idxReader );
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
        BufferedWriter writer = new BufferedWriter( new FileWriter( new File( outFileName ) ) );
        String query_num = null;
        BufferedReader in = new BufferedReader( new FileReader( queryFileName ) );
        TFIDFSimilarity similarity = null;
        int hitsCount = 50;

        while (true)
        {
            String line;
            line = in.readLine();
            try
            {
                if ( line.length() == -1 )
                    break;
            }
            catch (Exception e)
            {
                return;
            }
            StringTokenizer tknzr = new StringTokenizer( line );
            query_num = tknzr.nextToken();
            line = line.substring( query_num.length() ).trim();
            if (remoteQuery.length() > 0) line = remoteQuery;
            String queryStr = line;
            queryStr = QueryParser.escape(queryStr);
            QueryParser parser = new QueryParser(Version.LUCENE_48, Defs.FLD_TEXT, analyzer);
            Query query = parser.parse(queryStr);
            QueryTermVector queryTermVector = new QueryTermVector( line, analyzer );
            String[] terms = queryTermVector.getTerms();
            similarity = (TFIDFSimilarity) searcher.getSimilarity();

            TopDocs hits = searcher.search( query, hitsCount );
            System.out.println( "query" + " : " + query.toString() );
            System.out.println( hits.totalHits + " total matching documents" );

            // Query Expansion with Rocchio algorithm
            if ( queryExpansionFlag.equals( QueryExpansion.ROCCHIO_METHOD ) )
            {
                QueryExpansion queryExpansion;
                queryExpansion = new QueryExpansion( analyzer, searcher, similarity, properties );
                query = queryExpansion.expandQuery( queryStr, hits, properties );
                expandedQuery = query.toString("contents");
                System.out.println( "Expanded Query: " + query );
                hits = searcher.search( query, hitsCount );
                Vector<TermQuery> expandedQueryTerms = queryExpansion.getExpandedTerms();
                generateOutput( hits, expandedQueryTerms, query_num, writer, termCount, outCount, searcher, similarity, idxReader );
            }

            writer.flush();
            if (remoteQuery.length() > 0) break; // onetime call.
        }
        writer.close();
    }

    /**
     * Generates necessary output - in this case this output is used as input to matlab
     * @param hits
     * @param terms
     * @param query_num - tag of the query
     * @param writer
     * @param termCount
     * @param outCount
     * @param idxReader 
     * @param similarity 
     * @param searcher 
     * @throws IOException 
     */
    private static void generateOutput( TopDocs hits, Vector<TermQuery> terms, String query_num, BufferedWriter writer, int termCount, int outCount, IndexSearcher searcher, TFIDFSimilarity similarity,
            IndexReader idxReader ) throws IOException
    {
        logger.finer( "terms.size(): " + terms.size() );
        // Generate Output
        // For each doc
        for ( int i = 0; ((i < hits.scoreDocs.length) && (i < outCount)); i++ )
        {
            Document doc = searcher.doc(hits.scoreDocs[i].doc);
            String docno = ((Field) doc.getField( "DOCNO" )).stringValue();
            int docId = hits.scoreDocs[i].doc;
            float coord = Utils.coord( terms, doc, docId, similarity, idxReader );

            writer.write( query_num + " " + "Q0" + " " + docno + " " + (i + 1) + " " + hits.scoreDocs[i].score + " " + coord );
            // For each term output normalized: tf, idf, boostFactor
            for ( int j = 0; j < termCount; j++ )
            {
                if ( j < terms.size() )
                {
                    TermQuery termQuery = terms.elementAt( j );
                    Term term = termQuery.getTerm();
                    String termStr = term.text();
                    float tf = Utils.getTFNorm( termStr, doc, docId, similarity, idxReader, true );
                    float idf = Utils.getIDFNorm( termStr, terms, idxReader, similarity, true );
                    float boost = Utils.getBoostNorm( termQuery, terms );
                    writer.write( " " + tf + " " + idf + " " + boost + " " );
                }
                // If not enough terms pad with 0's
                else
                {
                    writer.write( " 0 0 0 " );
                }
            }
            writer.write( "\n" );
        }
    }
}
