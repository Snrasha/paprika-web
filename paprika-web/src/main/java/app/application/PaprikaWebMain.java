package app.application;

import static spark.Spark.*;
import static spark.debug.DebugScreen.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

import app.controller.FormController;
import app.controller.IndexController;
import app.controller.LoginController;
import app.controller.SignUpController;
import app.controller.VersionController;
import app.functions.DescriptionFunctions;
import app.utils.PathIn;
import spark.Spark;
import java.net.InetAddress;

public class PaprikaWebMain {

	// "bolt://localhost:7687" quand on n'utilise pas docker.
	public static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Pour se connecter à neo4J, on utilise une authentification en dur, 
	 * utilisateur: neo4j
	 * pass: paprika
	 */
	private static  Driver driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687",
			AuthTokens.basic("neo4j", "paprika"));

	private PaprikaWebMain() {

	}
	
	
	/**
	 * Prend le nom du container neo4j-praprika et renvoie son adresse.
	 * 
	 * @return
	 */
	private static String getHostName() {
		try {
			String str= InetAddress.getByName("neo4j-paprika").getHostAddress();
			PaprikaWebMain.LOGGER.trace(str);
			return str;
		} catch (final Exception e) {
			PaprikaWebMain.LOGGER.trace("Host of InetAddress 'neo4j-paprika' not found",e);
			return "localhost";
		}
	}


	/**
	 * Créer une nouvelle session ou si le driver est fermé, réouvre le driver avant.
	 * @return
	 */
	public static Session getSession(){
		Session session=null;

		try{
		 session =driver.session();
		 LOGGER.trace("Open a new session.");
		}
		catch(ServiceUnavailableException e){
			LOGGER.error("Driver problem, we re-open a driver.",e);
			driver.close();
			driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687",
					AuthTokens.basic("neo4j", "paprika"));
			 session =driver.session();
		}
		return session;
	}
	public static void main(String[] args) {

		
		new DescriptionFunctions().addAllClassicDescription();
		

		// Open the port 4567 for create a localhost server.
		port(4567);

		// Enable the debugscreen for know why we have this error, need be
		// optional on the futur
		enableDebugScreen();

		// Request of Spark for know where are the css or img in the ressources
		// of the project
		Spark.staticFileLocation("/public");

		// La page d'index.
		get("", IndexController.serveIndexPage);
		get("/", IndexController.serveIndexPage);
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

	}
}