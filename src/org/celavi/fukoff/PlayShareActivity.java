package org.celavi.fukoff;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
//import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public final class PlayShareActivity extends ListActivity {
    /** Tag for logging */
    //private static final String CLASSTAG = PlayShareActivity.class.getSimpleName();

    private FukoffsManager fukoffs_mg;
    private EventHandler handler;
    private EventHandler.TableRow table;

    private String selected_list_item;

    private static final int F_MENU_DELETE = 0x00;			//context menu id
    private static final int F_MENU_RENAME = 0x01;			//context menu id
    private static final int F_MENU_SHARE  = 0x02;			//context menu id
    private static final int F_MENU_UPLOAD = 0x03;			//context menu id

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_share);

        fukoffs_mg = new FukoffsManager();

        handler = new EventHandler(PlayShareActivity.this, fukoffs_mg);
        table = handler.new TableRow();

        /*sets the ListAdapter for our ListActivity and
        *gives our EventHandler class the same adapter
        */
        handler.setListAdapter(table);
        setListAdapter(table);

        /* register context menu for our list view */
        registerForContextMenu(getListView());
    }

    /**
     * This method will be called when an item in the list is selected.
     */
    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        final String item = handler.getData(position);
        File file = new File(fukoffs_mg.getCurrentDir() + "/" + item);
        String item_ext = null;

        try {
            item_ext = item.substring(item.lastIndexOf("."), item.length());

        } catch(IndexOutOfBoundsException e) {
            item_ext = "";
        }

        if (file.isFile()) {
            if (file.exists()) {
                    /*music file selected*/
                if (item_ext.equalsIgnoreCase(".mp3") ||
                    item_ext.equalsIgnoreCase(".3gpp") ||
                    item_ext.equalsIgnoreCase(".wma") ||
                    item_ext.equalsIgnoreCase(".m4a") ||
                    item_ext.equalsIgnoreCase(".m4p") ) {
                    Intent music_int = new Intent(this, AudioPlayblackActivity.class);
                    music_int.putExtra("MUSIC PATH", file.getAbsolutePath());
                    startActivity(music_int);
                } else {
                    /* generic intent */
                    Intent generic = new Intent();
                    generic.setAction(android.content.Intent.ACTION_VIEW);
                    generic.setDataAndType(Uri.fromFile(file), "application/*");
                    startActivity(generic);
                }
            }
        }

    }

    /**
     * Context menu start here
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);

        AdapterContextMenuInfo _info = (AdapterContextMenuInfo)info;
        selected_list_item = handler.getData(_info.position);

        /* is it a file */
        if(!fukoffs_mg.isDirectory(selected_list_item) ) {
            menu.setHeaderTitle("File Operations");
            menu.add(0, F_MENU_DELETE, 0, "Delete File");
            menu.add(0, F_MENU_RENAME, 0, "Rename File");
            menu.add(0, F_MENU_SHARE, 0, "Share File");
            menu.add(0, F_MENU_UPLOAD, 0, "Upload File");
        }
    }

    /**
     * Now we have to add functionality to every menu option we have created.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	super.onContextItemSelected(item);
        switch(item.getItemId()) {
            case F_MENU_DELETE:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Warning");
                builder.setIcon(R.drawable.warning);
                builder.setMessage("Deleting " + selected_list_item +
                                " cannot be undone. Are you sure you want to delete?");
                builder.setCancelable(false);

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        handler.deleteFile(fukoffs_mg.getCurrentDir() + "/" + selected_list_item);
                    }
                });
                AlertDialog alert_d = builder.create();
                alert_d.show();
                return true;
            case F_MENU_RENAME:
            	showDialog(F_MENU_RENAME);
                return true;
            case F_MENU_SHARE:
            	File file = new File(fukoffs_mg.getCurrentDir() + "/" + selected_list_item);
            	Intent share = new Intent(Intent.ACTION_SEND);
            	share.setType("audio/*");
            	share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            	startActivity(Intent.createChooser(share, "Share File"));
                return true;
            case F_MENU_UPLOAD:

                return true;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	final Dialog dialog = new Dialog(PlayShareActivity.this);

    		switch(id) {
    			case F_MENU_RENAME:
    				dialog.setContentView(R.layout.input_dialog);
    			    dialog.setTitle("Rename " + selected_list_item);
    			    dialog.setCancelable(false);

    			    ImageView rename_icon = (ImageView)dialog.findViewById(R.id.input_icon);
    			    rename_icon.setImageResource(R.drawable.rename);

    			    TextView rename_label = (TextView)dialog.findViewById(R.id.input_label);
    			    rename_label.setText(fukoffs_mg.getCurrentDir());
    			    final EditText rename_input = (EditText)dialog.findViewById(R.id.input_inputText);

    			    Button rename_cancel = (Button)dialog.findViewById(R.id.input_cancel_b);
    			    Button rename_create = (Button)dialog.findViewById(R.id.input_create_b);
    			    rename_create.setText("Rename");

    			    rename_create.setOnClickListener(new OnClickListener() {
    			    	public void onClick (View v) {
    			    		if(rename_input.getText().length() < 1)
    			    		     dialog.dismiss();
    			    		if(fukoffs_mg.renameTarget(fukoffs_mg.getCurrentDir() +"/"+ selected_list_item, rename_input.getText().toString()) == 0) {
    			    		     Toast.makeText(PlayShareActivity.this, selected_list_item + " was renamed to " +rename_input.getText().toString(),
    			    		     Toast.LENGTH_LONG).show();
    			    		} else {
    			    		     Toast.makeText(PlayShareActivity.this, selected_list_item + " was not renamed", Toast.LENGTH_LONG).show();
    			    		}

    			    		dialog.dismiss();
    			    	    handler.updateDirectory(fukoffs_mg.getFukoffsDir());
    			    	}
    			    });

    			    rename_cancel.setOnClickListener(new OnClickListener() {
    			    	public void onClick (View v) { dialog.dismiss(); }
    			    });
    			break;
    		}
			return dialog;
    }
}
