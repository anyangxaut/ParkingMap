package com.example.departmentmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;

import com.example.util.Dijkstra;
import com.example.util.Edge;
import com.example.util.Vertex;
import com.ls.widgets.map.model.MapObject;

public class Navigation {
	
	private static final String TAG = "Navigation";
    private Vertex[] vertices = null;
	
	// 获取最短路径
    public static List<Vertex> getShortestPathTo(Vertex target)
    {
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.previous)
            path.add(vertex);
        Collections.reverse(path);
        return path;
    }
	
    // 路径导航方法
	public List<Vertex> pathNavigation(MapObject start, MapObject end){
		// 获取到起点和终点的像素点
	Log.d(TAG, "start:" + start.getX() + "," + start.getY() + "/n end:"
	+ end.getX() + "," + end.getY());
	
	// (j * 46 - 19), (i * 44 - 19)计算起点index，该点的值和二维码中的相同
	// tmp计算行数，其中一行与有10个点，因此将计算出的行数乘以10在加上列的数字（即targetStartV或者targetEndV）
	
	// 计算起点二维码数据0-79
	int tmp =  ((start.getY()+19)/44) * 10;
	int targetStartV = ((start.getX() + 19) / 46) + tmp;
	// 计算终点二维码数据0-79
	tmp =  ((end.getY()+19)/44) * 10;
	int targetEndV = ((end.getX() + 19) / 46) + tmp;
	Log.d(TAG, "targetStartV:" + targetStartV + "targetEndV:" + targetEndV);
	// 计算起始点和终点之间的最短路径
	return computePath(targetStartV, targetEndV);
	}
	
//	利用dijkstra算法计算最短路径
	public List<Vertex> computePath(int StartV, int EndV){
		// 初始化顶点信息0-79，邻接边和其对应路径权值
        initVinfo();

        Dijkstra.dijkstraPath(vertices[StartV]);
		
//	        for (Vertex v : vertices)
//		{
//		    Log.d(TAG, "Distance to " + v + ": " + v.minDistance);
//		    List<Vertex> path = getShortestPathTo(v);
//		    Log.d(TAG, "Path: " + path);
//		}
        
        Log.d(TAG, "Distance to " + vertices[EndV] + ": " + vertices[EndV].minDistance);
        // 输出起始点到终点的路径规划序列
	    List<Vertex> path = getShortestPathTo(vertices[EndV]);
	    Log.d(TAG, "Path: " + path);
	    
	    return path;
	    }
	
	// 该方法内的数据根据不同的地图而不同，要根据实际环境进行设置
	public void initVinfo(){
		
		// 初始化80个顶点
				vertices = new Vertex[80];
				// 写入各个顶点信息name（0-79）,刚好与二维码内容一一对应
				for(int i = 0; i < 80; i++){
					
					vertices[i] = new Vertex("" + i);
				}
				
				vertices[0].adjacencies = new Edge[]{new Edge(vertices[1], 1), new Edge(vertices[10], 1)};
				// 初始化各个顶点的临接边信息
				for(int i = 1; i < 9; i++){
					
					vertices[i].adjacencies = new Edge[]{new Edge(vertices[i-1], 1), new Edge(vertices[i+1], 1)};
				}
				vertices[9].adjacencies = new Edge[]{new Edge(vertices[8], 1), new Edge(vertices[19], 1)};
				vertices[10].adjacencies = new Edge[]{new Edge(vertices[0], 1), new Edge(vertices[11], 1), new Edge(vertices[20], 1)};
		        for(int i = 11; i < 19; i++){
					
					vertices[i].adjacencies = new Edge[]{new Edge(vertices[i-1], 1), new Edge(vertices[i+1], 1)};
				}
		        vertices[19].adjacencies = new Edge[]{new Edge(vertices[9], 1), new Edge(vertices[18], 1), new Edge(vertices[29], 1)};
		        vertices[20].adjacencies = new Edge[]{new Edge(vertices[10], 1), new Edge(vertices[21], 1), new Edge(vertices[30], 1)};
		        for(int i = 21; i < 29; i++){
					
					vertices[i].adjacencies = new Edge[]{new Edge(vertices[i-1], 1), new Edge(vertices[i+1], 1)};
				}
		        vertices[29].adjacencies = new Edge[]{new Edge(vertices[19], 1), new Edge(vertices[28], 1), new Edge(vertices[39], 1)};
		        vertices[30].adjacencies = new Edge[]{new Edge(vertices[20], 1), new Edge(vertices[31], 1), new Edge(vertices[40], 1)};
		        for(int i = 31; i < 39; i++){
					
					vertices[i].adjacencies = new Edge[]{new Edge(vertices[i-1], 1), new Edge(vertices[i+1], 1)};
				}
		        vertices[39].adjacencies = new Edge[]{new Edge(vertices[29], 1), new Edge(vertices[38], 1), new Edge(vertices[49], 1)};
		        vertices[40].adjacencies = new Edge[]{new Edge(vertices[30], 1), new Edge(vertices[41], 1), new Edge(vertices[50], 1)};  
		        for(int i = 41; i < 49; i++){
					
					vertices[i].adjacencies = new Edge[]{new Edge(vertices[i-1], 1), new Edge(vertices[i+1], 1)};
				}
		        vertices[49].adjacencies = new Edge[]{new Edge(vertices[39], 1), new Edge(vertices[48], 1), new Edge(vertices[59], 1)};
		        vertices[50].adjacencies = new Edge[]{new Edge(vertices[40], 1), new Edge(vertices[51], 1), new Edge(vertices[60], 1)};  
		        for(int i = 51; i < 59; i++){
					
					vertices[i].adjacencies = new Edge[]{new Edge(vertices[i-1], 1), new Edge(vertices[i+1], 1)};
				}
		        vertices[59].adjacencies = new Edge[]{new Edge(vertices[49], 1), new Edge(vertices[58], 1), new Edge(vertices[69], 1)};
		        vertices[60].adjacencies = new Edge[]{new Edge(vertices[50], 1), new Edge(vertices[61], 1), new Edge(vertices[70], 1)};  
		        for(int i = 61; i < 69; i++){
					
					vertices[i].adjacencies = new Edge[]{new Edge(vertices[i-1], 1), new Edge(vertices[i+1], 1)};
				}
		        vertices[69].adjacencies = new Edge[]{new Edge(vertices[59], 1), new Edge(vertices[68], 1), new Edge(vertices[79], 1)};
		        vertices[70].adjacencies = new Edge[]{new Edge(vertices[60], 1), new Edge(vertices[71], 1)};  
		        for(int i = 71; i < 79; i++){
					
					vertices[i].adjacencies = new Edge[]{new Edge(vertices[i-1], 1), new Edge(vertices[i+1], 1)};
				}
		        vertices[79].adjacencies = new Edge[]{new Edge(vertices[69], 1), new Edge(vertices[78], 1)};
	}

}
