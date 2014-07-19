package in.student.project.queryexpansion;

import java.util.logging.Logger;

import junit.framework.TestCase;

public class SearchFilesTest extends TestCase
{

    private static Logger logger = Logger.getLogger( "SearchFilesTest" );    
    
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( SearchFilesTest.class );
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public SearchFilesTest( String arg0 )
    {
        super( arg0 );
    }

    public void testMain() throws Exception
    {
        String[] args = {"search.prop"};
//        SearchFilesRocchio.main( args );
        SearchFilesLDA.main( args );       
    }

}
