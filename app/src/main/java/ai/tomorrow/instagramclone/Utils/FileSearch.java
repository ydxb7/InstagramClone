package ai.tomorrow.instagramclone.Utils;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class FileSearch {
    private static final String TAG = "FileSearch";
    /**
     * Search a directory and return a list of all **directories** contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPaths(String directory){
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();

        for (int i = 0; i < listFiles.length; i++){
            if (listFiles[i].isDirectory()){
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }


    /**
     * Search a directory and return a list of all **files** contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePaths(String directory){
        Log.d(TAG, "getFilePaths: " + directory);
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        if (listFiles == null){
            listFiles = new File[]{};
        }

        for (int i = 0; i < listFiles.length; i++){
            if (listFiles[i].isFile()){
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }


}
