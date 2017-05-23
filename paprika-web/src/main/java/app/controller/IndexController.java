package app.controller;

import spark.*;


import java.io.IOException;

import java.util.*;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.model.Application;
import app.utils.PaprikaKeyWords;
import app.utils.PathIn;
import app.utils.RequestUtil;
import app.utils.ViewUtil;


public class IndexController {


	/**
	 * First index, who have the form.
	 */
	public static final Route serveIndexPage = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		return ViewUtil.render(request, model, PathIn.Template.INDEX);
	};

	/**
	 * Cette fonction récupère les informations de deux formulaires, le menu des
	 * applications et l'upload de versions. Le formulaire qui s'occupe des
	 * paramètres des versions ne se trouvent pas dans l'index, le reste des
	 * paramètres sera déduit de la version.
	 * 
	 * le second formulaire, pour l'instant, prend le nom du fichier et vire le
	 * .apk de fin
	 */
	public static final Route handleIndexaddApp = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		PaprikaFacade facade= PaprikaFacade.getInstance();
		Application application=RequestUtil.getSessionApplication(request);
		// Formulaire quand on ajoute un project.
		String project = request.queryParams("project");
		String menu = RequestUtil.getParamMenu(request);
		if (project != null) {
			long idProject=facade.addProject(RequestUtil.getSessionUser(request), project);
			if(idProject!=-1)
			request.session().attribute(PaprikaKeyWords.APPLICATION, facade.application(idProject));
		}
		// Formulaire quand on choisit le menu.
		else if (menu != null) {
			request.session().attribute(PaprikaKeyWords.APPLICATION,facade.application(Long.parseLong(menu)));
		}
		// Formulaire quand on upload un fichier.
		else if (menu == null && project == null) {
			boolean fileadded=addFile(request,application,facade);
			if(fileadded)	{
				model.put(PaprikaKeyWords.APPLICATION, application);
			}
		}
		return ViewUtil.render(request, model, PathIn.Template.INDEX);
	};
	
	  private IndexController() {
		    throw new IllegalAccessError("Controller class");
		  }

	  
	 private static boolean addFile(Request request,Application application,PaprikaFacade facade) throws IOException, ServletException{
			
			String location="/application";
			
			long maxFileSize = 100000000;
			long maxRequestSize = 100000000;
			int fileSizeThreshold = 1024;
			MultipartConfigElement multipartConfigElement = new MultipartConfigElement(location, maxFileSize,
					maxRequestSize, fileSizeThreshold);
			request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
			Part uploadedFile = request.raw().getPart("appAndroid");
			if (uploadedFile != null) {
				String fName = request.raw().getPart("appAndroid").getSubmittedFileName();
				int lastindex=fName.lastIndexOf('.');
				if(lastindex == -1) {
					PaprikaWebMain.LOGGER.error("The file have not format!");
					return false;
				}
				
				String realname = fName.substring(0, lastindex);
				String format = fName.substring(lastindex, fName.length());
				if (!".apk".equals(format)) {
					PaprikaWebMain.LOGGER.error("The file is not a .apk file! We have: "+format);
				}
				else {
					
					PaprikaWebMain.LOGGER.trace(realname);
					facade.addFile( RequestUtil.getSessionCurrentUser(request),application,fName,uploadedFile,realname);

					return true;
				}
			}
			return false;
	 }
}
