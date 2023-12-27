package com.example.mymessage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.mymessage.module.UserData
import com.example.mymessage.ui.theme.MyMessageTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@SuppressLint("StaticFieldLeak")
private var context: Context? = null

private lateinit var sharedPreferences: SharedPreferences
private lateinit var editor: SharedPreferences.Editor
private lateinit var auth: FirebaseAuth
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMessageTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    auth = FirebaseAuth.getInstance()
                    context = LocalContext.current
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("158053140209-0qbat8b2r9rop9rvvorlhqbd313vujb5.apps.googleusercontent.com")
                        .requestEmail()
                        .build()
                    val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
                    Column(
                        modifier = Modifier.wrapContentSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {

                            val signInIntent = mGoogleSignInClient.signInIntent
                            startActivityForResult(signInIntent, 1)

                        }, modifier = Modifier.background(Color.Blue)) {


                            Text(text = "Sign In")

                        }
                    }
                }
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {

                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken)
                Log.d("TAG", "onActivityResult: ")
            } catch (e: ApiException) {
                Log.d("TAG", "error: $e")

            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    val userData = UserData(
                        user?.displayName,
                        user?.uid,
                        user?.email,
                        user?.photoUrl.toString()
                    )
                    sharedPreferences =
                        context!!.getSharedPreferences("myShared", Context.MODE_PRIVATE)

                    editor = sharedPreferences.edit()
                    editor.putBoolean("isLogged", true)
                    editor.putString("userID", userData.uid)
                    editor.putString("uPhoto", userData.photo)
                    editor.putString("uName", userData.name)
                    editor.putString("uEmail", userData.email)
                    editor.apply()
                    Toast.makeText(context, "Successfully signed in!", Toast.LENGTH_SHORT)
                        .show()
                    val reference = Firebase.database.reference.child("users")
                    var b = true
                    reference.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val children = snapshot.children
                            children.forEach {
                                val user = it.getValue(UserData::class.java)
                                if (user != null && user.uid == userData.uid) {
                                    b = false
                                }
                            }
                            if (b) {
                                setUser(userData)
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("TAG", "onCancelled: ${error.message}")
                        }

                    })

                    val i = Intent(this, ChatsActivity::class.java)
                    i.putExtra("uid", userData.uid)
                    i.putExtra("userEmail", userData.email)
                    i.putExtra("userName", userData.name)
                    i.putExtra("userPhoto", userData.photo)
                    startActivity(i)


                } else {
                    Log.d("TAG", "error: Authentication Failed.")
                }
            }
    }


    private fun setUser(userData: UserData) {
        val userIdReference = Firebase.database.reference
            .child("users").child(userData.uid ?: "")
        userIdReference.setValue(userData).addOnSuccessListener {

            val i = Intent(this, ChatsActivity::class.java)
            i.putExtra("uid", userData.uid)
            startActivity(i)
        }
    }
}
