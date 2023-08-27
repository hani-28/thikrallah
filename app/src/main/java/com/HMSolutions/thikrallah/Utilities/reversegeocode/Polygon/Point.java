package com.HMSolutions.thikrallah.Utilities.reversegeocode.Polygon;

import java.util.Locale;

/**
 * Point on 2D landscape
 * 
 * @author Roman Kushnarenko (sromku@gmail.com)</br>
 */
public class Point
{
	public Point(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public float x;
	public float y;

	@Override
	public String toString()
	{
		return String.format(Locale.ENGLISH,"(%.2f,%.2f)", x, y);
	}
}