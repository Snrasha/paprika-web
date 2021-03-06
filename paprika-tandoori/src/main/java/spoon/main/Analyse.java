package spoon.main;

import java.io.IOException;

import org.neo4j.cypher.CypherException;

import tandoori.analyzer.GraphCreator;
import tandoori.entities.PaprikaApp;
import tandoori.entities.PaprikaLibrary;
import tandoori.metrics.MetricsCalculator;
import tandoori.neo4jBolt.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;


/**
 * 
 *
 * 
 * @author guillaume
 *
 */
public class Analyse {

	/**
	 * Copy of the runAnalysis of Tandoori, but modified.
	 * 
	 * @param args
	 * @return the PaprikaApp needed for the runQuery.
	 */
	public PaprikaApp runAnalysis(String[] args) {
	        System.out.println("Collecting metrics");
	        String input = args[0];
	        String output =  args[1];
	        String name =  args[2];
	        String sdkPath =  args[3];
	        String key =  args[4];
	        MainProcessorBolt mainProcessor = new MainProcessorBolt(name,"1.0", key, input,output, sdkPath);
	        mainProcessor.process();
	        System.out.println("process Mainprocessor done");
	        GraphCreator graphCreator = new GraphCreator(MainProcessorBolt.currentApp);
	        graphCreator.createClassHierarchy();
	        System.out.println(" createClassHierarchy done");
	        graphCreator.createCallGraph();
	        System.out.println(" createCallGraph done");
	        MetricsCalculator.calculateAppMetrics(MainProcessorBolt.currentApp);
	        System.out.println("Done");
	        
	        return MainProcessorBolt.currentApp;
	    }


	/**
	 * runQueryMode modified of the Tandoori.
	 * 
	 * @param args
	 */
	public  void runQueryMode(String[] args)  {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("paprika");
		Subparsers subparsers = parser.addSubparsers().dest("sub_command");

		Subparser queryParser = subparsers.addParser("query").help("Query the database");
		queryParser.addArgument("-k", "--key").required(true)
				.help("The application key for found the file on the database");
		queryParser.addArgument("-r", "--request").help("Request to execute");
		queryParser.addArgument("-dk", "--delKey").help("key to delete");
		queryParser.addArgument("-dp", "--delPackage").help("Package of the applications to delete");
		queryParser.addArgument("-d", "--details").type(Boolean.class).setDefault(false)
				.help("Show the concerned entity in the results");
		try {
			Namespace res = parser.parseArgs(args);
			this.queryMode(res);
		} catch (ArgumentParserException e) {
		} catch (CypherException e){
		}catch (IOException e){
		}
	}


    /**
     * Tandoori copy.
     * @param paprikaApp
     * @param libraryString
     */
    public static void addLibrary(PaprikaApp paprikaApp, String libraryString){
        PaprikaLibrary.createPaprikaLibrary(libraryString,paprikaApp);
    }

	private void launchStats(QueryEngineBolt queryEngine) throws IOException{
		QuartileCalculator quartileCalculator = new QuartileCalculator(queryEngine);
		quartileCalculator.calculateClassComplexityQuartile();
		quartileCalculator.calculateLackofCohesionInMethodsQuartile();
		quartileCalculator.calculateNumberOfAttributesQuartile();
		quartileCalculator.calculateNumberOfImplementedInterfacesQuartile();
		quartileCalculator.calculateNumberOfMethodsQuartile();
		quartileCalculator.calculateNumberofInstructionsQuartile();
		quartileCalculator.calculateCyclomaticComplexityQuartile();
		quartileCalculator.calculateNumberOfMethodsForInterfacesQuartile();
	}

	private void launchALLINFO(QueryEngineBolt queryEngine)throws IOException{
		queryEngine.getAllLCOM();
		queryEngine.getAllCyclomaticComplexity();
		queryEngine.getAllClassComplexity();
		queryEngine.getAllNumberOfMethods();
		queryEngine.countVariables();
		queryEngine.countInnerClasses();
		queryEngine.countAsyncClasses();
		queryEngine.countViews();
	}
	private void launchALLAP(QueryEngineBolt queryEngine,	Boolean details)throws IOException {
		ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
		CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
		LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
		SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
		BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
		MIMQuery.createMIMQuery(queryEngine).execute(details);
		IGSQuery.createIGSQuery(queryEngine).execute(details);
		LICQuery.createLICQuery(queryEngine).execute(details);
		NLMRQuery.createNLMRQuery(queryEngine).execute(details);
		OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
		HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
		HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
		HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
		UnsuitedLRUCacheSizeQuery.createUnsuitedLRUCacheSizeQuery(queryEngine).execute(details);
		InitOnDrawQuery.createInitOnDrawQuery(queryEngine).execute(details);
		UnsupportedHardwareAccelerationQuery.createUnsupportedHardwareAccelerationQuery(queryEngine)
				.execute(details);
		HashMapUsageQuery.createHashMapUsageQuery(queryEngine).execute(details);
		InvalidateWithoutRectQuery.createInvalidateWithoutRectQuery(queryEngine).execute(details);
		TrackingHardwareIdQuery.createTrackingHardwareIdQuery(queryEngine).execute(details);
	}
	
	private void launchFORCENOFUZZY(QueryEngineBolt queryEngine,Boolean details)throws IOException {
		CCQuery.createCCQuery(queryEngine).execute(details);
		LMQuery.createLMQuery(queryEngine).execute(details);
		SAKQuery.createSAKQuery(queryEngine).execute(details);
		BLOBQuery.createBLOBQuery(queryEngine).execute(details);
		HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).execute(details);
		HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).execute(details);
		HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).execute(details);
	}
	
	/**
	 * Only the ALLAP is used.
	 * @param arg
	 * @throws IOException
	 */
	public void queryMode(Namespace arg) throws IOException {
		QueryEngineBoltModified queryEngine = new QueryEngineBoltModified(Long.parseLong(arg.getString("key")));
		String request = arg.get("request");
		Boolean details = true;
		switch (request) {
		case "STATS":
			launchStats(queryEngine);
			break;
		case "ALLINFO":
			launchALLINFO(queryEngine);
			break;
		case "ALLAP":
			launchALLAP(queryEngine,details);
			break;
		case "FORCENOFUZZY":
			launchFORCENOFUZZY(queryEngine,details);
			break;
		default:
			queryEngine.executeRequest(request);
		}
	}
}
