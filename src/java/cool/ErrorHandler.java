package cool;

public class ErrorHandler {
    
	private static boolean errorFlag = false;
	public static void reportError(String filename, int lineNo, String error){
		errorFlag = true;
		System.err.println(filename+":"+lineNo+": "+error);
	}
	public static boolean getErrorFlag(){
		return errorFlag;
	}

}
