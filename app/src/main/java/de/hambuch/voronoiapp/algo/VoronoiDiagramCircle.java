package de.hambuch.voronoiapp.algo;

import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.NonNull;

import de.hambuch.voronoiapp.geometry.Circle;
import de.hambuch.voronoiapp.geometry.Point;

/**
 * Voronoi diagram with maximum spanning circle.
 * 
 * @author eric
 * 
 */
public class VoronoiDiagramCircle extends VoronoiDiagram {

	private Point cursor;

	private static class MaximumCircleVisitor implements DelaunayTriangulation.Visitor {

		private Circle maxCircle = null;

		@Override
		public void visit(DelauTriangle triangle) {
			if (triangle.isHalfplane())
				return;
			Circle circle = triangle.circumCircle();
			if (maxCircle == null)
				maxCircle = circle;
			else if (circle.getRadius() > maxCircle.getRadius())
				maxCircle = circle;
		}

		public Circle getMaxCircle() {
			return maxCircle;
		}
	}

	public VoronoiDiagramCircle() {
		super();
	}

	public VoronoiDiagramCircle(DelaunayTriangulation delaunay) {
		super(delaunay);
	}

	public void paint(@NonNull Canvas g) {
		super.paint(g);
		if (cursor != null) {
			Point nearest = super.pointLocation(cursor);
			if (nearest != null)
				new Circle(cursor, nearest.distance(cursor), Color.MAGENTA)
						.paint(g);
		}

		// draw maximum circle
		MaximumCircleVisitor visitor = new MaximumCircleVisitor();
		super.delaunay.visitTriangles(visitor);
		Circle circle = visitor.getMaxCircle();
		if (circle != null) {
			circle.setColor(Color.GREEN);
			circle.paint(g);
		}
	}
}
