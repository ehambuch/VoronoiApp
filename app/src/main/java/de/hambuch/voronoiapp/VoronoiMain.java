package de.hambuch.voronoiapp;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hambuch.voronoiapp.algo.ConvexHull;
import de.hambuch.voronoiapp.algo.DelaunayTriangulation;
import de.hambuch.voronoiapp.algo.VoronoiDiagram;
import de.hambuch.voronoiapp.algo.VoronoiException;
import de.hambuch.voronoiapp.geometry.GeomElement;
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
 * <li>1.9 (13): Wechsel Signaturkey, Fehlerkorrektur Permissions</li>
 * <li>1.10.0 (14): Android 12</li>
 * <li>1.11.0 (15): Share Image function, OSS licenses</li>
 * <li>1.12.0 (16): Android 13</li>
 * <li>1.13.0 (17/18): Android 14, Material 3 Design, Removed some functions, Export SVG</li>
 * <li>1.14.0 (19): Support for filled diagrams</li>
 * </ol>
 * @author Eric Hambuch (erichambuch@googlemail.com)
 */
public class VoronoiMain extends AppCompatActivity implements OnTouchListener {

	/**
	 * Background task to load image.
	 */
    private static class LoadImage implements Runnable {
    	private final Context context;
    	private final Uri uri;
    	private final int widht;
    	private final int height;
    	private final LoadImageCallback callback;

		LoadImage(Context context, Uri uri, int w, int h, LoadImageCallback callback) {
			this.context = context;
			this.uri = uri;
			this.widht = w;
			this.height = h;
			this.callback = callback;
		}

		@Override
		public void run() {
			try {
				Bitmap bitmap = decodeUri(context, uri, widht, height);
				if ( bitmap != null )
					callback.imageLoaded(bitmap);
			}
			catch(Exception e) {
				Log.e("VoronoiMain", "Error loading picture", e);
				callback.errorLoading(e);
			}
		}
	}

	interface LoadImageCallback {
		void imageLoaded(Bitmap bitmap);
		void errorLoading(Exception e);
	}


	private final DelaunayTriangulation triangulation = new DelaunayTriangulation();
	private VoronoiView voronoiView;
	private Point pointInMove;
	private boolean deleteMode = false;
	private Menu menu;

	private ActivityResultLauncher<Intent> loadImageLauncher;
	private final ExecutorService executors = Executors.newFixedThreadPool(1);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		voronoiView = (VoronoiView) findViewById(R.id.voronoiview);
		voronoiView.setOnTouchListener(this);
		voronoiView.setTriangulation(triangulation);
		registerForContextMenu(voronoiView);
		((ChipGroup)findViewById(R.id.chipGroup)).setOnCheckedStateChangeListener((group, checkedIds) -> {
			final Set<VoronoiView.DrawableElement> drawableElementSet = new HashSet<>();
			if(checkedIds.contains(R.id.filter_voronoi))
				drawableElementSet.add(VoronoiView.DrawableElement.VORONOI);
			if(checkedIds.contains(R.id.filter_voronoicolor))
				drawableElementSet.add(VoronoiView.DrawableElement.VORONOICOLORED);
			if(checkedIds.contains(R.id.filter_delaunaycolor))
				drawableElementSet.add(VoronoiView.DrawableElement.DELAUNAYCOLORED);
			if(checkedIds.contains(R.id.filter_delaunay))
				drawableElementSet.add(VoronoiView.DrawableElement.DELAUNAY);
			if(checkedIds.contains(R.id.filter_convex))
				drawableElementSet.add(VoronoiView.DrawableElement.CONVEXHULL);
			if(checkedIds.contains(R.id.filter_circle))
				drawableElementSet.add(VoronoiView.DrawableElement.MAXCIRCLE);
			if(drawableElementSet.isEmpty())
				drawableElementSet.add(VoronoiView.DrawableElement.VORONOI); // minimum: draw voronoi
			voronoiView.setDrawables(drawableElementSet);
			voronoiView.invalidate();
		});
		loadImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::loadBackgroundImage);
		voronoiView.post(new Runnable() {
			@Override
			public void run() {
				voronoiView.setBackgroundBitmap((Bitmap)null);
				voronoiView.setDrawables(Collections.singleton(VoronoiView.DrawableElement.VORONOI));
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		if ( triangulation.size() <= 3) { // for the first 3 points, tell the user what to do
			Snackbar.make(voronoiView, R.string.text_clickpoint, Snackbar.LENGTH_LONG).show();
		}
	}

	/** Create a new options menu from XML resource */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		this.menu = menu;
		return true;
	}

	/** Handles menu item selections */
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		final int itemId = item.getItemId();
		if(itemId ==  R.id.item_delete) {
			setDeleteMode(true);
			return true;
		} else if(itemId ==  R.id.item_new) {
			newDiagram();
			return true;
		} else if(itemId ==  R.id.item_load) {
				loadBackground();
				return true;
		} else if(itemId ==  R.id.item_share) {
			shareImage();
			return true;
		} else if(itemId == R.id.item_sharesvg) {
			exportSvg();
			return true;
		} else if(itemId ==  R.id.item_oss) {
			startActivity(new Intent(this, OssLicensesMenuActivity.class));
			return true;
		} else if(itemId == R.id.item_rate) {
			showRateGooglePlay();
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
					if ( triangulation.size() <= 3) { // notify user to set more points
						Snackbar.make(voronoiView, R.string.text_clickanotherpoint, Snackbar.LENGTH_LONG).show();
					}
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
        new MaterialAlertDialogBuilder(this).setCancelable(true).
                setMessage(R.string.text_cleardiagram).setTitle(R.string.text_new).
                setPositiveButton(android.R.string.yes, (dialog, which) -> {
					triangulation.clear();
					setDeleteMode(false);
					voronoiView.setBackgroundBitmap((Bitmap)null);
					voronoiView.invalidate();
			})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_delete).
                        create().show();
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

	private void showRateGooglePlay() {
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		} catch (android.content.ActivityNotFoundException anfe) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
		}
	}

	private void exportSvg() {
		final String title = "Voronoi_" + DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance()) + ".svg";
		OutputStream outputStream = null;
		try {
			// create temporary file
			final ContentResolver resolver = getApplicationContext().getContentResolver();
			final File imageDir = new File(getCacheDir(), "images"); // name from filepath.xml
			if(!imageDir.exists())
				imageDir.mkdir();
			final File file = new File(imageDir, title);
			final Uri uri = FileProvider.getUriForFile(this, "de.hambuch.voronoiapp.fileprovider", file);
			outputStream = resolver.openOutputStream(uri, "w");

			if(outputStream != null) {
				final List<GeomElement> elements = new ArrayList<>();
				// all points at once
				triangulation.points().forEachRemaining(elements::add);
				// and the graphics splitted into basic geometric elements
				final VoronoiDiagram diagramToExport = voronoiView.getVoronoiDiagram();
				if(diagramToExport != null)
					diagramToExport.exportToElements(elements);
				final DelaunayTriangulation delaunayToExport = voronoiView.getDelaunayTriang();
				if(delaunayToExport != null)
					delaunayToExport.visitTriangles(elements::add);
				final ConvexHull hullToExport = voronoiView.getConvexHull();
				if(hullToExport != null)
					elements.add(hullToExport.toPolygon());

				// and now we have a list of geom elements that can be exported
				new SvgExporter(outputStream).export(
						elements,
						new Rect(0,0, voronoiView.getWidth(), voronoiView.getHeight()));

				ContentValues newPictureDetails = new ContentValues();
				newPictureDetails.put(MediaStore.Images.Media.DISPLAY_NAME,
						title);
				newPictureDetails.put(MediaStore.Images.Media.TITLE,
						title);
				newPictureDetails.put(MediaStore.Images.Media.DESCRIPTION,
						"Created by VoronoiApp");

				outputStream.close();
				outputStream = null;

				// on emulator dump to console
				if(BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					grantUriPermission(getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
					try(InputStream inputStream = resolver.openInputStream(uri))
					{
						Log.d(VoronoiApp.APPNAME, new String(inputStream.readAllBytes()));
					}
				}

				// and share
				final Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
				shareIntent.setFlags(
						Intent.FLAG_GRANT_READ_URI_PERMISSION);
				shareIntent.setType("image/svg+xml"); //
				shareIntent.setClipData(ClipData.newUri(resolver, title, uri));
				startActivity(Intent.createChooser(shareIntent, getTitle()));
			}
		}
		catch(Exception e) {
			Log.e(getPackageName(),"Error saving SVG: ", e);
			Toast.makeText(this, "Error saving SVG: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
		finally {
			if (outputStream != null)
				try {
					outputStream.close();
				} catch (IOException e) {
					// ignore
				}
		}
	}
	
	/**
	 * Load background from gallery.
	 */
	private void loadBackground() {
		Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		loadImageLauncher.launch(Intent.createChooser(intent, getText(
        		R.string.title_selectbackground))); // callback see onCreate()
	}

	private void shareImage() {
		final String title = "Voronoi_" + DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance()) + ".png";
		OutputStream outputStream = null;
		try {
			// create temporary file
			final ContentResolver resolver = getApplicationContext().getContentResolver();
			final File imageDir = new File(getCacheDir(), "images"); // name from filepath.xml
			if(!imageDir.exists())
				imageDir.mkdir();
			final File file = new File(imageDir, title);
			final Uri uri = FileProvider.getUriForFile(this, "de.hambuch.voronoiapp.fileprovider", file);
			outputStream = resolver.openOutputStream(uri, "w");

			if(outputStream != null) {
				ContentValues newPictureDetails = new ContentValues();
				newPictureDetails.put(MediaStore.Images.Media.DISPLAY_NAME,
						title);
				newPictureDetails.put(MediaStore.Images.Media.TITLE,
						title);
				newPictureDetails.put(MediaStore.Images.Media.DESCRIPTION,
						"Created by VoronoiApp");
				voronoiView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputStream);
				outputStream.close();
				outputStream = null;

				// and share
				final Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
				shareIntent.setFlags(
						Intent.FLAG_GRANT_READ_URI_PERMISSION);
				shareIntent.setType("image/png");
				shareIntent.setClipData(ClipData.newUri(resolver, title, uri));
				startActivity(Intent.createChooser(shareIntent, getTitle()));
			}
		}
		catch(Exception e) {
			Log.e(getPackageName(),"Error saving picture: ", e);
			Toast.makeText(this, "Error saving picture: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
		finally {
			if (outputStream != null)
				try {
					outputStream.close();
				} catch (IOException e) {
					// ignore
				}
		}
	}

	protected void loadBackgroundImage(@NonNull ActivityResult selectedImageUri) {
		if(selectedImageUri == null || selectedImageUri.getData() == null)
			return;
		// Load image in background thread!
		executors.submit(new LoadImage(getApplicationContext(), selectedImageUri.getData().getData(), voronoiView.getWidth(), voronoiView.getHeight(),
				new LoadImageCallback() {
					@Override
					public void imageLoaded(Bitmap bitmap) {
						voronoiView.setBackgroundBitmap(bitmap);
						voronoiView.postInvalidate(); // from non-UI thread!
					}

					@Override
					public void errorLoading(Exception e) {
						findViewById(R.id.voronoiview).post(() -> Toast.makeText(VoronoiMain.this, getString(R.string.error_loadimage) + ": " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
					}
				}));
	}
	
	@Override
	public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("size", triangulation.size());
		float[] state = new float[triangulation.size()*2];
		int i = 0;
		for(Iterator<Point> e = triangulation.points(); e.hasNext(); ) {
			Point p = e.next();
			state[i++] = p.getX();
			state[i++] = p.getY();
		}
		savedInstanceState.putFloatArray("points", state);
	}
	
	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
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
	 * @throws FileNotFoundException on error
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