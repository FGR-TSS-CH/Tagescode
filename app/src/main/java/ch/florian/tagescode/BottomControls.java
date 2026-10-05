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
    private android.app.Dialog activeDialog;
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
        for(Button b:new Button[]{watch,check,txt,info}) {
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(62),1);
            if(b!=watch)params.leftMargin=dp(8);
            buttons.addView(b,params);
        }
        watch.setOnClickListener(v->send.run());check.setOnClickListener(v->verify.run());
        txt.setOnClickListener(v->showFile());info.setOnClickListener(v->showInfo());footer.addView(buttons);
        LinearLayout state=new LinearLayout(a);state.setGravity(17);state.setPadding(0,dp(9),0,dp(9));
        dot=new View(a);state.addView(dot,new LinearLayout.LayoutParams(dp(7),dp(7)));
        progress=new ProgressBar(a);state.addView(progress,new LinearLayout.LayoutParams(dp(18),dp(18)));
        status=new TextView(a);status.setTextSize(12);status.setPadding(dp(8),0,0,0);state.addView(status,new LinearLayout.LayoutParams(-2,-2));status.setGravity(17);footer.addView(state);LinearLayout.LayoutParams creditParams=new LinearLayout.LayoutParams(-2,-2);creditParams.topMargin=dp(6);footer.addView(credit,creditParams);
    }
    private boolean dark(){return (activity.getResources().getConfiguration().uiMode & 48)==32;}
    private android.graphics.drawable.GradientDrawable rounded(int color,int radius,int stroke){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
    private int dp(int n){return Math.round(n*activity.getResources().getDisplayMetrics().density);}
    private Button button(String text){Button b=new Button(activity);b.setText(text);b.setAllCaps(false);b.setTextSize(13);b.setMinWidth(0);b.setPadding(0,dp(8),0,dp(8));b.setMinimumHeight(0);b.setMinimumWidth(0);
        b.setStateListAnimator(null);b.setElevation(0);b.setBackgroundTintList(null);
        b.setTextColor(dark()?0xFFEFF3F6:0xFF005D9C);
        b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x33577790), rounded(dark()?0xFF253442:Color.WHITE,12,0xFF577790),null));
        int icon=text.equals("Uhr")?R.drawable.ic_action_watch:text.equals("Prüfen")?R.drawable.ic_action_check:text.equals("TXT")?R.drawable.ic_action_file:text.equals("Info")?R.drawable.ic_action_info:0;
        if(icon!=0){android.graphics.drawable.Drawable d=activity.getDrawable(icon);d.setBounds(0,0,dp(20),dp(20));d.setTint(activity.getColor(R.color.text_secondary));b.setCompoundDrawables(null,d,null,null);b.setCompoundDrawablePadding(dp(2));}
        return b;}
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
    private String lastError(){String value=activity.getSharedPreferences("code_import_status",0).getString("last_error","");return value==null||value.isEmpty()?"Keiner":value;}
    private String latestDate(){String value=CodeRepository.latestAvailableDate(activity);return value==null?"Keine":LocalDate.parse(value).format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));}
    private String stamp(long time){return time==0?"Noch nicht erfasst":new SimpleDateFormat("dd.MM.yyyy · HH:mm",Locale.GERMAN).format(new Date(time));}
    private String source(){return CloudCodeFileAccess.savedDisplayName(activity);}
    private LinearLayout panel(){LinearLayout p=new LinearLayout(activity);p.setOrientation(1);p.setPadding(dp(20),dp(8),dp(20),dp(12));return p;}
    private void row(LinearLayout parent,String label,String value){LinearLayout line=new LinearLayout(activity);line.setPadding(dp(8),dp(9),dp(8),dp(9));line.setBackgroundColor(dark()?0xFF1C252C:0xFFF0F3F5);TextView key=new TextView(activity),val=new TextView(activity);key.setText(label);val.setText(value);key.setTextSize(13);val.setTextSize(13);key.setTextColor(dark()?0xFFB6C6D0:0xFF526572);val.setTextColor(dark()?0xFFEDF2F6:0xFF161A1E);line.addView(key,new LinearLayout.LayoutParams(0,-2,1));line.addView(val,new LinearLayout.LayoutParams(0,-2,1));parent.addView(line);View divider=new View(activity);divider.setBackgroundColor(0x33577790);parent.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));}
    private void showInfo(){LinearLayout p=panel();LinearLayout themes=new LinearLayout(activity);for(boolean dark:new boolean[]{false,true}){Button b=button(dark?"Dunkel":"Hell");boolean active=dark==dark();
            if(active){b.setBackground(rounded(0xFF005D9C,9,0));b.setTextColor(Color.WHITE);}
            b.setOnClickListener(v->{activity.getSharedPreferences("appearance",0).edit().putInt("mode",dark?32:16).apply();if(activeDialog!=null)activeDialog.dismiss();activity.recreate();});LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,dp(40),1);tp.setMargins(dp(3),0,dp(3),dp(14));themes.addView(b,tp);}p.addView(themes);
        row(p,"App-Version",BuildConfig.VERSION_NAME+" (FGR)");row(p,"Builddatum",BuildConfig.BUILD_DATE);row(p,"Datenquelle",source());row(p,"Letzte Prüfung",stamp(activity.getSharedPreferences("code_import_status",0).getLong("last_attempt",0)));row(p,"Letzter Import",stamp(CodeRepository.lastNewCodesImport(activity)));row(p,"Neue Codes bei letzter Prüfung",String.valueOf(activity.getSharedPreferences("code_import_status",0).getInt("last_added",0)));row(p,"Gespeicherte Codes",String.valueOf(CodeRepository.codeCount(activity)));row(p,"Letzte Widget-Aktualisierung",stamp(activity.getSharedPreferences("code_import_status",0).getLong("widget_updated",0)));row(p,"Codes verfügbar bis",latestDate());row(p,"Letzte Garmin-Übertragung",stamp(activity.getSharedPreferences("garmin",0).getLong("last_background_transfer",0)));row(p,"Status",status.getText().toString());row(p,"Letzter Fehler",lastError());ScrollView scroll=new ScrollView(activity);scroll.addView(p);showDialog("Informationen",scroll);}
    private void showFile(){
        LinearLayout p=panel();
        TextView intro=new TextView(activity);intro.setText("Wähle Tagescodes.txt aus deinem OneDrive. Der Zugriff wird dauerhaft gespeichert.");intro.setTextSize(14);p.addView(intro);
        LinearLayout card=new LinearLayout(activity);card.setOrientation(1);card.setPadding(dp(14),dp(14),dp(14),dp(14));card.setBackground(rounded(dark()?0xFF1C252C:0xFFF0F3F5,12,0));
        TextView label=new TextView(activity);label.setText("Aktuell ausgewählt");label.setTextSize(12);card.addView(label);
        TextView name=new TextView(activity);name.setText(source());name.setTextSize(16);name.setTypeface(null,1);card.addView(name);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=dp(18);p.addView(card,cp);
        Button select=button("Datei auswählen");select.setTextColor(Color.WHITE);select.setBackground(rounded(0xFF005D9C,12,0));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(48));bp.topMargin=dp(16);p.addView(select,bp);
        TextView note=new TextView(activity);note.setText("Hinweis\n\nDie Datei wird in der Regel einmal pro Woche durch Power Automate aktualisiert. Du musst sie nur einmal auswählen.");note.setTextSize(13);note.setPadding(dp(14),dp(14),dp(14),dp(14));note.setBackground(rounded(dark()?0xFF1C252C:0xFFF0F3F5,12,0));LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.topMargin=dp(18);p.addView(note,np);
        android.app.Dialog dialog=createDialog("TXT-Datei",p);select.setOnClickListener(v->{dialog.dismiss();picker.run();});displayDialog(dialog);
    }
    private android.app.Dialog createDialog(String title,View content){
        android.app.Dialog dialog=new android.app.Dialog(activity);dialog.requestWindowFeature(1);
        LinearLayout shell=new LinearLayout(activity);shell.setOrientation(1);shell.setPadding(dp(8),dp(12),dp(8),dp(12));
        shell.setBackground(rounded(dark()?0xFF10181E:Color.WHITE,20,dark()?0xFF303B44:0xFFCBD3DA));
        LinearLayout heading=new LinearLayout(activity);heading.setGravity(16);heading.setPadding(dp(12),0,dp(8),dp(12));
        TextView caption=new TextView(activity);caption.setText(title);caption.setTextSize(20);caption.setTypeface(null,1);caption.setTextColor(dark()?0xFFEDF2F6:0xFF161A1E);
        heading.addView(caption,new LinearLayout.LayoutParams(0,-2,1));Button close=button("Schliessen");close.setTextSize(11);close.setBackgroundColor(Color.TRANSPARENT);close.setOnClickListener(v->dialog.dismiss());heading.addView(close,new LinearLayout.LayoutParams(dp(76),dp(40)));shell.addView(heading);
        shell.addView(content,new LinearLayout.LayoutParams(-1,-2));dialog.setContentView(shell);dialog.setCanceledOnTouchOutside(true);return dialog;
    }
    private void displayDialog(android.app.Dialog dialog){if(activeDialog!=null && activeDialog.isShowing())activeDialog.dismiss();activeDialog=dialog;dialog.show();android.view.Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));int width=Math.min(activity.getResources().getDisplayMetrics().widthPixels-dp(32),dp(460));window.setLayout(width,-2);}}
    private void showDialog(String title,View content){displayDialog(createDialog(title,content));}

}
