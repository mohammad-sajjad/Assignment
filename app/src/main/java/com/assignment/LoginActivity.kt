package com.assignment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.assignment.HomeActivity.Companion.extra_key
import com.assignment.utils.launchActivity
import com.assignment.utils.shortToast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    //a constant for detecting the login intent result
    private val RC_SIGN_IN = 234
    private val RC_FB_SIGN_IN = 12345
    private val TAG = "Login"

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private var mCallbackManager: CallbackManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        if (FirebaseApp.initializeApp(this) != null) {
            FirebaseApp.initializeApp(this)
        }
        else {
            shortToast("not initialize")
        }

        init()



    }

    private fun init() {
        mCallbackManager = CallbackManager.Factory.create()

        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        google_sign_in_button.setOnClickListener {
            signIn()
        }

        login_button.setReadPermissions("email", "public_profile");

        login_button.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {}
        })

    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, UI will update with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth!!.currentUser
                    val extra = HomeExtra(user?.displayName, user?.photoUrl, user?.email)
                    shortToast("Authentication Succeeded.")

                    launchActivity<HomeActivity> {
                        this.putExtra(extra_key, extra)
                    }

                } else {
                    // If sign-in fails, a message will display to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    shortToast("Authentication failed.")
                }
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            //Getting the GoogleSignIn Task
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)

                //authenticating with firebase
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                e.message?.let { shortToast(it) }
            }
        }
        else {
            mCallbackManager?.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct!!.id)

        //getting the auth credential
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        //Now using firebase we are signing in the user here
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth!!.currentUser
                    val extra = HomeExtra(user?.displayName, user?.photoUrl, user?.email)
                    shortToast("User Signed In")

                    launchActivity<HomeActivity> {
                        this.putExtra(extra_key, extra)
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    shortToast("Authentication failed.")

                }

            }
    }


    //this method is called on click
    private fun signIn() {
        //getting the google signin intent
        val signInIntent = mGoogleSignInClient!!.signInIntent

        //starting the activity for result
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
}