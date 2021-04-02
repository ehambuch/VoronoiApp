package de.hambuch.voronoiapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;

import de.hambuch.voronoiapp.algo.DelaunayTriangulation;
import de.hambuch.voronoiapp.algo.VoronoiException;
import de.hambuch.voronoiapp.geometry.Point;

/**
 * Voronoi main program.
 * <p>This is a migration of an old Java program of my master thesis about Voronoi Diagrams from 2001.</p>
 * <ol>
 * <li>V1.4 (5): added load picture</li>
 * <li>V1.5 (6): ported to Android 6.x, storage permissions, layout etc.</li>
 * <li>V1.6 (7): Android 9</li>
 * <li>V1.7 (8): Android 10, Google Crashalytics</li>
 * <li>1.8 (10): Android 11, Storage Handling</li>
 * <li>1.9 (13): Wechsel Signaturkey, Fehlerkorrektur Permissions</li></li>
 * </ol>
 * @author Eric Hambuch (erichambuch@googlemail.com)
 *
 */
public class VoronoiMain extends AppCompatActivity implements OnTouchListener {

	private static final int ACTIVITY_SELECT_PICTURE = 999;

    private static final int REQUEST_EXTERNAL_STORAGE = 123;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

	// TODO: make static
    private final class LoadImage extends AsyncTask<Uri, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Uri... uris) {
			try {
				return decodeUri(getApplicationContext(), uris[0], voronoiView.getWidth(), voronoiView.getHeight());
			}
			catch(FileNotFoundException e) {
				Log.e("VoronoiMain", "Error loading picture", e);
			}
			return null;
		}

		protected void onPostExecute(Bitmap result) {
			if ( result != null ) {
                voronoiView.setBackground(result);
                voronoiView.invalidate();
            }
			else
				Toast.makeText(getApplicationContext(), getString(R.string.error_loadimage), Toast.LENGTH_LONG).show();
		}
	}

	private final DelaunayTriangulation triangulation = new DelaunayTriangulation();
	private VoronoiView voronoiView;
	private Point pointInMove;
	private boolean deleteMode = false;
	private Menu menu;

	/** Create a new options menu from XML resource */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
        this.menu = menu;
		return true;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

		voronoiView = (VoronoiView) findViewById(R.id.voronoiview);
		voronoiView.setOnTouchListener(this);
		voronoiView.setTriangulation(triangulation);
		registerForContextMenu(voronoiView);
		voronoiView.showVoronoi();
	}

	/** Handles menu item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_delaunay:
            setCheckedItems(item);
			voronoiView.showDelaunay();
			return true;
		case R.id.item_hull:
            setCheckedItems(item);
			voronoiView.showConvexHull();
			return true;
		case R.id.item_voronoi:
            setCheckedItems(item);
			voronoiView.showVoronoi();
			return true;
		case R.id.item_circle:
            setCheckedItems(item);
			voronoiView.showMaxCircle();
			return true;
		case R.id.item_delete:
			setDeleteMode(true);
			return true;
		case R.id.item_new:
			newDiagram();
			return true;
		case R.id.item_save:
			saveImage();
			return true;
		case R.id.item_load:
			loadBackground();
			return true;
		case R.id.item_exit:
			this.finish(); // exit
			return true;
		}
		return super.onContextItemSelected(item); // not consume by me
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		voronoiView.performClick();
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if ( deleteMode ) {
				removePoint(new Point(event.getX(), event.getY()));
				setDeleteMode(false);
				return true;
			} 
			else if (pointInMove == null) {
				try {
					setDeleteMode(false);
					triangulation.insertPoint(new Point(event.getX(), event.getY(), Color.RED));
					voronoiView.invalidate();
				} catch(VoronoiException e) {
		// ignore
			}
				return true;
			} else
			{
				setDeleteMode(false);
				triangulation.movePoint(pointInMove, event.getX(), event.getY());
				v.invalidate();
				pointInMove = null;
				return true;
			}
		}
		else if ( event.getAction() == MotionEvent.ACTION_DOWN) {
			pointInMove = triangulation.findPoint(event.getX(), event.getY(), 40.0f); // depends on density
			return true; 
		}
		else if ( event.getAction() == MotionEvent.ACTION_MOVE) {
			if ( pointInMove != null ) {
				triangulation.movePoint(pointInMove, event.getX(), event.getY());
				v.invalidate();
				return true;
			}
		}
		else if ( event.getAction() == MotionEvent.ACTION_CANCEL) {
			pointInMove = null;
			setDeleteMode(false);
			return true;
		}
		return false;
	}
	
	public void newDiagram() {
		AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(this).setCancelable(true).
                setMessage(R.string.text_cleardiagram).setTitle(R.string.text_new).
                setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        triangulation.clear();
                        setDeleteMode(false);
                        voronoiView.setBackground((Bitmap)null);
                        voronoiView.invalidate();
                }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_delete).
                        create();
        alertDialog.show();
	}

    private void setCheckedItems(MenuItem tobeChecked) {
        menu.findItem(R.id.item_hull).setChecked(false);
        menu.findItem(R.id.item_voronoi).setChecked(false);
        menu.findItem(R.id.item_delaunay).setChecked(false);
        menu.findItem(R.id.item_circle).setChecked(false);
        tobeChecked.setChecked(true);
    }

	private void removePoint(Point p) {
		Point deleteP = triangulation.findPoint(p.getX(), p.getY(), 100.0f);
		if ( deleteP != null )
			triangulation.deletePoint(deleteP);
		voronoiView.invalidate();
		setDeleteMode(false);
	}

	private void setDeleteMode(boolean newMode) {
		deleteMode = newMode;
		if ( newMode ) 
			setTitle(R.string.title_delete);
			else
				setTitle(R.string.app_name);
		
	}
	
	
	/**
	 * Load background from gallery.
	 */
	private void loadBackground() {
		Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getText(
        		R.string.title_selectbackground)), ACTIVITY_SELECT_PICTURE);

	}
	
	/** 
	 * Save image as PNG to SD card. 
	 */
	private void saveImage() {
		String title = "Voronoi_" + DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance()) + ".png";
		voronoiView.setDrawingCacheQuality(VoronoiView.DRAWING_CACHE_QUALITY_HIGH);
		voronoiView.setDrawingCacheEnabled(true);
		OutputStream outputStream = null;
		try {
            if(!verifyStoragePermissions(this))
            	return;

			ContentResolver resolver = getApplicationContext().getContentResolver();
			Uri pictureCollection;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				pictureCollection = MediaStore.Images.Media
						.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
			} else {
				pictureCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			}
			ContentValues newPictureDetails = new ContentValues();
			newPictureDetails.put(MediaStore.Images.Media.DISPLAY_NAME,
					title);
			newPictureDetails.put(MediaStore.Images.Media.TITLE,
					title);
			newPictureDetails.put(MediaStore.Images.Media.DESCRIPTION,
					"Created by VoronoiApp");
			final Uri uri = resolver
					.insert(pictureCollection, newPictureDetails);
			outputStream = resolver.openOutputStream(uri, "w");
			voronoiView.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, outputStream);
			Toast.makeText(this, "Stored image to " + uri.getPath(), Toast.LENGTH_LONG).show();
		}
		catch(Exception e) {
			Toast.makeText(this, "Error saving picture: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
		finally {
			voronoiView.setDrawingCacheEnabled(false);
			if (outputStream != null)
				try {
					outputStream.close();
				} catch (IOException e) {
					// ignore
				}
		}
	}

    /**
     * Checks if the app has permission to write to device storage. Only allowed to Android 10.
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
	 * @return true if allowed
     */
    public static boolean verifyStoragePermissions(Activity activity) {
    	if (Build.VERSION.SDK_INT <=  Build.VERSION_CODES.Q) {
			// Check if we have write permission
			int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

			if (permission != PackageManager.PERMISSION_GRANTED) {
				// We don't have permission so prompt the user
				ActivityCompat.requestPermissions(
						activity,
						PERMISSIONS_STORAGE,
						REQUEST_EXTERNAL_STORAGE
				);
				return false;
			}
		}
    	return true;
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
    	super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		if ( resultCode == RESULT_OK && requestCode == ACTIVITY_SELECT_PICTURE ) {
			Uri selectedImageUri = imageReturnedIntent.getData();
            if ( selectedImageUri == null )
            	return;
			new LoadImage().execute(selectedImageUri);
		} else if ( resultCode == RESULT_OK && requestCode == REQUEST_EXTERNAL_STORAGE ) {
			saveImage(); // try again
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("size", triangulation.size());
		float[] state = new float[triangulation.size()*2];
		int i = 0;
		for(Enumeration<Point> e = triangulation.points(); e.hasMoreElements(); ) {
			Point p = e.nextElement();
			state[i++] = p.getX();
			state[i++] = p.getY();
		}
		savedInstanceState.putFloatArray("points", state);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		  super.onRestoreInstanceState(savedInstanceState);
		 
		  int size = savedInstanceState.getInt("size");
		  float[] state = savedInstanceState.getFloatArray("points");
		  triangulation.clear();
		  for(int i=0;i<size;i++) {
			  try {
				  triangulation.insertPoint(new Point(state[i*2], state[i*2+1]));
			  } catch(VoronoiException e) { // ignore
			  }
		  }
	}
	
	/**
	 * Scale a bitmap image to a give size
	 * @param selectedImage URI of image
	 * @param maxWidth the target width
	 * @param maxHeight the target height
	 * @return scaled image or <var>null</var>
	 * @throws FileNotFoundException
	 */
	private static Bitmap decodeUri(Context context, Uri selectedImage, int maxWidth, int maxHeight) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        //for (int size = Math.max (o.outHeight, o.outWidth); (size>>(scale-1)) > maxWidth; ++scale);

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o.outWidth = maxWidth;
        o.outHeight = maxHeight;
        o.inTargetDensity = 1; // TODO?
        o.inScaled = false; // ?
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);

    }
}