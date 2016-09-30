package com.gmail.sacchin13.myapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.support.v7.app.AppCompatActivity

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import android.util.Log
import io.realm.Credentials
import io.realm.ObjectServerError
import io.realm.User

import io.realm.ErrorCode.UNKNOWN_ACCOUNT
import io.realm.ErrorCode.INVALID_CREDENTIALS

class LoginActivity : AppCompatActivity() {
    private var mEmailView: AutoCompleteTextView? = null
    private var mPasswordView: EditText? = null
    private var mProgressView: View? = null
    private var mLoginFormView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mEmailView = findViewById(R.id.email) as AutoCompleteTextView

        mPasswordView = findViewById(R.id.password) as EditText
        mPasswordView!!.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                attemptLogin(false)
                return@OnEditorActionListener true
            }
            false
        })

        val mEmailSignInButton = findViewById(R.id.email_sign_in_button) as Button
        mEmailSignInButton.setOnClickListener { attemptLogin(false) }

        val mEmailSignUpButton = findViewById(R.id.email_sign_up_button) as Button
        mEmailSignUpButton.setOnClickListener { attemptLogin(true) }

        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)
    }

    private fun attemptLogin(signup: Boolean) {
        mEmailView!!.error = null
        mPasswordView!!.error = null

        val email = mEmailView!!.text.toString()
        val password = mPasswordView!!.text.toString()

        var cancel = false
        var focusView: View? = null

        if (TextUtils.isEmpty(password)) {
            mPasswordView!!.error = "Password is empty!"
            focusView = mPasswordView
            cancel = true
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView!!.error = "Email is empty!"
            focusView = mEmailView
            cancel = true
        }

        if (cancel) {
            focusView!!.requestFocus()
        } else {
            showProgress(true)

            val credentials = Credentials.usernamePassword(email, password, signup)
            val authUrl = "http://xx.xx.xx.xx:9080/auth"
            User.loginAsync(credentials, authUrl, LoginCallback())
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

            mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
            mLoginFormView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
                }
            })

            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        } else {
            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    inner class LoginCallback(): User.Callback{
        override fun onSuccess(user: User?) {
            showProgress(false)
            finish()
        }

        override fun onError(error: ObjectServerError?) {
            showProgress(false)
            val errorMsg: String =
                    when (error!!.errorCode) {
                        UNKNOWN_ACCOUNT -> "Account does not exists."
                        INVALID_CREDENTIALS -> "User name and password does not match"
                        else -> error.toString()
                    }
            Log.e("LoginCallback", errorMsg)
        }
    }
}

