package com.ipssi.geometry;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;

public class Polygon {
	public ArrayList<Point> points = new ArrayList<Point>();		

	public Polygon(String s){
		formPolygonFromText(s);
	}

	public Polygon(){
		
	}
	
	public void formPolygonFromText(String s){
		s = s.substring(s.indexOf("((")+2);
		while(s.contains(",")){
			String temp = s.substring(0,s.indexOf(','));
			Point p = new Point();
			String xy[] = temp.split(" ");
			p.setX(Double.parseDouble(xy[0]));
			p.setY(Double.parseDouble(xy[1]));
			this.addPoint(p);
			s = s.substring(s.indexOf(',')+1);
		} 
	}
	
	public void addPoint(Point p){
		points.add(p);
	}
	
	public int getSize(){
		return points.size();
	}
	
	public Point getIndex(int index) throws IndexOutOfBoundsException {
			return points.get(index);
	}
	
	public String toWKT(){
		StringBuilder s = new StringBuilder();
		s.append("Polygon((");
		int sz = points.size();
		for (int i=0,is=sz;i<is;i++) {
			if (i != 0)
				s.append(",");
			s.append(points.get(i).getX()).append(" ").append(points.get(i).getY());
		}
		if (sz > 1)
			s.append(",");
		if (sz > 0) {
			s.append(points.get(0).getX()).append(" ").append(points.get(0).getY());
		}
		s.append("))");
		return s.toString();		
	}
	
	public Polygon toCaretesian(){
		Polygon r = new Polygon();
		
		for(int i = 0; i < this.getSize(); i++){
			r.addPoint( (this.getIndex(i)).toCartesian() );
		}
		return r;
	}
	
	public static void main(String a[]){
		Polygon r = new Polygon("Polygon((0 0,1 1,3 0,3 3,3 0,0 0");
		System.out.println(r.toWKT());
		
	}
	
}
