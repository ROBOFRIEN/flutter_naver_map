package map.naver.plugin.net.note11.naver_map_plugin

import android.content.Context
import io.flutter.plugin.common.MethodChannel
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMap.OnMapClickListener
import com.naver.maps.map.NaverMap.OnSymbolClickListener
import com.naver.maps.map.NaverMap.OnCameraChangeListener
import com.naver.maps.map.NaverMap.OnCameraIdleListener
import com.naver.maps.map.NaverMap.OnMapLongClickListener
import com.naver.maps.map.NaverMap.OnIndoorSelectionChangeListener
import com.naver.maps.map.NaverMap.OnMapDoubleTapListener
import com.naver.maps.map.NaverMap.OnMapTwoFingerTapListener
import android.graphics.PointF
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.Symbol
import com.naver.maps.map.overlay.*
import map.naver.plugin.net.note11.naver_map_plugin.Convert.toJson
import map.naver.plugin.net.note11.naver_map_plugin.NaverMarkerController.MarkerController
import map.naver.plugin.net.note11.naver_map_plugin.NaverPathsController.PathController
import map.naver.plugin.net.note11.naver_map_plugin.NaverCircleController.CircleController
import map.naver.plugin.net.note11.naver_map_plugin.NaverPolygonController.PolygonController
import java.util.HashMap
import com.naver.maps.map.indoor.IndoorSelection
import com.naver.maps.map.indoor.*

class NaverMapListeners( // member variable
    private val channel: MethodChannel,
    private val context: Context,
    private val naverMap: NaverMap
) : OnMapClickListener,
    OnIndoorSelectionChangeListener,
    OnSymbolClickListener,
    OnCameraChangeListener,
    OnCameraIdleListener,
    OnMapLongClickListener,
    OnMapDoubleTapListener,
    OnMapTwoFingerTapListener,
    Overlay.OnClickListener {

    override fun onIndoorSelectionChange(p0 : IndoorSelection? ){
        val arguments: MutableMap<String, Any> = HashMap(2)
        if(p0 != null){
            var latlng = LatLng(p0.levelIndex.toDouble(), p0.levelIndex.toDouble())
            arguments["indoorLevel"] = p0.level.indoorView.levelId      // 이건 Index라서 못쓸듯 //
            arguments["floorName"] = p0.level.name
            arguments["zoneID"] = p0.zone.zoneId
            channel.invokeMethod("map#onIndoorLevelChange", arguments)
        }
    }

    override fun onMapClick(pointF: PointF, latLng: LatLng) {
        val arguments: MutableMap<String, Any> = HashMap(2)
        arguments["position"] = latLng.toJson()
        channel.invokeMethod("map#onTap", arguments)
    }

    override fun onMapLongClick(pointF: PointF, latLng: LatLng) {
        val arguments: MutableMap<String, Any> = HashMap(2)
        arguments["position"] = latLng.toJson()
        channel.invokeMethod("map#onLongTap", arguments)
    }

    override fun onMapDoubleTap(pointF: PointF, latLng: LatLng): Boolean {
        val arguments: MutableMap<String, Any> = HashMap(2)
        arguments["position"] = latLng.toJson()
        channel.invokeMethod("map#onMapDoubleTap", arguments)
        return false
    }

    override fun onMapTwoFingerTap(pointF: PointF, latLng: LatLng): Boolean {
        val arguments: MutableMap<String, Any> = HashMap(2)
        arguments["position"] = latLng.toJson()
        channel.invokeMethod("map#onMapTwoFingerTap", arguments)
        return false
    }

    override fun onSymbolClick(symbol: Symbol): Boolean {
        val arguments: MutableMap<String, Any> = HashMap(2)
        arguments["position"] = symbol.position.toJson()
        arguments["caption"] = symbol.caption
        channel.invokeMethod("map#onSymbolClick", arguments)
        return true
    }

    override fun onCameraChange(i: Int, b: Boolean) {
        val arguments: MutableMap<String, Any> = HashMap(2)
        val latLng = naverMap.cameraPosition.target
        arguments["position"] = latLng.toJson()
        var reason = 0
        when (i) {
            CameraUpdate.REASON_GESTURE -> reason = 1
            CameraUpdate.REASON_CONTROL -> reason = 2
            CameraUpdate.REASON_LOCATION -> reason = 3
        }
        arguments["reason"] = reason
        arguments["animated"] = b
        channel.invokeMethod("camera#move", arguments)
    }

    override fun onCameraIdle() {
        channel.invokeMethod("camera#idle", null)
    }

    override fun onClick(overlay: Overlay): Boolean {
        when (overlay) {
            is Marker -> {
                val controller = overlay.getTag() as MarkerController? ?: return true
                controller.toggleInfoWindow()
                val arguments: MutableMap<String, Any> = HashMap(2)
                arguments["markerId"] = controller.id
                arguments["iconWidth"] = controller.marker.icon.getIntrinsicWidth(context)
                arguments["iconHeight"] = controller.marker.icon.getIntrinsicHeight(context)
                channel.invokeMethod("marker#onTap", arguments)
                return true
            }
            is PathOverlay -> {
                val controller = overlay.getTag() as PathController? ?: return true
                val arguments: MutableMap<String, Any> = HashMap(2)
                arguments["pathId"] = controller.id
                channel.invokeMethod("path#onTap", arguments)
                return true
            }
            is CircleOverlay -> {
                val controller = overlay.getTag() as CircleController? ?: return true
                val arguments: MutableMap<String, Any> = HashMap(2)
                arguments["overlayId"] = controller.id
                channel.invokeMethod("circle#onTap", arguments)
                return true
            }
            is PolygonOverlay -> {
                val controller = overlay.getTag() as PolygonController? ?: return true
                val argument: MutableMap<String, Any> = HashMap(2)
                argument["polygonOverlayId"] = controller.id
                channel.invokeMethod("polygon#onTap", argument)
            }
        }
        return false
    }
}