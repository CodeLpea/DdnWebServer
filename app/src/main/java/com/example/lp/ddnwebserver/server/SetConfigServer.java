package com.example.lp.ddnwebserver.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import com.example.lp.ddnwebserver.Config;
import com.example.lp.ddnwebserver.MyApplication;
import com.example.lp.ddnwebserver.database.PhotoRecordDb;
import com.example.lp.ddnwebserver.model.CalibratPositionData;
import com.example.lp.ddnwebserver.model.CameraData;
import com.example.lp.ddnwebserver.model.FFCData;
import com.example.lp.ddnwebserver.model.PhotoDataRespons;
import com.example.lp.ddnwebserver.model.PictureData;
import com.example.lp.ddnwebserver.model.RecordQueryRequest;
import com.example.lp.ddnwebserver.model.TemperCameraData;
import com.example.lp.ddnwebserver.model.TemperatureData;
import com.example.lp.ddnwebserver.model.ValidAreaData;
import com.example.lp.ddnwebserver.model.VoiceData;
import com.example.lp.ddnwebserver.model.WifiData;
import com.example.lp.ddnwebserver.service.BindServiceTest;
import com.example.lp.ddnwebserver.util.PreferencesUtils;
import com.example.lp.ddnwebserver.util.TimeUtils;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

/***
 * 设置参数到本机
 * */
public class SetConfigServer {

    private static final String TAG = "SetConfigServer";
    private BindServiceTest.Mybinder myBinder;

    private Context context = MyApplication.getInstance();


    private static class InnerClass {
        private static SetConfigServer setConfigServer = new SetConfigServer();
    }

    private SetConfigServer() {
        context.bindService(new Intent(context, BindServiceTest.class), connection, BIND_AUTO_CREATE);
    }

    /*静态内部类单例*/
    public static SetConfigServer getInstance() {
        return InnerClass.setConfigServer;
    }

    /**
     * 设置wifi信息到本机
     * 由于wifi存在与服务中
     * 因此需要与服务绑定，通过binder控制
     */
    public void setWifiData(WifiData wifiData) {
        /*SP操作，存入本地*/
        /*启动wifi服务，发送*/
        if (myBinder != null) {
            myBinder.getBinderInfo();
            myBinder.setBinderInfo(wifiData);
        } else {
            Log.e(TAG, "myBinder==null ");
        }


    }

    /**
     * 设置语音信息到本机
     */
    public void setVoiceData(VoiceData voiceData) {
        Log.i(TAG, "setVoiceData: ");
        Log.i(TAG, "voiceData:getError_voice " + voiceData.getError_voice());
        Log.i(TAG, "voiceData:getNormal_voice" + voiceData.getNormal_voice());
        Log.i(TAG, "voiceData:getSystem_voice " + voiceData.getSystem_voice());
        Log.i(TAG, "voiceData:getVoice_speed " + voiceData.getVoice_speed());
        /*存入SP*/
        PreferencesUtils.put(Config.VOICE_SPEED, voiceData.getVoice_speed());
        PreferencesUtils.put(Config.NORMAL_VOICE, voiceData.getNormal_voice());
        PreferencesUtils.put(Config.SYSTEM_VOICE, voiceData.getSystem_voice());
        PreferencesUtils.put(Config.ERROR_VOICE, voiceData.getError_voice());
        /*直接修改Tts配置*/

    }

    /**
     * 设置温度信息到本机
     * 温度阈值
     */
    public void setTemperatureData(TemperatureData temperatureData) {
        /*存入SP*/
        /*直接修改配置*/
        Log.i(TAG, "setTemperatureData: " + temperatureData.toString());
        PreferencesUtils.put(Config.TEMPERATURE_THRESHOLD, temperatureData.getTemperature());
    }

    /**
     * 设置温度摄像头相关信息到本机
     * 目标距离
     */
    public void setTemperatureCameraData(TemperCameraData temperCameraData) {
        /*存入SP*/
        /*直接修改配置*/
        Log.i(TAG, "setTemperatureCameraData: " + temperCameraData.toString());
        PreferencesUtils.put(Config.DISTANCE, temperCameraData.getDistance());
    }


    /**
     * 设置摄像头信息到本机
     * 摄像头曝光值
     */
    public void setCameraData(CameraData cameraData) {
        /*存入SP*/
        /*直接修改配置*/
        Log.i(TAG, "setCameraData: " + cameraData.toString());
        PreferencesUtils.put(Config.CAMERA_EXPLORE, cameraData.getExplorer());
    }

    /**
     * 设置FFC信息到本机
     * FFC补偿参数，可为负数
     * FFC黑体校准参考值
     */
    public void setFFCData(FFCData ffcData) {
        /*存入SP*/
        /*直接修改配置*/
        //如果补偿不为空，则表示为设置FFC补偿参数
        if (ffcData == null) {
            //如果都是空的，则表示为平均黑体校准
            Log.i(TAG, "FFC平均黑体校准: ");
            return;
        }
        if (ffcData.getCompensation() != null) {
            Log.i(TAG, "FFC补偿参数，可为负数 :" + ffcData.getCompensation());
            PreferencesUtils.put(Config.FFC_COMPENSATION_PARAMETER, ffcData.getCompensation());
        } else if (ffcData.getCalibration() != null) {
            //如果黑体校准参数不为空，则表示为设置黑体校准参数
            Log.i(TAG, "FFC黑体校准参考值: " + ffcData.getCalibration());
            PreferencesUtils.put(Config.FFC_CALIBRATION_PARAMETER, ffcData.getCalibration());
        }
    }


    /**
     * 获取图片路径
     * 人脸定位
     */
    public PictureData getPictureData() {
        PictureData pictureData = new PictureData();
        pictureData.setPersonPath(Config.person_path);
        pictureData.setTemperPath(Config.temper_path);
        pictureData.setX1("198");
        pictureData.setY1("94");

        pictureData.setX2("292");
        pictureData.setY2("107");

        pictureData.setX3("292");
        pictureData.setY3("192");

        pictureData.setX4("198");
        pictureData.setY4("192");

        pictureData.setMoveX(PreferencesUtils.getString(Config.MOVEX, "0"));
        pictureData.setMoveY(PreferencesUtils.getString(Config.MOVEY, "0"));
        pictureData.setScale(PreferencesUtils.getFloat(Config.SCALE, 1));
        return pictureData;
    }


    /**
     * 设置红外位置校准信息
     */
    public void setCalibratPosition(CalibratPositionData calibratPositionData) {
        /*存入SP*/
        /*直接修改配置*/
        PreferencesUtils.put(Config.MOVEX, calibratPositionData.getMoveX());
        PreferencesUtils.put(Config.MOVEY, calibratPositionData.getMoveY());
        PreferencesUtils.put(Config.SCALE, calibratPositionData.getScale());
        Log.i(TAG, "setCalibratPosition: " + calibratPositionData.toString());
    }

    /**
     * 获取有效区域信息
     */
    public ValidAreaData getValidAreaData() {
        ValidAreaData validAreaData = new ValidAreaData();
        validAreaData.setLineUp(PreferencesUtils.getString(Config.LINEUP, "20"));
        validAreaData.setLineLeft(PreferencesUtils.getString(Config.LINELEFT, "20"));
        validAreaData.setLineDown(PreferencesUtils.getString(Config.LINEDWON, "620"));
        validAreaData.setLineRight(PreferencesUtils.getString(Config.LINERIGHT, "450"));
        return validAreaData;
    }

    /**
     * 设置有效区域信息
     */
    public void setValidAreaData(ValidAreaData validAreaData) {
        PreferencesUtils.put(Config.LINEUP, validAreaData.getLineUp());
        PreferencesUtils.put(Config.LINELEFT, validAreaData.getLineLeft());
        PreferencesUtils.put(Config.LINEDWON, validAreaData.getLineDown());
        PreferencesUtils.put(Config.LINERIGHT, validAreaData.getLineRight());
        Log.i(TAG, "setValidAreaData: " + validAreaData);
    }

    /**
     * 查询记录
     */
    public PhotoDataRespons queryRecord(RecordQueryRequest recordQueryRequest) {
        //根据条件查询的所有记录
        List<PhotoRecordDb> photoRecordDbList = null;
        //统计条件查询的数量
        List<PhotoRecordDb> countSizeList = null;
        //排序条件 asc为升序，desc为降序。
        String orders = "date asc";
        if (recordQueryRequest.getOrders() != null) {
            orders = recordQueryRequest.getOrders();
            Log.i(TAG, "queryRecord:orders " + orders);
        }
        //判断是否有时间条件，如果没有时间则默认为查询所有
        if (recordQueryRequest.getStarTime() == null) {
            photoRecordDbList = LitePal.select("personPath", "temperPath", "date", "temp")
                    .where(" temp>=" + recordQueryRequest.getMinTemp() + " and temp<=" + recordQueryRequest.getMaxTemp())
                    .limit(recordQueryRequest.getEverPageNumber())
                    .offset((recordQueryRequest.getCurrentpage() - 1) * recordQueryRequest.getEverPageNumber())
                    .order(orders)
                    .find(PhotoRecordDb.class);

            countSizeList = LitePal.select("personPath", "temperPath", "date", "temp")
                    .where(" temp>=" + recordQueryRequest.getMinTemp() + " and temp<=" + recordQueryRequest.getMaxTemp())
                    .find(PhotoRecordDb.class);
        } else {
            //根据时间区间，currentpage，size，温度区间。
            photoRecordDbList = LitePal.select("personPath", "temperPath", "date", "temp")
                    .where("date>=" + TimeUtils.getDatatoString(recordQueryRequest.getStarTime()) +
                            " and date<=" + TimeUtils.getDatatoString(recordQueryRequest.getEndTime()) +
                            " and temp>=" + recordQueryRequest.getMinTemp() +
                            " and temp<=" + recordQueryRequest.getMaxTemp())
                    .limit(recordQueryRequest.getEverPageNumber())
                    .offset((recordQueryRequest.getCurrentpage() - 1) * recordQueryRequest.getEverPageNumber())
                    .order(orders)
                    .find(PhotoRecordDb.class);

            countSizeList = LitePal.select("personPath", "temperPath", "date", "temp")
                    .where("date>=" + TimeUtils.getDatatoString(recordQueryRequest.getStarTime()) +
                            " and date<=" + TimeUtils.getDatatoString(recordQueryRequest.getEndTime()) +
                            " and temp>=" + recordQueryRequest.getMinTemp() +
                            " and temp<=" + recordQueryRequest.getMaxTemp())
                    .find(PhotoRecordDb.class);
        }

        Log.i(TAG, "queryRecord: " + countSizeList.size());
        //要把数据的数量包裹一起发过去
        PhotoDataRespons respons = new PhotoDataRespons();
        respons.setPhotoRecordDbList(photoRecordDbList);
        respons.setAllSize(countSizeList.size());
        return respons;
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (BindServiceTest.Mybinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


}
