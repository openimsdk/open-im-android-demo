
package io.openim.android.ouicalling.resident;

import android.content.Context;
import android.os.Build;

public interface ILeoricProcess {
	/**
	 * Initialization some files or other when 1st time 
	 */
	boolean onInit(Context context);

	/**
	 * when Persistent processName create
	 * 
	 */
	void onPersistentCreate(Context context, LeoricConfigs configs);

	/**
	 * when DaemonAssistant processName create
	 */
	void onDaemonAssistantCreate(Context context, LeoricConfigs configs);

	/**
	 * when watches the processName dead which it watched
	 */
	void onDaemonDead();

	
	class Fetcher {

		private static volatile ILeoricProcess mDaemonStrategy;

		/**
		 * fetch the strategy for this device
		 * 
		 * @return the daemon strategy for this device
		 */
		static ILeoricProcess fetchStrategy() {
			if (mDaemonStrategy != null) {
				return mDaemonStrategy;
			}
			int sdk = Build.VERSION.SDK_INT;
			mDaemonStrategy = new LeoricProcessImpl();
			return mDaemonStrategy;
		}
	}
}
