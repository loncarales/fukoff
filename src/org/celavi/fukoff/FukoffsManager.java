package org.celavi.fukoff;

import java.io.File;
import java.util.ArrayList;
import android.os.Environment;
//import android.util.Log;

public class FukoffsManager {
    /** Tag for logging */
 //   private static final String CLASSTAG = FukoffsManager.class.getSimpleName();

    /** ArrayList for directory content */
    private ArrayList<String> dir_content;
    /** Path for saving Fukoffs */
    private File path;

//    private static final int BUFFER = 2048;
//    private double dir_size = 0;

    /**
    * Constructs an object of the class
    * this class uses a stack to handle the navigation of directories.
    */
    public FukoffsManager() {
        dir_content = new ArrayList<String>();

        File sdCard = Environment.getExternalStorageDirectory();
        path = new File(sdCard + "/MyFukoffs");
    }

    /**
    * This will return a string of the current directory path
    * @return the current directory
    */
    public String getCurrentDir() {
        return path.getAbsolutePath();
    }

    /**
     *  Check if input parameter is a directory
     *
     * @param name
     * @return bool
     */
    public boolean isDirectory(String name) {
        return new File(path.getAbsolutePath() + "/" + name).isDirectory();
    }

    /**
     * The full path name of the file to delete.
     *
     * @param path name
     * @return
     */
    public int deleteTarget(String path) {
    	File target = new File(path);

    	if(target.exists() && target.isFile() && target.canWrite()) {
    		target.delete();
    		return 0;
    	}
    	return -1;
	}

    /**
    *
    * @param filePath
    * @param newName
    * @return
    */
    public int renameTarget(String filePath, String newName) {
    	File src = new File(filePath);
    	String ext = "";
    	File dest;

    	if(src.isFile())
    		/*get file extension*/
    		ext = filePath.substring(filePath.lastIndexOf("."), filePath.length());

    	if(newName.length() < 1)
    		return -1;

    	String temp = filePath.substring(0, filePath.lastIndexOf("/"));

    	dest = new File(temp + "/" + newName + ext);
    	if(src.renameTo(dest))
    		return 0;
    	else
    		return -1;
	}

    /**
     *
     * @return
     */
    public ArrayList<String> getFukoffsDir() {
        return populate_list();
    }

    /**
     * List Files that are in fukoffs folder. Since this function is called every time we need
     * to update the the list of files to be shown to the user, this is where
     * we do our sorting (by type, alphabetical, etc). // not implemented
     * @return
     */
    private ArrayList<String> populate_list() {
        if(!dir_content.isEmpty())
            dir_content.clear();

        if(path.exists() && path.canRead()) {
            String[] list = path.list();
            int len = list.length;

            /* add files to array list but omit hidden files / folders */
            for (int i = 0; i < len; i++) {
                if( (list[i].toString().charAt(0) != '.') && (!new File(path + "/" + list[i].toString()).isDirectory()) )  {
                    dir_content.add(list[i]);
                }
            }
        } else {
            dir_content.add("Empty");
        }

        return dir_content;
    }
}
