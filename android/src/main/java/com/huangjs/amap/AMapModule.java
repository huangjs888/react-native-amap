package com.huangjs.amap;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AMapModule extends ReactContextBaseJavaModule implements WeatherSearch.OnWeatherSearchListener,
        GeocodeSearch.OnGeocodeSearchListener {

    private static final String MODULE_NAME = "AMapModule";
    private final Map<String, Promise> promises = new HashMap<>();
    private GeocodeSearch geocoderSearch;
    private WeatherSearch weatherSearch;

    public AMapModule(ReactApplicationContext reactContext) {
        super(reactContext);
        try {
            weatherSearch = new WeatherSearch(reactContext);
            weatherSearch.setOnWeatherSearchListener(this);
        } catch (AMapException e) {
            weatherSearch = null;
        }
        try {
            geocoderSearch = new GeocodeSearch(reactContext);
            geocoderSearch.setOnGeocodeSearchListener(this);
        } catch (AMapException e) {
            geocoderSearch = null;
        }
    }

    @NonNull
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @ReactMethod
    public void resolveCoordinateByAddress(String address, String city, final Promise promise) {
        if (geocoderSearch == null) {
            promise.reject("-1", "Can not resolve longitude and latitude");
            return;
        }
        GeocodeQuery query = new GeocodeQuery(address, city);
        promises.put("resolveCoordinate", promise);
        geocoderSearch.getFromLocationNameAsyn(query);
        // 同步调用
        // GeocodeResult geocodeResult = geocoderSearch.getFromLocationName(query);
    }

    @ReactMethod
    public void resolveAddressByCoordinate(ReadableMap coordinate, final Promise promise) {
        if (geocoderSearch == null) {
            promise.reject("-1", "Can not resolve address");
            return;
        }
        RegeocodeQuery query = new RegeocodeQuery(
                new LatLonPoint(coordinate.getDouble("latitude"), coordinate.getDouble("longitude")),
                300, GeocodeSearch.AMAP);
        promises.put("resolveAddress", promise);
        geocoderSearch.getFromLocationAsyn(query);
        // 同步调用
        // RegeocodeResult regeocodeResult = geocoderSearch.getFromLocation(query);
    }

    @ReactMethod
    public void queryWeatherByCity(int type, String city, final Promise promise) {
        if (weatherSearch == null) {
            promise.reject("-1", "Can not query weather");
            return;
        }
        WeatherSearchQuery query = new WeatherSearchQuery(city, type == 0 ? WeatherSearchQuery.WEATHER_TYPE_LIVE : WeatherSearchQuery.WEATHER_TYPE_FORECAST);
        promises.put("queryWeather" + (type == 0 ? "Live" : "Forecast"), promise);
        // 检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
        weatherSearch.setQuery(query);
        weatherSearch.searchWeatherAsyn();

    }

    @ReactMethod
    public void coordinateConvert(int coordinateType, ReadableMap coordinate, final Promise promise) {
        ReactApplicationContext context = getReactApplicationContext();
        CoordinateConverter converter = new CoordinateConverter(context);
        converter.from(Types.COORDINATE_TYPES[coordinateType]);
        converter.coord(Types.mapToLatLng(coordinate));
        promise.resolve(Types.latLngToMap(converter.convert()));
    }

    @ReactMethod
    public void calculateLineDistance(ReadableMap coordinate1, ReadableMap coordinate2, final Promise promise) {
        if (coordinate1 == null || coordinate2 == null) {
            promise.resolve(0);
            return;
        }
        promise.resolve(AMapUtils.calculateLineDistance(Types.mapToLatLng(coordinate1), Types.mapToLatLng(coordinate2)));
    }

    @ReactMethod
    public void initSDK(String apiKey) {
        ReactApplicationContext context = getReactApplicationContext();
        if (apiKey != null && !"".equals(apiKey)) {
            // 地图SDK设置
            // MapsInitializer.setApiKey(apiKey);
            // 由于个人信息保护法的实施，从地图8.1.0版本起对旧版本SDK不兼容
            // 请务必确保调用SDK任何接口前先调用更新隐私合规updatePrivacyShow、updatePrivacyAgree两个接口
            // 否则可能产生的异常情况，比如不显示地图，编译不通过、空指针等
            MapsInitializer.updatePrivacyShow(context, true, true);
            MapsInitializer.updatePrivacyAgree(context, true);
            // 搜索SDK设置
            ServiceSettings.updatePrivacyShow(context, true, true);
            ServiceSettings.updatePrivacyAgree(context, true);
            // 定位SDK设置
            // AMapLocationClient.updatePrivacyShow(context, true, true);
            // AMapLocationClient.updatePrivacyAgree(context, true);
        }
    }

    @ReactMethod
    public void getVersion(final Promise promise) {
        // 因为调用原生获取信息，无法直接return，只能用promise
        promise.resolve(MapsInitializer.getVersion());
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        Promise promise = this.promises.remove("resolveAddress");
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            WritableMap addressMap = Arguments.createMap();
            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null) {
                RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
                addressMap.putString("country", address.getCountry());
                addressMap.putString("countryCode", address.getCountryCode());
                addressMap.putString("province", address.getProvince());
                addressMap.putString("city", address.getCity());
                addressMap.putString("cityCode", address.getCityCode());
                addressMap.putString("adCode", address.getAdCode());
                addressMap.putString("district", address.getDistrict());
                addressMap.putString("township", address.getTownship());
                addressMap.putString("towncode", address.getTowncode());
                addressMap.putString("building", address.getBuilding());
                addressMap.putString("neighborhood", address.getNeighborhood());
                addressMap.putString("address", address.getFormatAddress());
            }
            if (promise != null) promise.resolve(addressMap);
        } else {
            if (promise != null) promise.reject(String.valueOf(rCode), "解析地理地址失败，失败代码：" + rCode);
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
        Promise promise = this.promises.remove("resolveCoordinate");
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            WritableMap addressMap = Arguments.createMap();
            if (geocodeResult != null && geocodeResult.getGeocodeAddressList() != null) {
                List<GeocodeAddress> addresses = geocodeResult.getGeocodeAddressList();
                WritableArray addressesList = Arguments.createArray();
                for (GeocodeAddress ga : addresses) {
                    WritableMap itemMap = Arguments.createMap();
                    itemMap.putString("country", ga.getCountry());
                    itemMap.putString("province", ga.getProvince());
                    itemMap.putString("city", ga.getCity());
                    itemMap.putString("adCode", ga.getAdcode());
                    itemMap.putString("district", ga.getDistrict());
                    itemMap.putString("township", ga.getTownship());
                    itemMap.putString("building", ga.getBuilding());
                    itemMap.putString("neighborhood", ga.getNeighborhood());
                    itemMap.putString("address", ga.getFormatAddress());
                    itemMap.putString("postcode", ga.getPostcode());
                    itemMap.putString("level", ga.getLevel());
                    LatLonPoint latLonPoint = ga.getLatLonPoint();
                    WritableMap coordinateMap = Arguments.createMap();
                    coordinateMap.putDouble("latitude", latLonPoint.getLatitude());
                    coordinateMap.putDouble("longitude", latLonPoint.getLongitude());
                    itemMap.putMap("coordinate", coordinateMap);
                    addressesList.pushMap(itemMap);
                }
                addressMap.putArray("addresses", addressesList);
            }
            if (promise != null) promise.resolve(addressMap);
        } else {
            if (promise != null) promise.reject(String.valueOf(rCode), "解析经纬度失败，失败代码：" + rCode);
        }
    }

    @Override // 实况天气
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherResult, int rCode) {
        Promise promise = this.promises.remove("queryWeatherLive");
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            WritableMap weatherMap = Arguments.createMap();
            if (weatherResult != null && weatherResult.getLiveResult() != null) {
                LocalWeatherLive weather = weatherResult.getLiveResult();
                weatherMap.putString("province", weather.getProvince());
                weatherMap.putString("city", weather.getCity());
                weatherMap.putString("adCode", weather.getAdCode());
                weatherMap.putString("reportTime", weather.getReportTime());
                weatherMap.putString("weather", weather.getWeather());
                weatherMap.putString("humidity", weather.getHumidity());
                weatherMap.putString("temperature", weather.getTemperature());
                weatherMap.putString("windPower", weather.getWindPower());
                weatherMap.putString("windDirection", weather.getWindDirection());
            }
            if (promise != null) promise.resolve(weatherMap);
        } else {
            if (promise != null) promise.reject(String.valueOf(rCode), "查询实况天气失败，失败代码：" + rCode);
        }
    }

    @Override  // 预报天气
    public void onWeatherForecastSearched(LocalWeatherForecastResult weatherResult, int rCode) {
        Promise promise = this.promises.remove("queryWeatherForecast");
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            WritableMap weatherMap = Arguments.createMap();
            if (weatherResult != null && weatherResult.getForecastResult() != null) {
                LocalWeatherForecast weather = weatherResult.getForecastResult();
                weatherMap.putString("province", weather.getProvince());
                weatherMap.putString("city", weather.getCity());
                weatherMap.putString("adCode", weather.getAdCode());
                weatherMap.putString("reportTime", weather.getReportTime());
                WritableArray weathersList = Arguments.createArray();
                List<LocalDayWeatherForecast> list = weather.getWeatherForecast();
                for (LocalDayWeatherForecast ldwf : list) {
                    WritableMap itemMap = Arguments.createMap();
                    weatherMap.putString("date", ldwf.getDate());
                    weatherMap.putString("week", ldwf.getWeek());
                    weatherMap.putString("dayWeather", ldwf.getDayWeather());
                    weatherMap.putString("nightWeather", ldwf.getNightWeather());
                    weatherMap.putString("dayTemperature", ldwf.getDayTemp());
                    weatherMap.putString("nightTemperature", ldwf.getNightTemp());
                    weatherMap.putString("dayWindDirection", ldwf.getDayWindDirection());
                    weatherMap.putString("nightWindDirection", ldwf.getNightWindDirection());
                    weatherMap.putString("dayWindPower", ldwf.getDayWindPower());
                    weatherMap.putString("nightWindPower", ldwf.getNightWindPower());
                    weathersList.pushMap(itemMap);
                }
                weatherMap.putArray("forecast", weathersList);
            }
            if (promise != null) promise.resolve(weatherMap);
        } else {
            if (promise != null) promise.reject(String.valueOf(rCode), "查询预报天气失败，失败代码：" + rCode);
        }
    }
}