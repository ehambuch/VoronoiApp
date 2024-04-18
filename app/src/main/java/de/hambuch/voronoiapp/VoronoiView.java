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
import de.hambuch.voronoiapp.algo.DelauTriangle;
import de.hambuch.voronoiapp.algo.DelaunayTriangulation;
import de.hambuch.voronoiapp.algo.VoronoiDiagram;
import de.hambuch.voronoiapp.algo.VoronoiDiagramCircle;
import de.hambuch.voronoiapp.geometry.Point;
import de.hambuch.voronoiapp.geometry.Polygon;

/**
 * View that display the diagram.
 */
public class VoronoiView extends View {

	public enum DrawableElement {
		VORONOI, VORONOICOLORED, DELAUNAY, DELAUNAYCOLORED, CONVEXHULL, MAXCIRCLE
	}

	/**
	 * Define 6 colors of a color wheel, we do not support an algorithm for the 4-color-theorem.
	 */
	static final int[] COLORS = new int[]{
			Color.rgb(255, 255, 1), // yellow
			Color.rgb(0, 153, 0), // green
			Color.rgb(0, 101, 205), // blue
			Color.rgb(151,0,153), // violet
			Color.rgb(254,0,0), // red
			Color.rgb(252, 153,0) // orange
	};

	/**
	 * Visitor to color all delaunay triangles without color conflicts of adjacents.
	 */
	private static class PaintFilledDelaunay implements DelaunayTriangulation.Visitor {
		private int colorCounter = 0;

		private final Canvas canvas;

		protected PaintFilledDelaunay(Canvas convas) {
			this.canvas = convas;
		}

		@Override
		public void visit(@NonNull DelauTriangle triangle) {
			if (triangle.getFillColor() == 0) {
				setColorAccordingNeighbours(triangle);
				if (canvas != null)
					triangle.paint(canvas);
			} // else: already painted
		}

		protected void setColorAccordingNeighbours(DelauTriangle t) {
			int color = COLORS[(colorCounter++) % COLORS.length];
			if (t.getNeighbourAB() != null && color == t.getNeighbourAB().getFillColor()) {
				color = COLORS[(colorCounter++) % COLORS.length];
			} else if (t.getNeighbourBC() != null && color == t.getNeighbourBC().getFillColor()) {
				color = COLORS[(colorCounter++) % COLORS.length];
			} else if (t.getNeighbourCA() != null && color == t.getNeighbourCA().getFillColor()) {
				color = COLORS[(colorCounter++) % COLORS.length];
			}
			t.setFillColor(color);
		}
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
		if(elementsToDraw.contains(DrawableElement.DELAUNAYCOLORED) && drawableDelaunay != null) {
			paintFilled(canvas, drawableDelaunay);
		} else if (elementsToDraw.contains(DrawableElement.DELAUNAY) && drawableDelaunay != null) {
			drawableDelaunay.paint(canvas);
		}
		if(elementsToDraw.contains(DrawableElement.VORONOICOLORED) && drawableVoronoi != null) {
			paintFilled(canvas, drawableVoronoi);
		} else if (elementsToDraw.contains(DrawableElement.VORONOI) && drawableVoronoi != null) {
			drawableVoronoi.paint(canvas);
		}
		// hull and circle only as lines
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

	/**
	 * USe a special algorithm to paint a filled, colored voronoi diagram.
	 * <p>We do not implement this as the standard #paint() method in the element itself. The algorithm provided here is "primitive" and inefficient, as
	 * it convers to voronoi diagram into voronoi regions (Region) and the to closed Polygons that can be painted directly.
	 * Nevertheless for an reasonable number of points that should work.</p>
	 * @param voronoiDiagram the voronoi diagram to paint
	 */
	private void paintFilled(Canvas canvas, VoronoiDiagram voronoiDiagram) {
		// TODO: can we avoid the same color for adjacent cells if we process according to the delaunay triangles in the correct neighbouring order?
		// Idea: a point of the corresponding triangulation may have multiple triangles - all of them must have different colors
		int color = 0;
		for(Iterator<Point> iterator = voronoiDiagram.points(); iterator.hasNext(); ) {
			Polygon polygon = voronoiDiagram.toRegion(iterator.next()).clipTo(0,0,canvas.getWidth(), canvas.getHeight());
			if(polygon != null) {
				polygon.setFillColor(COLORS[(color++) % COLORS.length]);
				polygon.paint(canvas);
			}
		}
	}

	/**
	 * USe a special algorithm to paint a filled, colored delaunay triangulation.
	 * <p>We do not implement this as the standard #paint() method in the element itself, but simply visit all triangles and paint them.</p>
	 * @param triangulation the triangulation to paint
	 */
	private void paintFilled(final Canvas canvas, final DelaunayTriangulation triangulation) {
		// first reset all colors
		triangulation.visitTriangles(triangle -> triangle.setFillColor(0));
		triangulation.visitTriangles(new PaintFilledDelaunay(canvas));
		// and reset again, if drawn without filling next time
		triangulation.visitTriangles(triangle -> triangle.setFillColor(0));
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
