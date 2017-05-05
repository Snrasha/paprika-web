package app.utils;

/**
 * 
 * This file have be taken of the example of spark-basic-structure:
 * https://github.com/tipsy/spark-basic-structure
 *
 */
public class PathIn {


	  private PathIn() {
		    throw new IllegalAccessError("Utility class");
		  }
	
    public static class Web {
  
         public static final String INDEX = "/index/";
         public static final String LOGIN = "/login/";
         public static final String LOGOUT = "/logout/";
         public static final String SIGNUP = "/signUp/";
         public static final String FORM = "/form/";
         public static final String FORMDEL = "/form_delete/";
         public static final String VERSION = "/version/";

  	private Web(){
    		
    	}
    }

    public static class Template {
   
        public static final String INDEX = "/velocity/pages/index.vm";
        public static final String FORM_APP = "/velocity/forms/form_app.vm";
        public static final String FORM_TAB = "/velocity/forms/form_tab.vm";
        public static final String FORM_DELETE = "/velocity/forms/form_delete.vm";
        public static final String LOGIN = "/velocity/pages/login.vm";
        public static final String SIGNUP = "/velocity/pages/signUp.vm";
        public static final String VERSION = "/velocity/pages/version.vm";
        
 	private Template(){
    		
    	}
    }

}
