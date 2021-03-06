/***********************************************************************************

    Copyright (C) 2012-2014 Ahmet Öztürk (aoz_2@yahoo.com)

    This file is part of Lifeograph.

    Lifeograph is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Lifeograph is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Lifeograph.  If not, see <http://www.gnu.org/licenses/>.

 ***********************************************************************************/

package net.sourceforge.lifeograph;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class Lifeograph
{
    // CONSTANTS ===================================================================================
    //public static final String PROGRAM_NAME = "Lifeograph";
    public static final String LIFEOGRAPH_RELEASE_CODENAME = "artemisia";

    static final String LANG_INHERIT_DIARY = "d";

    // LIFEOGRAPH APPLICATION-WIDE FUNCTIONALITY ===================================================
    static boolean sSaveDiaryOnLogout = true;
    static boolean sFlagStartingDiaryEditingActivity = false;

    enum LoginStatus { LOGGED_OUT, LOGGED_IN }
    static LoginStatus sLoginStatus = LoginStatus.LOGGED_OUT;

    private enum PurchaseStatus { PS_UNKNOWN, PURCHASED, NOT_PURCHASED }
    private static PurchaseStatus mAdFreePurchased = PurchaseStatus.PS_UNKNOWN;

    static void setAdFreePurchased( boolean purchased ) {
        mAdFreePurchased = purchased ? PurchaseStatus.PURCHASED : PurchaseStatus.NOT_PURCHASED;
    }

    static boolean getAddFreeNotPurchased() {
        return( mAdFreePurchased == PurchaseStatus.NOT_PURCHASED );
    }

    static void showElem( DiaryElement elem ) {
        if( elem != null ) {
            switch( elem.get_type() ) {
                case ENTRY: {
                    Intent i = new Intent( sContext, ActivityEntry.class );
                    i.putExtra( "entry", elem.get_date_t() );
                    sFlagStartingDiaryEditingActivity = true;
                    sContext.startActivity( i );
                    break;
                }
                case TAG:
                case UNTAGGED:
                case CHAPTER:
                case TOPIC:
                case GROUP: {
                    Intent i = new Intent( sContext, ActivityChapterTag.class );
                    i.putExtra( "elem", elem.get_id() );
                    i.putExtra( "type", elem.get_type().i );
                    sFlagStartingDiaryEditingActivity = true;
                    sContext.startActivity( i );
                    break;
                }
            }
        }
        else
            Log.e( TAG, "null element passed to showElem" );
    }

    static void prepareForLogout() {
        Log.d( Lifeograph.TAG, "Lifeograph.prepareForLogout()" );

        if( sLoginStatus == LoginStatus.LOGGED_IN ) {
            if( sSaveDiaryOnLogout && !Diary.diary.is_read_only() ) {
                if( Diary.diary.write() == Result.SUCCESS )
                    showToast( "Diary saved successfully" );
                else
                    showToast( "Cannot write back changes" );
            }
            else
                Log.d( Lifeograph.TAG, "Diary is not saved" );
        }
    }

    static void handleDiaryEditingActivityDestroyed() {
        if( !sFlagStartingDiaryEditingActivity )
            prepareForLogout();
        sFlagStartingDiaryEditingActivity = false;
    }

    // ANDROID & JAVA HELPERS ======================================================================
    public static final String TAG = "LFO";

    static Context sContext = null;
    private static float sScreenWidth;
    private static float sScreenHeight;
    static float sDPIX;
    static float sDPIY;
    static final float MIN_HEIGHT_FOR_NO_EXTRACT_UI = 6.0f;

    public static String getStr( int i ) {
        if( sContext == null )
            return "CONTEXT IS NOT READY. SOMETHING IS WRONG!";
        else
            return sContext.getString( i );
    }

    static void showConfirmationPrompt( int message,
                                        int positiveText,
                                        DialogInterface.OnClickListener posListener,
                                        DialogInterface.OnClickListener negListener ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( sContext );
        builder.setMessage( message )
               .setPositiveButton( positiveText, posListener )
               .setNegativeButton( R.string.cancel, negListener );

        //AlertDialog alert = builder.create();
        builder.show();
    }

    static void showToast( String message ) {
        Toast.makeText( sContext, message, Toast.LENGTH_LONG ).show();
    }
    static void showToast( int message ) {
        Toast.makeText( sContext, message, Toast.LENGTH_LONG ).show();
    }

    static String joinPath( String p1, String p2) {
        File file1 = new File( p1 );
        return new File( file1, p2 ).getPath();
    }

//    public static String getEnvLang() {
//        return Locale.getDefault().getLanguage();
//    }

//    public static void initializeConstants( Context ctx ) {
//        sContext = ctx;
//        sIsLargeScreen = ( ctx.getResources().getConfiguration().screenLayout
//                & Configuration.SCREENLAYOUT_SIZE_MASK ) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
//    }

    static void updateScreenSizes() {
        if( sContext == null ) {
            Log.e( TAG, "Failed to update screen sizes" );
            return;
        }

        WindowManager wm = ( WindowManager ) sContext.getSystemService( Context.WINDOW_SERVICE );
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics( metrics );
        sScreenWidth = metrics.widthPixels / metrics.xdpi;
        sScreenHeight = metrics.heightPixels / metrics.ydpi;
        sDPIX = metrics.xdpi;
        sDPIY = metrics.ydpi;
        Log.d( TAG, "Updated the sizes: " + sScreenWidth + "x" + sScreenHeight );
    }

    static float getScreenShortEdge() {
        return( sScreenHeight > sScreenWidth ? sScreenWidth : sScreenHeight );
    }
    static float getScreenWidth() {
        return sScreenWidth;
    }
    static float getScreenHeight() {
        return sScreenHeight;
    }
//    public static boolean isLargeScreen() {
//        return sIsLargeScreen;
//    }

    static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals( state );
    }

}
