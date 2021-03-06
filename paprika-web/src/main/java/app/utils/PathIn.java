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
	
    /**
     * Used per velocity and the Main
     * 
     * @author guillaume
     *
     */
	@SuppressWarnings("javadoc")
    public static class Web {
  
         public static final String INDEX = "/paprika/index/";
         public static final String LOGIN = "/paprika/login/";
         public static final String LOGOUT = "/paprika/logout/";
         public static final String SIGNUP = "/paprika/signUp/";
         public static final String FORMDEL = "/paprika/form_delete/";
         public static final String VERSION = "/paprika/version/";
         public static final String ENACC="/paprika/enableAccount/";
         public static final String RESETSEND="/paprika/reset/send/";
         public static final String RESETRECEIVE="/paprika/reset/change/";

  	private Web(){
    		
    	}
    }

    /**
     * Used per the Main
     * 
     * @author guillaume
     *
     */
    @SuppressWarnings("javadoc")
    public static class Template {
   
    
		public static final String INDEX = "/velocity/pages/index.vm";
        public static final String FORM_DELETE = "/velocity/forms/form_delete.vm";
        public static final String LOGIN = "/velocity/pages/login.vm";
        public static final String SIGNUP = "/velocity/pages/signUp.vm";
        public static final String VERSION = "/velocity/pages/version.vm";
        public static final String ENACC="/velocity/pages/enableAccount.vm";
        public static final String RESET="/velocity/pages/reset.vm";
 	private Template(){
    		
    	}
    }

}
