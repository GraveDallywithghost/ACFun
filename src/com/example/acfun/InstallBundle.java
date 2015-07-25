package com.example.acfun;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apkplug.Bundle.BundleControl;
import org.apkplug.Bundle.OSGIServiceAgent;
import org.apkplug.Bundle.installCallback;
import org.apkplug.app.FrameworkFactory;
import org.apkplug.app.FrameworkInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
/**
 * 替代PropertyInstance.AutoStart()的功�?
 */
public class InstallBundle {
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	public boolean DEBUG=true;
	OSGIServiceAgent<BundleControl> agent=null;
	public InstallBundle(BundleContext mcontext){
		agent=new OSGIServiceAgent<BundleControl>(mcontext,BundleControl.class);
		sp = mcontext.getAndroidContext().getSharedPreferences("apkpluginstallconfig.ini",0);
		editor=sp.edit();
	}
	public void putString(String key, String value){
		editor.putString(key, value);
		editor.commit();
	}
	public String getString(String key, String value){
		return sp.getString(key,value);
	}
	/**
	 * 
	 * @param mcontext    
	 * @param plugfile        assets目录下的文件�?  �? drag-sort-listview.apk
	 * @param version         当前安装的插件版本号   如下次更新宿主时，可以提高这个版本好，以让框架安装最新宿主assets目录下的插件
	 * @param callback        安装事件回掉接口
	 * @param startlevel      插件启动级别 小于2 插件会在框架启动时被自动启动
	 * @param isCheckVersion  是否对比当前安装的插件与已安装插件的版本好，如果为true�? 新插件与已安装插件版本相同将不被更新。如果为false时将不检测版本直接覆盖已安装插件
	 * @throws Exception
	 */
	public void install(BundleContext mcontext,String plugfile,String version,installCallback callback,int startlevel,boolean isCheckVersion) throws Exception{
		// startlevel设置�?2插件不会自启 isCheckVersion不检测插件版本覆盖更�?
		File f1=null;
		try {
				
				if(!DEBUG){
					//不是调试模式
					String PlugVersion=this.getString(plugfile,null);
					if(PlugVersion!=null){
						if(PlugVersion.equals(version)){
							//如果本地已安装的插件版本等与目前插件的版本，那么就不安装�?
							return ;
						}
					}
					InputStream in=mcontext.getAndroidContext().getAssets().open(plugfile);
					f1=new File(mcontext.getAndroidContext().getFilesDir(),plugfile);
					if(!f1.exists()){
						copy(in, f1);
					}
					agent.getService().install(mcontext, "file:"+f1.getAbsolutePath(),callback, startlevel,isCheckVersion,false,false);
					//安装完成后删除文�?
					f1.delete();
					//将最新的插件版本号保存到本地
					this.putString(plugfile, version);
				}else{
					InputStream in=mcontext.getAndroidContext().getAssets().open(plugfile);
					f1=new File(mcontext.getAndroidContext().getFilesDir(),plugfile);
					copy(in, f1);
					agent.getService().install(mcontext, "file:"+f1.getAbsolutePath(),callback, startlevel,isCheckVersion,false,false);
				}
				
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		} catch (Exception e) {
				// TODO 自动生成�? catch �?
				e.printStackTrace();
		} 
	}
	/**
	 * �? assets目录下文件拷贝到文件夹中
	 * @param is
	 * @param outputFile
	 * @throws IOException
	 */
	private void copy(InputStream is, File outputFile)
	        throws IOException
	    {
	        OutputStream os = null;

	        try
	        {
	            os = new BufferedOutputStream(
	                new FileOutputStream(outputFile),4096);
	            byte[] b = new byte[4096];
	            int len = 0;
	            while ((len = is.read(b)) != -1)
	                os.write(b, 0, len);
	        }
	        finally
	        {
	            if (is != null) is.close();
	            if (os != null) os.close();
	        }
	    }
}
