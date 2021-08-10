package com.example.login_plugin

import android.R.attr
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.UMShareConfig
import com.umeng.socialize.bean.SHARE_MEDIA

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import java.util.HashMap
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import androidx.core.app.ActivityCompat.startActivityForResult
import android.R.attr.data
import android.os.Build
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope


/** LoginPlugin */
class LoginPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, ActivityResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    lateinit var context: Context
    lateinit var activity: Activity
    val VERSION = 1

    private val RC_SIGN_IN = 9001
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "login_plugin")
        channel.setMethodCallHandler(this)


    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("SDK $VERSION")

            }
            "authToLogin" -> {
                authToLogin(call, result)
            }

            "umInit" -> {
                umInit(call, result)
            }
        }
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    override fun onAttachedToActivity(binding: ActivityPluginBinding) {

        binding.addActivityResultListener(this)
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else {
            UMShareAPI.get(activity).onActivityResult(requestCode, resultCode, data)

        }
        return true
    }

    // 友盟 初始化
    private fun umInit(call: MethodCall, result: Result) {
        Log.i("auth", "init")
        val id = call.argument<String>("id") ?: ""
        UMConfigure.init(
            context, id, "umeng", UMConfigure.DEVICE_TYPE_PHONE, ""
        )
        result.success(null)
    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)
            val list = mutableListOf<Map<String, String?>>()
            list.add(mapOf(Pair("name", account.displayName)))
            list.add(mapOf(Pair("email", account.email)))
            list.add(mapOf(Pair("iconurl", account.photoUrl?.toString())))
            list.add(mapOf(Pair("id", account.id)))
            result?.success(list)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)

        }
    }

    private val TAG = "LoginPlugin"
    private fun authToLogin(call: MethodCall, result: Result) {
        Log.i("auth", "authToLogin")
        val authType = call.argument<Int>("authType") ?: 7
        val type = initMedia(authType)
        this.result = result
        if (type == SHARE_MEDIA.FACEBOOK) {
            authByFacebook(type, result)
        }
        if (type == SHARE_MEDIA.GOOGLEPLUS) {
            authByGoogle()
        }
    }

    var result: Result? = null
    private fun authByGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().requestProfile()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun authByFacebook(
        type: SHARE_MEDIA,
        result: Result
    ) {
        activity.runOnUiThread {
            UMShareAPI.get(activity)
                .getPlatformInfo(activity, type, object : UMAuthListener {
                    override fun onStart(p0: SHARE_MEDIA?) {

                    }

                    override fun onComplete(
                        p0: SHARE_MEDIA?,
                        p1: Int,
                        p2: MutableMap<String, String>?
                    ) {
                        Log.i("onComplete", "success")
                        val list = mutableListOf<Map<String, String>>()
                        p2?.map { bean ->
                            Log.i("onComplete", "${bean.key}:${bean.value}")
                            list.add(mapOf(Pair(bean.key, bean.value)))
                        }
                        result.success(list)
                    }

                    override fun onError(p0: SHARE_MEDIA?, p1: Int, p2: Throwable?) {
                        System.out.println(p2.toString())
                    }

                    override fun onCancel(p0: SHARE_MEDIA?, p1: Int) {
                    }

                })
        }
    }

    private fun initMedia(type: Int): SHARE_MEDIA {
        return when (type) {
            1 -> SHARE_MEDIA.SINA
            2 -> SHARE_MEDIA.WEIXIN
            3 -> SHARE_MEDIA.WEIXIN_CIRCLE
            4 -> SHARE_MEDIA.QZONE
            5 -> SHARE_MEDIA.EMAIL
            6 -> SHARE_MEDIA.SMS
            7 -> SHARE_MEDIA.FACEBOOK
            8 -> SHARE_MEDIA.TWITTER
            9 -> SHARE_MEDIA.WEIXIN_FAVORITE
            10 -> SHARE_MEDIA.GOOGLEPLUS
            11 -> SHARE_MEDIA.RENREN
            12 -> SHARE_MEDIA.TENCENT
            13 -> SHARE_MEDIA.DOUBAN
            14 -> SHARE_MEDIA.FACEBOOK_MESSAGER
            15 -> SHARE_MEDIA.YIXIN
            16 -> SHARE_MEDIA.YIXIN_CIRCLE
            17 -> SHARE_MEDIA.INSTAGRAM
            18 -> SHARE_MEDIA.PINTEREST
            19 -> SHARE_MEDIA.EVERNOTE
            20 -> SHARE_MEDIA.POCKET
            21 -> SHARE_MEDIA.LINKEDIN
            22 -> SHARE_MEDIA.FOURSQUARE
            23 -> SHARE_MEDIA.YNOTE
            24 -> SHARE_MEDIA.WHATSAPP
            25 -> SHARE_MEDIA.LINE
            26 -> SHARE_MEDIA.FLICKR
            27 -> SHARE_MEDIA.TUMBLR
            28 -> SHARE_MEDIA.ALIPAY
            29 -> SHARE_MEDIA.KAKAO
            30 -> SHARE_MEDIA.DROPBOX
            31 -> SHARE_MEDIA.VKONTAKTE
            32 -> SHARE_MEDIA.DINGTALK
            33 -> SHARE_MEDIA.MORE
            else -> SHARE_MEDIA.QQ
        }
    }

}
