package com.microsoft.holostruction.aad

import android.app.Application
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import com.unity3d.player.UnityPlayer

class AzureAd(val clientId: String, val authority: String, val redirectUrl: String) {
    private var application: IPublicClientApplication? = null
    fun signIn(scope: Array<String>, callbackTargetName: String, successCallback: String, errorCallback: String) {
        createApplication(fun () {
            signInInternal(scope, callbackTargetName, successCallback, errorCallback)
        }, fun (error)  {
            UnityPlayer.UnitySendMessage(callbackTargetName, errorCallback, error)
        })
    }

    private fun createApplication(callback: () -> Unit, error: (String?) -> Unit) {
        if (this.application == null) {
            PublicClientApplication.create(UnityPlayer.currentActivity,
                clientId,
                authority,
                redirectUrl,
                object : IPublicClientApplication.ApplicationCreatedListener {
                    override fun onCreated(application: IPublicClientApplication?) {
                        this@AzureAd.application = application
                        callback()
                    }
                    override fun onError(exception: MsalException?) {
                        error(exception?.localizedMessage)
                    }
                }
            )
        } else {
            callback()
        }
    }

    private fun signInInternal(scope: Array<String>, callbackTargetName: String, successCallback: String, errorCallback: String) {
        if (application == null) {
            UnityPlayer.UnitySendMessage(callbackTargetName, errorCallback, "Application is null.")
            return
        }

        this.application?.acquireToken(UnityPlayer.currentActivity, scope, object: AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                UnityPlayer.UnitySendMessage(callbackTargetName, successCallback,
                    "{ \"accessToken\": \"${authenticationResult?.accessToken}\", \"userName\": \"${authenticationResult?.account?.username}\"}")
            }

            override fun onCancel() {
                UnityPlayer.UnitySendMessage(callbackTargetName, errorCallback, "Canceled")
            }

            override fun onError(exception: MsalException?) {
                UnityPlayer.UnitySendMessage(callbackTargetName, errorCallback, exception?.localizedMessage)
            }
        });
    }
}