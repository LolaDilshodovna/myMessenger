package com.example.mymessage

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymessage.module.Message
import com.example.mymessage.ui.theme.MyMessageTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.text.SimpleDateFormat
import java.util.Date

class XabarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMessageTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("158053140209-0qbat8b2r9rop9rvvorlhqbd313vujb5.apps.googleusercontent.com")
                        .requestEmail()
                        .build()

                    val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

                    val messageList = remember {
                        mutableStateListOf(Message())
                    }

                    val uid = intent.getStringExtra("uid")

                    val useruid = intent.getStringExtra("useruid")

                    var text = remember {
                        mutableStateOf(TextFieldValue(""))
                    }

                    LazyColumn() {
                        items(messageList) {
                            val backgroundColor =
                                if (it.from == uid) Color.Red else Color.Gray
                            val myAlign =
                                if (it.from == uid) Alignment.End else Alignment.Start

                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalAlignment = myAlign
                            ) {
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = backgroundColor
                                    ),
                                    content = {
                                        Column(
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = it.text ?: "",
                                                color = Color.Black,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = it.date ?: "",
                                                color = Color.Black,
                                                fontSize = 8.sp,
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                )

                            }

                            Button(
                                modifier = Modifier.background(Color.Gray),
                                onClick = { mGoogleSignInClient.signOut() }) {
                                Text(text = "Sign Out")
                            }

                        }
                    }
                    val ref = Firebase.database.reference.child("users")
                        .child(uid!!)
                        .child("message")
                        .child(useruid!!)
                    ref.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val children = snapshot.children
                            messageList.clear()
                            children.forEach {
                                val message = it.getValue(Message::class.java)
                                messageList.add(message ?: Message())
                                Log.d("MESS", "onCreate: ${message?.text}")
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("ERR", "onCancelled: ${error.message}")
                        }

                    })
                    val sdf = SimpleDateFormat("HH:mm")
                    val currentDateAndTime = sdf.format(Date())
                    val m = Message(useruid, uid, text.value.text, currentDateAndTime)

                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        TextField(
                            text.value,
                            onValueChange = {
                                text.value = it
                            },
                            placeholder = { Text(text = "New message") },
                            label = { Text("Enter text") },
                            modifier = Modifier
                                .padding(8.dp),

                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            )
                        )


                        Button(onClick = {
                            val reference = Firebase.database.reference.child("users")
                            val key = reference.push().key.toString()
                            reference.child(uid)
                                .child("message")
                                .child(useruid)
                                .child(key)
                                .setValue(m)
                            reference.child(useruid)
                                .child("message")
                                .child(uid)
                                .child(key)
                                .setValue(m)

                            text.value = TextFieldValue("")
                        }, shape = CircleShape)
                        {
                            Text(text = "yuborish")
                        }

                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    MyMessageTheme {
    }
}