package cn.taes.mapapplication.ui.map

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import cn.taes.mapapplication.R
import cn.taes.mapapplication.databinding.ActivityMapBinding
import cn.taes.mapapplication.ui.map.CheckPermissionsActivity
import com.amap.api.fence.GeoFence
import com.amap.api.fence.GeoFenceClient
import com.amap.api.fence.GeoFenceListener
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.MyLocationStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapActivity : CheckPermissionsActivity(),LocationSource, AMapLocationListener, GeoFenceListener {

    private lateinit var activityMapBinding: ActivityMapBinding
    private var mlocationClient: AMapLocationClient? = null
    private var mListener: LocationSource.OnLocationChangedListener? = null
    private var mLocationOption: AMapLocationClientOption? = null
    // 地理围栏客户端
    private var fenceClient: GeoFenceClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMapBinding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(activityMapBinding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "禁用区地图"

        // 初始化地理围栏
        fenceClient = GeoFenceClient(this)
        activityMapBinding.mapview.onCreate(savedInstanceState)
        val mAMap = activityMapBinding.mapview.map
        mAMap.uiSettings.isRotateGesturesEnabled = false
        mAMap.moveCamera(CameraUpdateFactory.zoomBy(6f))
        mAMap.setLocationSource(this) // 设置定位监听
        mAMap.uiSettings.isMyLocationButtonEnabled = true // 设置默认定位按钮是否显示
        val myLocationStyle = MyLocationStyle()
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(
            BitmapDescriptorFactory.fromResource(R.drawable.gps_point)
        )
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0))
        // 自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(0f)
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0))
        // 将自定义的 myLocationStyle 对象添加到地图上
        mAMap.myLocationStyle = myLocationStyle
        mAMap.isMyLocationEnabled = true // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        activityMapBinding.mapview.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        activityMapBinding.mapview.onPause()
        deactivate()
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState!!)
        activityMapBinding.mapview.onSaveInstanceState(outState)
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        activityMapBinding.mapview.onDestroy()
        fenceClient?.removeGeoFence()
        mlocationClient?.onDestroy()
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        mListener = listener
        if (mlocationClient == null) {
            mlocationClient = AMapLocationClient(this)
            mLocationOption = AMapLocationClientOption()
            // 设置定位监听
            mlocationClient!!.setLocationListener(this)
            // 设置为高精度定位模式
            mLocationOption!!.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
            // 只是为了获取当前位置，所以设置为单次定位
            mLocationOption!!.setOnceLocation(true)
            // 设置定位参数
            mlocationClient!!.setLocationOption(mLocationOption)
            mlocationClient!!.startLocation()
        }
    }

    override fun deactivate() {
        mListener = null
        mlocationClient?.stopLocation()
        mlocationClient?.onDestroy()
        mlocationClient = null
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation.errorCode == 0) {
                mListener!!.onLocationChanged(amapLocation) // 显示系统小蓝点
            } else {
                Log.d("mapactivity","定位失败${amapLocation.errorCode}: ${amapLocation.errorInfo}")
                Toast.makeText(this,"定位失败${amapLocation.errorCode}: ${amapLocation.errorInfo}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onGeoFenceCreateFinished(geoFenceList: MutableList<GeoFence>?, errorCode: Int, customId: String?) {
        if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {}else{}
    }
}