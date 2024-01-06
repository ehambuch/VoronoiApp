package de.hambuch.voronoiapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hambuch.voronoiapp.algo.ConvexHull;
import de.hambuch.voronoiapp.algo.DelaunayTriangulation;
import de.hambuch.voronoiapp.algo.VoronoiDiagram;
import de.hambuch.voronoiapp.algo.VoronoiDiagramCircle;
import de.hambuch.voronoiapp.geometry.Point;

/**
 * View that display the diagram.
 */
public class VoronoiView extends View {

	public enum DrawableElement {
		VORONOI, VORONOICOLORED, DELAUNAY, CONVEXHULL, MAXCIRCLE;
	}

	private DelaunayTriangulation triang;
	private VoronoiDiagram drawableVoronoi;
	private DelaunayTriangulation drawableDelaunay;
	private VoronoiDiagramCircle drawableCircle;
	private ConvexHull drawableConvex;

	private BitmapDrawable background = null;

	private final Set<DrawableElement> elementsToDraw = new HashSet<>();

	public VoronoiView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setClickable(true);
	}

	public VoronoiView(@NonNull Context context) {
		this(context, null, 0);
	}

	public VoronoiView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public void setTriangulation(@NonNull DelaunayTriangulation triang) {
		this.triang = triang;
		this.drawableVoronoi = new VoronoiDiagram(triang);
		this.drawableDelaunay = triang;
		this.drawableCircle = new VoronoiDiagramCircle(triang);
		this.drawableConvex = new ConvexHull(triang);
	}

	public void setBackgroundBitmap(@Nullable Bitmap bitmap) {
		if (bitmap == null ) {
			this.background = null;
		}
		else {
			this.background = new BitmapDrawable(this.getResources(), bitmap);
		}
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

	public void onDraw(@NonNull Canvas canvas) {
		drawInternal(canvas);
	}

	private void drawInternal(Canvas canvas) {
		if (background != null) {
			background.setBounds(0, 0, getWidth()-1, getHeight()-1);
			background.draw(canvas);
		}
		else
			canvas.drawColor(Color.WHITE);

		// draw the elements, if enabled, can be draw over each other
		if(elementsToDraw.contains(DrawableElement.VORONOICOLORED) && drawableVoronoi != null) {
			drawableVoronoi.setFill(true); // TODO: not implemented yet
			drawableVoronoi.paint(canvas);
		} else if (elementsToDraw.contains(DrawableElement.VORONOI) && drawableVoronoi != null) {
			drawableVoronoi.setFill(false);
			drawableVoronoi.paint(canvas);
		}
		if (elementsToDraw.contains(DrawableElement.DELAUNAY) && drawableDelaunay != null) {
			drawableDelaunay.paint(canvas);
		}
		if (elementsToDraw.contains(DrawableElement.CONVEXHULL) && drawableConvex != null) {
			drawableConvex.paint(canvas);
		}
		if (elementsToDraw.contains(DrawableElement.MAXCIRCLE) && drawableCircle != null) {
			drawableCircle.paint(canvas);
		}

		// draw all points
		for (Iterator<Point> p = triang.points(); p.hasNext(); ) {
			p.next().paint(canvas);
		}
	}

	@Nullable
	protected ConvexHull getConvexHull() {
		if (elementsToDraw.contains(DrawableElement.CONVEXHULL) && drawableConvex != null) {
			return drawableConvex;
		} else
			return null;
	}

	@Nullable
	protected VoronoiDiagram getVoronoiDiagram() {
		if (elementsToDraw.contains(DrawableElement.VORONOI) && drawableVoronoi != null) {
			return drawableVoronoi;
		} else
			return null;
	}

	@Nullable
	protected DelaunayTriangulation getDelaunayTriang() {
		if (elementsToDraw.contains(DrawableElement.DELAUNAY) && drawableDelaunay != null) {
			return drawableDelaunay;
		} else
			return null;
	}

	/**
	 * Liefert das aktuelle Bild als Bitmap zurück. Dazu wird die Grafik komplett im Hintergrund nochmal neu aufgebaut.
	 * @return Grafik als Bitmap
	 */
	Bitmap getBitmap() {
		final Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888, false);
		final Canvas bitmapCanvas = new Canvas(bitmap);
		drawInternal(bitmapCanvas);
		return bitmap;
	}

	public void setDrawables(@NonNull Set<DrawableElement> drawables) {
		this.elementsToDraw.clear();
		this.elementsToDraw.addAll(drawables);
	}
}
