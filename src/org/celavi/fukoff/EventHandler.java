package org.celavi.fukoff;

import java.io.File;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
//import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EventHandler implements OnClickListener {
    /** Tag for logging */
    private static final String CLASSTAG = EventHandler.class.getSimpleName();

    private final Context context;
    private final FukoffsManager fukoffs_mg;
    private TableRow delegate;

    //private int color = Color.WHITE;

    private static final int DELETE_TYPE = 0x00;

    // the list used to feed info into the array adapter
    private ArrayList<String> data_source;

    /**
    * Creates an EventHandler object. This object is used to communicate
    * most work from the PlayShare activity to the FukoffsManager class.
    *
    * @param context The context of the main activity e.g Main
    * @param manager The FukoffsManager object that was instantiated from Main
    */
    public EventHandler(Context context, final FukoffsManager manager) {
        this.context = context;
        fukoffs_mg = manager;

        data_source = new ArrayList<String>(fukoffs_mg.getFukoffsDir());
        Log.v(Constants.LOGTAG, " " + EventHandler.CLASSTAG + " data_source: " + data_source);
    }

    /**
    * This method is called from the PlayShare activity and this has the same
    * reference to the same object so when changes are made here or there
    * they will display in the same way.
    *
    * @param adapter The TableRow object
    */
    public void setListAdapter(TableRow adapter) {
        delegate = adapter;
    }

    /**
    * will return the data in the ArrayList that holds the dir contents.
    *
    * @param position the indext of the arraylist holding the dir content
    * @return the data in the arraylist at position (position)
    */
    public String getData(int position) {
        if(position > data_source.size() - 1 || position < 0)
            return null;

        return data_source.get(position);
    }
    /**
    * Will delete the file name that is passed on a background
    * thread.
    *
    * @param name
    */
    public void deleteFile(String name) {
    	new BackgroundWork(DELETE_TYPE).execute(name);
    }

    /**
    * This method, handles the button presses of the top buttons found
    * in the Main activity.
    */
    public void onClick(View v) {

    }

    /**
    * called to update the file contents as the user navigates there
    * phones file system.
    *
    * @param content an ArrayList of the file/folders in the current directory.
    */
    public void updateDirectory(ArrayList<String> content) {
    	if(!data_source.isEmpty())
    		data_source.clear();

    	for(String data : content)
    		data_source.add(data);

    	delegate.notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView topView;
        ImageView icon;
    }

    /**
     * A nested class to handle displaying a custom view in the ListView that
     * is used in the PlayShare activity. If any icons are to be added, they must
     * be implemented in the getView method. This class is instantiated once in PlayShare
     * and has no reason to be instantiated again.
     *
     * @author aloncar
     *
     */
    public class TableRow extends ArrayAdapter<String> {
        public TableRow() {
             super(context, R.layout.tablerow, data_source);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            String temp = fukoffs_mg.getCurrentDir();
            File file = new File(temp + "/" + data_source.get(position));

            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.tablerow, parent, false);

                holder = new ViewHolder();
                holder.topView = (TextView)convertView.findViewById(R.id.top_view);
                holder.icon = (ImageView)convertView.findViewById(R.id.row_image);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            //holder.topView.setTextColor(color);

            if(file.isFile()) {
                String ext = file.toString();
                String sub_ext = ext.substring(ext.lastIndexOf(".") + 1);

                // We only have music file or text on by default
                if (sub_ext.equalsIgnoreCase("mp3") ||
                        sub_ext.equalsIgnoreCase("3gpp") ||
                        sub_ext.equalsIgnoreCase("wma") ||
                        sub_ext.equalsIgnoreCase("m4a") ||
                        sub_ext.equalsIgnoreCase("m4p")) {
                    holder.icon.setImageResource(R.drawable.music);
                }
            }

            holder.topView.setText(file.getName());

            return convertView;
        }
    }

    /**
    * A private inner class of EventHandler used to perform time extensive
    * operations. So the user does not think the the application has hung,
    * operations such as copy/past, search, unzip and zip will all be performed
    * in the background. This class extends AsyncTask in order to give the user
    * a progress dialog to show that the app is working properly.
    *
    * (note): this class will eventually be changed from using AsyncTask to using
    * Handlers and messages to perform background operations.
    *
    * @author Joe Berria
    */
    private class BackgroundWork extends AsyncTask<String, Void, ArrayList<String>> {

    	//private String file_name;
        private ProgressDialog pr_dialog;
        private int type;
        //private int copy_rtn;

        private BackgroundWork(int type) {
            this.type = type;
        }

        /**
        * This is done on the EDT thread. this is called before
        * doInBackground is called
        */
        @Override
        protected void onPreExecute() {
        	switch(type) {
        		case DELETE_TYPE:
        			pr_dialog = ProgressDialog.show(context, "Deleting", "Deleting files...", true, false);
        	    break;
        	}
        }

		@Override
		protected ArrayList<String> doInBackground(String... params) {
			switch(type) {
			case DELETE_TYPE:
				int size = params.length;

				for(int i = 0; i < size; i++)
					fukoffs_mg.deleteTarget(params[i]);

				return null;
			}
			return null;
		}

		/**
		* This is called when the background thread is finished. Like onPreExecute, anything
		* here will be done on the EDT thread.
		*/
		@Override
		protected void onPostExecute(final ArrayList<String> file) {
			//final CharSequence[] names;
			//int len = file != null ? file.size() : 0;

			switch(type) {
				case DELETE_TYPE:
					updateDirectory(fukoffs_mg.getFukoffsDir());
					pr_dialog.dismiss();
				break;
			}
		}
    }
}
