package de.hambuch.voronoiapp.algo;

import androidx.annotation.NonNull;

public class VoronoiException extends Exception
{

	private static final long serialVersionUID = 2707217959414643214L;

	public VoronoiException() {
		super();
	}
	public VoronoiException(@NonNull String s) {
		super(s);
	}
}
