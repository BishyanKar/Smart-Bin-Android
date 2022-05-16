package com.example.smartbin

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.smartbin.databinding.ActivityMainBinding
import com.example.smartbin.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    @Inject lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private val authViewModel by viewModels<AuthViewModel>()

    private var startSignInActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Timber.d("firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                Timber.tag(TAG).w(e, "Google sign in failed")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        supportActionBar?.hide()

        binding.progressCircular.visibility = View.GONE

        setContentView(view)

        initGoogleSignIn()
        initListeners()
    }

    private fun initListeners() {
        binding.btnSignIn.setOnClickListener {
            binding.progressCircular.visibility = View.VISIBLE
            signIn()
        }
    }

    fun initGoogleSignIn(){
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.tag(TAG).w(task.exception, "signInWithCredential:failure")
                    updateUI(null)
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startSignInActivityForResult.launch(signInIntent)
    }

    private fun updateUI(user: FirebaseUser?) {
        editor = sharedPreferences.edit()
        if(user!=null && sharedPreferences.getString(Constants.KEY_AUTH_TOKEN,null) == null)
        {
            user.getIdToken(false).addOnSuccessListener { token ->
                editor.putString(Constants.KEY_ID_TOKEN, token.token)
                editor.apply()
                authViewModel.login().observe(this, Observer {
                    if(it.body!=null){
                        editor.putString(Constants.KEY_AUTH_TOKEN, it.body.token)
                        editor.apply()
                        startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    }
                    else {
                        Timber.d(it.errorMessage)
                    }
                })
            }
                .addOnFailureListener {
                    Timber.e(it.message)
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
        else if(user != null) {
            startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
        else {
            binding.progressCircular.visibility = View.GONE
            //Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val TAG = "MainActivity"
        const val RC_SIGN_IN = 123
    }

}