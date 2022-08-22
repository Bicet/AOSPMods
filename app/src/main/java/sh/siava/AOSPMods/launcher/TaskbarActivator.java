package sh.siava.AOSPMods.launcher;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static sh.siava.AOSPMods.XPrefs.Xprefs;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import sh.siava.AOSPMods.AOSPMods;
import sh.siava.AOSPMods.XposedModPack;

@SuppressWarnings("RedundantThrows")
public class TaskbarActivator extends XposedModPack {
	private static final String listenPackage = AOSPMods.LAUNCHER_PACKAGE;
	
	public static final int TASKBAR_DEFAULT = 0;
	public static final int TASKBAR_ON = 1;
	public static final int TASKBAR_OFF = 2;
	
	private static int taskbarMode = 0;
	
	public TaskbarActivator(Context context) { super(context); }
	
	@Override
	public void updatePrefs(String... Key) {
		String taskbarModeStr = Xprefs.getString("taskBarMode", "0");
		
		if(Key.length > 0) {
			try {
				int newtaskbarMode = Integer.parseInt(taskbarModeStr);
				if (newtaskbarMode != taskbarMode) {
					taskbarMode = newtaskbarMode;
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			} catch (Exception ignored) {
			}
		}
		else
		{
			taskbarMode = Integer.parseInt(taskbarModeStr);
		}
	}
	
	@Override
	public boolean listensTo(String packageName) { return listenPackage.equals(packageName); }
	
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
		
		Class<?> info = findClass("com.android.launcher3.util.DisplayController$Info", lpparam.classLoader);
		
		
		hookAllMethods(info, "isTablet", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				switch (taskbarMode)
				{
					case TASKBAR_OFF:
						param.setResult(false);
						break;
					case TASKBAR_ON:
						param.setResult(true);
						break;
					case TASKBAR_DEFAULT:
						break;
				}
				
			}
		});
	}
}
