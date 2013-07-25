/****************************************************************************
Copyright (c) 2010-2011 cocos2d-x.org

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ****************************************************************************/
package org.cocos2dx.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

public class Cocos2dxTypefaces {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private static final HashMap<String, Typeface> sTypefaceCache = new HashMap<String, Typeface>();

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public static synchronized Typeface get(final Context pContext, final String pAssetName) {
		if (!Cocos2dxTypefaces.sTypefaceCache.containsKey(pAssetName)) {
			//final Typeface typeface = Typeface.createFromAsset(pContext.getAssets(), pAssetName);
			String pPath = pAssetName;
			try {
				ZipFile zf= new ZipFile(Cocos2dxHelper.getApkPath());
			    ZipEntry ze = zf.getEntry("assets/" + pPath);
			    if (ze == null) {
			        return null;
			    }
				
			    String extension = pPath.substring(pPath.indexOf("."));
			    
			    InputStream in = zf.getInputStream(ze);
		        File f = File.createTempFile("_FONT_", extension);
		        FileOutputStream out = new FileOutputStream(f);
		        
		        byte[] buffer = new byte[1024];
	            while(in.read(buffer) > -1) {
	                out.write(buffer);   
	            }
	            in.close();
	            out.close();
				
	            final Typeface typeface = Typeface.createFromFile(f);
	            Cocos2dxTypefaces.sTypefaceCache.put(pAssetName, typeface);
			} catch (final Exception e) {
				
				Log.e("Cocos2dxTypefaces", "error: " + e.getMessage(), e);
			}
		}

		return Cocos2dxTypefaces.sTypefaceCache.get(pAssetName);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
