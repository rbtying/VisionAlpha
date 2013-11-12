import org.opencv.core.Core;

import play.Application;
import play.GlobalSettings;

public class Global extends GlobalSettings {
	@Override
	public void beforeStart(Application app) {
		super.beforeStart(app);
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
}
