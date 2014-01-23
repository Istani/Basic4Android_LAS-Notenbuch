package de.informatikteam.notenbuch;

import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = true;
	public static final boolean includeTitle = false;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFirst) {
			processBA = new BA(this.getApplicationContext(), null, null, "de.informatikteam.notenbuch", "de.informatikteam.notenbuch.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		mostCurrent = this;
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
		BA.handler.postDelayed(new WaitForLayout(), 5);

	}
	private static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "de.informatikteam.notenbuch", "de.informatikteam.notenbuch.main");
        anywheresoftware.b4a.keywords.Common.ToastMessageShow("This application was developed with Basic4android trial version and should not be distributed.", true);
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "de.informatikteam.notenbuch.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
		return true;
	}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEvent(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
		this.setIntent(intent);
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null) //workaround for emulator bug (Issue 2423)
            return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
			if (mostCurrent == null || mostCurrent != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
		    processBA.raiseEvent(mostCurrent._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.sql.SQL _sql1 = null;
public static anywheresoftware.b4a.sql.SQL.CursorWrapper _cursor1 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _klassen_bezeichnung = null;
public anywheresoftware.b4a.objects.ScrollViewWrapper _list_klassen = null;
public anywheresoftware.b4a.objects.ScrollViewWrapper _list_schueler = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b_speichern = null;
public anywheresoftware.b4a.objects.PanelWrapper _camera = null;
public anywheresoftware.b4a.objects.ImageViewWrapper _schueler_bild = null;
public anywheresoftware.b4a.objects.ScrollViewWrapper _schueler_add_scroll = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edit_name = null;
public anywheresoftware.b4a.objects.EditTextWrapper _edit_notiz = null;
public anywheresoftware.b4a.objects.ScrollViewWrapper _auswahl_klassen = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b_main = null;
public anywheresoftware.b4a.objects.PanelWrapper _popup = null;
public static int _glob_klasse_auswahl_schuelerliste = 0;
public anywheresoftware.b4a.objects.LabelWrapper _label2 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton1 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton10 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton11 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton12 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton2 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton3 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton4 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton5 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton6 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton7 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton8 = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglebutton9 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b_noten_back = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b_save_noten = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 57;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 60;BA.debugLine="Activity.LoadLayout(\"main\")";
mostCurrent._activity.LoadLayout("main",mostCurrent.activityBA);
 //BA.debugLineNum = 61;BA.debugLine="If File.Exists(File.DirInternal,\"db.sql\") = False Then";
if (anywheresoftware.b4a.keywords.Common.File.Exists(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"db.sql")==anywheresoftware.b4a.keywords.Common.False) { 
 //BA.debugLineNum = 62;BA.debugLine="File.Copy(File.DirAssets,\"db.sql\",File.DirInternal,\"db.sql\")";
anywheresoftware.b4a.keywords.Common.File.Copy(anywheresoftware.b4a.keywords.Common.File.getDirAssets(),"db.sql",anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"db.sql");
 };
 //BA.debugLineNum = 65;BA.debugLine="If SQL1.IsInitialized = False Then";
if (_sql1.IsInitialized()==anywheresoftware.b4a.keywords.Common.False) { 
 //BA.debugLineNum = 66;BA.debugLine="SQL1.Initialize(File.DirInternal, \"db.sql\", False)";
_sql1.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"db.sql",anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 70;BA.debugLine="End Sub";
return "";
}
public static boolean  _activity_keypress(int _keycode) throws Exception{
 //BA.debugLineNum = 72;BA.debugLine="Sub Activity_KeyPress (KeyCode As Int) As Boolean";
 //BA.debugLineNum = 73;BA.debugLine="If KeyCode = KeyCodes.KEYCODE_BACK Then";
if (_keycode==anywheresoftware.b4a.keywords.Common.KeyCodes.KEYCODE_BACK) { 
 //BA.debugLineNum = 74;BA.debugLine="If Msgbox2(\"Programm Beenden?\", \"\", \"Ja\", \"Nein\", \"\", Null) = DialogResponse.POSITIVE Then";
if (anywheresoftware.b4a.keywords.Common.Msgbox2("Programm Beenden?","","Ja","Nein","",(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.Null),mostCurrent.activityBA)==anywheresoftware.b4a.keywords.Common.DialogResponse.POSITIVE) { 
 //BA.debugLineNum = 75;BA.debugLine="ExitApplication 'App is exiting";
anywheresoftware.b4a.keywords.Common.ExitApplication();
 }else {
 //BA.debugLineNum = 77;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 };
 };
 //BA.debugLineNum = 80;BA.debugLine="End Sub";
return false;
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 86;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 88;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 82;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 84;BA.debugLine="End Sub";
return "";
}
public static String  _b_foto_click() throws Exception{
anywheresoftware.b4a.objects.CameraW _cam = null;
 //BA.debugLineNum = 237;BA.debugLine="Sub B_FOTO_Click";
 //BA.debugLineNum = 238;BA.debugLine="Change_Layout(\"schueler_bild\")";
_change_layout("schueler_bild");
 //BA.debugLineNum = 239;BA.debugLine="Dim Cam As Camera";
_cam = new anywheresoftware.b4a.objects.CameraW();
 //BA.debugLineNum = 240;BA.debugLine="Cam.Initialize(Camera,\"Camera1\")";
_cam.Initialize(mostCurrent.activityBA,(android.view.ViewGroup)(mostCurrent._camera.getObject()),"Camera1");
 //BA.debugLineNum = 241;BA.debugLine="End Sub";
return "";
}
public static String  _b_kamera_speichern_click() throws Exception{
anywheresoftware.b4a.objects.CameraW _cam = null;
 //BA.debugLineNum = 232;BA.debugLine="Sub B_Kamera_Speichern_Click";
 //BA.debugLineNum = 233;BA.debugLine="Dim Cam As Camera";
_cam = new anywheresoftware.b4a.objects.CameraW();
 //BA.debugLineNum = 234;BA.debugLine="Cam.Initialize(Camera,\"Camera1\")";
_cam.Initialize(mostCurrent.activityBA,(android.view.ViewGroup)(mostCurrent._camera.getObject()),"Camera1");
 //BA.debugLineNum = 235;BA.debugLine="Cam.TakePicture";
_cam.TakePicture();
 //BA.debugLineNum = 236;BA.debugLine="End Sub";
return "";
}
public static String  _b_main_click() throws Exception{
 //BA.debugLineNum = 226;BA.debugLine="Sub B_Main_Click";
 //BA.debugLineNum = 227;BA.debugLine="Change_Layout(\"Main\")";
_change_layout("Main");
 //BA.debugLineNum = 228;BA.debugLine="End Sub";
return "";
}
public static String  _b_noten_back_click() throws Exception{
 //BA.debugLineNum = 498;BA.debugLine="Sub B_NOTEN_BACK_Click";
 //BA.debugLineNum = 499;BA.debugLine="B_NOTEN_BACK.tag=glob_klasse_auswahl_schuelerliste";
mostCurrent._b_noten_back.setTag((Object)(_glob_klasse_auswahl_schuelerliste));
 //BA.debugLineNum = 500;BA.debugLine="Btn_Click";
_btn_click();
 //BA.debugLineNum = 501;BA.debugLine="End Sub";
return "";
}
public static String  _b_save_noten_click() throws Exception{
String _note = "";
String _multi = "";
String _sid = "";
 //BA.debugLineNum = 475;BA.debugLine="Sub B_SAVE_NOTEN_Click";
 //BA.debugLineNum = 476;BA.debugLine="Dim note As String, multi As String, sid As String";
_note = "";
_multi = "";
_sid = "";
 //BA.debugLineNum = 477;BA.debugLine="sid=Label2.Tag";
_sid = BA.ObjectToString(mostCurrent._label2.getTag());
 //BA.debugLineNum = 479;BA.debugLine="If (ToggleButton1.Checked) Then note=\"1\"";
if ((mostCurrent._togglebutton1.getChecked())) { 
_note = "1";};
 //BA.debugLineNum = 480;BA.debugLine="If (ToggleButton2.Checked) Then note=\"2\"";
if ((mostCurrent._togglebutton2.getChecked())) { 
_note = "2";};
 //BA.debugLineNum = 481;BA.debugLine="If (ToggleButton3.Checked) Then note=\"3\"";
if ((mostCurrent._togglebutton3.getChecked())) { 
_note = "3";};
 //BA.debugLineNum = 482;BA.debugLine="If (ToggleButton4.Checked) Then note=\"4\"";
if ((mostCurrent._togglebutton4.getChecked())) { 
_note = "4";};
 //BA.debugLineNum = 483;BA.debugLine="If (ToggleButton5.Checked) Then note=\"5\"";
if ((mostCurrent._togglebutton5.getChecked())) { 
_note = "5";};
 //BA.debugLineNum = 484;BA.debugLine="If (ToggleButton6.Checked) Then note=\"6\"";
if ((mostCurrent._togglebutton6.getChecked())) { 
_note = "6";};
 //BA.debugLineNum = 486;BA.debugLine="If (ToggleButton7.Checked) Then note=note & \".3\"";
if ((mostCurrent._togglebutton7.getChecked())) { 
_note = _note+".3";};
 //BA.debugLineNum = 487;BA.debugLine="If (ToggleButton8.Checked) Then note=note & \".5\"";
if ((mostCurrent._togglebutton8.getChecked())) { 
_note = _note+".5";};
 //BA.debugLineNum = 488;BA.debugLine="If (ToggleButton9.Checked) Then note=note & \".7\"";
if ((mostCurrent._togglebutton9.getChecked())) { 
_note = _note+".7";};
 //BA.debugLineNum = 490;BA.debugLine="If (ToggleButton10.Checked) Then multi= \"1\"";
if ((mostCurrent._togglebutton10.getChecked())) { 
_multi = "1";};
 //BA.debugLineNum = 491;BA.debugLine="If (ToggleButton11.Checked) Then multi= \"2\"";
if ((mostCurrent._togglebutton11.getChecked())) { 
_multi = "2";};
 //BA.debugLineNum = 492;BA.debugLine="If (ToggleButton12.Checked) Then multi= \"3\"";
if ((mostCurrent._togglebutton12.getChecked())) { 
_multi = "3";};
 //BA.debugLineNum = 495;BA.debugLine="B_SAVE_NOTEN.tag=glob_klasse_auswahl_schuelerliste";
mostCurrent._b_save_noten.setTag((Object)(_glob_klasse_auswahl_schuelerliste));
 //BA.debugLineNum = 496;BA.debugLine="Btn_Click";
_btn_click();
 //BA.debugLineNum = 497;BA.debugLine="End Sub";
return "";
}
public static String  _b_schueler_speichern_click() throws Exception{
int _newid = 0;
int _i = 0;
int _schuler_id = 0;
anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper _cb = null;
String _sf = "";
String _kid = "";
anywheresoftware.b4a.objects.ConcreteViewWrapper _v = null;
 //BA.debugLineNum = 268;BA.debugLine="Sub B_Schueler_Speichern_Click";
 //BA.debugLineNum = 269;BA.debugLine="Dim NewID As Int";
_newid = 0;
 //BA.debugLineNum = 270;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT schueler_id FROM schueler\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT schueler_id FROM schueler")));
 //BA.debugLineNum = 271;BA.debugLine="If cursor1.RowCount > 0 Then";
if (_cursor1.getRowCount()>0) { 
 //BA.debugLineNum = 272;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step226 = 1;
final int limit226 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0); (step226 > 0 && _i <= limit226) || (step226 < 0 && _i >= limit226); _i = ((int)(0 + _i + step226))) {
 //BA.debugLineNum = 273;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 274;BA.debugLine="NewID = cursor1.GetInt(\"schueler_id\")";
_newid = _cursor1.GetInt("schueler_id");
 }
};
 };
 //BA.debugLineNum = 277;BA.debugLine="NewID = NewID +1 ' add 1 to the ID number to make a new ID field";
_newid = (int) (_newid+1);
 //BA.debugLineNum = 278;BA.debugLine="SQL1.ExecNonQuery(\"INSERT INTO schueler VALUES('\" & NewID & \"','\" & EDIT_Name.Text & \"','','','','','\" & EDIT_Notiz.text & \"')\")";
_sql1.ExecNonQuery("INSERT INTO schueler VALUES('"+BA.NumberToString(_newid)+"','"+mostCurrent._edit_name.getText()+"','','','','','"+mostCurrent._edit_notiz.getText()+"')");
 //BA.debugLineNum = 279;BA.debugLine="EDIT_Name.Text=\"\"";
mostCurrent._edit_name.setText((Object)(""));
 //BA.debugLineNum = 280;BA.debugLine="EDIT_Notiz.Text=\"\"";
mostCurrent._edit_notiz.setText((Object)(""));
 //BA.debugLineNum = 282;BA.debugLine="Dim schuler_id As Int";
_schuler_id = 0;
 //BA.debugLineNum = 283;BA.debugLine="schuler_id=NewID";
_schuler_id = _newid;
 //BA.debugLineNum = 285;BA.debugLine="Dim cb As CheckBox";
_cb = new anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper();
 //BA.debugLineNum = 286;BA.debugLine="Dim sf As String, kid As String";
_sf = "";
_kid = "";
 //BA.debugLineNum = 287;BA.debugLine="For Each v As View In Auswahl_Klassen.Panel.GetAllViewsRecursive";
_v = new anywheresoftware.b4a.objects.ConcreteViewWrapper();
final anywheresoftware.b4a.BA.IterableList group239 = mostCurrent._auswahl_klassen.getPanel().GetAllViewsRecursive();
final int groupLen239 = group239.getSize();
for (int index239 = 0;index239 < groupLen239 ;index239++){
_v.setObject((android.view.View)(group239.Get(index239)));
 //BA.debugLineNum = 288;BA.debugLine="sf=v.tag";
_sf = BA.ObjectToString(_v.getTag());
 //BA.debugLineNum = 289;BA.debugLine="If (sf.Contains(\"Checkbox\")) Then";
if ((_sf.contains("Checkbox"))) { 
 //BA.debugLineNum = 290;BA.debugLine="kid=sf.Replace(\"Checkbox|\",\"\")";
_kid = _sf.replace("Checkbox|","");
 //BA.debugLineNum = 291;BA.debugLine="cb=v";
_cb.setObject((android.widget.CheckBox)(_v.getObject()));
 //BA.debugLineNum = 292;BA.debugLine="If (cb.Checked=True) Then";
if ((_cb.getChecked()==anywheresoftware.b4a.keywords.Common.True)) { 
 //BA.debugLineNum = 293;BA.debugLine="NewID=0";
_newid = (int) (0);
 //BA.debugLineNum = 294;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT note_id FROM noten\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT note_id FROM noten")));
 //BA.debugLineNum = 295;BA.debugLine="If cursor1.RowCount > 0 Then";
if (_cursor1.getRowCount()>0) { 
 //BA.debugLineNum = 296;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step248 = 1;
final int limit248 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0); (step248 > 0 && _i <= limit248) || (step248 < 0 && _i >= limit248); _i = ((int)(0 + _i + step248))) {
 //BA.debugLineNum = 297;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 298;BA.debugLine="NewID = cursor1.GetInt(\"note_id\")";
_newid = _cursor1.GetInt("note_id");
 }
};
 };
 //BA.debugLineNum = 301;BA.debugLine="NewID = NewID +1 ' add 1 to the ID number to make a new ID field";
_newid = (int) (_newid+1);
 //BA.debugLineNum = 302;BA.debugLine="SQL1.ExecNonQuery(\"INSERT INTO noten VALUES('\" & NewID & \"','\" & schuler_id & \"', '\" & kid & \"','0','DEBUG')\")";
_sql1.ExecNonQuery("INSERT INTO noten VALUES('"+BA.NumberToString(_newid)+"','"+BA.NumberToString(_schuler_id)+"', '"+_kid+"','0','DEBUG')");
 };
 };
 }
;
 //BA.debugLineNum = 307;BA.debugLine="Msgbox(\"Schüler wurde angelegt\",\"Erfolgreich\")";
anywheresoftware.b4a.keywords.Common.Msgbox("Schüler wurde angelegt","Erfolgreich",mostCurrent.activityBA);
 //BA.debugLineNum = 308;BA.debugLine="Schueler_Add_Click";
_schueler_add_click();
 //BA.debugLineNum = 309;BA.debugLine="End Sub";
return "";
}
public static String  _b_speichern_click() throws Exception{
int _newid = 0;
int _i = 0;
 //BA.debugLineNum = 212;BA.debugLine="Sub B_Speichern_Click";
 //BA.debugLineNum = 213;BA.debugLine="Dim NewID As Int";
_newid = 0;
 //BA.debugLineNum = 214;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT klasse_id FROM klasse\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT klasse_id FROM klasse")));
 //BA.debugLineNum = 215;BA.debugLine="If cursor1.RowCount > 0 Then";
if (_cursor1.getRowCount()>0) { 
 //BA.debugLineNum = 216;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step174 = 1;
final int limit174 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0); (step174 > 0 && _i <= limit174) || (step174 < 0 && _i >= limit174); _i = ((int)(0 + _i + step174))) {
 //BA.debugLineNum = 217;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 218;BA.debugLine="NewID = cursor1.GetInt(\"klasse_id\")";
_newid = _cursor1.GetInt("klasse_id");
 }
};
 };
 //BA.debugLineNum = 221;BA.debugLine="NewID = NewID +1 ' add 1 to the ID number to make a new ID field";
_newid = (int) (_newid+1);
 //BA.debugLineNum = 222;BA.debugLine="SQL1.ExecNonQuery(\"INSERT INTO klasse VALUES('\" & NewID & \"','\" & Klassen_Bezeichnung.Text & \"')\")";
_sql1.ExecNonQuery("INSERT INTO klasse VALUES('"+BA.NumberToString(_newid)+"','"+mostCurrent._klassen_bezeichnung.getText()+"')");
 //BA.debugLineNum = 223;BA.debugLine="Klassen_Bezeichnung.Text=\"\"";
mostCurrent._klassen_bezeichnung.setText((Object)(""));
 //BA.debugLineNum = 224;BA.debugLine="Msgbox(\"Klasse wurde angelegt\",\"Erfolgreich\")";
anywheresoftware.b4a.keywords.Common.Msgbox("Klasse wurde angelegt","Erfolgreich",mostCurrent.activityBA);
 //BA.debugLineNum = 225;BA.debugLine="End Sub";
return "";
}
public static String  _btn_click() throws Exception{
anywheresoftware.b4a.objects.ButtonWrapper _b = null;
int _i = 0;
anywheresoftware.b4a.objects.ButtonWrapper _btn = null;
 //BA.debugLineNum = 114;BA.debugLine="Sub Btn_Click";
 //BA.debugLineNum = 115;BA.debugLine="Dim b As Button";
_b = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 116;BA.debugLine="b = Sender";
_b.setObject((android.widget.Button)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 117;BA.debugLine="glob_klasse_auswahl_schuelerliste=b.tag";
_glob_klasse_auswahl_schuelerliste = (int)(BA.ObjectToNumber(_b.getTag()));
 //BA.debugLineNum = 119;BA.debugLine="Change_Layout(\"klassen_schueler_liste\")";
_change_layout("klassen_schueler_liste");
 //BA.debugLineNum = 120;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT * FROM schueler INNER JOIN noten ON schueler.schueler_id=noten.schueler_id WHERE noten.klasse_id='\" & b.Tag & \"' ORDER BY schueler.name\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT * FROM schueler INNER JOIN noten ON schueler.schueler_id=noten.schueler_id WHERE noten.klasse_id='"+BA.ObjectToString(_b.getTag())+"' ORDER BY schueler.name")));
 //BA.debugLineNum = 121;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step84 = 1;
final int limit84 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0); (step84 > 0 && _i <= limit84) || (step84 < 0 && _i >= limit84); _i = ((int)(0 + _i + step84))) {
 //BA.debugLineNum = 122;BA.debugLine="Dim Btn As Button";
_btn = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 123;BA.debugLine="Btn.Initialize(\"Btn_schueler_in_klasse\")";
_btn.Initialize(mostCurrent.activityBA,"Btn_schueler_in_klasse");
 //BA.debugLineNum = 124;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 125;BA.debugLine="Btn.Tag=cursor1.GetString(\"schueler_id\")";
_btn.setTag((Object)(_cursor1.GetString("schueler_id")));
 //BA.debugLineNum = 126;BA.debugLine="Btn.Text=cursor1.GetString(\"name\")";
_btn.setText((Object)(_cursor1.GetString("name")));
 //BA.debugLineNum = 127;BA.debugLine="List_schueler.Panel.AddView(Btn, 10dip, 10dip + 60dip * i, 280dip, 50dip)";
mostCurrent._list_schueler.getPanel().AddView((android.view.View)(_btn.getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),(int) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60))*_i),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (280)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50)));
 //BA.debugLineNum = 128;BA.debugLine="If (10dip + 60dip * i>=List_schueler.Panel.Height-70) Then";
if ((anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60))*_i>=mostCurrent._list_schueler.getPanel().getHeight()-70)) { 
 //BA.debugLineNum = 129;BA.debugLine="List_schueler.Panel.Height=List_schueler.Panel.Height+80";
mostCurrent._list_schueler.getPanel().setHeight((int) (mostCurrent._list_schueler.getPanel().getHeight()+80));
 };
 }
};
 //BA.debugLineNum = 132;BA.debugLine="End Sub";
return "";
}
public static String  _btn_longclick() throws Exception{
anywheresoftware.b4a.objects.ButtonWrapper _b = null;
 //BA.debugLineNum = 133;BA.debugLine="Sub Btn_LongClick";
 //BA.debugLineNum = 134;BA.debugLine="Dim b As Button";
_b = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 135;BA.debugLine="b = Sender";
_b.setObject((android.widget.Button)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 136;BA.debugLine="If (Msgbox2(\"Wirklich löschen?\", \"löschen\", \"Ja\", \"Nein\",\"\",Null)=DialogResponse.POSITIVE) Then";
if ((anywheresoftware.b4a.keywords.Common.Msgbox2("Wirklich löschen?","löschen","Ja","Nein","",(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.Null),mostCurrent.activityBA)==anywheresoftware.b4a.keywords.Common.DialogResponse.POSITIVE)) { 
 //BA.debugLineNum = 137;BA.debugLine="SQL1.ExecNonQuery(\"DELETE FROM klasse WHERE klasse_id=\" & b.tag)";
_sql1.ExecNonQuery("DELETE FROM klasse WHERE klasse_id="+BA.ObjectToString(_b.getTag()));
 };
 //BA.debugLineNum = 139;BA.debugLine="Klassen_Button_Click";
_klassen_button_click();
 //BA.debugLineNum = 140;BA.debugLine="End Sub";
return "";
}
public static String  _btn_schueler_click() throws Exception{
anywheresoftware.b4a.objects.ButtonWrapper _b = null;
 //BA.debugLineNum = 168;BA.debugLine="Sub Btn_schueler_Click";
 //BA.debugLineNum = 169;BA.debugLine="Dim b As Button";
_b = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 170;BA.debugLine="b = Sender";
_b.setObject((android.widget.Button)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 171;BA.debugLine="Msgbox(b.tag,\"test\")";
anywheresoftware.b4a.keywords.Common.Msgbox(BA.ObjectToString(_b.getTag()),"test",mostCurrent.activityBA);
 //BA.debugLineNum = 172;BA.debugLine="End Sub";
return "";
}
public static String  _btn_schueler_in_klasse_click() throws Exception{
int _sid = 0;
anywheresoftware.b4a.objects.ButtonWrapper _b = null;
String _name = "";
int _i = 0;
 //BA.debugLineNum = 311;BA.debugLine="Sub Btn_schueler_in_klasse_click";
 //BA.debugLineNum = 312;BA.debugLine="Dim sid As Int";
_sid = 0;
 //BA.debugLineNum = 313;BA.debugLine="Change_Layout(\"noten_add\")";
_change_layout("noten_add");
 //BA.debugLineNum = 315;BA.debugLine="Dim b As Button";
_b = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 316;BA.debugLine="b = Sender";
_b.setObject((android.widget.Button)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 317;BA.debugLine="sid=b.Tag";
_sid = (int)(BA.ObjectToNumber(_b.getTag()));
 //BA.debugLineNum = 319;BA.debugLine="Dim Name As String";
_name = "";
 //BA.debugLineNum = 320;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT name FROM schueler WHERE schueler_id='\" & sid & \"'\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT name FROM schueler WHERE schueler_id='"+BA.NumberToString(_sid)+"'")));
 //BA.debugLineNum = 321;BA.debugLine="If cursor1.RowCount > 0 Then";
if (_cursor1.getRowCount()>0) { 
 //BA.debugLineNum = 322;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step270 = 1;
final int limit270 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0); (step270 > 0 && _i <= limit270) || (step270 < 0 && _i >= limit270); _i = ((int)(0 + _i + step270))) {
 //BA.debugLineNum = 323;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 324;BA.debugLine="Name = cursor1.GetString(\"name\")";
_name = _cursor1.GetString("name");
 }
};
 };
 //BA.debugLineNum = 328;BA.debugLine="Label2.Text=Name";
mostCurrent._label2.setText((Object)(_name));
 //BA.debugLineNum = 329;BA.debugLine="Label2.Tag=sid";
mostCurrent._label2.setTag((Object)(_sid));
 //BA.debugLineNum = 333;BA.debugLine="End Sub";
return "";
}
public static String  _btn_schueler_longclick() throws Exception{
anywheresoftware.b4a.objects.ButtonWrapper _b = null;
 //BA.debugLineNum = 173;BA.debugLine="Sub Btn_schueler_LongClick";
 //BA.debugLineNum = 174;BA.debugLine="Dim b As Button";
_b = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 175;BA.debugLine="b = Sender";
_b.setObject((android.widget.Button)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 176;BA.debugLine="If (Msgbox2(\"Wirklich löschen?\", \"löschen\", \"Ja\", \"Nein\",\"\",Null)=DialogResponse.POSITIVE) Then";
if ((anywheresoftware.b4a.keywords.Common.Msgbox2("Wirklich löschen?","löschen","Ja","Nein","",(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.Null),mostCurrent.activityBA)==anywheresoftware.b4a.keywords.Common.DialogResponse.POSITIVE)) { 
 //BA.debugLineNum = 177;BA.debugLine="SQL1.ExecNonQuery(\"DELETE FROM schueler WHERE schueler_id=\" & b.tag)";
_sql1.ExecNonQuery("DELETE FROM schueler WHERE schueler_id="+BA.ObjectToString(_b.getTag()));
 };
 //BA.debugLineNum = 179;BA.debugLine="Schueler_Button_Click";
_schueler_button_click();
 //BA.debugLineNum = 180;BA.debugLine="End Sub";
return "";
}
public static String  _camera1_picturetaken(byte[] _data) throws Exception{
anywheresoftware.b4a.objects.streams.File.OutputStreamWrapper _out = null;
int _newid = 0;
int _i = 0;
 //BA.debugLineNum = 247;BA.debugLine="Sub Camera1_PictureTaken (Data() As Byte)";
 //BA.debugLineNum = 248;BA.debugLine="Dim out As OutputStream";
_out = new anywheresoftware.b4a.objects.streams.File.OutputStreamWrapper();
 //BA.debugLineNum = 249;BA.debugLine="Dim NewID As Int";
_newid = 0;
 //BA.debugLineNum = 250;BA.debugLine="NewID=0";
_newid = (int) (0);
 //BA.debugLineNum = 251;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT schueler_id FROM schueler\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT schueler_id FROM schueler")));
 //BA.debugLineNum = 252;BA.debugLine="If cursor1.RowCount > 0 Then";
if (_cursor1.getRowCount()>0) { 
 //BA.debugLineNum = 253;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step211 = 1;
final int limit211 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0); (step211 > 0 && _i <= limit211) || (step211 < 0 && _i >= limit211); _i = ((int)(0 + _i + step211))) {
 //BA.debugLineNum = 254;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 255;BA.debugLine="NewID = cursor1.GetInt(\"schueler_id\")";
_newid = _cursor1.GetInt("schueler_id");
 }
};
 };
 //BA.debugLineNum = 258;BA.debugLine="NewID = NewID +1 ' add 1 to the ID number to make a new ID field";
_newid = (int) (_newid+1);
 //BA.debugLineNum = 261;BA.debugLine="out = File.OpenOutput(File.DirInternal, NewID & \".jpg\", False)";
_out = anywheresoftware.b4a.keywords.Common.File.OpenOutput(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),BA.NumberToString(_newid)+".jpg",anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 262;BA.debugLine="out.WriteBytes(Data, 0, Data.Length)";
_out.WriteBytes(_data,(int) (0),_data.length);
 //BA.debugLineNum = 263;BA.debugLine="out.Close";
_out.Close();
 //BA.debugLineNum = 264;BA.debugLine="Schueler_Add_Click";
_schueler_add_click();
 //BA.debugLineNum = 266;BA.debugLine="End Sub";
return "";
}
public static String  _camera1_ready(boolean _success) throws Exception{
anywheresoftware.b4a.objects.CameraW _cam = null;
 //BA.debugLineNum = 242;BA.debugLine="Sub Camera1_Ready(Success As Boolean)";
 //BA.debugLineNum = 243;BA.debugLine="Dim Cam As Camera";
_cam = new anywheresoftware.b4a.objects.CameraW();
 //BA.debugLineNum = 244;BA.debugLine="Cam=Sender";
_cam = (anywheresoftware.b4a.objects.CameraW)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA));
 //BA.debugLineNum = 245;BA.debugLine="Cam.StartPreview";
_cam.StartPreview();
 //BA.debugLineNum = 246;BA.debugLine="End Sub";
return "";
}
public static String  _change_layout(String _layout) throws Exception{
anywheresoftware.b4a.phone.Phone _test = null;
 //BA.debugLineNum = 90;BA.debugLine="Sub Change_Layout(layout As String)";
 //BA.debugLineNum = 91;BA.debugLine="Dim test As Phone";
_test = new anywheresoftware.b4a.phone.Phone();
 //BA.debugLineNum = 92;BA.debugLine="test.HideKeyboard(Activity)";
_test.HideKeyboard(mostCurrent._activity);
 //BA.debugLineNum = 93;BA.debugLine="Activity.RemoveAllViews";
mostCurrent._activity.RemoveAllViews();
 //BA.debugLineNum = 94;BA.debugLine="Activity.LoadLayout(layout)";
mostCurrent._activity.LoadLayout(_layout,mostCurrent.activityBA);
 //BA.debugLineNum = 95;BA.debugLine="Activity.Title=\"Notenbuch\"";
mostCurrent._activity.setTitle((Object)("Notenbuch"));
 //BA.debugLineNum = 96;BA.debugLine="End Sub";
return "";
}
public static String  _exit_click() throws Exception{
 //BA.debugLineNum = 141;BA.debugLine="Sub Exit_Click";
 //BA.debugLineNum = 142;BA.debugLine="If Msgbox2(\"Programm Beenden?\", \"\", \"Ja\", \"Nein\", \"\", Null) = DialogResponse.POSITIVE Then";
if (anywheresoftware.b4a.keywords.Common.Msgbox2("Programm Beenden?","","Ja","Nein","",(android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.Null),mostCurrent.activityBA)==anywheresoftware.b4a.keywords.Common.DialogResponse.POSITIVE) { 
 //BA.debugLineNum = 143;BA.debugLine="ExitApplication 'App is exiting";
anywheresoftware.b4a.keywords.Common.ExitApplication();
 };
 //BA.debugLineNum = 145;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _globals() throws Exception{
 //BA.debugLineNum = 22;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 25;BA.debugLine="Private Klassen_Bezeichnung As EditText";
mostCurrent._klassen_bezeichnung = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Private List_klassen As ScrollView";
mostCurrent._list_klassen = new anywheresoftware.b4a.objects.ScrollViewWrapper();
 //BA.debugLineNum = 27;BA.debugLine="Private List_schueler As ScrollView";
mostCurrent._list_schueler = new anywheresoftware.b4a.objects.ScrollViewWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Private B_Speichern As Button";
mostCurrent._b_speichern = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Private Camera As Panel";
mostCurrent._camera = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Private schueler_bild As ImageView";
mostCurrent._schueler_bild = new anywheresoftware.b4a.objects.ImageViewWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Private Schueler_add_scroll As ScrollView";
mostCurrent._schueler_add_scroll = new anywheresoftware.b4a.objects.ScrollViewWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Private EDIT_Name As EditText";
mostCurrent._edit_name = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 33;BA.debugLine="Private EDIT_Notiz As EditText";
mostCurrent._edit_notiz = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 34;BA.debugLine="Private Auswahl_Klassen As ScrollView";
mostCurrent._auswahl_klassen = new anywheresoftware.b4a.objects.ScrollViewWrapper();
 //BA.debugLineNum = 35;BA.debugLine="Private B_Main As Button";
mostCurrent._b_main = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 37;BA.debugLine="Dim PopUp As Panel";
mostCurrent._popup = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 38;BA.debugLine="Dim glob_klasse_auswahl_schuelerliste As Int";
_glob_klasse_auswahl_schuelerliste = 0;
 //BA.debugLineNum = 39;BA.debugLine="Private Label2 As Label";
mostCurrent._label2 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 40;BA.debugLine="Private ToggleButton1 As ToggleButton";
mostCurrent._togglebutton1 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 41;BA.debugLine="Private ToggleButton10 As ToggleButton";
mostCurrent._togglebutton10 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 42;BA.debugLine="Private ToggleButton11 As ToggleButton";
mostCurrent._togglebutton11 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 43;BA.debugLine="Private ToggleButton12 As ToggleButton";
mostCurrent._togglebutton12 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 44;BA.debugLine="Private ToggleButton2 As ToggleButton";
mostCurrent._togglebutton2 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 45;BA.debugLine="Private ToggleButton3 As ToggleButton";
mostCurrent._togglebutton3 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 46;BA.debugLine="Private ToggleButton4 As ToggleButton";
mostCurrent._togglebutton4 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 47;BA.debugLine="Private ToggleButton5 As ToggleButton";
mostCurrent._togglebutton5 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 48;BA.debugLine="Private ToggleButton6 As ToggleButton";
mostCurrent._togglebutton6 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 49;BA.debugLine="Private ToggleButton7 As ToggleButton";
mostCurrent._togglebutton7 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 50;BA.debugLine="Private ToggleButton8 As ToggleButton";
mostCurrent._togglebutton8 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 51;BA.debugLine="Private ToggleButton9 As ToggleButton";
mostCurrent._togglebutton9 = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 53;BA.debugLine="Private B_NOTEN_BACK As Button";
mostCurrent._b_noten_back = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 54;BA.debugLine="Private B_SAVE_NOTEN As Button";
mostCurrent._b_save_noten = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 55;BA.debugLine="End Sub";
return "";
}
public static String  _klassen_add_click() throws Exception{
 //BA.debugLineNum = 149;BA.debugLine="Sub Klassen_Add_Click";
 //BA.debugLineNum = 150;BA.debugLine="Change_Layout(\"klassen_add\")";
_change_layout("klassen_add");
 //BA.debugLineNum = 151;BA.debugLine="End Sub";
return "";
}
public static String  _klassen_bezeichnung_enterpressed() throws Exception{
 //BA.debugLineNum = 229;BA.debugLine="Sub Klassen_Bezeichnung_EnterPressed";
 //BA.debugLineNum = 230;BA.debugLine="B_Speichern_Click";
_b_speichern_click();
 //BA.debugLineNum = 231;BA.debugLine="End Sub";
return "";
}
public static String  _klassen_button_click() throws Exception{
int _i = 0;
anywheresoftware.b4a.objects.ButtonWrapper _btn = null;
 //BA.debugLineNum = 98;BA.debugLine="Sub Klassen_Button_Click";
 //BA.debugLineNum = 99;BA.debugLine="Change_Layout(\"klassen_liste\")";
_change_layout("klassen_liste");
 //BA.debugLineNum = 101;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT * FROM klasse ORDER BY bezeichnung\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT * FROM klasse ORDER BY bezeichnung")));
 //BA.debugLineNum = 102;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step66 = 1;
final int limit66 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0); (step66 > 0 && _i <= limit66) || (step66 < 0 && _i >= limit66); _i = ((int)(0 + _i + step66))) {
 //BA.debugLineNum = 103;BA.debugLine="Dim Btn As Button";
_btn = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 104;BA.debugLine="Btn.Initialize(\"Btn\")";
_btn.Initialize(mostCurrent.activityBA,"Btn");
 //BA.debugLineNum = 105;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 106;BA.debugLine="Btn.Tag=cursor1.GetString(\"klasse_id\")";
_btn.setTag((Object)(_cursor1.GetString("klasse_id")));
 //BA.debugLineNum = 107;BA.debugLine="Btn.Text=cursor1.GetString(\"bezeichnung\")";
_btn.setText((Object)(_cursor1.GetString("bezeichnung")));
 //BA.debugLineNum = 108;BA.debugLine="List_klassen.Panel.AddView(Btn, 10dip, 10dip + 60dip * i, 280dip, 50dip)";
mostCurrent._list_klassen.getPanel().AddView((android.view.View)(_btn.getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),(int) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60))*_i),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (280)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50)));
 //BA.debugLineNum = 109;BA.debugLine="If (10dip + 60dip * i>=List_klassen.Panel.Height) Then";
if ((anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60))*_i>=mostCurrent._list_klassen.getPanel().getHeight())) { 
 //BA.debugLineNum = 110;BA.debugLine="List_klassen.Panel.Height=List_klassen.Panel.Height+80";
mostCurrent._list_klassen.getPanel().setHeight((int) (mostCurrent._list_klassen.getPanel().getHeight()+80));
 };
 }
};
 //BA.debugLineNum = 113;BA.debugLine="End Sub";
return "";
}
public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 18;BA.debugLine="Dim SQL1 As SQL";
_sql1 = new anywheresoftware.b4a.sql.SQL();
 //BA.debugLineNum = 19;BA.debugLine="Dim cursor1 As Cursor";
_cursor1 = new anywheresoftware.b4a.sql.SQL.CursorWrapper();
 //BA.debugLineNum = 20;BA.debugLine="End Sub";
return "";
}
public static String  _schueler_add_click() throws Exception{
int _newid = 0;
int _i = 0;
anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper _btn = null;
 //BA.debugLineNum = 181;BA.debugLine="Sub Schueler_Add_Click";
 //BA.debugLineNum = 182;BA.debugLine="Change_Layout(\"schueler_add\")";
_change_layout("schueler_add");
 //BA.debugLineNum = 183;BA.debugLine="Dim NewID As Int";
_newid = 0;
 //BA.debugLineNum = 184;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT schueler_id FROM schueler\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT schueler_id FROM schueler")));
 //BA.debugLineNum = 185;BA.debugLine="If cursor1.RowCount > 0 Then";
if (_cursor1.getRowCount()>0) { 
 //BA.debugLineNum = 186;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step147 = 1;
final int limit147 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0); (step147 > 0 && _i <= limit147) || (step147 < 0 && _i >= limit147); _i = ((int)(0 + _i + step147))) {
 //BA.debugLineNum = 187;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 188;BA.debugLine="NewID = cursor1.GetInt(\"schueler_id\")";
_newid = _cursor1.GetInt("schueler_id");
 }
};
 };
 //BA.debugLineNum = 191;BA.debugLine="NewID = NewID +1 ' add 1 to the ID number to make a new ID field";
_newid = (int) (_newid+1);
 //BA.debugLineNum = 192;BA.debugLine="If File.Exists(File.DirInternal,NewID & \".jpg\")=True Then";
if (anywheresoftware.b4a.keywords.Common.File.Exists(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),BA.NumberToString(_newid)+".jpg")==anywheresoftware.b4a.keywords.Common.True) { 
 //BA.debugLineNum = 193;BA.debugLine="schueler_bild.Bitmap = LoadBitmap(File.DirInternal,NewID & \".jpg\")";
mostCurrent._schueler_bild.setBitmap((android.graphics.Bitmap)(anywheresoftware.b4a.keywords.Common.LoadBitmap(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),BA.NumberToString(_newid)+".jpg").getObject()));
 };
 //BA.debugLineNum = 198;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT * FROM klasse ORDER BY bezeichnung\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT * FROM klasse ORDER BY bezeichnung")));
 //BA.debugLineNum = 199;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step157 = 1;
final int limit157 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0); (step157 > 0 && _i <= limit157) || (step157 < 0 && _i >= limit157); _i = ((int)(0 + _i + step157))) {
 //BA.debugLineNum = 200;BA.debugLine="Dim Btn As CheckBox";
_btn = new anywheresoftware.b4a.objects.CompoundButtonWrapper.CheckBoxWrapper();
 //BA.debugLineNum = 201;BA.debugLine="Btn.Initialize(\"x\")";
_btn.Initialize(mostCurrent.activityBA,"x");
 //BA.debugLineNum = 202;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 203;BA.debugLine="Btn.Tag=\"Checkbox|\" & cursor1.GetString(\"klasse_id\")";
_btn.setTag((Object)("Checkbox|"+_cursor1.GetString("klasse_id")));
 //BA.debugLineNum = 204;BA.debugLine="Btn.Text=cursor1.GetString(\"bezeichnung\")";
_btn.setText((Object)(_cursor1.GetString("bezeichnung")));
 //BA.debugLineNum = 205;BA.debugLine="Btn.TextColor=Colors.Black:";
_btn.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 206;BA.debugLine="Auswahl_Klassen.Panel.AddView(Btn, 10dip, 10dip + 60dip * i, 280dip, 50dip)";
mostCurrent._auswahl_klassen.getPanel().AddView((android.view.View)(_btn.getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),(int) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60))*_i),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (280)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50)));
 //BA.debugLineNum = 207;BA.debugLine="If (10dip + 60dip * i>Auswahl_Klassen.Panel.Height-50) Then";
if ((anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60))*_i>mostCurrent._auswahl_klassen.getPanel().getHeight()-50)) { 
 //BA.debugLineNum = 208;BA.debugLine="Auswahl_Klassen.Panel.Height=Auswahl_Klassen.Panel.Height+100";
mostCurrent._auswahl_klassen.getPanel().setHeight((int) (mostCurrent._auswahl_klassen.getPanel().getHeight()+100));
 };
 }
};
 //BA.debugLineNum = 211;BA.debugLine="End Sub";
return "";
}
public static String  _schueler_button_click() throws Exception{
int _i = 0;
anywheresoftware.b4a.objects.ButtonWrapper _btn = null;
 //BA.debugLineNum = 152;BA.debugLine="Sub Schueler_Button_Click";
 //BA.debugLineNum = 153;BA.debugLine="Change_Layout(\"schueler_liste\")";
_change_layout("schueler_liste");
 //BA.debugLineNum = 154;BA.debugLine="cursor1 = SQL1.ExecQuery(\"SELECT * FROM schueler ORDER BY name\")";
_cursor1.setObject((android.database.Cursor)(_sql1.ExecQuery("SELECT * FROM schueler ORDER BY name")));
 //BA.debugLineNum = 155;BA.debugLine="For i = 0 To cursor1.RowCount - 1";
{
final int step117 = 1;
final int limit117 = (int) (_cursor1.getRowCount()-1);
for (_i = (int) (0); (step117 > 0 && _i <= limit117) || (step117 < 0 && _i >= limit117); _i = ((int)(0 + _i + step117))) {
 //BA.debugLineNum = 156;BA.debugLine="Dim Btn As Button";
_btn = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 157;BA.debugLine="Btn.Initialize(\"Btn_schueler\")";
_btn.Initialize(mostCurrent.activityBA,"Btn_schueler");
 //BA.debugLineNum = 158;BA.debugLine="cursor1.Position = i";
_cursor1.setPosition(_i);
 //BA.debugLineNum = 159;BA.debugLine="Btn.Tag=cursor1.GetString(\"schueler_id\")";
_btn.setTag((Object)(_cursor1.GetString("schueler_id")));
 //BA.debugLineNum = 160;BA.debugLine="Btn.Text=cursor1.GetString(\"name\")";
_btn.setText((Object)(_cursor1.GetString("name")));
 //BA.debugLineNum = 161;BA.debugLine="List_schueler.Panel.AddView(Btn, 10dip, 10dip + 60dip * i, 280dip, 50dip)";
mostCurrent._list_schueler.getPanel().AddView((android.view.View)(_btn.getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)),(int) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60))*_i),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (280)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (50)));
 //BA.debugLineNum = 162;BA.debugLine="If (10dip + 60dip * i>=List_schueler.Panel.Height-70) Then";
if ((anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))+anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60))*_i>=mostCurrent._list_schueler.getPanel().getHeight()-70)) { 
 //BA.debugLineNum = 163;BA.debugLine="List_schueler.Panel.Height=List_schueler.Panel.Height+80";
mostCurrent._list_schueler.getPanel().setHeight((int) (mostCurrent._list_schueler.getPanel().getHeight()+80));
 };
 }
};
 //BA.debugLineNum = 167;BA.debugLine="End Sub";
return "";
}
public static String  _settings_click() throws Exception{
 //BA.debugLineNum = 146;BA.debugLine="Sub Settings_Click";
 //BA.debugLineNum = 148;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton1_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 334;BA.debugLine="Sub ToggleButton1_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 335;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 336;BA.debugLine="ToggleButton6.Checked=False";
mostCurrent._togglebutton6.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 337;BA.debugLine="ToggleButton5.Checked=False";
mostCurrent._togglebutton5.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 338;BA.debugLine="ToggleButton4.Checked=False";
mostCurrent._togglebutton4.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 339;BA.debugLine="ToggleButton3.Checked=False";
mostCurrent._togglebutton3.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 340;BA.debugLine="ToggleButton2.Checked=False";
mostCurrent._togglebutton2.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 341;BA.debugLine="ToggleButton1.Checked=False";
mostCurrent._togglebutton1.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 343;BA.debugLine="ToggleButton1.Checked=True";
mostCurrent._togglebutton1.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 346;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton10_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 444;BA.debugLine="Sub ToggleButton10_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 445;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 446;BA.debugLine="ToggleButton10.Checked=False";
mostCurrent._togglebutton10.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 447;BA.debugLine="ToggleButton11.Checked=False";
mostCurrent._togglebutton11.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 448;BA.debugLine="ToggleButton12.Checked=False";
mostCurrent._togglebutton12.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 450;BA.debugLine="ToggleButton10.Checked=True";
mostCurrent._togglebutton10.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 453;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton11_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 454;BA.debugLine="Sub ToggleButton11_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 455;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 456;BA.debugLine="ToggleButton10.Checked=False";
mostCurrent._togglebutton10.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 457;BA.debugLine="ToggleButton11.Checked=False";
mostCurrent._togglebutton11.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 458;BA.debugLine="ToggleButton12.Checked=False";
mostCurrent._togglebutton12.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 460;BA.debugLine="ToggleButton11.Checked=True";
mostCurrent._togglebutton11.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 463;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton12_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 464;BA.debugLine="Sub ToggleButton12_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 465;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 466;BA.debugLine="ToggleButton10.Checked=False";
mostCurrent._togglebutton10.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 467;BA.debugLine="ToggleButton11.Checked=False";
mostCurrent._togglebutton11.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 468;BA.debugLine="ToggleButton12.Checked=False";
mostCurrent._togglebutton12.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 470;BA.debugLine="ToggleButton12.Checked=True";
mostCurrent._togglebutton12.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 473;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton2_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 347;BA.debugLine="Sub ToggleButton2_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 348;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 349;BA.debugLine="ToggleButton6.Checked=False";
mostCurrent._togglebutton6.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 350;BA.debugLine="ToggleButton5.Checked=False";
mostCurrent._togglebutton5.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 351;BA.debugLine="ToggleButton4.Checked=False";
mostCurrent._togglebutton4.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 352;BA.debugLine="ToggleButton3.Checked=False";
mostCurrent._togglebutton3.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 353;BA.debugLine="ToggleButton2.Checked=False";
mostCurrent._togglebutton2.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 354;BA.debugLine="ToggleButton1.Checked=False";
mostCurrent._togglebutton1.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 356;BA.debugLine="ToggleButton2.Checked=True";
mostCurrent._togglebutton2.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 359;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton3_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 360;BA.debugLine="Sub ToggleButton3_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 361;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 362;BA.debugLine="ToggleButton6.Checked=False";
mostCurrent._togglebutton6.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 363;BA.debugLine="ToggleButton5.Checked=False";
mostCurrent._togglebutton5.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 364;BA.debugLine="ToggleButton4.Checked=False";
mostCurrent._togglebutton4.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 365;BA.debugLine="ToggleButton3.Checked=False";
mostCurrent._togglebutton3.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 366;BA.debugLine="ToggleButton2.Checked=False";
mostCurrent._togglebutton2.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 367;BA.debugLine="ToggleButton1.Checked=False";
mostCurrent._togglebutton1.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 369;BA.debugLine="ToggleButton3.Checked=True";
mostCurrent._togglebutton3.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 372;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton4_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 373;BA.debugLine="Sub ToggleButton4_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 374;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 375;BA.debugLine="ToggleButton6.Checked=False";
mostCurrent._togglebutton6.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 376;BA.debugLine="ToggleButton5.Checked=False";
mostCurrent._togglebutton5.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 377;BA.debugLine="ToggleButton4.Checked=False";
mostCurrent._togglebutton4.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 378;BA.debugLine="ToggleButton3.Checked=False";
mostCurrent._togglebutton3.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 379;BA.debugLine="ToggleButton2.Checked=False";
mostCurrent._togglebutton2.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 380;BA.debugLine="ToggleButton1.Checked=False";
mostCurrent._togglebutton1.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 382;BA.debugLine="ToggleButton4.Checked=True";
mostCurrent._togglebutton4.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 385;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton5_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 386;BA.debugLine="Sub ToggleButton5_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 387;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 388;BA.debugLine="ToggleButton6.Checked=False";
mostCurrent._togglebutton6.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 389;BA.debugLine="ToggleButton5.Checked=False";
mostCurrent._togglebutton5.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 390;BA.debugLine="ToggleButton4.Checked=False";
mostCurrent._togglebutton4.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 391;BA.debugLine="ToggleButton3.Checked=False";
mostCurrent._togglebutton3.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 392;BA.debugLine="ToggleButton2.Checked=False";
mostCurrent._togglebutton2.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 393;BA.debugLine="ToggleButton1.Checked=False";
mostCurrent._togglebutton1.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 395;BA.debugLine="ToggleButton5.Checked=True";
mostCurrent._togglebutton5.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 398;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton6_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 399;BA.debugLine="Sub ToggleButton6_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 400;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 401;BA.debugLine="ToggleButton6.Checked=False";
mostCurrent._togglebutton6.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 402;BA.debugLine="ToggleButton5.Checked=False";
mostCurrent._togglebutton5.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 403;BA.debugLine="ToggleButton4.Checked=False";
mostCurrent._togglebutton4.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 404;BA.debugLine="ToggleButton3.Checked=False";
mostCurrent._togglebutton3.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 405;BA.debugLine="ToggleButton2.Checked=False";
mostCurrent._togglebutton2.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 406;BA.debugLine="ToggleButton1.Checked=False";
mostCurrent._togglebutton1.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 408;BA.debugLine="ToggleButton6.Checked=True";
mostCurrent._togglebutton6.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 411;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton7_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 413;BA.debugLine="Sub ToggleButton7_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 414;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 415;BA.debugLine="ToggleButton7.Checked=False";
mostCurrent._togglebutton7.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 416;BA.debugLine="ToggleButton8.Checked=False";
mostCurrent._togglebutton8.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 417;BA.debugLine="ToggleButton9.Checked=False";
mostCurrent._togglebutton9.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 419;BA.debugLine="ToggleButton7.Checked=True";
mostCurrent._togglebutton7.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 422;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton8_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 423;BA.debugLine="Sub ToggleButton8_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 424;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 425;BA.debugLine="ToggleButton7.Checked=False";
mostCurrent._togglebutton7.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 426;BA.debugLine="ToggleButton8.Checked=False";
mostCurrent._togglebutton8.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 427;BA.debugLine="ToggleButton9.Checked=False";
mostCurrent._togglebutton9.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 429;BA.debugLine="ToggleButton8.Checked=True";
mostCurrent._togglebutton8.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 432;BA.debugLine="End Sub";
return "";
}
public static String  _togglebutton9_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 433;BA.debugLine="Sub ToggleButton9_CheckedChange(Checked As Boolean)";
 //BA.debugLineNum = 434;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 435;BA.debugLine="ToggleButton7.Checked=False";
mostCurrent._togglebutton7.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 436;BA.debugLine="ToggleButton8.Checked=False";
mostCurrent._togglebutton8.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 437;BA.debugLine="ToggleButton9.Checked=False";
mostCurrent._togglebutton9.setChecked(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 439;BA.debugLine="ToggleButton9.Checked=True";
mostCurrent._togglebutton9.setChecked(anywheresoftware.b4a.keywords.Common.True);
 };
 //BA.debugLineNum = 442;BA.debugLine="End Sub";
return "";
}
}
