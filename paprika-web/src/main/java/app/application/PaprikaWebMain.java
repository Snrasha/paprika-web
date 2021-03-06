package app.application;

import static spark.Spark.*;
import static spark.debug.DebugScreen.*;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

import app.controller.EnableAccountController;
import app.controller.FormController;
import app.controller.IndexController;
import app.controller.LoginController;
import app.controller.SignUpController;
import app.controller.VersionController;
import app.functions.DescriptionFunctions;
import app.functions.DataSaveFunctions;
import app.utils.PathIn;
import spark.Spark;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * PaprikaWebMain is the main class of paprika-web
 * http://sparkjava.com/documentation#cookies
 * https://docs.oracle.com/cd/E19509-01/820-3503/ggfen/index.html
 * https://docs.oracle.com/cd/E19509-01/820-3503/ggfen/
 * Keystore.jks : keytool -genkeypair -keystore keystore2.jks -alias toto -keyalg RSA -keysize 2048 -dname "CN=toto"
 * 
 * 
 * For contact me: snrasha@gmail.com
 * 
 * @author guillaume
 * 
 */
public class PaprikaWebMain {
	/**
	 * The logger of Paprika-web
	 */
	public static final Logger LOGGER = LogManager.getLogger();

	/**
	 * If True, enable all captcha and the https.
	 */
	public static final boolean ENABLEALLSECURITY = true;
	private static int versionOnAnalyze = 0;
	private static LinkedBlockingQueue<String[]> containerQueue;
	private static Timer timer;

	/**
	 * Pour se connecter à neo4J, on utilise une authentification en dur,
	 * utilisateur: neo4j pass: paprika
	 */
	private static Driver driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687",
			AuthTokens.basic("neo4j", getKeyNeo4j()));

	private PaprikaWebMain() {

	}

	/**
	 * Prend le nom du container neo4j-praprika et renvoie son adresse.
	 * 
	 * @return
	 */
	private static String getHostName() {
		try {
			String str = InetAddress.getByName("neo4j-paprika").getHostAddress();
			PaprikaWebMain.LOGGER.trace(str);
			return str;
		} catch (final Exception e) {
			PaprikaWebMain.LOGGER.trace("Host of InetAddress 'neo4j-paprika' not found", e);
			return "localhost";
		}
	}

	/**
	 * Return the neo4j key
	 * 
	 * @return
	 */
	private static String getKeyNeo4j() {

		try {
		InputStream is;
		is = new FileInputStream("./info.json");

		String jsonTxt;
		
				jsonTxt = IOUtils.toString(is);
	
		
		JSONObject json = new JSONObject(jsonTxt);

		return json.getString("neo4j_pwd");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";

	}
	/**
	 * Create a new session, if the driver is closed, he re-open the driver.
	 * 
	 * @return a new Session.
	 */
	public static Session getSession() {
		Session session = null;

		try {
			session = driver.session();
			LOGGER.trace("Open a new session.");
		} catch (ServiceUnavailableException e) {
			LOGGER.error("Driver problem, we re-open a driver.", e);
			driver.close();

			driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687", AuthTokens.basic("neo4j", getKeyNeo4j()));
			session = driver.session();
	
		}
		return session;
	}

	/**
	 * We save data on Neo4j, for relaunch after without problem. Do not save
	 * the queue, just container who are running.
	 */
	private static void loadSave() {

		if (containerQueue == null) {
			// The number of analyze who can be run on same time.
			String[] containerRun = new String[2];
			StringBuilder command = new StringBuilder("MATCH(n:DataSave) return ");
			for (int i = 0; i < containerRun.length; i++) {
				command.append("n.containerRun" + i + ",");
			}

			containerRun = new DataSaveFunctions().searchContainer(containerRun);
			// The number of Analyze who can be put on the queue.
			containerQueue = new LinkedBlockingQueue<>(3);
			timer = new Timer();

			PaprikaTimer task = new PaprikaTimer(containerRun);
			// think to 2 Minutes in reality.
			timer.schedule(task, 0, 60000); // 1000= 1seconde
		}
	}

	/**
	 * The main who launch all methods spark.
	 * 
	 * @param args
	 *            this main do not have argument
	 */
	public static void main(String[] args) {

		new DescriptionFunctions().addAllClassicDescription();
		loadSave();

		if (!PaprikaWebMain.ENABLEALLSECURITY)
			port(80);
		else
			port(443);
		enableDebugScreen();
		Spark.staticFileLocation("/public");
		if (PaprikaWebMain.ENABLEALLSECURITY)
			try {
				InputStream is;
				is = new FileInputStream("./info.json");

				String jsonTxt;
				jsonTxt = IOUtils.toString(is);
				JSONObject json = new JSONObject(jsonTxt);

				String keyStorePassword = json.getString("keystore_key");
			
				if (keyStorePassword != null) {
					String keyStoreLocation = "./clientkeystore.jks";
					secure(keyStoreLocation, keyStorePassword, null, null);

				}
			} catch (IOException e) {
				System.out.println("Not success");
				return;
			}
	
		// La page d'index.
		get("/paprika", IndexController.resetProjectIndexPage);
		get("/paprika/", IndexController.resetProjectIndexPage);
		get("/paprika/index", IndexController.resetProjectIndexPage);
		get(PathIn.Web.INDEX, IndexController.serveIndexPage);
		// La page de login, quand tu veux te connecter.
		get(PathIn.Web.LOGIN, LoginController.serveLoginPage);
		// La page d'index avec la demande de logout.
		get(PathIn.Web.LOGOUT, LoginController.handleLogoutPost);
		// La page d'inscription, quand tu veux t'inscrire.
		get(PathIn.Web.SIGNUP, SignUpController.serveSignUpPage);
		// Partie SETTINGs
		get(PathIn.Web.FORMDEL, FormController.serveFormDELPage);

		// Mis sur indexController car il est basé sur l'index
		get(PathIn.Web.VERSION, VersionController.serveVersionPage);

		get(PathIn.Web.ENACC, EnableAccountController.servePage);
		get("/paprika/reset/", FormController.serveFormResetSendPage);
		get(PathIn.Web.RESETSEND, FormController.serveFormResetSendPage);
		get(PathIn.Web.RESETRECEIVE, FormController.serveFormResetReceivePage);

		// get(PathIn.Web.ZIP, (request, response) -> getFile(request,
		// response));

		/*
		 * Reçoit les données du formulaire de login et renvoie à l'index.
		 * Demande à la page login que le formulaire envoie sur /index/,
		 * handleloginpost attrape alors la requête en passant.
		 * 
		 */
		post(PathIn.Web.INDEX, IndexController.handleIndexaddApp);
		post(PathIn.Web.VERSION, VersionController.handleVersionPost);

		post(PathIn.Web.LOGIN, LoginController.handleLoginPost);

		// Reçoit les données du formulaire de signup et renvoie à l'index.
		post(PathIn.Web.SIGNUP, SignUpController.handleSignUpPost);

		post(PathIn.Web.FORMDEL, FormController.handleFormDeletePost);

		post(PathIn.Web.ENACC, EnableAccountController.handlePost);
		post(PathIn.Web.RESETSEND, FormController.handleFormResetPost);
		post(PathIn.Web.RESETRECEIVE, FormController.handleFormResetPost);

	}

	/**
	 * The queue for know if we can run a new container.
	 * 
	 * @return the containerQueue
	 */
	public static LinkedBlockingQueue<String[]> getContainerqueue() {
		return containerQueue;
	}

	/**
	 * Return the number of version who is on analyze currently.
	 * 
	 * @return a number of active analyze
	 */
	public synchronized static int getVersionOnAnalyze() {
		return versionOnAnalyze;
	}

	/**
	 * Add the value on the versionOnAnalyze
	 * 
	 * @param value
	 */
	public synchronized static void addVersionOnAnalyze(int value) {
		PaprikaWebMain.versionOnAnalyze += value;
	}

}