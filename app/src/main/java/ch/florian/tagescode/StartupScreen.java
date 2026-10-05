package ch.florian.tagescode;

import android.app.Activity;
import android.view.Gravity;
import android.widget.*;

/** Branded launch overlay, using unmodified day/night source images. */
final class StartupScreen {
    static void show(Activity activity, Runnable ready) {
        boolean dark=(activity.getResources().getConfiguration().uiMode & 48)==32;
        int foreground=dark?0xFFF1F4F6:0xFF171A1E, muted=dark?0xFF9CA3AA:0xFF6E767E;
        android.view.ViewGroup host=activity.findViewById(android.R.id.content);
        FrameLayout splash=new FrameLayout(activity);splash.setBackgroundColor(dark?0xFF14171B:0xFFF4F6F8);splash.setClickable(true);
        LinearLayout group=new LinearLayout(activity);group.setOrientation(1);group.setGravity(Gravity.CENTER);
        ImageView logo=new ImageView(activity);logo.setImageResource(R.drawable.splash_original_logo);logo.setScaleType(ImageView.ScaleType.FIT_CENTER);logo.setContentDescription("Videojet");
        int width=Math.min(activity.getResources().getDisplayMetrics().widthPixels-dp(activity,64),dp(activity,340));
        group.addView(logo,new LinearLayout.LayoutParams(width,Math.round(width*900f/3900f)));
        LinearLayout swiss=new LinearLayout(activity);swiss.setGravity(Gravity.CENTER);
        ImageView cross=new ImageView(activity);cross.setImageResource(R.drawable.ic_swiss_badge);swiss.addView(cross,new LinearLayout.LayoutParams(dp(activity,13),dp(activity,13)));
        TextView country=new TextView(activity);country.setText("  Schweiz");country.setTextSize(14);country.setTextColor(muted);swiss.addView(country);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,-2);sp.topMargin=dp(activity,16);group.addView(swiss,sp);
        TextView title=new TextView(activity);title.setText("Tagescode");title.setLetterSpacing(0.18f);title.setTextSize(17);title.setTextColor(foreground);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-2,-2);tp.topMargin=dp(activity,14);group.addView(title,tp);
        splash.addView(group,new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER));
        TextView version=new TextView(activity);version.setText("Version "+BuildConfig.VERSION_NAME);version.setTextColor(muted);version.setTextSize(11);version.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams vp=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);vp.bottomMargin=dp(activity,52);splash.addView(version,vp);
        host.addView(splash,new android.view.ViewGroup.LayoutParams(-1,-1));
        splash.postDelayed(()->{host.removeView(splash);if(!activity.isFinishing()&&!activity.isDestroyed())ready.run();},700);
    }
    private static int dp(Activity a,int n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
}
