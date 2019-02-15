package wcfb.com.music;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

public class PermissionHelper {
    private AskPermissionCallBack callBack = null;

    public void checkPermission(final Activity activity, final AskPermissionCallBack callBack){
        this.callBack = callBack;
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(activity)
                        .setMessage("app需要开启权限才能使用此功能")
                        .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                                activity.startActivity(intent);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(callBack != null)
                                    callBack.onFailed();
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2000);
            }
        } else {
            if(callBack != null)
                callBack.onSuccess();
        }
    }

    interface AskPermissionCallBack{
        void onSuccess();
        void onFailed();
    }
}
