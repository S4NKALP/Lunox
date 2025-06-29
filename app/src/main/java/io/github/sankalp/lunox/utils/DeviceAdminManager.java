package io.github.sankalp.lunox.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.github.sankalp.lunox.R;

public class DeviceAdminManager {
    
    private static final String TAG = "DeviceAdminManager";
    
    public static boolean isDeviceAdminActive(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(context, io.github.sankalp.lunox.utils.lunoxDeviceAdminReceiver.class);
        return dpm.isAdminActive(adminComponent);
    }
    
    public static void requestDeviceAdminPermission(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(context, io.github.sankalp.lunox.utils.lunoxDeviceAdminReceiver.class);
        
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.device_admin_description));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    public static boolean lockDevice(Context context) {
        if (!isDeviceAdminActive(context)) {
            Toast.makeText(context, R.string.device_admin_disabled, Toast.LENGTH_SHORT).show();
            requestDeviceAdminPermission(context);
            return false;
        }
        
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(context, io.github.sankalp.lunox.utils.lunoxDeviceAdminReceiver.class);
            dpm.lockNow();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to lock device", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
} 