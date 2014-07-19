package in.student.project.util;

import java.io.*;
import java.util.*;

import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

public class Utils
{
    // Values used in caching functions
    // Stores cached maxIdf
    public static float maxIdf = -1;
    // Caches idf values
    public static Hashtable<String, Float> idfTbl = null;
    public static Vector<TermQuery> terms = null;
    public static Document doc = null;
    public static int docTermCount = 0;
    

    /**
     * score(q,d) =?(t in q) tf(t in d) * idf(t) * getBoost(t.field in d) * lengthNorm(t.field in d) * coord(q,d) * queryNorm(q)
     * @param term
     * @param similarity
     * @return
     */    
    public static float scoreTerm( Document doc, String termStr, int docId, TFIDFSimilarity similarity, IndexReader idxReader, IndexSearcher searcher ) 
    throws IOException
    {
        Term term = new Term( Defs.FLD_TEXT, termStr );
        // tf(t in d) * idf(t) * getBoost(t.field in d) * lengthNorm(t.field in d) * coord(q,d) * queryNorm(q)
        // tf(t in d)    
        float tf = getTF(term.text(), docId, idxReader );
        // idf(t)
        float idf = similarity.idf( idxReader.docFreq(term), idxReader.numDocs() );
        // getBoost(t.field in d)
        float boost = new Float( "1.0").floatValue();
        // lengthNorm(t.field in d)
        float lengthNorm = getLengthNorm( doc, (DefaultSimilarity) similarity );
        // coord(q,d)
        float coord = new Float( "1.0").floatValue();        
        // queryNorm(q)        
        float queryNorm = new Float( "1.0").floatValue();      
        // tf(t in d) * idf(t) * getBoost(t.field in d) * lengthNorm(t.field in d) * coord(q,d) * queryNorm(q)
        float score = tf * idf * boost * lengthNorm * coord * queryNorm;
        
        return score;
    }
    
    public static float getIDF( String termStr, IndexReader reader, TFIDFSimilarity similarity )
    throws IOException
    {
        Term term = new Term( Defs.FLD_TEXT, termStr );
        float idf = similarity.idf( reader.docFreq(term), reader.numDocs() );
        return idf;
    }

    
    public static float getIDFNorm( String termStr, Vector<TermQuery> terms,
    		IndexReader searcher, TFIDFSimilarity similarity )
            throws IOException
    {
        return getIDFNorm( termStr, terms, searcher, similarity, false );
    }    

    /**
     * 
     * @param termStr
     * @param terms
     * @param searcher
     * @param similarity
     * @param cache - indicates if values will be cached
     * @return
     * @throws IOException
     */
    public static float getIDFNorm( String termStr, Vector<TermQuery> terms, IndexReader idxReader, TFIDFSimilarity similarity, boolean cache )
    throws IOException
    {
        // get maxIDF
        // if cache and terms are equal get a cached value
        // else find maxIdf
        float maxIdf = 0;
        if ( cache && terms.equals( Utils.terms ) )
        {
            maxIdf = Utils.maxIdf;
        }
        else
        {
            maxIdf = getMaxIDF(terms, idxReader, similarity);
            // Cache the value
            Utils.maxIdf = maxIdf;
            Utils.terms = terms;
        }
                
        // Normalize
        Term term = new Term( Defs.FLD_TEXT, termStr );
        float idf = similarity.idf( idxReader.docFreq(term), idxReader.numDocs() );
        float idfNorm = idf / maxIdf;
        
        return idfNorm;
    }
    
    
    private static float getMaxIDF( Vector<TermQuery> terms, IndexReader idxReader, TFIDFSimilarity similarity ) throws IOException
    {
        float maxIdf = 0;

        for ( int i = 0; i < terms.size(); i++ )
        {
            Term term = terms.elementAt( i ).getTerm();
            float idf = similarity.idf( idxReader.docFreq(term), idxReader.numDocs() );
            if ( maxIdf < idf )
            {
                maxIdf = idf;
            }
        }
        return maxIdf;
    }
    

    /**
     * Use similarity class instead
     */
    private static float getLengthNorm( Document doc, DefaultSimilarity similarity )
    {
    	IndexableField fld = doc.getField( Defs.FLD_TEXT );
        int numTokens = new StringTokenizer( fld.stringValue() ).countTokens();
        return similarity.lengthNorm( new FieldInvertState( Defs.FLD_TEXT ));

//        return similarity.lengthNorm( Defs.FLD_TEXT, numTokens );
    }

    
    /**
     *
     * @return tf * lengthNorm = [0;1]
     */
    public static float getTFNorm( String termStr, Document doc, int docId, TFIDFSimilarity similarity, IndexReader idxReader, boolean cache ) 
    throws IOException
    {
        float tf = getTF(termStr, docId, idxReader );
        //System.out.print( tf + " : " );
        // Normalize with similarity
        tf = similarity.tf( tf );
        //System.out.print( tf + " : " );        
        // Length Normazliation
        int docTermCount = getDocTermCount( doc, cache );
        FieldInvertState invertState = new FieldInvertState(Defs.FLD_TEXT);
        invertState.setLength(docTermCount);
        
        tf = tf * similarity.lengthNorm(invertState);
        //System.out.println( tf + "(" + docTermCount + ")" );        
        return tf;
    }
    
    public static int getDocTermCount( Document doc )
    {
        return getDocTermCount( doc, false );
    }
    
    public static int getDocTermCount( Document doc, boolean cache )
    {
        int docTermCount; 

        if ( cache )
        {
            if ( doc.equals( Utils.doc ) )
            {
                return Utils.docTermCount;
            }
            // cache is empty
            else
            {
                // Calculate docTermCount
                docTermCount = getDocTermCount( doc, false );
                // cache values
                Utils.doc = doc;
                Utils.docTermCount = docTermCount;
                return docTermCount;
            }
        }
        else
        {
            StringBuffer strb = new StringBuffer();
            String[] txt = doc.getValues( Defs.FLD_TEXT );
            for ( int i = 0; i < txt.length; i++ )
            {
                strb.append( txt[i] );
            }
            StringTokenizer tknzr = new StringTokenizer( strb.toString() );
            docTermCount = tknzr.countTokens();
            return docTermCount;
        }
    }
    
    
    
    public static float getTF( String term, int docId, IndexReader idxReader ) throws IOException, NullPointerException
    {
        // tf(t in d)
        Terms termFreqVector = idxReader.getTermVector( docId, Defs.FLD_TEXT );
        String[] terms = new String[0];
        int freqs[];
        ArrayList<String> temp1 = new ArrayList<String>();
        ArrayList<Integer> temp2 = new ArrayList<Integer>();
        TermsEnum termsEnum1 = termFreqVector.iterator(null);
        BytesRef term1 = null;
		while((term1 = termsEnum1.next()) != null) {
			temp1.add(termsEnum1.term().utf8ToString());
			temp2.add((int)termsEnum1.totalTermFreq());
		}
		terms = temp1.toArray( terms );

		int[] ret = new int[temp2.size()];
	    int i = 0;
	    for (Integer e : temp2)  
	        ret[i++] = e.intValue();
	    freqs = ret;

        boolean found = false;
        float tf = 0;
        for ( i = 0; i < terms.length && !found; i++ )
        {
            if ( term.equals( terms[i] ) )
            {
                tf = freqs[i];
                found = true;                
            }
        }
        return tf;
    }

    public static float coord( Vector<TermQuery> terms, Document doc, int docId, Similarity similarity, IndexReader idxReader ) 
    throws IOException
    {
        int maxOverlap = terms.size();
        int overlap = 0;
        // Calculate overlap (terms w/ freq > 0
        for ( int i = 0; i < terms.size(); i++ )
        {
        	float tf = 0;
        	try {
        		tf = getTF( terms.elementAt(i).getTerm().text(), docId, idxReader );
        	} catch (Exception e) { continue; }
            if ( tf > 0 )
            {
                overlap++;
            }
        }        
        
        float coord = similarity.coord(overlap, maxOverlap);
        //System.out.println( overlap + " : " + maxOverlap + " : " + coord  );
        return coord;
    }

    
    /**
     * Returns normalized boost factor
     * @param termQuery
     * @param terms
     * @return
     */
    public static float getBoostNorm( TermQuery termQuery, Vector<TermQuery> terms )
    {
        float max = 0;
        
        // Find max
        Iterator<TermQuery> itr = terms.iterator();
        while ( itr.hasNext() )
        {
            TermQuery tq = itr.next();
            float boost = tq.getBoost();
            if ( boost > max )
            {
                max = boost;
            }
        }
        
        // Normalize
        float boost = termQuery.getBoost() / max;
        
        return boost;
    }
    
    
}
