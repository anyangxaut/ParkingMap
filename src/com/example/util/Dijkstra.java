package com.example.util;

import java.util.PriorityQueue;

/** 利用dijkstra算法计算最短路径
 * 
 * Dijkstra(迪杰斯特拉)算法是典型的最短路径路由算法，用于计算一个节点到其他所有节点的最短路径。
 * 主要特点是以起始点为中心向外层层扩展，直到扩展到终点为止。
 * Dijkstra算法能得出最短路径的最优解，但由于它遍历计算的节点很多，所以效率低。
 */
public class Dijkstra {

	    public static void dijkstraPath(Vertex source)
	    {
	        source.minDistance = 0.;
	        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
	      	vertexQueue.add(source);

		while (!vertexQueue.isEmpty()) {
		    Vertex u = vertexQueue.poll();

	            // Visit each edge exiting u
	            for (Edge e : u.adjacencies)
	            {
	                Vertex v = e.target;
	                double weight = e.weight;
	                double distanceThroughU = u.minDistance + weight;
			if (distanceThroughU < v.minDistance) {
			    vertexQueue.remove(v);
			    v.minDistance = distanceThroughU ;
			    v.previous = u;
			    vertexQueue.add(v);
			}
	            }
	        }
	    }
}
