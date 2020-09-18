package de.hambuch.voronoiapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Enumeration;

import de.hambuch.voronoiapp.algo.ConvexHull;
import de.hambuch.voronoiapp.algo.DelaunayTriangulation;
import de.hambuch.voronoiapp.algo.VoronoiDiagram;
import de.hambuch.voronoiapp.algo.VoronoiDiagramCircle;
import de.hambuch.voronoiapp.geometry.GeomElement;
import de.hambuch.voronoiapp.geometry.Point;

public class VoronoiView extends View {

	private DelaunayTriangulation triang;
	private GeomElement drawable = null;
	private Drawable backgroundBitmap = null;
	
	public VoronoiView(Context context) {
		super(context);
		setClickable(true);
	}

	public VoronoiView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setClickable(true);
	}

	public VoronoiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
	}

	public void setTriangulation(DelaunayTriangulation triang) {
		this.triang = triang;
	}

	public void setBackground(Bitmap bitmap) {
		if (bitmap == null )
			this.backgroundBitmap = null;
		else
			this.backgroundBitmap = new BitmapDrawable(this.getResources(), bitmap);
	}
	
	public void onDraw(Canvas canvas) {
		if (backgroundBitmap == null) {
			canvas.drawColor(Color.WHITE);
		} else { // with image
			backgroundBitmap.setBounds(0,0,this.getWidth()-1,this.getHeight()-1);
			backgroundBitmap.draw(canvas);
		}
		
		if (drawable != null) {
			drawable.paint(canvas);
		}
		for (Enumeration<Point> p = triang.points(); p.hasMoreElements();) {
			p.nextElement().paint(canvas);
		}		
	}

	public void showDelaunay() {
		drawable = triang;
		invalidate();
	}

	public void showVoronoi() {
		drawable = new VoronoiDiagram(triang);
		invalidate();
	}

	public void showConvexHull() {
		drawable = new ConvexHull(triang);
		invalidate();
	}

	public void showMaxCircle() {
		drawable = new VoronoiDiagramCircle(triang);
		invalidate();
	}
}
