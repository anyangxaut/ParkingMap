package com.example.departmentmap;
import java.util.LinkedList;
import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {

	 public static LinkedList<Activity> sAllActivitys = new LinkedList<Activity>();  
     
	    @Override  
	    protected void onCreate(Bundle bundle) {  
	        super.onCreate(bundle);  
	        sAllActivitys.add(this);  
	    }  
	      
	    @Override  
	    public void onDestroy() {  
	        super.onDestroy();  
	        sAllActivitys.remove(this);  
	    }  
	  
	      
	    public static void finishAll() {  
	        for(Activity activity : sAllActivitys) {  
	            activity.finish();  
	        }  
	          
	        sAllActivitys.clear();  
	    }  
	  
	    public static void exit() {  
	        finishAll();  
	        // 这个主要是用来关闭进程的, 把所有activity finish  
	        // 的话，进程是不会关闭的  
	        System.exit(0);  
	    }  
}
