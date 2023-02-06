* [2023.2.6更新](#202326更新)
* [背景](#背景)
* [方案架构](#方案架构)
* [JS与Android Native通信方案](#js与android-native通信方案)
  * [WebView和Native的基本调用方式](#webview和native的基本调用方式)
  * [Native-&gt;JS的通信图](#native-js的通信图)
* [JSBridge Android 类图](#jsbridge-android-类图)
* [JSBridge目录树](#jsbridge目录树)
* [依赖库](#依赖库)
* [相关类介绍](#相关类介绍)
  * [<strong>JSRequest &lt;T&gt;</strong>](#jsrequest-t)
  * [<strong>JSResponse &lt;T&gt;</strong>](#jsresponse-t)
  * [<strong>JSBridge</strong>](#jsbridge)
  * [<strong>IJSAsyncRequest</strong>](#ijsasyncrequest)
  * [<strong>IJSSyncRequest</strong>](#ijssyncrequest)
  * [<strong>IJSSub</strong>](#ijssub)
  * [<strong>JSBridgeConfig</strong>](#jsbridgeconfig)
* [使用示例](#使用示例)
  * [<strong>1.初始化JSBridge</strong>](#1初始化jsbridge)
  * [<strong>2.定义    @JavascriptInterface接口注入到webview</strong>](#2定义----javascriptinterface接口注入到webview)
  * [<strong>注入接口到webview</strong>](#注入接口到webview)
  * [<strong>3.根据协议实现处理器</strong>](#3根据协议实现处理器)
  * [<strong>4.在JSBridge的配置文件声明处理器对应的请求协议</strong>](#4在jsbridge的配置文件声明处理器对应的请求协议)
  * [<strong>5.让前端开发人员调用一下</strong>](#5让前端开发人员调用一下)

# 2023.2.6更新

+ 新增native调用JS方法的接口，具体例子在 com.sample.mix.demo.repository.impl.NativeRepository.getUserNameFromJS()
+ 优化JSBriage类的内部逻辑

# 背景

基于WebView（Android下是WebView，iOS下是WKWebView）控件加载H5页面，同时通过框架提供的接口，实现H5使用对设备摄像头、文件系统等设备能力的调用。

# 方案架构

[架构方案](https://github.com/BAByte/MVVMDemo)

# JS与Android Native通信方案

概述：对js和java的相互调用方式选型后，根据业务需求设计了一套通信方案

## WebView和Native的基本调用方式

1.Js通过native挂载的homeViewModel（叫什么都行）对象调用native方法。

2.native通过方法返回值返回数据或通过调用JS方法传参达到返回数据的目的。

非耗时操作使用同步调用 **AND** 耗时操作使用异步调用

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_18b63713-acc9-4070-9343-f69c79aaed64.png?raw=true)

基于上文中的：”WebView和Native的基本调用方式“，**设计出消息订阅的****通信方案**--**JSBridge**。

**JSBridge Demo 项目地址：**

**Andoird :[JSBridgeSample](https://github.com/BAByte/JSBridgeSample-t)**

**WEB:预留**

## Native->JS的通信图

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_93be14ed-8c15-40b9-8228-10ae134aaef8.png?raw=true)

# JSBridge Android 类图

观察者模式。

1.JSBridgeConfig为处理订阅配消息，处理器就成为订阅者

2.由webview端或者android本地（电池，网络状态改变）通过NativeRepository发布消息

3.JSBridge分发消息

4.不同类型的订阅者处理消息

5.消息由JSRequest定义，具体参数类型只有订阅者自己知道，JSBridge不关心具体使用的数据类型

6.JSBridge通过JSResponse得到订阅者的处理结果，转成json后回复给webview

![image](https://github.com/BAByte/pic/blob/master/%E4%BC%81%E4%B8%9A%E5%BE%AE%E4%BF%A1%E6%88%AA%E5%9B%BE_dadeb328-121c-4a74-aa46-867a76049a26.png?raw=true)

# JSBridge目录树

比较简单就不做介绍了

```kotlin
├── jsbridge
│   ├── IJSAsyncRequest.kt
│   ├── IJSSub.kt
│   ├── IJSSyncRequest.kt
│   ├── JSBridge.kt
│   ├── JSBridgeConfig.kt
│   └── imp
│       ├── request
│       │   ├── async
│       │   │   └── ToSetNetwork.kt
│       │   └── sync
│       │       └── GetVersionCode.kt
│       └── sub
│           └── ListenNetworkStatus.kt
```

# 依赖库

```kotlin
implementation "com.google.code.gson:gson:2.8.6"
```

# 相关类介绍

## **JSRequest `<T>`**

请求协议对应实体类：使用泛型区别不同的业务对应的数据结构

```kotlin
@Keep
data class JSRequest<T>(
    /**
     * 用于区分前端调用native的方法类型
     * ex: request.setDeviceName
     */
    val method:String,

    /**
     * 用于区分请求
     */
    val requestId:String,

    /**
     * 前端带过来的参数
     */
    val params:T
)
```

## **JSResponse `<T>`**

回复协议对应实体类：使用泛型区别不同的业务对应的数据结构

```kotlin
@Keep
data class JSResponse<T>(
    /**
     * 用于区分前端调用native的方法类型
     *
     * 回复前端
     * response.setDeviceName
     *
     * 主动通知前端网络状态
     * sub.connectStatus
     */
    var method:String,

    /**
     * 用于区分请求
     */
    val requestId:String,

    val code:String,
    val message:String,
    val data: T
)
```

## **JSBridge**

负责分发前端的请求到不同的处理器，该类不需要改动。

## **IJSAsyncRequest**

异步请求处理器接口，也叫消息的订阅者，处理前端某个请求的类都实现自该接口，该类型的订阅者通过JSBridge的方法回复JS

```kotlin
interface IJSAsyncRequest {
    fun onRequest(params: String)
}
```

## **IJSSyncRequest**

同步步请求处理器接口，也叫消息的订阅者，处理前端某个请求的类都实现自该接口。这类型的订阅者直接通过返回值返回数据给JS

```kotlin
interface IJSSyncRequest {
  	//通过返回值返回数据给JS
    fun onRequest(params: String): JSResponse<Any>
}
```

## **IJSSub**

推送消息处理器接口，也叫消息的订阅者，主动推送数据给JS，传递数据的类实现都实现自该接口，该类型的订阅者通过JSBridge的方法回复JS

```kotlin
interface IJSSub {
    fun onRequest(params: String)
}
```

## **JSBridgeConfig**

处理器与请求的关系表

```kotlin
/**
 * 建立method与处理器的关系
 */
object JSBridgeConfig {
    //状态码
    const val SUCCESS_CODE = "000000"
    const val UNKNOWN_ERROR_CODE = "999999"

    //js回调名称，由前端定义的，可以直接声明在这，或者使用JSBridge提供的设置接口
    var CALL_BACK_NAME = "JSBridgeReceiveMessage"

    init {
        with(JSBridge) {
            //异步
            //sub
            asyncRequestMap["xxx"] = xxx

            //request
            asyncRequestMap["$ASYNC_TYPE.xxx"] = xxxx

            //同步
            syncRequestMap["$SYNC_TYPE.xxxx"] = xxx
        }
    }
}
```

# 使用示例

简单的介绍使用示例，更为详细的内容请阅读sample的源码

## **1.初始化JSBridge**

```kotlin
    /**
     * 初始化
     */
   fun initJSBridge(webViewClient: WebView) {
        JSBridge.setWebClient(webViewClient)
        JSBridge.startHandler()
    }
```

## **2.定义    @JavascriptInterface接口注入到webview**

```kotlin
class JSBridgeModel{  
		@JavascriptInterface
    fun requestFromJS(requestJson: String): String {
       //具体实现使用JSBridge的方法
      return JSBridge.requestFromJS(requestJson)
    }
}
```

## **注入接口到webview**

```kotlin
//向前端注入对象
webview.addJavascriptInterface(JSBridgeModel(), "JSBridgeModel")
```

## **3.根据协议实现处理器**

简单的给一个示例，更多示例请详细看sample的代码

该处理器的协议：

请求

```kotlin
{
    "method": "async.request.toSetNetwork",
    "requestId":"12313",
    "params": {
    }
}
```

回复：

```kotlin
{
    "method": "response.getIotConnectStatus",
    "requestId":"12313",
    "code":"12313",
    "message":"12313",
    "data": {
    }
}
```

**处理器的实现**

```kotlin
/**
 * 打开设置：
 * 获取参数 这里的例子是没有参数，如果需要：请给JSRequest或JSResponse传入对应的data class
 */
class ToSetNetwork : IJSAsyncRequest, CoroutineScope by MainScope() {
    override fun onRequest(params: String) {
        launch(Dispatchers.Main) {
            var request: JSRequest<NoneRequest>? = null
            try {
                val type = object : TypeToken<JSRequest<NoneRequest>>() {}.type
                request = JsonUtils.fromJson(params, type)
                if (request == null) {
                    return@launch
                }
                App.ctx.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
                val response = JSResponse(
                    request.method,
                    request.requestId,
                    JSBridgeConfig.SUCCESS_CODE,
                    "",
                    NoneResponse("")
                )
                JSBridge.onResponse(response, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
```

## **4.在JSBridge的配置文件声明处理器对应的请求协议**

```kotlin

/**
 * 建立method与处理器的关系
 */
object JSBridgeConfig {
    //状态码
    const val SUCCESS_CODE = "000000"
    const val UNKNOWN_ERROR_CODE = "999999"

    //js回调名称，由前端定义的，可以直接声明在这，或者使用JSBridge提供的设置接口
    var CALL_BACK_NAME = "JSBridgeReceiveMessage"

    init {
        with(JSBridge) {
            //异步
            //request
            //给上方的处理器注册
            asyncRequestMap["$ASYNC_TYPE.toSetNetwork"] = ToSetNetwork()
        }
    }
}
```

## **5.让前端开发人员调用一下**

如果没有前端，可以用chrome的调试工具模拟前端调用：

+ 0.在初始化webview时，打开调试模式：

```kotlin
        //打开webview的调试模式，todo 注：线上环境不建议打开
        WebView.setWebContentsDebuggingEnabled(true)
```

+ 1在chrome地址栏输入:chrome://inspect 。   注：在edge是edge://inspect
+ 2.选择你的设备和应用并使用挂载在window上的java对象

```kotlin
//调到控制台执行如下代码：设备就会打开设置啦！
window.JSBridgeModel.requestFromJS(JSON.stringify(JSON.parse('{"method":"async.request.toSetNetwork","requestId":"12313","params": {}}')))
```
