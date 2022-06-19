package sh.siava.AOSPMods.systemui;

import static de.robv.android.xposed.XposedHelpers.*;
import static de.robv.android.xposed.XposedBridge.*;

import android.content.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import sh.siava.AOSPMods.AOSPMods;
import sh.siava.AOSPMods.XposedModPack;
import sh.siava.AOSPMods.XPrefs;

public class KeyGuardPinScrambler extends XposedModPack {
	private static final String listenPackage = AOSPMods.SYSTEM_UI_PACKAGE;
	
	private static boolean shufflePinEnabled = false;
	
	public KeyGuardPinScrambler(Context context) { super(context); }
	
	@Override
	public void updatePrefs(String... Key) {
		shufflePinEnabled = XPrefs.Xprefs.getBoolean("shufflePinEnabled", false);
	}
	
	@Override
	public boolean listensTo(String packageName) { return listenPackage.equals(packageName); }
	
	List digits = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
	
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
		if(!lpparam.packageName.equals(listenPackage)) return;
		
		Class<?> NumPadKeyClass = findClass("com.android.keyguard.NumPadKey", lpparam.classLoader);
		Class<?> KeyguardAbsKeyInputViewControllerClass = findClass("com.android.keyguard.KeyguardAbsKeyInputViewController", lpparam.classLoader);
		
		Collections.shuffle(digits);
		
		hookAllMethods(KeyguardAbsKeyInputViewControllerClass, "verifyPasswordAndUnlock", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if(!shufflePinEnabled) return;

				Collections.shuffle(digits);
			}
		});
		
		hookAllConstructors(NumPadKeyClass, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if(!shufflePinEnabled) return;
				
				int mDigit = getIntField(param.thisObject, "mDigit");
				Object mDigitText = getObjectField(param.thisObject, "mDigitText");
				setObjectField(param.thisObject, "mDigit", digits.get(mDigit));
				callMethod(mDigitText, "setText", Integer.toString((int) digits.get(mDigit)));
			}
		});
	}
}