package com.example.util;

	// 边类型
	public class Edge
	{
	    public final Vertex target;
	    public final double weight;
	    public Edge(Vertex argTarget, double argWeight)
	    { target = argTarget; weight = argWeight; }
	}