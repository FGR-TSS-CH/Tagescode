package ch.florian.tagescode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class BottomControls {
    private final Activity activity;
    private final TextView status;
    private final ProgressBar progress;
    private final Button check;
    private final Button watch;
    private final View dot;
    private final Runnable picker;
    BottomControls(Activity a, TextView oldVersion, Runnable verify, Runnable choose, Runnable send) {
        activity=a; picker=choose;
        LinearLayout footer=(LinearLayout)oldVersion.getParent();
        View credit=footer.getChildAt(0); footer.removeView(credit);
        if(credit instanceof LinearLayout){LinearLayout c=(LinearLayout)credit;for(int i=0;i<c.getChildCount();i++)if(c.getChildAt(i) instanceof TextView)((TextView)c.getChildAt(i)).setTextSize(11);}
        for(int i=0;i<footer.getChildCount();i++)footer.getChildAt(i).setVisibility(View.GONE);
        LinearLayout buttons=new LinearLayout(a);
        watch=button("Uhr"); Button txt=button("TXT"), info=button("Info"); check=button("Prüfen");
        for(Button b:new Button[]{watch,check,txt,info})buttons.addView(b,new LinearLayout.LayoutParams(0,dp(56),1));
        watch.setOnClickListener(v->send.run());check.setOnClickListener(v->verify.run());
        txt.setOnClickListener(v->showFile());info.setOnClickListener(v->showInfo());footer.addView(buttons);
        LinearLayout state=new LinearLayout(a);state.setGravity(17);state.setPadding(0,dp(9),0,dp(9));
        dot=new View(a);state.addView(dot,new LinearLayout.LayoutParams(dp(7),dp(7)));
        progress=new ProgressBar(a);state.addView(progress,new LinearLayout.LayoutParams(dp(18),dp(18)));
        status=new TextView(a);status.setTextSize(12);status.setPadding(dp(8),0,0,0);state.addView(status);footer.addView(state);footer.addView(credit);
    }
    private int dp(int n){return Math.round(n*activity.getResources().getDisplayMetrics().density);}
    private Button button(String text){Button b=new Button(activity);b.setText(text);b.setAllCaps(false);b.setTextSize(13);b.setMinWidth(0);b.setPadding(0,0,0,0);return b;}
    void update(boolean busy){
        for(int id:new int[]{R.id.availabilityView,R.id.dataStatusView,R.id.checkStatusView,R.id.buildInfoView})activity.findViewById(id).setVisibility(View.GONE);
        check.setEnabled(!busy);progress.setVisibility(busy?View.VISIBLE:View.GONE);
        String message;int color=0xFFB18512;
        if(busy){message="Daten werden geprüft …";color=0xFF579FCB;}
        else if(!CloudCodeFileAccess.hasSavedFile(activity))message="Keine TXT-Datei ausgewählt";
        else if(CodeRepository.lastCheckFailed(activity)){message=activity.getSharedPreferences("code_import_status",0).getString("last_error","Import fehlgeschlagen");color=0xFFDC5264;}
        else if(!CodeRepository.getCodeForToday(activity).matches("[0-9]{6}")){message="Tagescode für heute fehlt";color=0xFFDC5264;}
        else if(CodeRepository.lastSuccessfulImport(activity)==0)message="Prüfung erforderlich";
        else {LocalDate tomorrow=LocalDate.now().plusDays(1);boolean exists=CodeRepository.getCodeForDate(activity,tomorrow.getYear(),tomorrow.getMonthValue()-1,tomorrow.getDayOfMonth()).matches("[0-9]{6}");message=exists?"Tagescodes aktuell":"Für morgen ist noch kein Tagescode verfügbar";if(exists)color=0xFF35A56F;}
        status.setText(message);status.setTextColor(activity.getColor(R.color.text_secondary));
        android.graphics.drawable.GradientDrawable circle=new android.graphics.drawable.GradientDrawable();circle.setShape(1);circle.setColor(color);dot.setBackground(circle);dot.setVisibility(busy?View.GONE:View.VISIBLE);
    }
    void watchBusy(boolean busy){watch.setEnabled(!busy);watch.setText(busy?"Sendet …":"Uhr");}
    void importing(){status.setText("Neue Codes werden importiert …");}
    private String stamp(long time){return time==0?"Noch nicht erfasst":new SimpleDateFormat("dd.MM.yyyy · HH:mm",Locale.GERMAN).format(new Date(time));}
    private String source(){String uri=activity.getSharedPreferences("tagescode_cloud_storage",Context.MODE_PRIVATE).getString("cloud_code_file_uri",null);if(uri==null)return "Keine Datei ausgewählt";try(android.database.Cursor c=activity.getContentResolver().query(android.net.Uri.parse(uri),new String[]{android.provider.OpenableColumns.DISPLAY_NAME},null,null,null)){if(c!=null&&c.moveToFirst())return c.getString(0);}catch(Exception ignored){}return "Gespeicherte TXT-Datei";}
    private LinearLayout panel(){LinearLayout p=new LinearLayout(activity);p.setOrientation(1);p.setPadding(dp(20),dp(8),dp(20),dp(12));return p;}
    private void row(LinearLayout parent,String label,String value){LinearLayout line=new LinearLayout(activity);line.setPadding(dp(8),dp(9),dp(8),dp(9));line.setBackgroundColor(activity.getColor(R.color.window_background));TextView key=new TextView(activity),val=new TextView(activity);key.setText(label);val.setText(value);key.setTextSize(13);val.setTextSize(13);line.addView(key,new LinearLayout.LayoutParams(0,-2,1));line.addView(val,new LinearLayout.LayoutParams(0,-2,1));parent.addView(line);View divider=new View(activity);divider.setBackgroundColor(0x33577790);parent.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));}
    private void showInfo(){LinearLayout p=panel();LinearLayout themes=new LinearLayout(activity);for(boolean dark:new boolean[]{false,true}){Button b=button(dark?"Dunkel":"Hell");b.setOnClickListener(v->{activity.getSharedPreferences("appearance",0).edit().putInt("mode",dark?32:16).apply();activity.recreate();});themes.addView(b,new LinearLayout.LayoutParams(0,dp(48),1));}p.addView(themes);
        row(p,"App-Version",BuildConfig.VERSION_NAME+" (FGR)");row(p,"Builddatum",BuildConfig.BUILD_DATE);row(p,"Datenquelle",source());row(p,"Letzte Prüfung",stamp(activity.getSharedPreferences("code_import_status",0).getLong("last_attempt",0)));row(p,"Letzter Import",stamp(CodeRepository.lastNewCodesImport(activity)));row(p,"Neue Codes",String.valueOf(activity.getSharedPreferences("code_import_status",0).getInt("last_added",0)));row(p,"Gespeicherte Codes",String.valueOf(CodeRepository.codeCount(activity)));row(p,"Letzte Widget-Aktualisierung",stamp(activity.getSharedPreferences("code_import_status",0).getLong("widget_updated",0)));row(p,"Codes verfügbar bis",String.valueOf(CodeRepository.latestAvailableDate(activity)));row(p,"Letzte Garmin-Übertragung",stamp(activity.getSharedPreferences("garmin",0).getLong("last_background_transfer",0)));row(p,"Status",status.getText().toString());row(p,"Letzter Fehler",activity.getSharedPreferences("code_import_status",0).getString("last_error","Keiner erfasst"));ScrollView scroll=new ScrollView(activity);scroll.addView(p);new AlertDialog.Builder(activity).setTitle("Informationen").setView(scroll).setPositiveButton("Schliessen",null).show();}
    private void showFile(){LinearLayout p=panel();TextView intro=new TextView(activity);intro.setText("Wähle Tagescodes.txt aus deinem OneDrive. Der Zugriff wird dauerhaft gespeichert.");p.addView(intro);row(p,"Aktuell ausgewählt",source());Button select=button("Datei auswählen");p.addView(select);TextView note=new TextView(activity);note.setPadding(0,dp(18),0,dp(12));note.setText("Hinweis\nDie Datei wird in der Regel einmal pro Woche durch Power Automate aktualisiert. Du musst sie nur einmal auswählen.");p.addView(note);AlertDialog dialog=new AlertDialog.Builder(activity).setTitle("Tagescodes.txt auswählen").setView(p).setNegativeButton("Schliessen",null).create();select.setTextColor(Color.WHITE); select.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF005D9C));select.setOnClickListener(v->{dialog.dismiss();picker.run();});dialog.show();}
}
