package com.xyoye.dandanplay.utils.permissionchecker;

import android.os.Build;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.xyoye.dandanplay.utils.permissionchecker.PermissionsPageManager.MANUFACTURER_MEIZU;
import static com.xyoye.dandanplay.utils.permissionchecker.PermissionsPageManager.MANUFACTURER_OPPO;
import static com.xyoye.dandanplay.utils.permissionchecker.PermissionsPageManager.MANUFACTURER_XIAOMI;

/**
 * Created by joker on 2017/9/16.
 */

public class ManufacturerSupportUtil {
    private static String[] forceManufacturers = {MANUFACTURER_XIAOMI, MANUFACTURER_MEIZU};
    private static Set<String> forceSet = new HashSet<>(Arrays.asList(forceManufacturers));
    private static String[] underMHasPermissionsRequestManufacturer = {MANUFACTURER_XIAOMI,
            MANUFACTURER_MEIZU, MANUFACTURER_OPPO};
    private static Set<String> underMSet = new HashSet<>(Arrays.asList
            (underMHasPermissionsRequestManufacturer));

    /**
     * those manufacturer that need request by some special measures, above
     * {@link Build.VERSION_CODES#M}
     *
     * @return
     */
    public static boolean isForceManufacturer() {
        return forceSet.contains(PermissionsPageManager.getManufacturer());
    }

    /**
     * those manufacturer that need request permissions under {@link Build.VERSION_CODES#M},
     * above {@link Build.VERSION_CODES#LOLLIPOP}
     *
     * @return
     */
    public static boolean isUnderMHasPermissionRequestManufacturer() {
        return underMSet.contains(PermissionsPageManager.getManufacturer());
    }

    public static boolean isLocationMustNeedGpsManufacturer() {
        return PermissionsPageManager.getManufacturer().equalsIgnoreCase(MANUFACTURER_OPPO);
    }

    /**
     * 1.is under {@link Build.VERSION_CODES#M}, above
     * {@link Build.VERSION_CODES#LOLLIPOP}
     * 2.has permissions check
     * 3.open under check
     * <p>
     * now, we know {@link PermissionsPageManager#isXIAOMI()}, {@link PermissionsPageManager#isMEIZU()}
     *
     * @param isUnderMNeedChecked
     * @return
     */
    public static boolean isUnderMNeedChecked(boolean isUnderMNeedChecked) {
        return isUnderMHasPermissionRequestManufacturer() && isUnderMNeedChecked &&
                isAndroidL();
    }

    /**
     * Build version code is under 6.0 but above 5.0
     *
     * @return
     */
    public static boolean isAndroidL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES
                .LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    public static Set<String> getForceSet() {
        return forceSet;
    }

    public static Set<String> getUnderMSet() {
        return underMSet;
    }
}
